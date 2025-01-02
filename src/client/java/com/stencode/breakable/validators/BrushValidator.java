package com.stencode.breakable.validators;

import net.minecraft.item.ItemUsageContext;

public class BrushValidator extends Validator {

    @Override
    public int damageWhenUsedOnBlock(ItemUsageContext context) {
        // It's possible to first brush a normal block and then move over to the brushable block.
        // This means that it's possible to break your brush if you allow it to be used on normal blocks, even though
        // that doesn't cost any durability.
        return 1;
    }
}
