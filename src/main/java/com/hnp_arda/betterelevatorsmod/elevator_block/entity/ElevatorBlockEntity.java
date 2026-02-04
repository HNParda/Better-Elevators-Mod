package com.hnp_arda.betterelevatorsmod.elevator_block.entity;

import com.hnp_arda.betterelevatorsmod.elevator_block.screen.ElevatorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.IContainerFactory;
import org.jetbrains.annotations.Nullable;
import com.hnp_arda.betterelevatorsmod.elevator_block.ElevatorBlock;
import net.minecraft.world.phys.AABB;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Comparator;

import net.minecraft.world.level.Level;

import static com.hnp_arda.betterelevatorsmod.ModRegister.*;

public class ElevatorBlockEntity extends BlockEntity implements MenuProvider, IContainerFactory<ElevatorMenu> {

    public static final int FACE_NORTH = 1;
    public static final int FACE_SOUTH = 2;
    public static final int FACE_WEST = 4;
    public static final int FACE_EAST = 8;
    private static final int CABIN_SEARCH_RADIUS = 64;
    private static final double DEFAULT_MOVE_SPEED = 0.1;

    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            // Sync to master if this is not the master
            if (masterPos != null && !masterPos.equals(worldPosition)) {
                BlockEntity master = level.getBlockEntity(masterPos);
                if (master instanceof ElevatorBlockEntity masterEntity) {
                    masterEntity.setChanged();
                }
            }
            
