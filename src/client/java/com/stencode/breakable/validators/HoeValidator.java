package com.stencode.breakable.validators;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.function.Predicate;

public class HoeValidator extends Validator {
    protected static final Map<Block, Predicate<ItemUsageContext>> TILLING_ACTIONS;

    @Override
    public int damageWhenUsedOnBlock(ItemUsageContext context) {
        BlockPos blockPos = context.getBlockPos();

        Predicate<ItemUsageContext> predicate = TILLING_ACTIONS.get(context.getWorld().getBlockState(blockPos).getBlock());
        if (predicate == null)
            return 0;

        if (predicate.test(context))
            return 1;

        return 0;
    }

    static {
        TILLING_ACTIONS = Maps.newHashMap(ImmutableMap.of(
                Blocks.GRASS_BLOCK, HoeItem::canTillFarmland,
                Blocks.DIRT_PATH, HoeItem::canTillFarmland,
                Blocks.DIRT, HoeItem::canTillFarmland,
                Blocks.COARSE_DIRT, HoeItem::canTillFarmland,
                Blocks.ROOTED_DIRT, (itemUsageContext) -> true));
    }
}
