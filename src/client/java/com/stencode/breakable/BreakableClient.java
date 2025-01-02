package com.stencode.breakable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.stencode.breakable.validators.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.*;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

public class BreakableClient implements ClientModInitializer {
    protected static final Map<Class<?>, Validator> VALIDATORS;

    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitializeClient() {
        LOGGER.info("Loading Breakable...");
        ModConfig.init();

        KeyBinding overwriteKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.stencode.breakable.overwrite",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.stencode.breakable"
        ));

        UseItemCallback.EVENT.register((player, world, hand) -> {
            // When the player is right-clicking an item without looking at a block or entity
            ItemStack itemStack = player.getStackInHand(hand);
            if (!ModConfig.INSTANCE.enabled || !itemStack.isDamageable() || overwriteKey.isPressed())
                return ActionResult.PASS;

            Item item = itemStack.getItem();
            if (item instanceof FishingRodItem) {
                // The maximum amount of damage on a fishing rod that can be done in 1 action is yanking a mob which costs 5 durability.
                if (itemStack.getMaxDamage() - itemStack.getDamage() <= 5) {
                    notify(player);
                    return ActionResult.FAIL;
                }
            } else if (item instanceof CrossbowItem) {
                Item projectile = player.getProjectileType(itemStack).getItem();
                int damage;
                if (projectile instanceof FireworkRocketItem) {
                    damage = 3;
                } else {
                    damage = 1;
                }
                RegistryEntry<Enchantment> entry = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.MULTISHOT);
                int projectiles = EnchantmentHelper.getLevel(entry, itemStack) == 0 ? 1 : 3;
                damage *= projectiles;

                if (itemStack.getMaxDamage() - itemStack.getDamage() <= damage) {
                    notify(player);
                    return ActionResult.FAIL;
                }
            } else if (item instanceof BowItem || item instanceof TridentItem) {
                if (itemStack.getMaxDamage() - itemStack.getDamage() <= 1) {
                    notify(player);
                    return ActionResult.FAIL;
                }
            } else if (item instanceof ShieldItem) {
                if (itemStack.getMaxDamage() - itemStack.getDamage() <= ModConfig.INSTANCE.shieldDamageThreshold) {
                    notify(player);
                    return ActionResult.FAIL;
                }
            }

            return ActionResult.PASS;
        });

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            // When the player is breaking a block with the item they are holding
            ItemStack itemStack = player.getStackInHand(hand);
            if (!ModConfig.INSTANCE.enabled || !itemStack.isDamageable() || overwriteKey.isPressed())
                return ActionResult.PASS;

            Item item = itemStack.getItem();
            // Tridents and swords take 2 damage from mining blocks
            if (item instanceof TridentItem || item instanceof SwordItem) {
                if (itemStack.getMaxDamage() - itemStack.getDamage() > 2)
                    return ActionResult.PASS;
            } else {
                if (itemStack.getMaxDamage() - itemStack.getDamage() > 1)
                    return ActionResult.PASS;
            }

            // Only items with a tool component can take damage from breaking blocks, so let it pass if it's not a tool
            ToolComponent toolComponent = itemStack.get(DataComponentTypes.TOOL);
            if (toolComponent == null || toolComponent.damagePerBlock() <= 0)
                return ActionResult.PASS;

            notify(player);
            return ActionResult.FAIL;
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            // When the player is hitting an entity with the item they are holding
            ItemStack itemStack = player.getStackInHand(hand);
            if (!ModConfig.INSTANCE.enabled || !itemStack.isDamageable() || overwriteKey.isPressed())
                return ActionResult.PASS;

            Item item = itemStack.getItem();
            // Mining tools will take 2 damage when hitting an entity
            // Only mining tools, swords, tridents and maces take damage from hitting entities
            if (item instanceof MiningToolItem) {
                if (itemStack.getMaxDamage() - itemStack.getDamage() > 2)
                    return ActionResult.PASS;

                notify(player);
                return ActionResult.FAIL;
            } else if (item instanceof SwordItem || item instanceof TridentItem || item instanceof MaceItem) {
                if (itemStack.getMaxDamage() - itemStack.getDamage() > 1)
                    return ActionResult.PASS;

                notify(player);
                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            // When the item the player is holding is right-clicked on a block
            ItemStack itemStack = player.getStackInHand(hand);
            if (!ModConfig.INSTANCE.enabled || !itemStack.isDamageable() || overwriteKey.isPressed())
                return ActionResult.PASS;

            Item item = itemStack.getItem();
            ItemUsageContext context = new ItemUsageContext(world, player, hand, itemStack, hitResult);
            // There are a lot of different type of rules for using an item on blocks, so separating them into their own
            // validator class is a lot easier
            Validator validator = VALIDATORS.get(item.getClass());
            if (validator == null)
                return ActionResult.PASS;

            int damage = validator.damageWhenUsedOnBlock(context);
            if (itemStack.getMaxDamage() - itemStack.getDamage() > damage)
                return ActionResult.PASS;

            notify(player);
            return ActionResult.FAIL;
        });
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            // When the item the player is holding is right-clicked on an entity
            ItemStack itemStack = player.getStackInHand(hand);
            if (!ModConfig.INSTANCE.enabled || !itemStack.isDamageable() || overwriteKey.isPressed())
                return ActionResult.PASS;

            if (itemStack.getMaxDamage() - itemStack.getDamage() > 1)
                return ActionResult.PASS;

            Item item = itemStack.getItem();
            if (item instanceof ShearsItem) {
                if (entity instanceof SheepEntity || entity instanceof MooshroomEntity || entity instanceof SnowGolemEntity) {
                    notify(player);
                    return ActionResult.FAIL;
                }
            }

            return ActionResult.PASS;
        });
    }

    private void notify(PlayerEntity player) {
        if (ModConfig.INSTANCE.showNotification)
            player.sendMessage(Text.of("Item is too damaged to use!"), true);
    }

    static {
        VALIDATORS = Maps.newHashMap(ImmutableMap.of(
                HoeItem.class, new HoeValidator(),
                ShovelItem.class, new ShovelValidator(),
                AxeItem.class, new AxeValidator(),
                ShearsItem.class, new ShearsValidator(),
                FlintAndSteelItem.class, new FlintAndSteelValidator(),
                BrushItem.class, new BrushValidator()));
    }

}