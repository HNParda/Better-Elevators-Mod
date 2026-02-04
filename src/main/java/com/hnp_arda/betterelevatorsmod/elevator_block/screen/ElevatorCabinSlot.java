package com.hnp_arda.betterelevatorsmod.elevator_block.screen;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import static com.hnp_arda.betterelevatorsmod.ModRegister.itemCabin;

public class ElevatorCabinSlot extends SlotItemHandler {
    
    public ElevatorCabinSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        // Only allow elevator_cabin items
        return stack.is(itemCabin.get());
    }

    @Override
    public int getMaxStackSize() {
        // Only allow 1 cabin
        return 1;
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        // Only allow 1 cabin
        return 1;
    }
}
