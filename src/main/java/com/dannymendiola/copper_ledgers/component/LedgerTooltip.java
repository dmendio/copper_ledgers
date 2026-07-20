package com.dannymendiola.copper_ledgers.component;





import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
// import net.minecraft.world.item.component.TooltipContext;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.core.component.DataComponentGetter;
import java.util.function.Consumer;

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
            // Now you have direct access to whatever fields you put in LedgerContents.
            // For example, looping through the items inside your ledger:
            // for (var itemStack : contents.items()) {
            //     textConsumer.accept(Component.literal("- ")
            //         .append(itemStack.getName())
            //         .withStyle(ChatFormatting.GRAY));
            // }

            // StringBuilder contentTip = new StringBuilder();

            for (int i = 0; i < contents.items().size(); i++) {
                if (i < 3) {
                    Item item = contents.items().get(i);
                    String name = new ItemStack(item).getItemName().getString();
                    textConsumer.accept(Component.translatable("• " + name).withStyle(ChatFormatting.GRAY));
                } else if (i < 4) {
                    textConsumer.accept(Component.literal(""));
                    textConsumer.accept(Component.translatable("... and " + (contents.items().size() - 3) + " more item types" + (contents.items().size() - 3 == 1 ? "" : "s")).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                    break;
                }
            }


            // textConsumer.accept(Component.translatable(contentTip.toString()).withStyle(ChatFormatting.GRAY));
            
            // Or if you just have a custom string inside LedgerContents:
            // textConsumer.accept(Component.literal(contents.myCustomString()).withStyle(ChatFormatting.GRAY));
        } else {
            textConsumer.accept(Component.translatable("(Blank)").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }
}




// import java.util.Random;
// import java.util.function.Consumer;

// import com.mojang.serialization.Codec;
// import com.mojang.serialization.codecs.RecordCodecBuilder;

// import net.minecraft.ChatFormatting;
// import net.minecraft.core.component.DataComponentGetter;
// import net.minecraft.network.RegistryFriendlyByteBuf;
// import net.minecraft.network.chat.Component;
// import net.minecraft.network.codec.ByteBufCodecs;
// import net.minecraft.network.codec.StreamCodec;
// import net.minecraft.world.item.Item.TooltipContext;
// import net.minecraft.world.item.TooltipFlag;
// import net.minecraft.world.item.component.TooltipProvider;

// public record LedgerTooltip() implements TooltipProvider {
//     public static final Codec<LedgerTooltip> CODEC = RecordCodecBuilder.create(instance ->
//         instance.group(
//             Codec.INT.fieldOf("click_count").forGetter(LedgerTooltip::clickCount)
//         ).apply(instance, LedgerTooltip::new)
//     );

//     public static final StreamCodec<RegistryFriendlyByteBuf, LedgerTooltip> STREAM_CODEC = StreamCodec.composite(
//         ByteBufCodecs.VAR_INT,
//         LedgerTooltip::clickCount,
//         LedgerTooltip::new
//     );

//     @Override
//     public void addToTooltip(TooltipContext tooltip, Consumer<Component> textConsumer, TooltipFlag type, DataComponentGetter components) {
//         Random r = new Random();
//         int num = r.nextInt(100);
//         textConsumer.accept(Component.translatable("num: " + num, this.clickCount).withStyle(ChatFormatting.GRAY));
//     }
    
// }
