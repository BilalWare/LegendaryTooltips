package com.anthonyhilyard.legendarytooltips.compat;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

// TODO: Re-enable when Relics mod is updated for 1.21.8
public final class RelicsHandler
{
	public static boolean hasTooltipDecor(ItemStack itemStack, LocalPlayer player)
	{
		return false;
	}
}
