package com.mineblock11.fabulousclouds;

import com.mineblock11.fabulousclouds.config.ConfigScreenFactory;
import com.mineblock11.fabulousclouds.config.FabulousCloudsConfig;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.gui.screen.Screen;

import java.io.IOException;

public class FabulousClouds implements ModInitializer {
    private static FabulousClouds instance;
    private static FabulousCloudsConfig config;

    {
        instance = this;
    }

    public static FabulousClouds getInstance() {
        return instance;
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

    @Override
    public void onInitialize() {
        initConfig();
    }

    public Screen createConfigScreen(Screen parent) {
        return ConfigScreenFactory.createConfigScreen(parent, config);
    }
}
