package com.hnp_arda.betterelevatorsmod;

import com.hnp_arda.betterelevatorsmod.blocks.elevator_block.ElevatorBlock;
import com.hnp_arda.betterelevatorsmod.blocks.elevator_block.entity.ElevatorBlockEntity;
import com.hnp_arda.betterelevatorsmod.blocks.elevator_block.screen.ElevatorMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static com.hnp_arda.betterelevatorsmod.BetterElevatorsMod.MODID;
import static net.minecraft.core.registries.Registries.MENU;

public class ModRegister {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(MENU, MODID);


   // public static final DeferredHolder<MenuType<?>, MenuType<ElevatorMenu>> ELEVATOR_MENU = MENUS.register("elevator_menu", () -> new MenuType(ElevatorMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>, MenuType<ElevatorMenu>> ELEVATOR_MENU = MENUS.register("elevator_menu", () -> IMenuTypeExtension.create(ElevatorMenu::new));


    public static final DeferredBlock<ElevatorBlock> blockElevatorBlock = BLOCKS.register("elevator_block", ElevatorBlock::new);

    // public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.register("bismuth", () -> new Item(new Item.Properties()));
    public static final DeferredItem<BlockItem> itemElevatorBlock = ITEMS.registerSimpleBlockItem("elevator_block", blockElevatorBlock);

    public static final Supplier<BlockEntityType<ElevatorBlockEntity>> entityElevatorBlock = BLOCK_ENTITY_TYPES.register("elevator_block_entity",
            () -> BlockEntityType.Builder.of(ElevatorBlockEntity::new, blockElevatorBlock.get()).build(null));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> betterElevatorsTab = CREATIVE_MODE_TABS.register("better_elevators_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.betterelevatorsmod"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> itemElevatorBlock.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(itemElevatorBlock.get());
            }).build());

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        BLOCKS.register(eventBus);
        BLOCK_ENTITY_TYPES.register(eventBus);
        CREATIVE_MODE_TABS.register(eventBus);
        MENUS.register(eventBus);
    }

}
