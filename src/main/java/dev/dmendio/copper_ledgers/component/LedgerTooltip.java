package dev.dmendio.copper_ledgers.component;

import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.core.component.DataComponentGetter;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;

// An empty record works perfectly as a stateless behavior marker
public record LedgerTooltip() implements TooltipProvider {

    // Codec.unit tells Minecraft to save nothing to disk, but successfully load this empty record
    public static final Codec<LedgerTooltip> CODEC = Codec.EMPTY.codec().xmap(unit -> new LedgerTooltip(), toolTime -> com.mojang.datafixers.util.Unit.INSTANCE);
    
    // StreamCodec.unit does the same for network packets
    public static final StreamCodec<RegistryFriendlyByteBuf, LedgerTooltip> STREAM_CODEC = StreamCodec.unit(new LedgerTooltip());

    @Override
    public void addToTooltip(TooltipContext tooltip, Consumer<Component> textConsumer, TooltipFlag type, DataComponentGetter components) {
        // Query the item's other data components directly using the getter!
        LedgerContents contents = components.get(ModComponents.LEDGER_CONTENTS);
        
        if (contents != null) {
            for (int i = 0; i < contents.items().size(); i++) {
                if (i < 3) {
                    Item item = contents.items().get(i);
                    String name = new ItemStack(item).getItemName().getString();
                    textConsumer.accept(Component.translatable("• " + name).withStyle(ChatFormatting.GRAY));
                } else if (i < 4) {
                    textConsumer.accept(Component.literal(""));
                    textConsumer.accept(Component.translatable("... and " + (contents.items().size() - 3) + " more item type" + (contents.items().size() - 3 == 1 ? "" : "s")).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                    break;
                }
            }
        } else {
            textConsumer.accept(Component.literal(""));
            textConsumer.accept(Component.translatable("(Blank)").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }
}