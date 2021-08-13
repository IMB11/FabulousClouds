package net.misterslime.fabulousclouds.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.misterslime.fabulousclouds.FabulousClouds;

public class ConfigModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> FabulousClouds.getInstance().createConfigScreen(parent);
    }
}
