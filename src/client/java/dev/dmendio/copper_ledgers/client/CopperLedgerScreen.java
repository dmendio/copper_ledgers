package dev.dmendio.copper_ledgers.client;

import java.util.List;

import dev.dmendio.copper_ledgers.CopperLedgers;
import dev.dmendio.copper_ledgers.component.LedgerContents;
import dev.dmendio.copper_ledgers.screen.custom.CopperLedgerMenu;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Client-side screen for the Copper Ledger.
 * Renders a 54-slot grid with ghost items (semi-transparent) and the player inventory below.
 */
public class CopperLedgerScreen extends AbstractContainerScreen<CopperLedgerMenu> {

    private CopperLedgerMenu menu;

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
        CopperLedgers.MOD_ID, "textures/gui/ledger/ledger_gui.png"
    );


    public CopperLedgerScreen(CopperLedgerMenu menu, Inventory playerInventory, Component title) {
        final int IMAGE_WIDTH = 176;
        final int IMAGE_HEIGHT = 222;
        super(menu, playerInventory, title, IMAGE_WIDTH, IMAGE_HEIGHT);
        // Position the inventory label to match the gap between ledger grid and player inventory
        this.inventoryLabelY = this.imageHeight - 94;
        this.menu = menu;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);

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

    @Override
    protected void extractSlot(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY) {
        if (slot.index < CopperLedgerMenu.LEDGER_SLOTS) {
            int x = slot.x;
            int y = slot.y;

            graphics.fill(x, y, x + 16, y + 16, 0x33d66d48);

            super.extractSlot(graphics, slot, mouseX, mouseY);

            graphics.outline(x, y, 16, 16, 0x66803921);

        }

        super.extractSlot(graphics, slot, mouseX, mouseY);

    }

    @Override
    public List<Component> getTooltipFromContainerItem(ItemStack stack) {
        List<Component> lines = super.getTooltipFromContainerItem(stack).subList(0, 1); // just the item name

        // Check if player is hovering over one of the 54 Ledger Slots
        if (this.hoveredSlot != null && this.hoveredSlot.index < CopperLedgerMenu.LEDGER_SLOTS) {
            lines.add(Component.literal(""));
            lines.add(Component.translatable("Ledger entry")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            lines.add(Component.translatable("(Click to remove)")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        } else if (this.hoveredSlot != null) {
            LedgerContents contents = menu.getLedgerContents();
            
            // check if contents already has hovered item's type
            boolean hasItemType = contents.hasItem(this.hoveredSlot.getItem().getItem());
            

            if (hasItemType) {
                lines.add(Component.translatable("(Already in ledger)")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            } else {
                lines.add(Component.translatable("(Click to add to ledger)")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            }
            
        }

        return lines;
    }
}
