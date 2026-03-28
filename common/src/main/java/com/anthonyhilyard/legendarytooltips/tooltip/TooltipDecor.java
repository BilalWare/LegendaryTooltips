package com.anthonyhilyard.legendarytooltips.tooltip;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.List;

import com.anthonyhilyard.iceberg.util.GuiHelper;
import com.anthonyhilyard.iceberg.util.Tooltips;
import com.anthonyhilyard.iceberg.util.Tooltips.TooltipColors;
import com.anthonyhilyard.iceberg.util.Easing.EasingType;
import com.anthonyhilyard.iceberg.util.Tooltips.TitleBreakComponent;
import com.anthonyhilyard.iceberg.util.Easing;
import com.anthonyhilyard.legendarytooltips.LegendaryTooltips;
import com.anthonyhilyard.legendarytooltips.config.LegendaryTooltipsConfig;
import com.anthonyhilyard.legendarytooltips.config.LegendaryTooltipsConfig.FrameDefinition;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.TextColor;

public class TooltipDecor
{
	public static final ResourceLocation DEFAULT_BORDERS = ResourceLocation.fromNamespaceAndPath(LegendaryTooltips.MODID, "textures/gui/tooltip_borders.png");

	private static float shineTimer = 1.5f;

	public static void setCurrentTooltipBorderStart(int color)
	{
		Tooltips.currentColors = new TooltipColors(Tooltips.currentColors.backgroundColorStart(), Tooltips.currentColors.backgroundColorEnd(), TextColor.fromRgb(color), Tooltips.currentColors.borderColorEnd());
	}

	public static void setCurrentTooltipBorderEnd(int color)
	{
		Tooltips.currentColors = new TooltipColors(Tooltips.currentColors.backgroundColorStart(), Tooltips.currentColors.backgroundColorEnd(), Tooltips.currentColors.borderColorStart(), TextColor.fromRgb(color));
	}

	public static void setCurrentTooltipBackgroundStart(int color)
	{
		Tooltips.currentColors = new TooltipColors(TextColor.fromRgb(color), Tooltips.currentColors.backgroundColorEnd(), Tooltips.currentColors.borderColorStart(), Tooltips.currentColors.borderColorEnd());
	}

	public static void setCurrentTooltipBackgroundEnd(int color)
	{
		Tooltips.currentColors = new TooltipColors(Tooltips.currentColors.backgroundColorStart(), TextColor.fromRgb(color), Tooltips.currentColors.borderColorStart(), Tooltips.currentColors.borderColorEnd());
	}

	public static void updateTimer(float deltaTime)
	{
		if (shineTimer > 0.0f)
		{
			shineTimer -= deltaTime;
		}
	}

	public static void resetTimer()
	{
		shineTimer = 1.5f;
	}
	
	public static void drawShadow(GuiGraphics graphics, int x, int y, int width, int height)
	{
		int shadowColor = 0x44000000;

		GuiHelper.drawGradientRect(graphics, 390, x - 1,         y + height + 4, x + width + 4, y + height + 5, shadowColor, shadowColor);
		GuiHelper.drawGradientRect(graphics, 390, x + width + 4, y - 1,          x + width + 5, y + height + 5, shadowColor, shadowColor);

		GuiHelper.drawGradientRect(graphics, 390, x + width + 3, y + height + 3, x + width + 4, y + height + 4, shadowColor, shadowColor);

		GuiHelper.drawGradientRect(graphics, 390, x,             y + height + 5, x + width + 5, y + height + 6, shadowColor, shadowColor);
		GuiHelper.drawGradientRect(graphics, 390, x + width + 5, y,              x + width + 6, y + height + 5, shadowColor, shadowColor);
	}

	public static void drawSeparator(GuiGraphics graphics, int x, int y, int width, int color)
	{
		GuiHelper.drawGradientRectHorizontal(graphics, 400, x, y, x + width / 2, y + 1, color & 0xFFFFFF, color);
		GuiHelper.drawGradientRectHorizontal(graphics, 400, x + width / 2, y, x + width, y + 1, color, color & 0xFFFFFF);
	}

	/**
	 * Calculate the index of the first ClientTextTooltip component in the list.
	 * This is needed because non-text components (e.g., item models) may precede the title text.
	 */
	private static int calculateTitleStart(List<ClientTooltipComponent> components)
	{
		for (int i = 0; i < components.size(); i++)
		{
			if (components.get(i) instanceof ClientTextTooltip)
			{
				return i;
			}
		}
		return 0;
	}

