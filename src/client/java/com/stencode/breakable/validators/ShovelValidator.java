package com.stencode.breakable.validators;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ShovelItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ShovelValidator extends Validator {
    protected static final List<Block> PATH_STATES;

    @Override
    public int damageWhenUsedOnBlock(ItemUsageContext context) {
        BlockPos blockPos = context.getBlockPos();
        World world = context.getWorld();
        BlockState blockState = world.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (context.getSide() == Direction.DOWN)
            return 0;

        if (PATH_STATES.contains(block) && world.getBlockState(blockPos.up()).isAir())
            return 1;

        if (block instanceof CampfireBlock && blockState.get(CampfireBlock.LIT))
            return 1;

        return 0;
    }

    static {
        PATH_STATES = new ArrayList<>(List.of(
                Blocks.GRASS_BLOCK,
                Blocks.DIRT,
                Blocks.PODZOL,
                Blocks.COARSE_DIRT,
                Blocks.MYCELIUM,
                Blocks.ROOTED_DIRT));
    }
}
