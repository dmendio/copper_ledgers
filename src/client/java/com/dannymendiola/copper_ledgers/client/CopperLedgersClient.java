package com.dannymendiola.copper_ledgers.client;

import com.dannymendiola.copper_ledgers.ModMenuTypes;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public class CopperLedgersClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Register the Copper Ledger screen factory
		// MenuScreens.register(ModMenuTypes.COPPER_LEDGER_MENU, CopperLedgerScreen::new);
	}
}