package dev.dmendio.copper_ledgers.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.dmendio.copper_ledgers.ModItems;
import dev.dmendio.copper_ledgers.component.LedgerContents;
import dev.dmendio.copper_ledgers.component.ModComponents;

import net.minecraft.world.Container;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.TransportItemsBetweenContainers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Mixin(TransportItemsBetweenContainers.class)
public class TransportItemsBetweenContainersMixin {

    // TransportItemsBetweenContainers::doReachedTargetInteraction()
    @Inject(method="Lnet/minecraft/world/entity/ai/behavior/TransportItemsBetweenContainers;doReachedTargetInteraction(Lnet/minecraft/world/entity/PathfinderMob;Lnet/minecraft/world/Container;Ljava/util/function/BiConsumer;Ljava/util/function/BiConsumer;Ljava/util/function/BiConsumer;Ljava/util/function/BiConsumer;)V", at = @At("HEAD"), cancellable = true)
    private void checkCopperLedgers(
        final PathfinderMob body,
        final Container container,
        final BiConsumer<PathfinderMob, Container> onPickupSuccess,
        final BiConsumer<PathfinderMob, Container> onPickupFailure,
        final BiConsumer<PathfinderMob, Container> onPlaceSuccess,
        final BiConsumer<PathfinderMob, Container> onPlaceFailure,
        CallbackInfo ci
    ) {
        int chestSize = container.getContainerSize();

        List<ItemStack> ledgers = new ArrayList<>();

        for (int i = 0; i < chestSize; i++) {
            ItemStack stack = container.getItem(i);
            if (stack.is(ModItems.COPPER_LEDGER)) {
                ledgers.add(stack);
            }
        }

        for (ItemStack ledger : ledgers) {
            if (ledgerHasItemMatchingHandItem(body, ledger)) {
                // have the golem place the item and cancel out of default method
                onPlaceSuccess.accept(body, container);
                ci.cancel();
                return;
            }
        }
    }

    private static boolean ledgerHasItemMatchingHandItem(final PathfinderMob body, final ItemStack ledger) {
        Item golemHandItem = body.getMainHandItem().getItem();
        LedgerContents contents = ledger.get(ModComponents.LEDGER_CONTENTS);
        if (contents == null) return false;
        return contents.hasItem(golemHandItem);
    }
    
}
