package dev.dmendio.copper_ledgers.client.mixin;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.dmendio.copper_ledgers.ModItems;
import dev.dmendio.copper_ledgers.component.LedgerContents;
import dev.dmendio.copper_ledgers.component.ModComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends Screen {

    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Shadow @Final
    protected AbstractContainerMenu menu;

    @Shadow
    protected @Nullable Slot hoveredSlot;

    @Shadow
    protected abstract List<Component> getTooltipFromContainerItem(final ItemStack itemStack);

    @Inject(method = "extractTooltip", at = @At("HEAD"), cancellable = true)
    private void addLedgerQuickAddTooltip(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, CallbackInfo ci) {

        // if (this.hoveredSlot != null && this.hoveredSlot.getClass().getName().contains("Creative")) return;
        if ((
            (Screen)this) instanceof CreativeModeInventoryScreen creativeScreen && 
            !creativeScreen.isInventoryOpen()
        ) return;

        if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            ItemStack cursorStack = this.menu.getCarried();
            ItemStack hoveredItem = this.hoveredSlot.getItem();

            if (hoveredItem.getItem().equals(ModItems.COPPER_LEDGER)) return;

            if (
                (cursorStack != null && hoveredItem != null) &&
                cursorStack.count() == 1 &&
                cursorStack.getItem().equals(ModItems.COPPER_LEDGER)
            ) {
                String hoveredName = this.getTooltipFromContainerItem(hoveredItem).get(0).getString();

                List<Component> newTooltip = new ArrayList<Component>();
                newTooltip.add(Component.translatable(hoveredName).withStyle(ChatFormatting.GRAY));

                
                LedgerContents ledgerContents = cursorStack.get(ModComponents.LEDGER_CONTENTS);
                if (ledgerContents != null && ledgerContents.items().size() == LedgerContents.MAX_ENTRIES && !ledgerContents.hasItem(hoveredItem)) {
                    newTooltip.add(Component.translatable("Ledger is full!").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
                } else if (ledgerContents != null && ledgerContents.hasItem(hoveredItem)) {
                    newTooltip.add(Component.translatable("Click to remove from ledger").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC));
                } else {
                    newTooltip.add(Component.translatable("Click to add to ledger").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC));
                }

                graphics.setTooltipForNextFrame(
                    this.font, 
                    newTooltip,
                    hoveredItem.getTooltipImage(),
                    mouseX,
                    mouseY,
                    (Identifier)hoveredItem.get(DataComponents.TOOLTIP_STYLE)
                );
    
                ci.cancel();
            }
        }
    }
}
