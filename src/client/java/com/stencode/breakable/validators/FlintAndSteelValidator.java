package com.stencode.breakable.validators;

import net.minecraft.block.*;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FlintAndSteelValidator extends Validator {

    @Override
    public int damageWhenUsedOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);
        if (!CampfireBlock.canBeLit(blockState) && !CandleBlock.canBeLit(blockState) && !CandleCakeBlock.canBeLit(blockState)) {
            if (AbstractFireBlock.canPlaceAt(world, blockPos.offset(context.getSide()), context.getHorizontalPlayerFacing()))
                return 1;
        }

        return 0;
    }

}
