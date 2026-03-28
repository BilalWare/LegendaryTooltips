package com.anthonyhilyard.legendarytooltips.fabric.client;

import com.anthonyhilyard.legendarytooltips.LegendaryTooltips;
import com.anthonyhilyard.legendarytooltips.client.LegendaryTooltipsClient;
import com.anthonyhilyard.legendarytooltips.config.FrameResourceParser;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

public final class LegendaryTooltipsFabricClient implements ClientModInitializer
{
	@Override
	public void onInitializeClient()
	{
		LegendaryTooltipsClient.init();

		// Register resource reload listener directly via Fabric API to avoid remapping issues
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener()
		{
			@Override
			public ResourceLocation getFabricId()
			{
				return ResourceLocation.fromNamespaceAndPath(LegendaryTooltips.MODID, "frame_definitions");
			}

			@Override
			public void onResourceManagerReload(ResourceManager resourceManager)
			{
				FrameResourceParser.INSTANCE.onResourceManagerReload(resourceManager);
			}
		});
	}
}
