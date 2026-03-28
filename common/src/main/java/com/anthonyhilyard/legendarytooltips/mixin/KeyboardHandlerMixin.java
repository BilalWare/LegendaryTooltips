package com.anthonyhilyard.legendarytooltips.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.anthonyhilyard.legendarytooltips.LegendaryTooltips;

import net.minecraft.client.KeyboardHandler;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin
{
	// Note: "method_1454" is an intermediary name for the lambda calling Screen.keyReleased.
	// If this mixin fails to apply in 1.21.8, the intermediary name may have changed and needs updating.
	@ModifyArg(method = "method_1454", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;keyReleased(III)Z"), index = 0)
	private static int releaseTooltipScroll2(int i, int j, int k)
	{
		if (LegendaryTooltips.scrollTooltips.matches(i, j))
		{
			LegendaryTooltips.scrollTooltipsKeyDown = false;
		}
		return i;
	}
}
