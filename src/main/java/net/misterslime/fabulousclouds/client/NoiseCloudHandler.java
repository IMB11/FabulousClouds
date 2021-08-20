package net.misterslime.fabulousclouds.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.misterslime.fabulousclouds.FabulousClouds;
import net.misterslime.fabulousclouds.config.FabulousCloudsConfig;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public final class NoiseCloudHandler {

    public static List<CloudTexture> cloudTextures = new LinkedList<CloudTexture>() {};

    private static long cloudIdx = -1;
    private static long timeIdx = -1;
    private static long lastTime = -1;

    public static void update() {
        Minecraft client = Minecraft.getInstance();
        assert client.level != null;
        long time = client.level.getGameTime();
        if (time > lastTime) {
            lastTime = time;
            updateSkyCover(time);

            long update = time / 600;
            if (update > timeIdx) {
                timeIdx = update;
                for (CloudTexture cloudTexture : cloudTextures) {
                    cloudTexture.updateImage(time);
                }
            }

            for (CloudTexture cloudTexture : cloudTextures) {
                cloudTexture.updatePixels();
            }
        }
    }

    public static void updateSkyCover(long time) {
        long idx = time / 12000;

        if (idx > cloudIdx) {
            cloudIdx = idx;

            for (CloudTexture cloudTexture : cloudTextures) {
                cloudTexture.randomizeSkyCover();
            }
        }
    }

    public static void initCloudTextures(ResourceLocation defaultCloud) {
        for (FabulousCloudsConfig.CloudLayer cloudLayer : FabulousClouds.getConfig().cloud_layers) {
            Random random = new Random();

            cloudTextures.add(new CloudTexture(new ResourceLocation("fabulousclouds", "textures/environment/" + random.hashCode() + ".png")));
        }

        if (FabulousClouds.getConfig().enable_default_cloud_layer) {
            cloudTextures.add(new CloudTexture(defaultCloud));
        }
    }
}