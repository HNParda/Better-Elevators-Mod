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

    private static final EntityDataAccessor<BlockPos> MASTER_POS = SynchedEntityData.defineId(ElevatorCabinEntity.class, EntityDataSerializers.BLOCK_POS);

    private static final EntityDataAccessor<Integer> WIDTH = SynchedEntityData.defineId(ElevatorCabinEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> DEPTH = SynchedEntityData.defineId(ElevatorCabinEntity.class, EntityDataSerializers.INT);

    public ElevatorCabinEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.noPhysics = false;
        this.blocksBuilding = true;
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

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        // Very small dimensions for the entity itself, collision is handled separately
        return EntityDimensions.fixed(0.1f, 0.1f);
    }

    @Override
    protected AABB makeBoundingBox() {
        // Minimal bounding box for the entity center point
        Vec3 pos = position();
        return new AABB(pos.x - 0.05, pos.y, pos.z - 0.05, pos.x + 0.05, pos.y + 0.1, pos.z + 0.05);
    }

    @Override
    public void tick() {
        super.tick();

        // Provide platform collision for entities above
        if (!this.level().isClientSide) {
            providePlatformCollision();
        }
    }

    private void providePlatformCollision() {
        float width = getWidth();
        float depth = getDepth();
        float halfWidth = width / 2.0f;
        float halfDepth = depth / 2.0f;

        Vec3 pos = position();

        // Create a thin platform at the cabin floor level (Y + 0 to Y + 0.2)
        AABB platformBox = new AABB(pos.x - halfWidth, pos.y, pos.z - halfDepth, pos.x + halfWidth, pos.y + 0.2, pos.z + halfDepth);

        // Find all entities that might be standing on the platform
        List<Entity> entities = this.level().getEntities(this, platformBox.inflate(0, 2, 0), entity -> entity != this && !entity.isPassenger());

        for (Entity entity : entities) {
            AABB entityBox = entity.getBoundingBox();

            // Check if entity is above the platform (feet are near platform top)
            if (entityBox.minY >= pos.y - 0.5 && entityBox.minY <= pos.y + 0.5) {
                // Check if entity is within platform horizontal bounds
                if (entityBox.maxX > platformBox.minX && entityBox.minX < platformBox.maxX && entityBox.maxZ > platformBox.minZ && entityBox.minZ < platformBox.maxZ) {

                    // Place entity on top of platform if they're falling through
                    if (entity.getDeltaMovement().y < 0 && entityBox.minY < pos.y + 0.2) {
                        entity.setPos(entity.getX(), pos.y + 0.2, entity.getZ());
                        entity.setDeltaMovement(entity.getDeltaMovement().x, 0, entity.getDeltaMovement().z);
                        entity.setOnGround(true);
                        entity.fallDistance = 0;
                    }
                }
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        pBuilder.define(MASTER_POS, BlockPos.ZERO);
        pBuilder.define(WIDTH, 1);
        pBuilder.define(DEPTH, 1);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        if (pCompound.contains("MasterX")) {
            setMasterPos(new BlockPos(pCompound.getInt("MasterX"), pCompound.getInt("MasterY"), pCompound.getInt("MasterZ")));
        }
        setDimensions(pCompound.getInt("Width"), pCompound.getInt("Depth"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        BlockPos masterPos = getMasterPos();
        pCompound.putInt("MasterX", masterPos.getX());
        pCompound.putInt("MasterY", masterPos.getY());
        pCompound.putInt("MasterZ", masterPos.getZ());
        pCompound.putInt("Width", getWidth());
        pCompound.putInt("Depth", getDepth());
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false; // Don't block movement
    }

    @Override
    protected boolean canRide(Entity pEntity) {
        return false;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        super.onSyncedDataUpdated(pKey);
        if (WIDTH.equals(pKey) || DEPTH.equals(pKey)) {
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
