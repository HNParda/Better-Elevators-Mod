package com.hnp_arda.betterelevatorsmod.blocks.elevator_block.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import static com.hnp_arda.betterelevatorsmod.ModRegister.ELEVATOR_MENU;

public class ElevatorMenu extends AbstractContainerMenu {
    public ElevatorMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        super(ELEVATOR_MENU.get(), pContainerId);
        checkContainerSize(inv, 2);
    }


    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return null;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }
}
