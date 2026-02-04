package com.hnp_arda.betterelevatorsmod.elevator_block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ElevatorCabinEntity extends Entity {

    public static final float PLATFORM_HEIGHT = 2.0f / 16.0f;
    public static final float FLOOR_PADDING_DOWN = 2.0f / 16.0f;
    public static final int DEFAULT_WALL_HEIGHT_BLOCKS = 2;
    public static final float WALL_THICKNESS = 0.1f;

    public static final int COLLISION_LAYER_MAIN = 0;
    public static final int COLLISION_LAYER_FLOOR = 1;
    public static final int COLLISION_LAYER_ROOF = 2;
    public static final int COLLISION_LAYER_WALL_NORTH = 3;
    public static final int COLLISION_LAYER_WALL_SOUTH = 4;
    public static final int COLLISION_LAYER_WALL_WEST = 5;
    public static final int COLLISION_LAYER_WALL_EAST = 6;

    private static final EntityDataAccessor<BlockPos> MASTER_POS = SynchedEntityData.defineId(ElevatorCabinEntity.class, EntityDataSerializers.BLOCK_POS);

    private static final EntityDataAccessor<Integer> WIDTH = SynchedEntityData.defineId(ElevatorCabinEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> DEPTH = SynchedEntityData.defineId(ElevatorCabinEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> WALL_HEIGHT_BLOCKS = SynchedEntityData.defineId(ElevatorCabinEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> COLLISION_LAYER = SynchedEntityData.defineId(ElevatorCabinEntity.class, EntityDataSerializers.INT);

    public ElevatorCabinEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.noPhysics = true;
        this.blocksBuilding = true;
        this.setNoGravity(true);
    }

    public BlockPos getMasterPos() {
        return this.entityData.get(MASTER_POS);
    }

    public void setMasterPos(BlockPos pos) {
        this.entityData.set(MASTER_POS, pos);
    }

    public void setDimensions(int width, int depth) {
        this.entityData.set(WIDTH, width);
        this.entityData.set(DEPTH, depth);
        refreshDimensions();
    }

    public int getWidth() {
        return this.entityData.get(WIDTH);
    }

    public int getDepth() {
        return this.entityData.get(DEPTH);
    }

    public void setWallHeightBlocks(int heightBlocks) {
        this.entityData.set(WALL_HEIGHT_BLOCKS, Math.max(1, heightBlocks));
        refreshDimensions();
    }

    public int getWallHeightBlocks() {
        return this.entityData.get(WALL_HEIGHT_BLOCKS);
    }

    public float getWallHeight() {
        return Math.max(1, getWallHeightBlocks());
    }

    public void setCollisionLayer(int layer) {
        this.entityData.set(COLLISION_LAYER, layer);
        refreshDimensions();
    }

    public int getCollisionLayer() {
        return this.entityData.get(COLLISION_LAYER);
    }

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        int layer = getCollisionLayer();
        if (layer == COLLISION_LAYER_MAIN) {
            return EntityDimensions.fixed(0.1f, 0.1f);
        }

        float width = Math.max(.5f, getWidth());
        float depth = Math.max(.5f, getDepth());
        float diameter = Math.max(width, depth);

        if (layer == COLLISION_LAYER_FLOOR || layer == COLLISION_LAYER_ROOF) {
            return EntityDimensions.fixed(diameter, PLATFORM_HEIGHT);
        }

        return EntityDimensions.fixed(diameter, getWallHeight());
    }

    @Override
    protected AABB makeBoundingBox() {
        Vec3 pos = position();
        int layer = getCollisionLayer();
        if (layer == COLLISION_LAYER_MAIN) {
            return new AABB(pos.x - 0.05, pos.y, pos.z - 0.05, pos.x + 0.05, pos.y + 0.1, pos.z + 0.05);
        }
        double halfWidth = Math.max(1.0, getWidth()) / 2.0;
        double halfDepth = Math.max(1.0, getDepth()) / 2.0;

        if (layer == COLLISION_LAYER_FLOOR) {
            return new AABB(
                pos.x - halfWidth,
                pos.y - FLOOR_PADDING_DOWN,
                pos.z - halfDepth,
                pos.x + halfWidth,
                pos.y + PLATFORM_HEIGHT,
                pos.z + halfDepth
            );
        }

        if (layer == COLLISION_LAYER_ROOF) {
            double minY = pos.y + getWallHeight() - PLATFORM_HEIGHT;
            return new AABB(pos.x - halfWidth, minY, pos.z - halfDepth, pos.x + halfWidth, minY + PLATFORM_HEIGHT, pos.z + halfDepth);
        }

        if (layer == COLLISION_LAYER_WALL_NORTH) {
            return new AABB(
                pos.x - halfWidth,
                pos.y,
                pos.z - halfDepth,
                pos.x + halfWidth,
                pos.y + getWallHeight(),
                pos.z - halfDepth + WALL_THICKNESS
            );
        }

        if (layer == COLLISION_LAYER_WALL_SOUTH) {
            return new AABB(
                pos.x - halfWidth,
                pos.y,
                pos.z + halfDepth - WALL_THICKNESS,
                pos.x + halfWidth,
                pos.y + getWallHeight(),
                pos.z + halfDepth
            );
        }

        if (layer == COLLISION_LAYER_WALL_WEST) {
            return new AABB(
                pos.x - halfWidth,
                pos.y,
                pos.z - halfDepth,
                pos.x - halfWidth + WALL_THICKNESS,
                pos.y + getWallHeight(),
                pos.z + halfDepth
            );
        }

        if (layer == COLLISION_LAYER_WALL_EAST) {
            return new AABB(
                pos.x + halfWidth - WALL_THICKNESS,
                pos.y,
                pos.z - halfDepth,
                pos.x + halfWidth,
                pos.y + getWallHeight(),
                pos.z + halfDepth
            );
        }

        return new AABB(pos.x - 0.05, pos.y, pos.z - 0.05, pos.x + 0.05, pos.y + 0.1, pos.z + 0.05);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        pBuilder.define(MASTER_POS, BlockPos.ZERO);
        pBuilder.define(WIDTH, 1);
        pBuilder.define(DEPTH, 1);
        pBuilder.define(WALL_HEIGHT_BLOCKS, DEFAULT_WALL_HEIGHT_BLOCKS);
        pBuilder.define(COLLISION_LAYER, COLLISION_LAYER_MAIN);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        if (pCompound.contains("MasterX")) {
            setMasterPos(new BlockPos(pCompound.getInt("MasterX"), pCompound.getInt("MasterY"), pCompound.getInt("MasterZ")));
        }
        setDimensions(pCompound.getInt("Width"), pCompound.getInt("Depth"));
        if (pCompound.contains("WallHeight")) {
            setWallHeightBlocks(pCompound.getInt("WallHeight"));
        }
        if (pCompound.contains("CollisionLayer")) {
            setCollisionLayer(pCompound.getInt("CollisionLayer"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        BlockPos masterPos = getMasterPos();
        pCompound.putInt("MasterX", masterPos.getX());
        pCompound.putInt("MasterY", masterPos.getY());
        pCompound.putInt("MasterZ", masterPos.getZ());
        pCompound.putInt("Width", getWidth());
        pCompound.putInt("Depth", getDepth());
        pCompound.putInt("WallHeight", getWallHeightBlocks());
        pCompound.putInt("CollisionLayer", getCollisionLayer());
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return getCollisionLayer() != COLLISION_LAYER_MAIN;
    }

    @Override
    protected boolean canRide(Entity pEntity) {
        return false;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        super.onSyncedDataUpdated(pKey);
        if (WIDTH.equals(pKey) || DEPTH.equals(pKey) || WALL_HEIGHT_BLOCKS.equals(pKey) || COLLISION_LAYER.equals(pKey)) {
            this.setBoundingBox(makeBoundingBox());
        }
    }

    @Override
    public boolean isPickable() {
        return false; // Don't intercept clicks
    }

    @Override
    public boolean skipAttackInteraction(Entity pEntity) {
        return true; // Can't be attacked
    }
}