            // Handle cabin spawn/despawn on slot 0 changes
            if (slot == 0 && isMaster()) {
                updateCabinEntity();
            }
        }
    };

    @Nullable
    private BlockPos masterPos;

    private int cabinHeight = 2;
    @Nullable
    private Integer currentFloorStartY;
    private final Map<Integer, Integer> floorFaceMasks = new HashMap<>();
    private boolean moving;
    private double cabinY;
    private double targetCabinY;
    private double moveSpeed = DEFAULT_MOVE_SPEED;

    public ItemStackHandler getItemHandler() {
        // If this is a slave block, get the master's inventory
        if (masterPos != null && !masterPos.equals(worldPosition) && level != null) {
            BlockEntity master = level.getBlockEntity(masterPos);
            if (master instanceof ElevatorBlockEntity masterEntity) {
                return masterEntity.getItemHandler();
            }
        }
        // Otherwise return our own
        return itemHandler;
    }

    public void setMasterPos(@Nullable BlockPos masterPos) {
        this.masterPos = masterPos;
        setChanged();
    }

    @Nullable
    public BlockPos getMasterPos() {
        return masterPos;
    }

    public boolean isMaster() {
        return masterPos == null || masterPos.equals(worldPosition);
    }

    public ElevatorBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(entityElevatorBlock.get(), pPos, pBlockState);
    }

    public Component getDisplayName() {
        return Component.translatable("block.betterelevatorsmod.elevator_block");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new ElevatorMenu(pContainerId, pPlayerInventory, this);
    }

    @Override
    public ElevatorMenu create(int pContainerId, Inventory pPlayerInventory, RegistryFriendlyByteBuf data) {
        return new ElevatorMenu(pContainerId, pPlayerInventory, this);
    }

    private void updateCabinEntity() {
        if (level == null || level.isClientSide()) return;
        
        boolean hasCabin = !itemHandler.getStackInSlot(0).isEmpty() && 
                          itemHandler.getStackInSlot(0).is(itemCabin.get());
        
        if (hasCabin) {
            spawnCabinEntities();
        } else {
            removeCabinEntities();
        }
    }

    @Nullable
    private ElevatorCabinEntity findCabinEntity(int layer) {
        if (level == null) return null;
        
        // Search for cabin entity near the master position
        AABB searchArea = getCabinSearchArea();
        List<ElevatorCabinEntity> cabins = level.getEntitiesOfClass(
            ElevatorCabinEntity.class, 
            searchArea,
            cabin -> cabin.getMasterPos().equals(worldPosition) && cabin.getCollisionLayer() == layer
        );
        
        return cabins.isEmpty() ? null : cabins.get(0);
    }

    private List<ElevatorCabinEntity> findCabinEntities() {
        if (level == null) return new ArrayList<>();
        AABB searchArea = getCabinSearchArea();
        return level.getEntitiesOfClass(
            ElevatorCabinEntity.class,
            searchArea,
            cabin -> cabin.getMasterPos().equals(worldPosition)
        );
    }

    private void spawnCabinEntities() {
        removeCabinEntities();
        if (level == null || level.isClientSide()) return;

        // Calculate cabin dimensions based on elevator structure
        ArrayList<BlockPos> elevatorBlocks = findElevatorStructure();
        if (elevatorBlocks.isEmpty()) return;

        int minX = elevatorBlocks.stream().mapToInt(BlockPos::getX).min().orElse(worldPosition.getX());
        int maxX = elevatorBlocks.stream().mapToInt(BlockPos::getX).max().orElse(worldPosition.getX());
        int minZ = elevatorBlocks.stream().mapToInt(BlockPos::getZ).min().orElse(worldPosition.getZ());
        int maxZ = elevatorBlocks.stream().mapToInt(BlockPos::getZ).max().orElse(worldPosition.getZ());

        int width = maxX - minX + 1;
        int depth = maxZ - minZ + 1;

        double centerX = (minX + maxX) / 2.0 + 0.5;
        double centerZ = (minZ + maxZ) / 2.0 + 0.5;
        int floorStartY = getCurrentFloorStartY();
        int faceMask = getFloorFaceMask(floorStartY);
        double baseY = floorStartY;
        cabinY = baseY;

        spawnCabinEntity(ElevatorCabinEntity.COLLISION_LAYER_MAIN, centerX, baseY, centerZ, width, depth);
        spawnCabinEntity(ElevatorCabinEntity.COLLISION_LAYER_FLOOR, centerX, baseY, centerZ, width, depth);
        spawnCabinEntity(ElevatorCabinEntity.COLLISION_LAYER_ROOF, centerX, baseY, centerZ, width, depth);

        if ((faceMask & FACE_NORTH) == 0) {
            spawnCabinEntity(ElevatorCabinEntity.COLLISION_LAYER_WALL_NORTH, centerX, baseY, centerZ, width, depth);
        }
        if ((faceMask & FACE_SOUTH) == 0) {
            spawnCabinEntity(ElevatorCabinEntity.COLLISION_LAYER_WALL_SOUTH, centerX, baseY, centerZ, width, depth);
        }
        if ((faceMask & FACE_WEST) == 0) {
            spawnCabinEntity(ElevatorCabinEntity.COLLISION_LAYER_WALL_WEST, centerX, baseY, centerZ, width, depth);
        }
        if ((faceMask & FACE_EAST) == 0) {
            spawnCabinEntity(ElevatorCabinEntity.COLLISION_LAYER_WALL_EAST, centerX, baseY, centerZ, width, depth);
        }
    }

    private void spawnCabinEntity(int collisionLayer, double x, double y, double z, int width, int depth) {
        if (level == null || level.isClientSide()) return;

        ElevatorCabinEntity cabin = new ElevatorCabinEntity(ELEVATOR_CABIN_ENTITY.get(), level);
        cabin.setPos(x, y, z);
        cabin.setMasterPos(worldPosition);
        cabin.setDimensions(width, depth);
        cabin.setWallHeightBlocks(cabinHeight);
        cabin.setCollisionLayer(collisionLayer);
        
        level.addFreshEntity(cabin);
    }

    private ArrayList<BlockPos> findElevatorStructure() {
        if (level == null) return new ArrayList<>();
        
        ArrayList<BlockPos> elevatorBlockList = new ArrayList<>();
        ArrayList<BlockPos> checkedBlockList = new ArrayList<>();
        elevatorBlockList.add(worldPosition);
        
        // Use the same scanning logic as ElevatorBlock
        scanBlocks(level, elevatorBlockList, checkedBlockList);
        
        return checkedBlockList;
    }

    private void removeCabinEntities() {
        if (level == null || level.isClientSide()) return;
        for (ElevatorCabinEntity cabin : findCabinEntities()) {
            cabin.discard();
        }
    }

    private AABB getCabinSearchArea() {
        return new AABB(worldPosition).inflate(CABIN_SEARCH_RADIUS);
    }

    public void moveToFloorStartY(int floorStartY) {
        if (level == null || level.isClientSide()) return;
        targetCabinY = floorStartY;
        if (!moving) {
            moving = true;
            if (cabinY == 0.0) {
                cabinY = getCurrentFloorStartY();
            }
        }
    }

    public void setMoveSpeed(double moveSpeed) {
        this.moveSpeed = Math.max(0.01, moveSpeed);
    }

    public void tickServer() {
        if (level == null || level.isClientSide()) return;
        if (!isMaster()) return;
        if (!moving) return;

        double remaining = targetCabinY - cabinY;
        if (Math.abs(remaining) <= moveSpeed) {
            double delta = remaining;
            double baseY = cabinY;
            cabinY = targetCabinY;
            moveEntitiesInCabin(delta, baseY, cabinY);
            moveCabinEntities(delta);
            moving = false;
            setCurrentFloorStartY((int) Math.round(targetCabinY));
            return;
        }

        double delta = Math.signum(remaining) * moveSpeed;
        double baseY = cabinY;
        cabinY += delta;
        moveEntitiesInCabin(delta, baseY, cabinY);
        moveCabinEntities(delta);
    }

    private void moveCabinEntities(double deltaY) {
        if (Math.abs(deltaY) < 1.0E-6) return;
        for (ElevatorCabinEntity cabin : findCabinEntities()) {
            cabin.setPos(cabin.getX(), cabin.getY() + deltaY, cabin.getZ());
        }
    }

    private void moveEntitiesInCabin(double deltaY, double baseY, double targetY) {
        if (Math.abs(deltaY) < 1.0E-6) return;
        ElevatorCabinEntity reference = findCabinEntity(ElevatorCabinEntity.COLLISION_LAYER_MAIN);
        if (reference == null) {
            List<ElevatorCabinEntity> cabins = findCabinEntities();
            if (cabins.isEmpty()) return;
            reference = cabins.get(0);
        }

        double halfWidth = Math.max(1.0, reference.getWidth()) / 2.0;
        double halfDepth = Math.max(1.0, reference.getDepth()) / 2.0;
        double height = reference.getWallHeight();
        double minY = Math.min(baseY, targetY) - 0.5;
        double maxY = Math.max(baseY, targetY) + height + 0.5;
        AABB cabinBox = new AABB(
            reference.getX() - halfWidth,
            minY,
            reference.getZ() - halfDepth,
            reference.getX() + halfWidth,
            maxY,
            reference.getZ() + halfDepth
        );

        List<net.minecraft.world.entity.Entity> entities = level.getEntities(
            reference,
            cabinBox,
            entity -> !(entity instanceof ElevatorCabinEntity)
        );

        for (net.minecraft.world.entity.Entity entity : entities) {
            entity.move(net.minecraft.world.entity.MoverType.SELF, new net.minecraft.world.phys.Vec3(0, deltaY, 0));
            if (entity instanceof net.minecraft.world.entity.LivingEntity living) {
                living.setOnGround(true);
                living.fallDistance = 0;
            }
        }
    }
    public int getCabinHeight() {
        return cabinHeight;
    }

    public void setCabinHeight(int cabinHeight) {
        this.cabinHeight = Math.max(1, cabinHeight);
        setChanged();
        updateCabinEntity();
    }

    public void setCurrentFloorStartY(int startY) {
        this.currentFloorStartY = startY;
        setChanged();
        updateCabinEntity();
    }

    public int getCurrentFloorStartY() {
        if (currentFloorStartY == null) {
            currentFloorStartY = worldPosition.getY();
        }
        return currentFloorStartY;
    }

    public void setFloorFaceMask(int floorStartY, int faceMask) {
        floorFaceMasks.put(floorStartY, faceMask);
        setChanged();
        updateCabinEntity();
    }

    public void ensureFloorEntry(int floorStartY) {
        if (!floorFaceMasks.containsKey(floorStartY)) {
            floorFaceMasks.put(floorStartY, 0);
            setChanged();
        }
    }

    public void removeFloorEntry(int floorStartY) {
        if (floorFaceMasks.remove(floorStartY) != null) {
            if (currentFloorStartY != null && currentFloorStartY == floorStartY) {
                currentFloorStartY = null;
            }
            setChanged();
            updateCabinEntity();
        }
    }

    public int getFloorFaceMask(int floorStartY) {
        return floorFaceMasks.getOrDefault(floorStartY, 0);
    }

    public List<Integer> getSortedFloorStartYs() {
        ArrayList<Integer> floors = new ArrayList<>(floorFaceMasks.keySet());
        floors.sort(Comparator.naturalOrder());
        return floors;
    }

    @Nullable
    public Integer getFloorStartYByIndex(int index) {
        if (index < 1) {
            return null;
        }
        List<Integer> floors = getSortedFloorStartYs();
        if (index > floors.size()) {
            return null;
        }
        return floors.get(index - 1);
    }

    private void scanBlocks(Level pLevel, ArrayList<BlockPos> posList, ArrayList<BlockPos> checkedList) {
        boolean checkAgain = false;
        ArrayList<BlockPos> tempList = new ArrayList<>();
        for (BlockPos blockPos : posList) {
            checkedList.add(blockPos);
            if (checkBlocks(pLevel, blockPos, tempList, checkedList)) checkAgain = true;
        }
        if (checkAgain) scanBlocks(pLevel, tempList, checkedList);
    }

    private boolean checkBlocks(Level pLevel, BlockPos blockPos, ArrayList<BlockPos> posList, ArrayList<BlockPos> checkedList) {
        BlockPos[] list = new BlockPos[]{blockPos.above(), blockPos.below(), blockPos.north(), blockPos.east(), blockPos.south(), blockPos.west()};
        boolean checkAgain = false;
        for (int i = 0; i < 6; i++) {
            BlockPos tempPos = list[i];
            net.minecraft.world.level.block.Block tempBlock = pLevel.getBlockState(tempPos).getBlock();
            if (tempBlock instanceof ElevatorBlock && !checkedList.contains(tempPos) && !posList.contains(tempPos)) {
                posList.add(tempPos);
                checkAgain = true;
            }
        }
        return checkAgain;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        // Only load inventory if this is the master
        if (isMaster()) {
            itemHandler.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
        // Load master position
        if (tag.contains("MasterX")) {
            masterPos = new BlockPos(
                tag.getInt("MasterX"),
                tag.getInt("MasterY"),
                tag.getInt("MasterZ")
            );
        } else {
            masterPos = null;
        }

        cabinHeight = tag.contains("CabinHeight") ? tag.getInt("CabinHeight") : 2;
        if (tag.contains("CurrentFloorStartY")) {
            currentFloorStartY = tag.getInt("CurrentFloorStartY");
        } else {
            currentFloorStartY = null;
        }

        floorFaceMasks.clear();
        if (tag.contains("FloorMasks", Tag.TAG_LIST)) {
            ListTag list = tag.getList("FloorMasks", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                int startY = entry.getInt("StartY");
                int mask = entry.getInt("Mask");
                floorFaceMasks.put(startY, mask);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        // Only save inventory if this is the master
        if (isMaster()) {
            tag.put("Inventory", itemHandler.serializeNBT(registries));
        }
        // Save master position
        if (masterPos != null) {
            tag.putInt("MasterX", masterPos.getX());
            tag.putInt("MasterY", masterPos.getY());
            tag.putInt("MasterZ", masterPos.getZ());
        }

        tag.putInt("CabinHeight", cabinHeight);
        if (currentFloorStartY != null) {
            tag.putInt("CurrentFloorStartY", currentFloorStartY);
        }

        ListTag list = new ListTag();
        for (Map.Entry<Integer, Integer> entry : floorFaceMasks.entrySet()) {
            CompoundTag floorTag = new CompoundTag();
            floorTag.putInt("StartY", entry.getKey());
            floorTag.putInt("Mask", entry.getValue());
            list.add(floorTag);
        }
        tag.put("FloorMasks", list);
    }

    public void dropContents() {
        if (level != null && isMaster()) {
            // Remove cabin entity before dropping items
            removeCabinEntities();
            
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), itemHandler.getStackInSlot(i));
            }
        }
    }
}
