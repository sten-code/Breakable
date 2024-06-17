package com.stencode.breakable.validators;

import com.google.common.collect.BiMap;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoneycombItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AxeValidator extends Validator {
    protected static final List<Block> STRIPPABLE_BLOCKS;

    @Override
    public int damageWhenUsedOnBlock(ItemUsageContext context) {
        PlayerEntity playerEntity = context.getPlayer();
        if (context.getHand().equals(Hand.MAIN_HAND) && playerEntity.getOffHandStack().isOf(Items.SHIELD) && !playerEntity.shouldCancelInteraction())
            return 0;

        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);
        Block block = blockState.getBlock();

        // If the targeted block is a block that can be stripped, block the action.
        if (STRIPPABLE_BLOCKS.contains(block))
            return 1;

        // If that block can be unoxidized, also block the action.
        if (Oxidizable.getDecreasedOxidationState(blockState).isPresent())
            return 1;

        // If the block can be unwaxed, also block the action.
        Optional<BlockState> optional = Optional.ofNullable(
                (Block) ((BiMap<?, ?>) HoneycombItem.WAXED_TO_UNWAXED_BLOCKS.get())
                        .get(blockState.getBlock()))
                .map((b) -> b.getStateWithProperties(blockState));
        if (optional.isPresent())
            return 1;

        return 0;
    }

    static {
        STRIPPABLE_BLOCKS = new ArrayList<>(List.of(
                Blocks.OAK_WOOD,
                Blocks.OAK_LOG,
                Blocks.DARK_OAK_WOOD,
                Blocks.DARK_OAK_LOG,
                Blocks.ACACIA_WOOD,
                Blocks.ACACIA_LOG,
                Blocks.CHERRY_WOOD,
                Blocks.CHERRY_LOG,
                Blocks.BIRCH_WOOD,
                Blocks.BIRCH_LOG,
                Blocks.JUNGLE_WOOD,
                Blocks.JUNGLE_LOG,
                Blocks.SPRUCE_WOOD,
                Blocks.SPRUCE_LOG,
                Blocks.WARPED_STEM,
                Blocks.WARPED_HYPHAE,
                Blocks.CRIMSON_STEM,
                Blocks.CRIMSON_HYPHAE,
                Blocks.MANGROVE_WOOD,
                Blocks.MANGROVE_LOG,
                Blocks.BAMBOO_BLOCK
        ));
    }
}
