package com.dannymendiola.copper_ledgers;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/**
 * Registers custom DataComponentTypes for the Copper Ledgers mod.
 */
public class ModDataComponents {

    public static final DataComponentType<LedgerContents> LEDGER_CONTENTS = Registry.register(
        BuiltInRegistries.DATA_COMPONENT_TYPE,
        Identifier.fromNamespaceAndPath(CopperLedgers.MOD_ID, "ledger_contents"),
        DataComponentType.<LedgerContents>builder()
            .persistent(LedgerContents.CODEC)
            .networkSynchronized(LedgerContents.STREAM_CODEC)
            .build()
    );

    public static void initialize() {
        CopperLedgers.LOGGER.info("Registering {} data components", CopperLedgers.MOD_ID);
    }
}
