package dev.dmendio.copper_ledgers;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.ItemComponentTooltipProviderRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dmendio.copper_ledgers.component.ModComponents;

public class CopperLedgers implements ModInitializer {
	public static final String MOD_ID = "copper_ledgers";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModComponents.initialize();
		ModMenuTypes.initialize();
        ModItems.initialize();

        ItemComponentTooltipProviderRegistry.addAfter(DataComponents.DAMAGE, ModComponents.LEDGER_TOOLTIP);
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
