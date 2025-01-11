package com.hnp_arda.betterelevatorsmod.blocks.elevator_block;

import com.hnp_arda.betterelevatorsmod.blocks.elevator_block.entity.ElevatorBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.hnp_arda.betterelevatorsmod.blocks.elevator_block.ElevatorState.*;

public class ElevatorBlock extends Block implements EntityBlock {

    public static final EnumProperty<ElevatorState> STATE = EnumProperty.create("elevator_state", ElevatorState.class);

    public ElevatorBlock() {
        super(Properties.of().dynamicShape().noOcclusion().pushReaction(PushReaction.BLOCK));
        this.registerDefaultState(this.defaultBlockState().setValue(STATE, BASE));
    }

    private static void scanBlocks(LevelAccessor pLevel, ArrayList<BlockPos> posList, ArrayList<BlockPos> checkedList) {
        boolean checkAgain = false;
        ArrayList<BlockPos> tempList = new ArrayList<>();
        for (BlockPos blockPos : posList) {
            checkedList.add(blockPos);
            if (checkBlocks(pLevel, blockPos, tempList, checkedList)) checkAgain = true;
        }
        if (checkAgain) scanBlocks(pLevel, tempList, checkedList);
    }

    private static boolean checkBlocks(LevelAccessor pLevel, BlockPos blockPos, ArrayList<BlockPos> posList, ArrayList<BlockPos> checkedList) {
        BlockPos[] list = new BlockPos[]{blockPos.above(), blockPos.below(), blockPos.north(), blockPos.east(), blockPos.south(), blockPos.west()};
        boolean checkAgain = false;
        for (int i = 0; i < 6; i++) {
            BlockPos tempPos = list[i];
            Block tempBlock = pLevel.getBlockState(tempPos).getBlock();
            if (tempBlock instanceof ElevatorBlock && !checkedList.contains(tempPos) && !posList.contains(tempPos)) {
                posList.add(tempPos);
                checkAgain = true;
            }
        }
        return checkAgain;
    }

