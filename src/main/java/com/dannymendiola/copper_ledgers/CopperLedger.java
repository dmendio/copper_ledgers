package com.dannymendiola.copper_ledgers;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class CopperLedger extends Item {
    public CopperLedger(Properties props)  {
        super(props);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }

        // USE ITEM

        return InteractionResult.SUCCESS;
    }
}
