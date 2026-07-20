package com.dannymendiola.copper_ledgers.client;

import com.dannymendiola.copper_ledgers.CopperLedgerMenu;
import com.dannymendiola.copper_ledgers.CopperLedgers;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Client-side screen for the Copper Ledger.
 * Renders a 54-slot grid with ghost items (semi-transparent) and the player inventory below.
 */
public class CopperLedgerScreen extends AbstractContainerScreen<CopperLedgerMenu> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
        CopperLedgers.MOD_ID, "textures/gui/ledger/ledger_gui.png"
    );

    public CopperLedgerScreen(CopperLedgerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        // Match the double-chest layout dimensions
        // this.imageWidth = 176;
        // this.imageHeight = 222;
        // Position the inventory label to match the gap between ledger grid and player inventory
        this.inventoryLabelY = this.imageHeight - 94;
    }

    // @Override
    // protected void renderBg(/*still not resolved ->*/GuiGraphicsExtractor graphics, float partialTick, int mouseX, int mouseY) {
    //     graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    // }
//     @Override
// public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
//     // graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
// }
    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        // You must pass the render pipeline as argument #1, and specify texture width/height (usually 256x256) at the end
        graphics.blit(
            net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, 
            TEXTURE, 
            this.leftPos, 
            this.topPos, 
            0.0F, 0.0F, 
            this.imageWidth, 
            this.imageHeight, 
            256, 256
        );
    }
}
