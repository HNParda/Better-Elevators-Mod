package com.hnp_arda.betterelevatorsmod.blocks.elevator_block;

import net.minecraft.util.StringRepresentable;

public enum ElevatorState implements StringRepresentable {
    BASE("base"),

    BASE_TOP("base_top"),
    BASE_MIDDLE("base_middle"),
    BASE_BOTTOM("base_bottom"),





    TOP_CORNER_SOUTH("top_corner_south"),
    TOP_CORNER_WEST("top_corner_west"),
    TOP_CORNER_NORTH("top_corner_north"),
    TOP_CORNER_EAST("top_corner_east"),

    MIDDLE_CORNER_SOUTH("middle_corner_south"),
    MIDDLE_CORNER_WEST("middle_corner_west"),
    MIDDLE_CORNER_NORTH("middle_corner_north"),
    MIDDLE_CORNER_EAST("middle_corner_east"),

    BOTTOM_CORNER_SOUTH("bottom_corner_south"),
    BOTTOM_CORNER_WEST("bottom_corner_west"),
    BOTTOM_CORNER_NORTH("bottom_corner_north"),
    BOTTOM_CORNER_EAST("bottom_corner_east"),




    TOP_SINGLE_SOUTH("top_single_south"),
     TOP_SINGLE_WEST("top_single_west"),
    TOP_SINGLE_NORTH("top_single_north"),
     TOP_SINGLE_EAST("top_single_east"),

    MIDDLE_SINGLE_SOUTH("middle_single_south"),
     MIDDLE_SINGLE_WEST("middle_single_west"),
    MIDDLE_SINGLE_NORTH("middle_single_north"),
     MIDDLE_SINGLE_EAST("middle_single_east"),

    BOTTOM_SINGLE_SOUTH("bottom_single_south"),
     BOTTOM_SINGLE_WEST("bottom_single_west"),
    BOTTOM_SINGLE_NORTH("bottom_single_north"),
     BOTTOM_SINGLE_EAST("bottom_single_east"),




    SIDE_TOP("side_top"),
    SIDE_MIDDLE("side_middle"),
    SIDE_BOTTOM("side_bottom");

    private final String name;

    private ElevatorState(String pName) {
        this.name = pName;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
