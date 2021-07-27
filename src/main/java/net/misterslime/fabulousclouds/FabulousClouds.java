package net.misterslime.fabulousclouds;

import net.fabricmc.api.ModInitializer;
import net.misterslime.fabulousclouds.config.FabulousCloudsConfig;

import java.io.IOException;

public class FabulousClouds implements ModInitializer {

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
}
