package com.dannymendiola.copper_ledgers.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.dannymendiola.copper_ledgers.ModItems;
import com.dannymendiola.copper_ledgers.component.ModComponents;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@Mixin(GrindstoneMenu.class)
public abstract class GrindstoneMenuMixin extends AbstractContainerMenu {

    @Shadow @Final private Container repairSlots;
    @Shadow @Final private Container resultSlots; 

    protected GrindstoneMenuMixin(MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    /**
     * Inject at the TAIL of the constructor to replace input slots 0 and 1
     * with custom slots that allow COPPER_LEDGER alongside standard items.
     */
    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("TAIL"))
    private void replaceGrindstoneSlots(int containerId, Inventory playerInventory, ContainerLevelAccess access, CallbackInfo ci) {
        // Swap slot 0 (Top input)
        this.slots.set(0, new Slot(this.repairSlots, 0, 49, 19) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                boolean grindableLedger = stack.is(ModItems.COPPER_LEDGER) && (stack.get(ModComponents.LEDGER_CONTENTS) != null);

                return grindableLedger || stack.isDamageableItem() || stack.isEnchanted() || stack.is(net.minecraft.world.item.Items.ENCHANTED_BOOK);
            }
        });
    }

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void handleCopperLedgerGrind(CallbackInfo ci) {
        ItemStack topInput = this.repairSlots.getItem(0);
        ItemStack bottomInput = this.repairSlots.getItem(1);

        boolean isTopLedger = topInput.is(ModItems.COPPER_LEDGER);
        boolean isBottomLedger = bottomInput.is(ModItems.COPPER_LEDGER);

        if ((isTopLedger && bottomInput.isEmpty()) || (isBottomLedger && topInput.isEmpty())) {
            ItemStack outputLedger = new ItemStack(ModItems.COPPER_LEDGER);
            this.resultSlots.setItem(0, outputLedger);
            this.broadcastChanges();
            ci.cancel();
        }
    }
}