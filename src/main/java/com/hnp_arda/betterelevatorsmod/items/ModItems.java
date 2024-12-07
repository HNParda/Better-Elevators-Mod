package com.hnp_arda.betterelevatorsmod.items;

import com.hnp_arda.betterelevatorsmod.blocks.elevator_block.ElevatorBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.hnp_arda.betterelevatorsmod.ExampleMod.MODID;

public class ModItems {


    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);


    public static final DeferredBlock<Block> blockElevatorBlock = BLOCKS.register("elevator_block", ElevatorBlock::new);

    public static final DeferredItem<BlockItem> itemElevatorBlock = ITEMS.registerSimpleBlockItem("elevator_block", blockElevatorBlock);

   // public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.register("bismuth", () -> new Item(new Item.Properties()));

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> betterElevatorsTab = CREATIVE_MODE_TABS.register("better_elevators_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.betterelevatorsmod"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> itemElevatorBlock.get().getDefaultInstance())
            .displayItems((parameters, output) -> { output.accept(itemElevatorBlock.get());
             //   output.accept(ModItems.EXAMPLE_ITEM);
            }).build());





    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        BLOCKS.register(eventBus);
        CREATIVE_MODE_TABS.register(eventBus);
    }

}
