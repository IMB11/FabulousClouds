package net.misterslime.fabulousclouds.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;

import java.io.IOException;

public class ConfigScreenFactory {

    public static Screen createConfigScreen(Screen parent, FabulousCloudsConfig config) {
        FabulousCloudsConfig defaultConfig = FabulousCloudsConfig.DEFAULT;

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(new TranslatableComponent("title.fabulousclouds.config"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(new TranslatableComponent("category.fabulousclouds.general"));

        builder.setSavingRunnable(() -> {
            try {
                FabulousCloudsConfig.save(config);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableComponent("option.fabulousclouds.vibrant_clouds"), config.vibrant_clouds)
                .setDefaultValue(defaultConfig.vibrant_clouds)
                .setTooltip(new TranslatableComponent("tooltip.fabulousclouds.vibrant_clouds"))
                .setSaveConsumer(newValue -> config.vibrant_clouds = newValue)
                .build());

        general.addEntry(entryBuilder.startFloatField(new TranslatableComponent("option.fabulousclouds.vibrance_intensity"), config.vibrance_intensity)
                .setDefaultValue(defaultConfig.vibrance_intensity)
                .setTooltip(new TranslatableComponent("tooltip.fabulousclouds.vibrance_intensity"))
                .setSaveConsumer(newValue -> config.vibrance_intensity = newValue)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableComponent("option.fabulousclouds.enable_default_cloud_layer"), config.enable_default_cloud_layer)
                .setDefaultValue(defaultConfig.enable_default_cloud_layer)
                .setTooltip(new TranslatableComponent("tooltip.fabulousclouds.enable_default_cloud_layer"))
                .setSaveConsumer(newValue -> config.enable_default_cloud_layer = newValue)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableComponent("option.fabulousclouds.noise_clouds"), config.noise_clouds)
                .setDefaultValue(defaultConfig.noise_clouds)
                .setTooltip(new TranslatableComponent("tooltip.fabulousclouds.noise_clouds"))
                .setSaveConsumer(newValue -> config.noise_clouds = newValue)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableComponent("option.fabulousclouds.debug_noise_clouds"), config.debug_noise_clouds)
                .setDefaultValue(defaultConfig.debug_noise_clouds)
                .setTooltip(new TranslatableComponent("tooltip.fabulousclouds.debug_noise_clouds"))
                .setSaveConsumer(newValue -> config.debug_noise_clouds = newValue)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableComponent("option.fabulousclouds.offset_cloud_rendering"), config.offset_cloud_rendering)
                .setDefaultValue(defaultConfig.offset_cloud_rendering)
                .setTooltip(new TranslatableComponent("tooltip.fabulousclouds.offset_cloud_rendering"))
                .setSaveConsumer(newValue -> config.offset_cloud_rendering = newValue)
                .build());

        return builder.build();
    }
}
