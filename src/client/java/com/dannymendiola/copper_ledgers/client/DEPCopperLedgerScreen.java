package com.dannymendiola.copper_ledgers.client;

import com.dannymendiola.copper_ledgers.CopperLedgerMenu;
import com.dannymendiola.copper_ledgers.CopperLedgers;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/**
 * Client-side screen for the Copper Ledger.
 * Renders a 54-slot grid with ghost items (semi-transparent) and the player inventory below.
 */
public class DEPCopperLedgerScreen extends AbstractContainerScreen<CopperLedgerMenu> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
        CopperLedgers.MOD_ID, "textures/gui/ledger/ledger_gui.png"
    );

    public DEPCopperLedgerScreen(CopperLedgerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        // Match the double-chest layout dimensions
        // this.imageWidth = 176;
        // this.imageHeight = 222;
        // Position the inventory label to match the gap between ledger grid and player inventory
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderSlot(GuiGraphics graphics, Slot slot) {
        // Apply a ghost effect (semi-transparent) to items in the ledger's 54 ghost slots
        if (slot.index < CopperLedgerMenu.LEDGER_SLOTS && slot.hasItem()) {
            // RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.45f);
            super.renderSlot(graphics, slot);
            // RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        } else {
            super.renderSlot(graphics, slot);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
