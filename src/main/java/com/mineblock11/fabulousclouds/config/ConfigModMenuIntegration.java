package com.mineblock11.fabulousclouds.config;


import com.mineblock11.fabulousclouds.FabulousClouds;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ConfigModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> FabulousClouds.getInstance().createConfigScreen(parent);
    }
}