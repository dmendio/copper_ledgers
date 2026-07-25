package dev.dmendio.copper_ledgers.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.dmendio.copper_ledgers.ModItems;
import dev.dmendio.copper_ledgers.component.LedgerContents;
import dev.dmendio.copper_ledgers.component.ModComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;



@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {

    @Shadow
    private ItemStack carried;
    
    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
    private void ledgerClicked(final int slotIndex, final int buttonNum, final ContainerInput containerInput, final Player player, CallbackInfo ci) {
        if (
            containerInput == ContainerInput.QUICK_MOVE &&
            carried.is(ModItems.COPPER_LEDGER)
        ) {
            // ((AbstractContainerMenu)this).getSlot(slotIndex)
            AbstractContainerMenu thisMenu = (AbstractContainerMenu)(Object)this;

            if (slotIndex >= 0 && slotIndex < thisMenu.slots.size()) {
                Slot clickedSlot = thisMenu.getSlot(slotIndex);
                if (clickedSlot.getItem().is(ModItems.COPPER_LEDGER)) {

                    LedgerContents carriedContents = carried.get(ModComponents.LEDGER_CONTENTS);

                    if (carriedContents == null) return;

                    ItemStack clickedLedger = clickedSlot.getItem();

                    if (carriedContents.hasItem(clickedLedger)) {
                        LedgerContents newContents = carriedContents.getItemsWithRemoved(clickedLedger);

                        if (newContents.isEmpty()) {
                            carried.remove(ModComponents.LEDGER_CONTENTS);
                            carried.set(DataComponents.MAX_STACK_SIZE, 64);
                        } else {
                            carried.set(ModComponents.LEDGER_CONTENTS, newContents);
                        }
                    } else {
                        carried.set(
                            ModComponents.LEDGER_CONTENTS, 
                            carriedContents.getItemsWithAdded(clickedLedger)
                        );
                    }
                    
                    ci.cancel();
                }
            }
        }
    }
}
