package dev.dmendio.copper_ledgers;

import dev.dmendio.copper_ledgers.component.LedgerContents;
import dev.dmendio.copper_ledgers.component.ModComponents;
import dev.dmendio.copper_ledgers.screen.custom.CopperLedgerMenu;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CopperLedger extends Item {
    public CopperLedger(Properties props) {
        super(props);
    }

    @Override
    public boolean overrideStackedOnOther(final ItemStack self, final Slot slot, final ClickAction clickAction, final Player player) {

        if (
            clickAction != ClickAction.PRIMARY
        ) return false;

        ItemStack hoveredItem = slot.getItem();

        if (
            self.getCount() != 1 || 
            hoveredItem.isEmpty()
        ) return false;

        LedgerContents contents = self.getOrDefault(ModComponents.LEDGER_CONTENTS, LedgerContents.EMPTY);


        if (contents.hasItem(hoveredItem)) {
            LedgerContents newContents = contents.getItemsWithRemoved(hoveredItem.getItem());

            if (newContents.isEmpty()) {
                self.remove(ModComponents.LEDGER_CONTENTS);
                self.set(DataComponents.MAX_STACK_SIZE, 64);
            } else {
                self.set(ModComponents.LEDGER_CONTENTS, newContents);
            }

            player.playSound(SoundEvents.COPPER_HIT, 1.0f, 0.8f);

        } else {
            if (contents.items().size() == LedgerContents.MAX_ENTRIES) {
                return false;
            }

            self.set(
                ModComponents.LEDGER_CONTENTS,
                contents.getItemsWithAdded(hoveredItem.getItem())
            );
            self.set(DataComponents.MAX_STACK_SIZE, 1);

            player.playSound(SoundEvents.COPPER_STEP);
        }

        return true; //super.overrideStackedOnOther(self, slot, clickAction, player);
    }

    @Override
    public boolean isBarVisible(final ItemStack stack) {
        return stack.get(ModComponents.LEDGER_CONTENTS) != null;
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

            if (!hasSpaceExcludingSlot(player, splitOff, slotIndex)) {
                // Inventory is full or cannot fit the split stack — abort without opening
                return InteractionResult.FAIL;
            }

            insertExcludingSlot(player, splitOff, slotIndex);

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

        level.playSound(
            null,
            player.getX(), player.getY(), player.getZ(),
            SoundEvents.COPPER_HIT,
            SoundSource.PLAYERS,
            1.0f, 1.0f
        );
        level.playSound(
            null,
            player.getX(), player.getY(), player.getZ(),
            SoundEvents.BOOK_PAGE_TURN,
            SoundSource.PLAYERS,
            1.0f, 1.0f
        );

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


}