package com.mineblock11.fabulousclouds.client;

import com.mineblock11.fabulousclouds.FabulousClouds;

import java.util.LinkedList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

public final class NoiseCloudHandler {

    public static List<CloudTexture> cloudTextures = new LinkedList<CloudTexture>() {
    };

    private static long cloudIdx = -1;
    private static long timeIdx = -1;
    private static long lastTime = -1;

    public static void update() {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.world != null;
        long time = client.world.getTime();
        if (time > lastTime) {
            lastTime = time;
            updateSkyCover(time);

            long update = time / 600;
            if (update > timeIdx) {
                timeIdx = update;
                for (CloudTexture cloudTexture : cloudTextures) {
                    if (cloudTexture.cloudsTexture.getImage() != null) {
                        cloudTexture.updateImage(time);
                    }
                }
            }

            for (CloudTexture cloudTexture : cloudTextures) {
                if (cloudTexture.cloudsTexture.getImage() != null) {
                    cloudTexture.updatePixels();
                }
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

    public static void initCloudTextures(Identifier defaultCloud) {
        CloudTexture defaultCloudTexture = new CloudTexture(defaultCloud);

        for (int i = 0; i < FabulousClouds.getConfig().cloud_layers.length; i++) {
            cloudTextures.add(FabulousClouds.getConfig().noise_clouds ? new CloudTexture(new Identifier("fabulousclouds", "textures/environment/clouds" + i + ".png")) : defaultCloudTexture);
        }

        if (FabulousClouds.getConfig().enable_default_cloud_layer) {
            cloudTextures.add(defaultCloudTexture);
        }
    }
}