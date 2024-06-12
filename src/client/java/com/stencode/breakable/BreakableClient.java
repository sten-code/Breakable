package com.stencode.breakable;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;

public class BreakableClient implements ClientModInitializer {
	public static final Logger LOGGER = LogManager.getLogger();

	@Override
	public void onInitializeClient() {
		LOGGER.info("Loading Breakable...");
		ModConfig.init();

		UseItemCallback.EVENT.register((player, world, hand) -> {
			ItemStack itemStack = player.getStackInHand(hand);
			if (ModConfig.INSTANCE.enabled && itemStack.isDamageable() && itemStack.getMaxDamage() - itemStack.getDamage() <= 1) {
				if (ModConfig.INSTANCE.showNotification)
					player.sendMessage(Text.of("Item is too damaged to use!"), true);
				return TypedActionResult.fail(itemStack);
			}

			return TypedActionResult.pass(itemStack);
		});

		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> blockCallback(player, hand));
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> blockCallback(player, hand));
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> blockCallback(player, hand));
	}

	@NotNull
	private ActionResult blockCallback(PlayerEntity player, Hand hand) {
		ItemStack itemStack = player.getStackInHand(hand);
		if (ModConfig.INSTANCE.enabled && itemStack.isDamageable() && itemStack.getMaxDamage() - itemStack.getDamage() <= 1) {
			if (ModConfig.INSTANCE.showNotification)
				player.sendMessage(Text.of("Item is too damaged to use!"), true);
			return ActionResult.FAIL;
		}
		return ActionResult.PASS;
	}
}