	public static void drawBorder(GuiGraphics graphics, int x, int y, int width, int height, ItemStack item, List<ClientTooltipComponent> components, Font font, FrameDefinition frameDefinition, boolean comparison, int index)
	{
		// If this is a comparison tooltip, we need to draw the separator first.
		if (comparison)
		{
			// Now draw a separator under the "equipped" badge.
			drawSeparator(graphics, x - 3 + 1, y - 3 + 1 + 12, width, Tooltips.currentColors.borderColorStart().getValue());

			// Comparison tooltips need an extra pixel of height to account for the separator line.
			height++;
		}

		// If the separate name border is enabled, draw it now.
		if (LegendaryTooltipsConfig.getInstance().nameSeparator.get() &&
		   (LegendaryTooltipsConfig.getInstance().showSeparatorForEmpty.get() || !item.isEmpty()))
		{
			// Determine the number of "title lines".  This will be the number of lines before the first TitleBreakComponent.
			// If for some reason there is no TitleBreakComponent, we'll default to 1.
			// If the TitleBreakComponent is the last component, don't draw the separator.
			int titleLines = Tooltips.calculateTitleLines(components);
			int numComponents = components.size();

			// Count how many components are not text components, except for (titleLines) count.
			for (int i = 0; i < components.size(); i++)
			{
				if (!(components.get(i) instanceof ClientTextTooltip) && !(components.get(i) instanceof ItemModelComponent))
				{
					numComponents--;
					if (numComponents == titleLines)
					{
						break;
					}
				}
			}

			if (titleLines < numComponents)
			{
				int offset = 0;

				// Find the index of the first text component, which is where the actual title will start.
				int titleStart = calculateTitleStart(components);

				// If we are displaying a model, adjust the offset for it.
				if (components.stream().anyMatch(c -> c instanceof ItemModelComponent))
				{
					offset -= 2;
				}

				// If there are any title break components after the title lines, include those in the count.
				for (int i = titleStart + titleLines; i < components.size(); i++)
				{
					if (components.get(i) instanceof TitleBreakComponent)
					{
						titleLines++;
					}
					else
					{
						break;
					}
				}

				// Calculate the offset, which is the height of all components before the title plus the height of all title lines.
				for (int i = 0; i < titleStart + titleLines && i < components.size(); i++)
				{
					ClientTooltipComponent component = components.get(i);
					if (component instanceof ClientTextTooltip)
					{
						offset += Math.max(component.getHeight(font), font.lineHeight);
					}
					else
					{
						offset += component.getHeight(font);
						if (i <= titleStart)
						{
							offset += 2;
						}
					}
				}

				// Now draw the separator under the title.
				drawSeparator(graphics, x - 3 + 1, y - 3 + 2 + offset, width, Tooltips.currentColors.borderColorStart().getValue());
			}
		}

		if (frameDefinition.index() == LegendaryTooltipsConfig.STANDARD_BORDER.index() ||
			frameDefinition.index() == LegendaryTooltipsConfig.NO_BORDER.index())
		{
			return;
		}

		if (LegendaryTooltipsConfig.getInstance().shineEffect.get())
		{
			// Draw shiny effect here.
			if (shineTimer >= 0.5f && shineTimer <= 1.5f)
			{
				float interval = 1.0f - Mth.clamp((shineTimer - 0.5f) * 2.0f - 0.5f, -0.5f, 1.5f);
				int alpha = 0x77 << 24;

				int horizontalMin = x - 3;
				int horizontalMax = x + width + 3;

				int left = (int)Easing.Ease(horizontalMin, horizontalMax, Math.clamp(interval - 0.35f, 0.0f, 1.0f), EasingType.Quad);
				int middle = (int)Easing.Ease(horizontalMin, horizontalMax, Math.clamp(interval, 0.0f, 1.0f), EasingType.Quad);
				int right = (int)Easing.Ease(horizontalMin, horizontalMax, Math.clamp(interval + 0.35f, 0.0f, 1.0f), EasingType.Quad);
				GuiHelper.drawGradientRectHorizontal(graphics, 400,   left, y - 3, middle, y - 3 + 1, 0x00FFFFFF, 0x00FFFFFF | alpha);
				GuiHelper.drawGradientRectHorizontal(graphics, 400, middle, y - 3,  right, y - 3 + 1, 0x00FFFFFF | alpha, 0x00FFFFFF);
			}

			if (shineTimer <= 1.0f)
			{
				float interval = Mth.clamp(shineTimer, 0.0f, 1.0f);
				int alpha = (int)(0x55 * interval) << 24;

				int verticalMin = y - 3 + 1;
				int verticalMax = y + height + 3 - 1;
				int verticalInterval = (int)Mth.lerp(interval * interval, verticalMax, verticalMin);
				GuiHelper.drawGradientRect(graphics, 400, x - 3, Math.max(verticalInterval - 12, verticalMin), x - 3 + 1, Math.min(verticalInterval, verticalMax), 0x00FFFFFF, 0x00FFFFFF | alpha);
				GuiHelper.drawGradientRect(graphics, 400, x - 3, Math.max(verticalInterval, verticalMin), x - 3 + 1, Math.min(verticalInterval + 12, verticalMax), 0x00FFFFFF | alpha, 0x00FFFFFF);
			}
		}

		// Default texture dimensions for the border sprite sheet. 128x128 is the standard size used by
		// LegendaryTooltips border textures. Resource packs should match this dimension.
		int textureWidth = 128;
		int textureHeight = 128;

		final int frameIndex = frameDefinition.index();
		final int frameWidth = frameDefinition.frameWidth();
		final int partSize = frameDefinition.partSize();
		final int partOffset = frameDefinition.partOffset();
		final int cornerOffset = frameDefinition.cornerOffset();
		final int frameHeight = partSize * 2;
		final int partWidth = frameWidth - partSize * 2;

		// Here we will overlay a 6-patch border over the tooltip to make it look fancy.
		// Use GuiGraphics.blit with RenderPipelines.GUI_TEXTURED and the border texture resource location.
		ResourceLocation borderTexture = frameDefinition.resource() != null ? frameDefinition.resource() : DEFAULT_BORDERS;

		// Render top-left corner.
		graphics.blit(RenderPipelines.GUI_TEXTURED, borderTexture, x - partSize + cornerOffset, y - partSize + cornerOffset, (float)((frameIndex / 8) * frameWidth), (float)((frameIndex * frameHeight) % textureHeight), partSize, partSize, textureWidth, textureHeight);

		// Render top-right corner.
		graphics.blit(RenderPipelines.GUI_TEXTURED, borderTexture, x + width - cornerOffset, y - partSize + cornerOffset, (float)((frameWidth - partSize) + (frameIndex / 8) * frameWidth), (float)((frameIndex * frameHeight) % textureHeight), partSize, partSize, textureWidth, textureHeight);

		// Render bottom-left corner.
		graphics.blit(RenderPipelines.GUI_TEXTURED, borderTexture, x - partSize + cornerOffset, y + height - cornerOffset, (float)((frameIndex / 8) * frameWidth), (float)((frameIndex * frameHeight) % textureHeight + partSize), partSize, partSize, textureWidth, textureHeight);

		// Render bottom-right corner.
		graphics.blit(RenderPipelines.GUI_TEXTURED, borderTexture, x + width - cornerOffset, y + height - cornerOffset, (float)((frameWidth - partSize) + (frameIndex / 8) * frameWidth), (float)((frameIndex * frameHeight) % textureHeight + partSize), partSize, partSize, textureWidth, textureHeight);

		// Only render central embellishments if the tooltip is 48 pixels wide or more.
		if (width >= partWidth)
		{
			// Render top central embellishment.
			graphics.blit(RenderPipelines.GUI_TEXTURED, borderTexture, x + (width / 2) - (partWidth / 2), y - partSize + partOffset, (float)(partSize + (frameIndex / 8) * frameWidth), (float)((frameIndex * frameHeight) % textureHeight), partWidth, partSize, textureWidth, textureHeight);

			// Render bottom central embellishment.
			graphics.blit(RenderPipelines.GUI_TEXTURED, borderTexture, x + (width / 2) - (partWidth / 2), y + height - partOffset, (float)(partSize + (frameIndex / 8) * frameWidth), (float)((frameIndex * frameHeight) % textureHeight + partSize), partWidth, partSize, textureWidth, textureHeight);
		}
	}
}
