package com.dannymendiola.copper_ledgers;

import com.dannymendiola.copper_ledgers.screen.custom.CopperLedgerMenu;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

/**
 * Registers custom MenuTypes for the Copper Ledgers mod.
 */
public class ModMenuTypes {

    public static final MenuType<CopperLedgerMenu> COPPER_LEDGER_MENU = Registry.register(
        BuiltInRegistries.MENU,
        Identifier.fromNamespaceAndPath(CopperLedgers.MOD_ID, "copper_ledger"),
        new MenuType<>(CopperLedgerMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );

    public static void initialize() {
        // Force static initialization of the menu type
        CopperLedgers.LOGGER.info("Registering {} menu types", CopperLedgers.MOD_ID);
    }
}
