package net.misterslime.fabulousclouds;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.misterslime.fabulousclouds.config.FabulousCloudsConfig;
import net.misterslime.fabulousclouds.util.CloudTexture;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public final class NoiseCloudHandler {

    public static List<CloudTexture> cloudTextures = new LinkedList<CloudTexture>() {};

    private static long cloudIdx = -1;
    private static long timeIdx = -1;
    private static long lastTime = -1;

    public static void update() {
        long time = MinecraftClient.getInstance().world.getTime();
        FabulousCloudsConfig config = FabulousClouds.getConfig();
        if (time > lastTime) {
            lastTime = time;

            long idx = time / 12000;

            if (idx > cloudIdx) {
                cloudIdx = idx;
            }

            long update = time / 600;

            if (update > timeIdx) {
                timeIdx = update;
                for (CloudTexture cloudTexture : cloudTextures) {
                    cloudTexture.updateImage(time);
                }
            }

            for (CloudTexture cloudTexture  : cloudTextures) {
                cloudTexture.updatePixels();
            }
        }
    }

    public static void initCloudTextures(Identifier defaultCloud) {
        for (FabulousCloudsConfig.CloudLayer cloudLayer : FabulousClouds.getConfig().cloud_layers) {
            Random random = new Random();

            cloudTextures.add(new CloudTexture(new Identifier("fabulousclouds", "textures/environment/" + random.hashCode() + ".png")));
        }

        if (FabulousClouds.getConfig().enable_default_cloud_layer) {
            cloudTextures.add(new CloudTexture(defaultCloud));
        }
    }

    public static class PixelCoordinate {
        public int posX;
        public int posZ;
        public boolean fading;

        public PixelCoordinate(int posX, int posZ, boolean fading) {
            this.posX = posX;
            this.posZ = posZ;
            this.fading = fading;
        }
    }
}