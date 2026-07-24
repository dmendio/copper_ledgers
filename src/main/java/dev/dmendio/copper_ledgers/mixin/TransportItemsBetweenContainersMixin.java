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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.TransportItemsBetweenContainers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(TransportItemsBetweenContainers.class)
public class TransportItemsBetweenContainersMixin {

    // TransportItemsBetweenContainers::doReachedTargetInteraction()
    @Inject(method="Lnet/minecraft/world/entity/ai/behavior/TransportItemsBetweenContainers;doReachedTargetInteraction(Lnet/minecraft/world/entity/PathfinderMob;Lnet/minecraft/world/Container;Ljava/util/function/BiConsumer;Ljava/util/function/BiConsumer;Ljava/util/function/BiConsumer;Ljava/util/function/BiConsumer;)V", at = @At("HEAD"), cancellable = true)
    private void checkCopperLedgers(
        final PathfinderMob golem,
        final Container container,
        final BiConsumer<PathfinderMob, Container> onPickupSuccess,
        final BiConsumer<PathfinderMob, Container> onPickupFailure,
        final BiConsumer<PathfinderMob, Container> onPlaceSuccess,
        final BiConsumer<PathfinderMob, Container> onPlaceFailure,
        CallbackInfo ci
    ) {
        int chestSize = container.getContainerSize();

        List<ItemStack> chestLedgers = new ArrayList<>();

        for (int i = 0; i < chestSize; i++) {
            ItemStack stack = container.getItem(i);
            if (stack.is(ModItems.COPPER_LEDGER)) {
                chestLedgers.add(stack);
            }
        }

        // Play a sound if the golem is going through a ledger
        if (chestLedgers.size() >= 1) {
            Level level = golem.level();
            level.playSound(
                null,
                golem.getX(), golem.getY(), golem.getZ(),
                SoundEvents.COPPER_HIT,
                SoundSource.PLAYERS,
                1.0f, 1.0f
            );
            level.playSound(
                null,
                golem.getX(), golem.getY(), golem.getZ(),
                SoundEvents.BOOK_PAGE_TURN,
                SoundSource.PLAYERS,
                1.0f, 1.0f
            );
        }

        boolean holdingLedger = golem.getMainHandItem().is(ModItems.COPPER_LEDGER);

        if (holdingLedger) {
            for (ItemStack ledger : chestLedgers) {
                if (ledger.get(ModComponents.LEDGER_CONTENTS) == null) {
                    onPlaceSuccess.accept(golem, container);
                    ci.cancel();
                    return;
                }

                if (ledgerHasItemMatchingHandItem(golem, ledger)) {
                    onPlaceSuccess.accept(golem, container);
                    ci.cancel();
                    return;
                }
            }
            if (!chestLedgers.isEmpty()) {
                // the only ledgers in the chest are non-empty and do not specify ledger
                onPlaceFailure.accept(golem, container);
                ci.cancel();
            }
            return;
        }

        for (ItemStack ledger : chestLedgers) {
            if (ledgerHasItemMatchingHandItem(golem, ledger)) {
                // have the golem place the item and cancel out of default method
                onPlaceSuccess.accept(golem, container);
                ci.cancel();
                return;
            }
        }
    }

    private static boolean ledgerHasItemMatchingHandItem(final PathfinderMob golem, final ItemStack ledger) {
        Item golemHandItem = golem.getMainHandItem().getItem();
        LedgerContents contents = ledger.get(ModComponents.LEDGER_CONTENTS);
        if (contents == null) return false;
        return contents.hasItem(golemHandItem);
    }
    
}
