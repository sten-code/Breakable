package com.stencode.breakable.validators;

import net.minecraft.block.*;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ShearsValidator extends Validator {

    @Override
    public int damageWhenUsedOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);
        Block block = blockState.getBlock();
        // Beehives and Bee Nests are both the BeehiveBlock instance
        if (block instanceof PumpkinBlock || block instanceof BeehiveBlock)
            return 1;

        if (block instanceof AbstractPlantStemBlock abstractPlantStemBlock)
            if (!abstractPlantStemBlock.hasMaxAge(blockState))
                return 1;

        return 0;
    }

}
