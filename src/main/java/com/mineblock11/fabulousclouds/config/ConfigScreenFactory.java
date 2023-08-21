package com.mineblock11.fabulousclouds.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.io.IOException;

public class ConfigScreenFactory {

    public static Screen createConfigScreen(Screen parent, FabulousCloudsConfig config) {
        FabulousCloudsConfig defaultConfig = FabulousCloudsConfig.DEFAULT;

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("title.fabulousclouds.config"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("category.fabulousclouds.general"));

        builder.setSavingRunnable(() -> {
            try {
                FabulousCloudsConfig.save(config);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.fabulousclouds.vibrant_clouds"), config.vibrant_clouds)
                .setDefaultValue(defaultConfig.vibrant_clouds)
                .setTooltip(Text.translatable("tooltip.fabulousclouds.vibrant_clouds"))
                .setSaveConsumer(newValue -> config.vibrant_clouds = newValue)
                .build());

        general.addEntry(entryBuilder.startFloatField(Text.translatable("option.fabulousclouds.vibrance_intensity"), config.vibrance_intensity)
                .setDefaultValue(defaultConfig.vibrance_intensity)
                .setTooltip(Text.translatable("tooltip.fabulousclouds.vibrance_intensity"))
                .setSaveConsumer(newValue -> config.vibrance_intensity = newValue)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.fabulousclouds.enable_default_cloud_layer"), config.enable_default_cloud_layer)
                .setDefaultValue(defaultConfig.enable_default_cloud_layer)
                .setTooltip(Text.translatable("tooltip.fabulousclouds.enable_default_cloud_layer"))
                .setSaveConsumer(newValue -> config.enable_default_cloud_layer = newValue)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.fabulousclouds.noise_clouds"), config.noise_clouds)
                .setDefaultValue(defaultConfig.noise_clouds)
                .setTooltip(Text.translatable("tooltip.fabulousclouds.noise_clouds"))
                .setSaveConsumer(newValue -> config.noise_clouds = newValue)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.fabulousclouds.debug_noise_clouds"), config.debug_noise_clouds)
                .setDefaultValue(defaultConfig.debug_noise_clouds)
                .setTooltip(Text.translatable("tooltip.fabulousclouds.debug_noise_clouds"))
                .setSaveConsumer(newValue -> config.debug_noise_clouds = newValue)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.fabulousclouds.offset_cloud_rendering"), config.offset_cloud_rendering)
                .setDefaultValue(defaultConfig.offset_cloud_rendering)
                .setTooltip(Text.translatable("tooltip.fabulousclouds.offset_cloud_rendering"))
                .setSaveConsumer(newValue -> config.offset_cloud_rendering = newValue)
                .build());

        return builder.build();
    }
}