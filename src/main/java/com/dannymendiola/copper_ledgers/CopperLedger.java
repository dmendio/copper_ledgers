package com.dannymendiola.copper_ledgers;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CopperLedger extends Item {
    public CopperLedger(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = player.getItemInHand(hand);

        // Stack splitting: ensure exactly 1 ledger is held before opening
        if (stack.getCount() > 1) {
            int remaining = stack.getCount() - 1;
            ItemStack splitOff = stack.copyWithCount(remaining);

            // Attempt to place the remainder into the player's inventory
            if (!player.getInventory().add(splitOff)) {
                // Inventory is completely full — abort without opening
                return InteractionResult.FAIL;
            }

            // If add() absorbed some but not all, check for leftovers
            if (!splitOff.isEmpty()) {
                // Could not fit everything — give back what we took and abort
                // (This shouldn't happen if add() returned true, but be safe)
                player.getInventory().add(splitOff);
                return InteractionResult.FAIL;
            }

            // Set the hand stack to exactly 1
            stack.setCount(1);
        }

        // Determine the slot index of the ledger in the player's inventory
        // Offhand is slot 40 in the player's Inventory
        int slotIndex = hand == InteractionHand.MAIN_HAND
            ? player.getInventory().getSelectedSlot()
            : 40;

        // Open the ledger menu
        if (player instanceof ServerPlayer serverPlayer) {
            final int finalSlotIndex = slotIndex;
            serverPlayer.openMenu(new SimpleMenuProvider(
                (syncId, playerInventory, p) -> new CopperLedgerMenu(syncId, playerInventory, finalSlotIndex),
                Component.translatable("container.copper_ledgers.copper_ledger")
            ));
        }

        return InteractionResult.SUCCESS;
    }
}
