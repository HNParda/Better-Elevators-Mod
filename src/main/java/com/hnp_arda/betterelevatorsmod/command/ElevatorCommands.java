package com.hnp_arda.betterelevatorsmod.command;

import com.hnp_arda.betterelevatorsmod.elevator_block.entity.ElevatorBlockEntity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class ElevatorCommands {

    private ElevatorCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("elevator")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("cabinheight")
                    .then(Commands.argument("height", IntegerArgumentType.integer(1, 64))
                        .executes(ctx -> {
                            BlockPos pos = BlockPos.containing(ctx.getSource().getPosition());
                            return setCabinHeight(ctx.getSource(), pos, IntegerArgumentType.getInteger(ctx, "height"));
                        })
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                            .executes(ctx -> setCabinHeight(
                                ctx.getSource(),
                                BlockPosArgument.getLoadedBlockPos(ctx, "pos"),
                                IntegerArgumentType.getInteger(ctx, "height")
                            ))
                        )
                    )
                )
                .then(Commands.literal("floor")
                    .then(Commands.literal("add")
                        .then(Commands.argument("startY", IntegerArgumentType.integer())
                            .executes(ctx -> {
                                BlockPos pos = BlockPos.containing(ctx.getSource().getPosition());
                                return addFloor(ctx.getSource(), pos, IntegerArgumentType.getInteger(ctx, "startY"));
                            })
                            .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(ctx -> addFloor(
                                    ctx.getSource(),
                                    BlockPosArgument.getLoadedBlockPos(ctx, "pos"),
                                    IntegerArgumentType.getInteger(ctx, "startY")
                                ))
                            )
                        )
                    )
                    .then(Commands.literal("remove")
                        .then(Commands.argument("startY", IntegerArgumentType.integer())
                            .executes(ctx -> {
                                BlockPos pos = BlockPos.containing(ctx.getSource().getPosition());
                                return removeFloor(ctx.getSource(), pos, IntegerArgumentType.getInteger(ctx, "startY"));
                            })
                            .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(ctx -> removeFloor(
                                    ctx.getSource(),
                                    BlockPosArgument.getLoadedBlockPos(ctx, "pos"),
                                    IntegerArgumentType.getInteger(ctx, "startY")
                                ))
                            )
                        )
                    )
                    .then(Commands.literal("current")
                        .then(Commands.argument("startY", IntegerArgumentType.integer())
                            .executes(ctx -> {
                                BlockPos pos = BlockPos.containing(ctx.getSource().getPosition());
                                return setCurrentFloor(ctx.getSource(), pos, IntegerArgumentType.getInteger(ctx, "startY"));
                            })
                            .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(ctx -> setCurrentFloor(
                                    ctx.getSource(),
                                    BlockPosArgument.getLoadedBlockPos(ctx, "pos"),
                                    IntegerArgumentType.getInteger(ctx, "startY")
                                ))
                            )
                        )
                    )
                    .then(Commands.literal("currentindex")
                        .then(Commands.argument("index", IntegerArgumentType.integer(1))
                            .executes(ctx -> {
                                BlockPos pos = BlockPos.containing(ctx.getSource().getPosition());
                                return setCurrentFloorByIndex(ctx.getSource(), pos, IntegerArgumentType.getInteger(ctx, "index"));
                            })
                            .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(ctx -> setCurrentFloorByIndex(
                                    ctx.getSource(),
                                    BlockPosArgument.getLoadedBlockPos(ctx, "pos"),
                                    IntegerArgumentType.getInteger(ctx, "index")
                                ))
                            )
                        )
                    )
                    .then(Commands.literal("moveindex")
                        .then(Commands.argument("index", IntegerArgumentType.integer(1))
                            .executes(ctx -> {
                                BlockPos pos = BlockPos.containing(ctx.getSource().getPosition());
                                return moveToFloorByIndex(ctx.getSource(), pos, IntegerArgumentType.getInteger(ctx, "index"));
                            })
                            .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(ctx -> moveToFloorByIndex(
                                    ctx.getSource(),
                                    BlockPosArgument.getLoadedBlockPos(ctx, "pos"),
                                    IntegerArgumentType.getInteger(ctx, "index")
                                ))
                            )
                        )
                    )
                    .then(Commands.literal("list")
                        .executes(ctx -> {
                            BlockPos pos = BlockPos.containing(ctx.getSource().getPosition());
                            return listFloors(ctx.getSource(), pos);
                        })
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                            .executes(ctx -> listFloors(ctx.getSource(), BlockPosArgument.getLoadedBlockPos(ctx, "pos")))
                        )
                    )
                )
                .then(Commands.literal("faces")
                    .then(Commands.argument("startY", IntegerArgumentType.integer())
                        .then(Commands.argument("mask", IntegerArgumentType.integer(0, 15))
                            .executes(ctx -> {
                                BlockPos pos = BlockPos.containing(ctx.getSource().getPosition());
                                return setFaces(
                                    ctx.getSource(),
                                    pos,
                                    IntegerArgumentType.getInteger(ctx, "startY"),
                                    IntegerArgumentType.getInteger(ctx, "mask")
                                );
                            })
                            .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(ctx -> setFaces(
                                    ctx.getSource(),
                                    BlockPosArgument.getLoadedBlockPos(ctx, "pos"),
                                    IntegerArgumentType.getInteger(ctx, "startY"),
                                    IntegerArgumentType.getInteger(ctx, "mask")
                                ))
                            )
                        )
                    )
                )
                .then(Commands.literal("facesindex")
                    .then(Commands.argument("index", IntegerArgumentType.integer(1))
                        .then(Commands.argument("mask", IntegerArgumentType.integer(0, 15))
                            .executes(ctx -> {
                                BlockPos pos = BlockPos.containing(ctx.getSource().getPosition());
                                return setFacesByIndex(
                                    ctx.getSource(),
                                    pos,
                                    IntegerArgumentType.getInteger(ctx, "index"),
                                    IntegerArgumentType.getInteger(ctx, "mask")
                                );
                            })
                            .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(ctx -> setFacesByIndex(
                                    ctx.getSource(),
                                    BlockPosArgument.getLoadedBlockPos(ctx, "pos"),
                                    IntegerArgumentType.getInteger(ctx, "index"),
                                    IntegerArgumentType.getInteger(ctx, "mask")
                                ))
                            )
                        )
                    )
                )
        );
    }

    private static int setCabinHeight(CommandSourceStack source, BlockPos pos, int height) {
        ElevatorBlockEntity elevator = getElevatorEntity(source, pos);
        if (elevator == null) {
            return 0;
        }
        elevator.setCabinHeight(height);
        source.sendSuccess(() -> Component.literal("Cabin height set to " + height), true);
        return 1;
    }

    private static int addFloor(CommandSourceStack source, BlockPos pos, int startY) {
        ElevatorBlockEntity elevator = getElevatorEntity(source, pos);
        if (elevator == null) {
            return 0;
        }
        elevator.ensureFloorEntry(startY);
        source.sendSuccess(() -> Component.literal("Added floor start Y " + startY), true);
        return 1;
    }

    private static int removeFloor(CommandSourceStack source, BlockPos pos, int startY) {
        ElevatorBlockEntity elevator = getElevatorEntity(source, pos);
        if (elevator == null) {
            return 0;
        }
        elevator.removeFloorEntry(startY);
        source.sendSuccess(() -> Component.literal("Removed floor start Y " + startY), true);
        return 1;
    }

    private static int setCurrentFloor(CommandSourceStack source, BlockPos pos, int startY) {
        ElevatorBlockEntity elevator = getElevatorEntity(source, pos);
        if (elevator == null) {
            return 0;
        }
        elevator.setCurrentFloorStartY(startY);
        source.sendSuccess(() -> Component.literal("Current floor start Y set to " + startY), true);
        return 1;
    }

    private static int setFaces(CommandSourceStack source, BlockPos pos, int startY, int mask) {
        ElevatorBlockEntity elevator = getElevatorEntity(source, pos);
        if (elevator == null) {
            return 0;
        }
        elevator.ensureFloorEntry(startY);
        elevator.setFloorFaceMask(startY, mask);
        source.sendSuccess(() -> Component.literal("Face mask for floor " + startY + " set to " + mask), true);
        return 1;
    }

    private static int setCurrentFloorByIndex(CommandSourceStack source, BlockPos pos, int index) {
        ElevatorBlockEntity elevator = getElevatorEntity(source, pos);
        if (elevator == null) {
            return 0;
        }
        Integer startY = elevator.getFloorStartYByIndex(index);
        if (startY == null) {
            source.sendFailure(Component.literal("No floor at index " + index));
            return 0;
        }
        elevator.setCurrentFloorStartY(startY);
        source.sendSuccess(() -> Component.literal("Current floor set to index " + index + " (startY " + startY + ")"), true);
        return 1;
    }

    private static int moveToFloorByIndex(CommandSourceStack source, BlockPos pos, int index) {
        ElevatorBlockEntity elevator = getElevatorEntity(source, pos);
        if (elevator == null) {
            return 0;
        }
        Integer startY = elevator.getFloorStartYByIndex(index);
        if (startY == null) {
            source.sendFailure(Component.literal("No floor at index " + index));
            return 0;
        }
        elevator.moveToFloorStartY(startY);
        source.sendSuccess(() -> Component.literal("Moving to index " + index + " (startY " + startY + ")"), true);
        return 1;
    }

    private static int setFacesByIndex(CommandSourceStack source, BlockPos pos, int index, int mask) {
        ElevatorBlockEntity elevator = getElevatorEntity(source, pos);
        if (elevator == null) {
            return 0;
        }
        Integer startY = elevator.getFloorStartYByIndex(index);
        if (startY == null) {
            source.sendFailure(Component.literal("No floor at index " + index));
            return 0;
        }
        elevator.ensureFloorEntry(startY);
        elevator.setFloorFaceMask(startY, mask);
        source.sendSuccess(() -> Component.literal("Face mask for index " + index + " (startY " + startY + ") set to " + mask), true);
        return 1;
    }

    private static int listFloors(CommandSourceStack source, BlockPos pos) {
        ElevatorBlockEntity elevator = getElevatorEntity(source, pos);
        if (elevator == null) {
            return 0;
        }
        var floors = elevator.getSortedFloorStartYs();
        if (floors.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No floors configured."), false);
            return 1;
        }
        StringBuilder sb = new StringBuilder("Floors: ");
        for (int i = 0; i < floors.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("#").append(i + 1).append("=Y").append(floors.get(i));
        }
        source.sendSuccess(() -> Component.literal(sb.toString()), false);
        return 1;
    }

    private static ElevatorBlockEntity getElevatorEntity(CommandSourceStack source, BlockPos pos) {
        BlockEntity blockEntity = source.getLevel().getBlockEntity(pos);
        if (!(blockEntity instanceof ElevatorBlockEntity elevator)) {
            source.sendFailure(Component.literal("No elevator block entity at " + pos.getX() + " " + pos.getY() + " " + pos.getZ()));
            return null;
        }

        BlockPos masterPos = elevator.getMasterPos();
        if (masterPos != null && !masterPos.equals(pos)) {
            BlockEntity master = source.getLevel().getBlockEntity(masterPos);
            if (master instanceof ElevatorBlockEntity masterElevator) {
                return masterElevator;
            }
        }

        return elevator;
    }
}
