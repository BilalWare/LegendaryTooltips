package com.anthonyhilyard.legendarytooltips.mixin;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.anthonyhilyard.iceberg.util.Tooltips;
import com.anthonyhilyard.legendarytooltips.tooltip.TooltipScroll;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.gui.font.glyphs.BakedGlyph;

@Mixin(BakedGlyph.class)
public class BakedGlyphMixin
{
	@Shadow
	@Final
	private float down;

	@Unique
	private Vector3f currentTop = new Vector3f();

	@Unique
	private Vector3f currentBottom = new Vector3f();

	@Inject(method = "render(ZFFFLorg/joml/Matrix4f;Lcom/mojang/blaze3d/vertex/VertexConsumer;IZI)V", at = @At("HEAD"))
	private void grabLocals(boolean italic, float x, float y, float z, Matrix4f matrix4f, VertexConsumer vertexConsumer, int color, boolean bold, int light, CallbackInfo info)
	{
		matrix4f.transformPosition(x, y, z, currentTop);
		matrix4f.transformPosition(x, y + down, z, currentBottom);
	}

	private final float fadeThickness = 10.0f;
	@Unique
	private float calculateFadeAlpha(float vertexY)
	{
		// TODO: Tooltips.getCurrentRenderContext() does not exist in this Iceberg version. Using index 0 as default.
		int contextIndex = 0;
		float scrollTop = TooltipScroll.getScrollTop(contextIndex);
		float scrollBottom = TooltipScroll.getScrollBottom(contextIndex);
		float topFadeStart = scrollTop;
		float topFadeEnd = scrollTop + fadeThickness;
		float bottomFadeStart = scrollBottom - fadeThickness;
		float bottomFadeEnd = scrollBottom;

		if (vertexY < topFadeEnd)
		{
			return Math.clamp((vertexY - topFadeStart) / fadeThickness, 0.0f, 1.0f);
		}
		else if (vertexY > bottomFadeStart)
		{
			return Math.clamp((bottomFadeEnd - vertexY) / fadeThickness, 0.0f, 1.0f);
		}
		return 1.0f;
	}

	@Unique
	private int applyFadeAlpha(int color, float vertexY)
	{
		float alpha = calculateFadeAlpha(vertexY);
		int existingAlpha = (color >> 24) & 0xFF;
		int newAlpha = (int)(existingAlpha * alpha);
		return (color & 0x00FFFFFF) | (newAlpha << 24);
	}

	@ModifyArg(method = "render(ZFFFLorg/joml/Matrix4f;Lcom/mojang/blaze3d/vertex/VertexConsumer;IZI)V",
		at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;color(I)Lcom/mojang/blaze3d/vertex/VertexConsumer;", ordinal = 0), index = 0)
	private int fadeGlyphs0(int color)
	{
		if (TooltipScroll.isTooltipVisible(0 /* TODO: Tooltips.getCurrentRenderContext() unavailable */))
		{
			return applyFadeAlpha(color, currentTop.y);
		}
		return color;
	}

	@ModifyArg(method = "render(ZFFFLorg/joml/Matrix4f;Lcom/mojang/blaze3d/vertex/VertexConsumer;IZI)V",
		at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;color(I)Lcom/mojang/blaze3d/vertex/VertexConsumer;", ordinal = 1), index = 0)
	private int fadeGlyphs1(int color)
	{
		if (TooltipScroll.isTooltipVisible(0 /* TODO: Tooltips.getCurrentRenderContext() unavailable */))
		{
			return applyFadeAlpha(color, currentBottom.y);
		}
		return color;
	}

	@ModifyArg(method = "render(ZFFFLorg/joml/Matrix4f;Lcom/mojang/blaze3d/vertex/VertexConsumer;IZI)V",
		at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;color(I)Lcom/mojang/blaze3d/vertex/VertexConsumer;", ordinal = 2), index = 0)
	private int fadeGlyphs2(int color)
	{
		if (TooltipScroll.isTooltipVisible(0 /* TODO: Tooltips.getCurrentRenderContext() unavailable */))
		{
			return applyFadeAlpha(color, currentBottom.y);
		}
		return color;
	}

	@ModifyArg(method = "render(ZFFFLorg/joml/Matrix4f;Lcom/mojang/blaze3d/vertex/VertexConsumer;IZI)V",
		at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;color(I)Lcom/mojang/blaze3d/vertex/VertexConsumer;", ordinal = 3), index = 0)
	private int fadeGlyphs3(int color)
	{
		if (TooltipScroll.isTooltipVisible(0 /* TODO: Tooltips.getCurrentRenderContext() unavailable */))
		{
			return applyFadeAlpha(color, currentTop.y);
		}
		return color;
	}
}
