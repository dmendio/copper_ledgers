package com.dannymendiola.copper_ledgers;

import java.util.function.Function;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public class ModItems {
    
    public static <T extends Item> T register(String name, Function<Item.Properties, T> itemFactory, Item.Properties settings) {
        // Create the item key.
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(CopperLedgers.MOD_ID, name));

        // Create the item instance.
        T item = itemFactory.apply(settings.setId(itemKey));

        // Register the item
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);

        return item;
    }

    public static final Item COPPER_LEDGER = register(
        "copper_ledger", Item::new, new Item.Properties().stacksTo(1));
        
    public static void initialize() {
        // add the item to the creative menu
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
            .register((tab) -> {
                tab.accept(COPPER_LEDGER);
            });
    }

}
