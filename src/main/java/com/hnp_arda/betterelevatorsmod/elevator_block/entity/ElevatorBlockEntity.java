package com.hnp_arda.betterelevatorsmod.elevator_block.entity;

import com.hnp_arda.betterelevatorsmod.elevator_block.screen.ElevatorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
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

import net.minecraft.world.level.Level;

import static com.hnp_arda.betterelevatorsmod.ModRegister.*;

public class ElevatorBlockEntity extends BlockEntity implements MenuProvider, IContainerFactory<ElevatorMenu> {

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
            // Spawn cabin if it doesn't exist
            ElevatorCabinEntity existing = findCabinEntity();
            if (existing == null) {
                spawnCabinEntity();
            }
        } else {
            // Remove cabin if it exists
            ElevatorCabinEntity existing = findCabinEntity();
            if (existing != null) {
                existing.discard();
            }
        }
    }

    @Nullable
    private ElevatorCabinEntity findCabinEntity() {
        if (level == null) return null;
        
        // Search for cabin entity near the master position
        AABB searchArea = new AABB(worldPosition).inflate(5.0);
        List<ElevatorCabinEntity> cabins = level.getEntitiesOfClass(
            ElevatorCabinEntity.class, 
            searchArea,
            cabin -> cabin.getMasterPos().equals(worldPosition)
        );
        
        return cabins.isEmpty() ? null : cabins.get(0);
    }

    private void spawnCabinEntity() {
        if (level == null || level.isClientSide()) return;
        
        // Calculate cabin dimensions based on elevator structure
        ArrayList<BlockPos> elevatorBlocks = findElevatorStructure();
        if (elevatorBlocks.isEmpty()) return;
        
        // Find min/max X and Z coordinates
        int minX = elevatorBlocks.stream().mapToInt(BlockPos::getX).min().orElse(worldPosition.getX());
        int maxX = elevatorBlocks.stream().mapToInt(BlockPos::getX).max().orElse(worldPosition.getX());
        int minZ = elevatorBlocks.stream().mapToInt(BlockPos::getZ).min().orElse(worldPosition.getZ());
        int maxZ = elevatorBlocks.stream().mapToInt(BlockPos::getZ).max().orElse(worldPosition.getZ());
        
        // Calculate width and depth in blocks
        int width = maxX - minX + 1;
        int depth = maxZ - minZ + 1;
        
        // Calculate center position
        double centerX = (minX + maxX) / 2.0 + 0.5;
        double centerZ = (minZ + maxZ) / 2.0 + 0.5;
        
        // Spawn at master block Y position (bottom of elevator)
        ElevatorCabinEntity cabin = new ElevatorCabinEntity(ELEVATOR_CABIN_ENTITY.get(), level);
        cabin.setPos(centerX, worldPosition.getY(), centerZ);
        cabin.setMasterPos(worldPosition);
        cabin.setDimensions(width, depth);
        
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
    }

    public void dropContents() {
        if (level != null && isMaster()) {
            // Remove cabin entity before dropping items
            ElevatorCabinEntity cabin = findCabinEntity();
            if (cabin != null) {
                cabin.discard();
            }
            
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), itemHandler.getStackInSlot(i));
            }
        }
    }
}
