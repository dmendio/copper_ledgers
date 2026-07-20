package com.dannymendiola.copper_ledgers;


import java.util.List;

import net.minecraft.client.renderer.gizmos.DrawableGizmoPrimitives.Text;
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

        // Determine the slot index of the ledger in the player's inventory
        int slotIndex = hand == InteractionHand.MAIN_HAND
            ? player.getInventory().getSelectedSlot()
            : 40; // Offhand slot index in Inventory

        // Stack splitting: ensure exactly 1 ledger is held before opening
        if (stack.getCount() > 1) {
            int remaining = stack.getCount() - 1;
            ItemStack splitOff = stack.copyWithCount(remaining);

            // 1. Check if the inventory has enough space, ignoring the active hand slot
            if (!hasSpaceExcludingSlot(player, splitOff, slotIndex)) {
                // Inventory is full or cannot fit the split stack — abort without opening
                return InteractionResult.FAIL;
            }

            // 2. Safely insert the split stack into other inventory slots
            insertExcludingSlot(player, splitOff, slotIndex);

            // 3. Set the active hand stack to exactly 1
            stack.setCount(1);
        }

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

    /**
     * Checks if player's inventory has enough space to fit stackToInsert without using excludedSlot.
     */
    private boolean hasSpaceExcludingSlot(Player player, ItemStack stackToInsert, int excludedSlot) {
        int needed = stackToInsert.getCount();
        int availableSpace = 0;

        for (int i = 0; i < 36; i++) { // 0-35 are standard inventory + hotbar
            if (i == excludedSlot) continue;

            ItemStack existing = player.getInventory().getItem(i);
            if (existing.isEmpty()) {
                availableSpace += stackToInsert.getMaxStackSize();
            } else if (ItemStack.isSameItemSameComponents(existing, stackToInsert)) {
                availableSpace += Math.max(0, existing.getMaxStackSize() - existing.getCount());
            }

            if (availableSpace >= needed) {
                return true;
            }
        }
        return false;
    }

    /**
     * Inserts stackToInsert into player's main inventory/hotbar, strictly skipping excludedSlot.
     */
    private void insertExcludingSlot(Player player, ItemStack stackToInsert, int excludedSlot) {
        ItemStack remainder = stackToInsert.copy();

        // Pass 1: Merge into existing matching stacks
        for (int i = 0; i < 36 && !remainder.isEmpty(); i++) {
            if (i == excludedSlot) continue;

            ItemStack existing = player.getInventory().getItem(i);
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, remainder)) {
                int space = existing.getMaxStackSize() - existing.getCount();
                if (space > 0) {
                    int countToAdd = Math.min(remainder.getCount(), space);
                    existing.grow(countToAdd);
                    remainder.shrink(countToAdd);
                }
            }
        }

        // Pass 2: Fill empty slots
        for (int i = 0; i < 36 && !remainder.isEmpty(); i++) {
            if (i == excludedSlot) continue;

            ItemStack existing = player.getInventory().getItem(i);
            if (existing.isEmpty()) {
                int countToAdd = Math.min(remainder.getCount(), remainder.getMaxStackSize());
                ItemStack newStack = remainder.copyWithCount(countToAdd);
                player.getInventory().setItem(i, newStack);
                remainder.shrink(countToAdd);
            }
        }
    }

    // @Override
    // public void appendToolTip(ItemStack stack, TooltipContext tipCtx, List<Text> tooltip, TooltipType type)
}