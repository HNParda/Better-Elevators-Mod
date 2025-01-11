package com.hnp_arda.betterelevatorsmod.blocks.elevator_block.entity;

import com.hnp_arda.betterelevatorsmod.blocks.elevator_block.screen.ElevatorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import static com.hnp_arda.betterelevatorsmod.ModRegister.entityElevatorBlock;

public class ElevatorBlockEntity extends BlockEntity implements MenuProvider {


    public ElevatorBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(entityElevatorBlock.get(), pPos, pBlockState);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.betterelevatorsmod.elevator_block");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new ElevatorMenu(pContainerId, pPlayerInventory, null);
    }


    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
    }


}
