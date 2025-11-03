package com.hnp_arda.betterelevatorsmod.elevator_block.screen;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import static com.hnp_arda.betterelevatorsmod.ModRegister.itemCabin;

public class PlaceableBlockSlot extends SlotItemHandler {

    public PlaceableBlockSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        // Don't allow cabin items
        if (stack.is(itemCabin.get())) {
            return false;
        }
        // Only allow BlockItems (placeable blocks)
        return stack.getItem() instanceof BlockItem;
    }
}