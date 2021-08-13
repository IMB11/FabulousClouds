package net.misterslime.fabulousclouds;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.misterslime.fabulousclouds.config.ConfigScreenFactory;
import net.misterslime.fabulousclouds.config.FabulousCloudsConfig;

import java.io.IOException;

public class FabulousClouds implements ModInitializer {

	{ instance = this; }
	private static FabulousClouds instance;
	public static FabulousClouds getInstance() {
		return instance;
	}

	private static FabulousCloudsConfig config;

	@Override
	public void onInitialize() {
		initConfig();
	}

	private static void initConfig() {
		try {
			config = FabulousCloudsConfig.load();
		} catch (IOException e) {
			throw new RuntimeException("Error loading Fabulous Clouds config!", e);
		}
	}

	public static FabulousCloudsConfig getConfig() {
		if (config == null) {
			initConfig();
		}
		return config;
	}

	public Screen createConfigScreen(Screen parent) {
		if (FabricLoader.getInstance().isModLoaded("cloth-config2")) {
			return ConfigScreenFactory.createConfigScreen(parent, config);
		}
		return null;
	}
}
