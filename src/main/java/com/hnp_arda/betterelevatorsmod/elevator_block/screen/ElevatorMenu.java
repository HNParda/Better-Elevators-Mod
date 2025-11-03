package com.hnp_arda.betterelevatorsmod.elevator_block.screen;

import com.hnp_arda.betterelevatorsmod.elevator_block.entity.ElevatorBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemStackHandler;

import static com.hnp_arda.betterelevatorsmod.ModRegister.ELEVATOR_MENU;
import static com.hnp_arda.betterelevatorsmod.ModRegister.blockElevatorBlock;
import static com.hnp_arda.betterelevatorsmod.ModRegister.itemCabin;

public class ElevatorMenu extends AbstractContainerMenu {

    private final ElevatorBlockEntity blockEntity;
    private final ItemStackHandler itemHandler;
    private final ContainerLevelAccess containerLevelAccess;

    // Constructor for server side
    public ElevatorMenu(int pContainerId, Inventory inv, ElevatorBlockEntity blockEntity) {
        super(ELEVATOR_MENU.get(), pContainerId);
        this.blockEntity = blockEntity;
        this.itemHandler = blockEntity.getItemHandler();
        this.containerLevelAccess = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        // Add the 2 elevator item slots
        // Slot 0: Only accepts 1 elevator_cabin item
        this.addSlot(new ElevatorCabinSlot(itemHandler, 0, 62, 35));
        // Slot 1: Only accepts placeable blocks (BlockItems), no cabins
        this.addSlot(new PlaceableBlockSlot(itemHandler, 1, 98, 35));

        // Player Inventory Slots (3 rows of 9 slots)
        int startX = 8;
        int startY = 84;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, startX + col * 18, startY + row * 18));
            }
        }

        // Player Hotbar (1 row of 9 slots)
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inv, col, startX + col * 18, startY + 58));
        }
    }

    // Constructor for client side (called from packet)
    public ElevatorMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, getBlockEntity(inv, extraData));
    }

    private static ElevatorBlockEntity getBlockEntity(Inventory inv, FriendlyByteBuf extraData) {
        BlockEntity blockEntity = inv.player.level().getBlockEntity(extraData.readBlockPos());
        if (blockEntity instanceof ElevatorBlockEntity elevatorBlockEntity) {
            return elevatorBlockEntity;
        }
        throw new IllegalStateException("Block entity is not an ElevatorBlockEntity!");
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            // If clicking on one of the elevator slots (0-1)
            if (pIndex < 2) {
                // Try to move to player inventory
                if (!this.moveItemStackTo(slotStack, 2, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // If clicking on player inventory, try to move to elevator slots
                // Try slot 0 first (cabin slot) only if it's a cabin item
                if (slotStack.is(itemCabin.get())) {
                    if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    // Otherwise try slot 1
                    if (!this.moveItemStackTo(slotStack, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(pPlayer, slotStack);
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(this.containerLevelAccess, pPlayer, blockElevatorBlock.get());
    }

    public ElevatorBlockEntity getBlockEntity() {
        return blockEntity;
    }
}
