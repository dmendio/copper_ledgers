package dev.dmendio.copper_ledgers.screen.custom;

import dev.dmendio.copper_ledgers.CopperLedger;
import dev.dmendio.copper_ledgers.ModMenuTypes;
import dev.dmendio.copper_ledgers.component.LedgerContents;
import dev.dmendio.copper_ledgers.component.ModComponents;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Server/client menu for the Copper Ledger.
 *
 * The top 54 slots display "ghost" entries (item types stored in the ledger).
 * The bottom 36 slots mirror the player's inventory for click-to-add functionality.
 * No physical items are ever moved — all interactions modify the DataComponent on the held ledger stack.
 */
public class CopperLedgerMenu extends AbstractContainerMenu {

    /** The number of ghost slots in the ledger grid. */
    public static final int LEDGER_SLOTS = 54;

    /** Total slots: 54 ledger + 27 main inventory + 9 hotbar. */
    public static final int TOTAL_SLOTS = LEDGER_SLOTS + 36;

    /** The display container backing the 54 ghost slots. */
    private final SimpleContainer ledgerDisplay = new SimpleContainer(LEDGER_SLOTS);

    /** Reference to the player's inventory, used to locate the held ledger stack. */
    private final Inventory playerInventory;

    /** The inventory slot index that held the ledger when the menu was opened. */
    private final int ledgerSlotIndex;

    /**
     * Server-side constructor — called when the player opens the ledger.
     */
    public CopperLedgerMenu(int syncId, Inventory playerInventory, int ledgerSlotIndex) {
        super(ModMenuTypes.COPPER_LEDGER_MENU, syncId);
        this.playerInventory = playerInventory;
        this.ledgerSlotIndex = ledgerSlotIndex;

        // Add 54 ghost slots (6 rows × 9 columns)
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new GhostSlot(ledgerDisplay, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        // Add 27 main inventory slots (3 rows × 9 columns)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new LockedPlayerSlot(playerInventory, col + (row + 1) * 9, 8 + col * 18, 140 + row * 18));
            }
        }

        // Add 9 hotbar slots
        for (int col = 0; col < 9; col++) {
            this.addSlot(new LockedPlayerSlot(playerInventory, col, 8 + col * 18, 198));
        }

        // Populate the ghost display from the current ledger data
        refreshGhostSlots();
    }

    /**
     * Client-side constructor — called by MenuType's factory.
     * The client doesn't know the ledger slot index, but it doesn't need it
     * because all data mutations happen server-side.
     */
    public CopperLedgerMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, playerInventory.getSelectedSlot());
    }

    /**
     * Gets the current LedgerContents from the held ledger ItemStack.
     */
    public LedgerContents getLedgerContents() {
        ItemStack ledgerStack = playerInventory.getItem(ledgerSlotIndex);
        if (ledgerStack.isEmpty() || !(ledgerStack.getItem() instanceof CopperLedger)) {
            return LedgerContents.EMPTY;
        }
        LedgerContents contents = ledgerStack.get(ModComponents.LEDGER_CONTENTS);
        return contents != null ? contents : LedgerContents.EMPTY;
    }

    /**
     * Writes the LedgerContents back to the held ledger ItemStack,
     * and updates the MAX_STACK_SIZE component accordingly.
     */
    private void setLedgerContents(LedgerContents contents) {
        ItemStack ledgerStack = playerInventory.getItem(ledgerSlotIndex);
        if (ledgerStack.isEmpty() || !(ledgerStack.getItem() instanceof CopperLedger)) return;

        if (contents.isEmpty()) {
            // Remove the component entirely when empty
            ledgerStack.remove(ModComponents.LEDGER_CONTENTS);
            ledgerStack.set(DataComponents.MAX_STACK_SIZE, 64);
        } else {
            ledgerStack.set(ModComponents.LEDGER_CONTENTS, contents);
            ledgerStack.set(DataComponents.MAX_STACK_SIZE, 1);
        }
    }

    /**
     * Refreshes the ghost slot display from the current LedgerContents.
     * Each slot gets a single-count ItemStack of the stored item type for display.
     */
    private void refreshGhostSlots() {
        LedgerContents contents = getLedgerContents();
        for (int i = 0; i < LEDGER_SLOTS; i++) {
            if (i < contents.size()) {
                ledgerDisplay.setItem(i, new ItemStack(contents.items().get(i), 1));
            } else {
                ledgerDisplay.setItem(i, ItemStack.EMPTY);
            }
        }
    }

    /**
     * Overrides all click behavior
     * No physical items are moved. Clicks either add or remove item types from the ledger data.
     */
    @Override
    public void clicked(int slotIndex, int button, ContainerInput clickType, Player player) {
        // Only process simple pickup clicks (left/right click)
        if (clickType != ContainerInput.PICKUP) {
            // Suppress all other click types (shift-click, drag, swap, clone, throw)
            return;
        }

        if (slotIndex < 0 || slotIndex >= TOTAL_SLOTS) {
            // Click outside the inventory — do nothing
            return;
        }

        if (slotIndex < LEDGER_SLOTS) {
            LedgerContents contents = getLedgerContents();
            if (slotIndex < contents.size()) {
                LedgerContents newContents = contents.removeItem(slotIndex);
                setLedgerContents(newContents);
                refreshGhostSlots();
                broadcastChanges();
            }
        } else {
            Slot clickedSlot = this.slots.get(slotIndex);
            ItemStack stackInSlot = clickedSlot.getItem();

            if (!stackInSlot.isEmpty() && stackInSlot.getItem() != Items.AIR) {
                Item itemType = stackInSlot.getItem();
                LedgerContents contents = getLedgerContents();
                LedgerContents newContents = contents.addItem(itemType);

                // Only update if something actually changed (item wasn't a duplicate)
                if (newContents != contents) {
                    setLedgerContents(newContents);
                    refreshGhostSlots();
                    broadcastChanges();
                }
            }
        }

        // Always clear the carried item to prevent any cursor pickup
        this.setCarried(ItemStack.EMPTY);
    }

    /**
     * No shift-click transfers in this menu.
     */
    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    /**
     * The menu is valid as long as the player still has the ledger in the expected slot.
     */
    @Override
    public boolean stillValid(Player player) {
        ItemStack ledgerStack = playerInventory.getItem(ledgerSlotIndex);
        return !ledgerStack.isEmpty() && ledgerStack.getItem() instanceof CopperLedger;
    }

    // ── Inner slot classes ──────────────────────────────────────────────

    /**
     * A slot that never allows placing or picking up items.
     * Used for the 54 ghost display slots in the ledger grid.
     */
    private static class GhostSlot extends Slot {
        public GhostSlot(SimpleContainer container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }

    /**
     * A slot backed by the player's inventory that prevents item pickup.
     * Items are displayed but cannot be taken — clicks are handled by the menu's clicked() override.
     */
    private static class LockedPlayerSlot extends Slot {
        public LockedPlayerSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }
}
