package com.dannymendiola.copper_ledgers.component;

import java.util.Random;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public record LedgerTooltip(int clickCount) implements TooltipProvider {
    public static final Codec<LedgerTooltip> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("click_count").forGetter(LedgerTooltip::clickCount)
        ).apply(instance, LedgerTooltip::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, LedgerTooltip> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        LedgerTooltip::clickCount,
        LedgerTooltip::new
    );

    @Override
    public void addToTooltip(TooltipContext tooltip, Consumer<Component> textConsumer, TooltipFlag type, DataComponentGetter components) {
        Random r = new Random();
        int num = r.nextInt(100);
        textConsumer.accept(Component.translatable("num: " + num, this.clickCount).withStyle(ChatFormatting.GRAY));
    }
    
}