    public static VoxelShape rotateShape(byte times, VoxelShape shape) {
        VoxelShape[] buffer = new VoxelShape[]{shape, Shapes.empty()};
        for (byte i = 0; i < times; i++) {
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = Shapes.or(buffer[1], Shapes.create(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
            buffer[0] = buffer[1];
            buffer[1] = Shapes.empty();
        }
        return buffer[0];
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(STATE);
    }

    @Override
    public void destroy(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
        resetStructure(pLevel, pPos);
        super.destroy(pLevel, pPos, pState);
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        resetStructure(level, pos);
        super.onBlockExploded(state, level, pos, explosion);
    }

    void resetStructure(LevelAccessor pLevel, BlockPos pPos) {
        ArrayList<BlockPos> elevatorBlockList = new ArrayList<>();
        ArrayList<BlockPos> checkedBlockList = new ArrayList<>();
        elevatorBlockList.add(pPos);
        scanBlocks(pLevel, elevatorBlockList, checkedBlockList);
        if (!pLevel.isClientSide()) {
            for (BlockPos blockPos : checkedBlockList)
                if (blockPos != pPos)
                    pLevel.setBlock(blockPos, pLevel.getBlockState(blockPos).setValue(STATE, BASE), 2);
        }
    }

    @Override
    protected VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return switchShape(pState);
    }












    @Override
    protected InteractionResult useWithoutItem(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, BlockHitResult pHitResult) {

        if (!pLevel.isClientSide()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof ElevatorBlockEntity) {
                pPlayer.openMenu((MenuProvider) blockEntity);
            }
        }
        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }


    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ElevatorBlockEntity(pPos, pState);
    }

    @Override
    protected RenderShape getRenderShape( BlockState pState) {
        return RenderShape.MODEL;
    }







    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        if (!pLevel.isClientSide()) {
            boolean formed = true;
            ArrayList<BlockPos> elevatorBlockList = new ArrayList<>();
            ArrayList<BlockPos> checkedBlockList = new ArrayList<>();
            elevatorBlockList.add(pPos);
            scanBlocks(pLevel, elevatorBlockList, checkedBlockList);
            Set<Integer> listPosX = new HashSet<>();
            Set<Integer> listPosY = new HashSet<>();
            Set<Integer> listPosZ = new HashSet<>();
            checkedBlockList.forEach(blockPos -> {
                listPosX.add(blockPos.getX());
                listPosY.add(blockPos.getY());
                listPosZ.add(blockPos.getZ());
            });
            if (listPosY.size() < 4) formed = false;
            for (int currentX = Collections.min(listPosX); currentX <= Collections.max(listPosX); currentX++) {
                for (int currentY = Collections.min(listPosY); currentY <= Collections.max(listPosY); currentY++) {
                    for (int currentZ = Collections.min(listPosZ); currentZ <= Collections.max(listPosZ); currentZ++) {
                        Block currentBlock = pLevel.getBlockState(new BlockPos(currentX, currentY, currentZ)).getBlock();
                        if (!(currentBlock instanceof ElevatorBlock)) {
                            formed = false;
                            break;
                        }
                    }
                }
            } // check all block if structure can be formed

            if (formed) formStructure(checkedBlockList, pLevel);
            else for (BlockPos blockPos : checkedBlockList)
                pLevel.setBlockAndUpdate(blockPos, pLevel.getBlockState(blockPos).setValue(STATE, BASE));
        } //check if multiblock structure can be formed
    }

    private void formStructure(ArrayList<BlockPos> elevatorBlockList, Level pLevel) {

        for (BlockPos blockPos : elevatorBlockList) {

            pLevel.setBlockAndUpdate(blockPos, checkState(pLevel, blockPos));

        }

    }

    private BlockState checkState(Level pLevel, BlockPos blockPos) {
        int surroundings = checkSurroundings(pLevel, blockPos);
        String result = "elevator_block_";
        Rotation rotation = Rotation.NONE;

        if (surroundings % 5 == 0) result += "middle_";
        else if (surroundings % 5 == 2) result += "bottom_";
        else if (surroundings % 5 == 3) result += "top_";

        double temp = Math.floor((double) surroundings / 10);
        if (temp == 50 || temp == 82 || temp == 68 || temp == 70 || temp == 80 || temp == 100) result += "side";
        else if (temp == 18 || temp == 20 || temp == 30 || temp == 32) {
            if (temp == 20) result += "single_north";
            if (temp == 32) result += "single_east";
            if (temp == 30) result += "single_south";
            if (temp == 18) result += "single_west";
        } else if (temp == 38 || temp == 48 || temp == 52 || temp == 62) {
            if (temp == 38) result += "corner_north";
            if (temp == 52) result += "corner_east";
            if (temp == 62) result += "corner_south";
            if (temp == 48) result += "corner_west";
        } else result += "base";


        ElevatorState state;
        switch (result) {
            case "elevator_block_top_base" -> state = BASE_TOP;
            case "elevator_block_middle_base" -> state = BASE_MIDDLE;
            case "elevator_block_bottom_base" -> state = BASE_BOTTOM;

            case "elevator_block_top_side" -> state = SIDE_TOP;
            case "elevator_block_middle_side" -> state = SIDE_MIDDLE;
            case "elevator_block_bottom_side" -> state = SIDE_BOTTOM;


            case "elevator_block_top_corner_south" -> state = TOP_CORNER_SOUTH;
            case "elevator_block_top_corner_west" -> state = TOP_CORNER_WEST;
            case "elevator_block_top_corner_north" -> state = TOP_CORNER_NORTH;
            case "elevator_block_top_corner_east" -> state = TOP_CORNER_EAST;

            case "elevator_block_middle_corner_south" -> state = MIDDLE_CORNER_SOUTH;
            case "elevator_block_middle_corner_west" -> state = MIDDLE_CORNER_WEST;
            case "elevator_block_middle_corner_north" -> state = MIDDLE_CORNER_NORTH;
            case "elevator_block_middle_corner_east" -> state = MIDDLE_CORNER_EAST;

            case "elevator_block_bottom_corner_south" -> state = BOTTOM_CORNER_SOUTH;
            case "elevator_block_bottom_corner_west" -> state = BOTTOM_CORNER_WEST;
            case "elevator_block_bottom_corner_north" -> state = BOTTOM_CORNER_NORTH;
            case "elevator_block_bottom_corner_east" -> state = BOTTOM_CORNER_EAST;


            case "elevator_block_top_single_south" -> state = TOP_SINGLE_SOUTH;
            case "elevator_block_top_single_west" -> state = TOP_SINGLE_WEST;
            case "elevator_block_top_single_north" -> state = TOP_SINGLE_NORTH;
            case "elevator_block_top_single_east" -> state = TOP_SINGLE_EAST;

            case "elevator_block_middle_single_south" -> state = MIDDLE_SINGLE_SOUTH;
            case "elevator_block_middle_single_west" -> state = MIDDLE_SINGLE_WEST;
            case "elevator_block_middle_single_north" -> state = MIDDLE_SINGLE_NORTH;
            case "elevator_block_middle_single_east" -> state = MIDDLE_SINGLE_EAST;

            case "elevator_block_bottom_single_south" -> state = BOTTOM_SINGLE_SOUTH;
            case "elevator_block_bottom_single_west" -> state = BOTTOM_SINGLE_WEST;
            case "elevator_block_bottom_single_north" -> state = BOTTOM_SINGLE_NORTH;
            case "elevator_block_bottom_single_east" -> state = BOTTOM_SINGLE_EAST;


            default -> state = BASE;
        }

        BlockState blockState = pLevel.getBlockState(blockPos);
        return blockState.setValue(STATE, state).rotate(pLevel, blockPos, rotation);
    }

    private int checkSurroundings(LevelAccessor pLevel, BlockPos blockPos) {
        int result = 0;
        if (pLevel.getBlockState(blockPos.above()).getBlock() instanceof ElevatorBlock) result += 2;
        if (pLevel.getBlockState(blockPos.below()).getBlock() instanceof ElevatorBlock) result += 3;

        if (pLevel.getBlockState(blockPos.north()).getBlock() instanceof ElevatorBlock) result += 200;
        if (pLevel.getBlockState(blockPos.south()).getBlock() instanceof ElevatorBlock) result += 300;

        if (pLevel.getBlockState(blockPos.east()).getBlock() instanceof ElevatorBlock) result += 320;
        if (pLevel.getBlockState(blockPos.west()).getBlock() instanceof ElevatorBlock) result += 180;


        return result;
    }

    public VoxelShape switchShape(BlockState pState) {
        ElevatorState elevatorState = pState.getValue(STATE);
        VoxelShape shape;
        switch (elevatorState) {
            case BASE_TOP -> shape = getBaseTopShape();
            case BASE_MIDDLE -> shape = getBaseMiddleShape();
            case BASE_BOTTOM -> shape = getBaseBottomShape();


            case TOP_CORNER_NORTH -> shape = getCornerTopShape();
            case TOP_CORNER_EAST -> shape = rotateShape((byte) 1, getCornerTopShape());
            case TOP_CORNER_SOUTH -> shape = rotateShape((byte) 2, getCornerTopShape());
            case TOP_CORNER_WEST -> shape = rotateShape((byte) 3, getCornerTopShape());

            case MIDDLE_CORNER_NORTH -> shape = getCornerMiddleShape();
            case MIDDLE_CORNER_EAST -> shape = rotateShape((byte) 1, getCornerMiddleShape());
            case MIDDLE_CORNER_SOUTH -> shape = rotateShape((byte) 2, getCornerMiddleShape());
            case MIDDLE_CORNER_WEST -> shape = rotateShape((byte) 3, getCornerMiddleShape());

            case BOTTOM_CORNER_NORTH -> shape = getCornerBottomShape();
            case BOTTOM_CORNER_EAST -> shape = rotateShape((byte) 1, getCornerBottomShape());
            case BOTTOM_CORNER_SOUTH -> shape = rotateShape((byte) 2, getCornerBottomShape());
            case BOTTOM_CORNER_WEST -> shape = rotateShape((byte) 3, getCornerBottomShape());


            case TOP_SINGLE_NORTH -> shape = getSingleTopShape();
            case TOP_SINGLE_EAST -> shape = rotateShape((byte) 1, getSingleTopShape());
            case TOP_SINGLE_SOUTH -> shape = rotateShape((byte) 2, getSingleTopShape());
            case TOP_SINGLE_WEST -> shape = rotateShape((byte) 3, getSingleTopShape());

            case MIDDLE_SINGLE_NORTH -> shape = getSingleMiddleShape();
            case MIDDLE_SINGLE_EAST -> shape = rotateShape((byte) 1, getSingleMiddleShape());
            case MIDDLE_SINGLE_SOUTH -> shape = rotateShape((byte) 2, getSingleMiddleShape());
            case MIDDLE_SINGLE_WEST -> shape = rotateShape((byte) 3, getSingleMiddleShape());

            case BOTTOM_SINGLE_NORTH -> shape = getSingleBottomShape();
            case BOTTOM_SINGLE_EAST -> shape = rotateShape((byte) 1, getSingleBottomShape());
            case BOTTOM_SINGLE_SOUTH -> shape = rotateShape((byte) 2, getSingleBottomShape());
            case BOTTOM_SINGLE_WEST -> shape = rotateShape((byte) 3, getSingleBottomShape());


            case SIDE_TOP -> shape = getSideTopShape();
            case SIDE_MIDDLE -> shape = getSideMiddleShape();
            case SIDE_BOTTOM -> shape = getSideBottomShape();


            default -> shape = Shapes.block();
        }
        return shape;
    }

    public VoxelShape getBaseTopShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0.9375, 0, 1, 1, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0, 0.0625, 0.125, 0.9375, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0, 0.0625, 0.9375, 0.9375, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0, 0.875, 0.9375, 0.9375, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0, 0.875, 0.125, 0.9375, 0.9375), BooleanOp.OR);
        return shape;
    }

    public VoxelShape getBaseMiddleShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.06249999999999996, 0, 0.0625, 0.12499999999999996, 1, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.8749999999999998, 0, 0.0625, 0.9374999999999998, 1, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.8749999999999998, 0, 0.875, 0.9374999999999998, 1, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.06249999999999996, 0, 0.875, 0.12499999999999996, 1, 0.9375), BooleanOp.OR);
        return shape;
    }

    public VoxelShape getBaseBottomShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0, 0, 1, 0.0625, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.0625, 0.0625, 0.125, 1, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0.0625, 0.0625, 0.9375, 1, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0.0625, 0.875, 0.9375, 1, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.0625, 0.875, 0.125, 1, 0.9375), BooleanOp.OR);
        return shape;
    }

    public VoxelShape getCornerTopShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0.9375, 0, 1, 1, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0, 0.875, 0.9375, 0.9375, 0.9375), BooleanOp.OR);
        return shape;
    }

    public VoxelShape getCornerMiddleShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.875, 0, 0.875, 0.9375, 1, 0.9375), BooleanOp.OR);
        return shape;
    }

    public VoxelShape getCornerBottomShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0, 0, 1, 0.0625, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0.0625, 0.875, 0.9375, 1, 0.9375), BooleanOp.OR);
        return shape;
    }

    public VoxelShape getSingleTopShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0.9375, 0, 1, 1, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0, 0.875, 0.125, 0.9375, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0, 0.875, 0.9375, 0.9375, 0.9375), BooleanOp.OR);
        return shape;
    }

    public VoxelShape getSingleMiddleShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.0625, 0, 0.875, 0.125, 1, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0, 0.875, 0.9375, 1, 0.9375), BooleanOp.OR);
        return shape;
    }

    public VoxelShape getSingleBottomShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0, 0, 1, 0.0625, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.0625, 0.875, 0.125, 1, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0.0625, 0.875, 0.9375, 1, 0.9375), BooleanOp.OR);
        return shape;
    }

    public VoxelShape getSideTopShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0.9375, 0, 1, 1, 1), BooleanOp.OR);
        return shape;
    }

    public VoxelShape getSideMiddleShape() {
        return Shapes.empty();
    }

    public VoxelShape getSideBottomShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0, 0, 1, 0.0625, 1), BooleanOp.OR);
        return shape;
    }

}

