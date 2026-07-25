package dev.dmendio.copper_ledgers.component;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Immutable record storing the list of item types registered in a Copper Ledger.
 * Serialized to disk via {@link #CODEC} and synced over the network via {@link #STREAM_CODEC}.
 */
public record LedgerContents(List<Item> items) {

    public static final int MAX_ENTRIES = 54;

    public static final LedgerContents EMPTY = new LedgerContents(List.of());

    public static final Codec<LedgerContents> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            BuiltInRegistries.ITEM.byNameCodec().listOf().fieldOf("items").forGetter(LedgerContents::items)
        ).apply(instance, LedgerContents::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, LedgerContents> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.registry(BuiltInRegistries.ITEM.key()).apply(ByteBufCodecs.list()),
        LedgerContents::items,
        LedgerContents::new
    );

    /**
     * Returns a new LedgerContents with the given item appended, if not already present
     * and the list hasn't reached capacity.
     */
    public LedgerContents getItemsWithAdded(Item item) {
        if (items.size() >= MAX_ENTRIES) return this;
        if (items.contains(item)) return this;
        List<Item> newList = new ArrayList<>(items);
        newList.add(item);
        return new LedgerContents(List.copyOf(newList));
    }

    public LedgerContents getItemsWithAdded(ItemStack stack) {
        return getItemsWithAdded(stack.getItem());
    }

    /**
     * Returns a new LedgerContents with the item at the given index removed.
     */
    public LedgerContents getItemsWithRemoved(int index) {
        if (index < 0 || index >= items.size()) return this;
        List<Item> newList = new ArrayList<>(items);
        newList.remove(index);
        return new LedgerContents(List.copyOf(newList));
    }

    public LedgerContents getItemsWithRemoved(Item item) {
        if (!items.contains(item)) return this;
        List<Item> newList = new ArrayList<>(items);
        newList.remove(item);
        return new LedgerContents(List.copyOf(newList));
    }

    public LedgerContents getItemsWithRemoved(ItemStack stack) {
        return getItemsWithRemoved(stack.getItem());
    }

    public boolean hasItem(Item item) {
        return items.contains(item);
    }

    public boolean hasItem(ItemStack itemStack) {
        return items.contains(itemStack.getItem());
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int size() {
        return items.size();
    }
}
