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

    // Ledger tooltip for quick add/remove
    @Inject(method = "extractTooltip", at = @At("HEAD"), cancellable = true)
    private void addLedgerQuickAddTooltip(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, CallbackInfo ci) {

        if ((
            (Screen)this) instanceof CreativeModeInventoryScreen creativeScreen && 
            !creativeScreen.isInventoryOpen()
        ) return;

        if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            ItemStack cursorStack = this.menu.getCarried();
            ItemStack hoveredItem = this.hoveredSlot.getItem();

            if (
                (cursorStack != null && hoveredItem != null) &&
                cursorStack.count() == 1 &&
                cursorStack.is(ModItems.COPPER_LEDGER)
            ) {
                String hoveredName = this.getTooltipFromContainerItem(hoveredItem).get(0).getString();

                String clickType = hoveredItem.is(ModItems.COPPER_LEDGER) ? "Shift+Click" : "Click";

                List<Component> newTooltip = new ArrayList<Component>();

                LedgerContents ledgerContents = cursorStack.get(ModComponents.LEDGER_CONTENTS);

                newTooltip.add(Component.translatable(
                    hoveredName +
                    (ledgerContents == null || !ledgerContents.hasItem(hoveredItem) ? "-> Ledger" : "")
                ));

                if (
                    ledgerContents != null &&
                    ledgerContents.items().size() == LedgerContents.MAX_ENTRIES && 
                    !ledgerContents.hasItem(hoveredItem)
                ) {
                    newTooltip.add(Component.translatable("Ledger is full!").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
                } else if (
                    ledgerContents != null && 
                    ledgerContents.hasItem(hoveredItem)) {
                    newTooltip.add(Component.translatable(
                        "Already in ledger"
                    ).withStyle(ChatFormatting.GRAY));
                    newTooltip.add(Component.translatable(
                        clickType + " to remove")
                        .withStyle(
                            ChatFormatting.YELLOW, ChatFormatting.ITALIC
                    ));
                } else {
                    newTooltip.add(Component.translatable(
                        clickType + " to add")
                        .withStyle(
                            ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC
                    ));
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

    // Highlight other slots when ledger on cursor
    @Inject(
        method = "extractContents",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;extractSlots(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V",
            shift = At.Shift.AFTER
        )
    )
    private void renderLedgerSlotIndicators(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        ItemStack carried = this.menu.getCarried();
        if (carried.isEmpty() || !(carried.is(ModItems.COPPER_LEDGER))) {
            return;
        }

        LedgerContents contents = carried.get(ModComponents.LEDGER_CONTENTS);
        if (contents == null || contents.items().isEmpty()) {
            return;
        }

        for (Slot slot : this.menu.slots) {
            if (!slot.isActive()) {
                continue;
            }

            ItemStack slotStack = slot.getItem();
            if (slotStack.isEmpty()) {
                continue;
            }

            int x = slot.x;
            int y = slot.y;

            if (contents.hasItem(slotStack.getItem())) {
                graphics.fill(x, y, x + 16, y + 16, 0x60c0c0c0);
            }
        }
    }
}
