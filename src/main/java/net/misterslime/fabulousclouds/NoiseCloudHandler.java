package net.misterslime.fabulousclouds;

import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;

import java.nio.file.Watchable;
import java.util.Random;

public final class NoiseCloudHandler {

    private static final int COLOR = 255 << 24 | 255 << 16 | 255 << 8 | 255;

    private static long cloudIdx = -1;
    private static long timeIdx = -1;
    private static NativeImageBackedTexture cloudsTexture;
    private static long lastTime = -1;
    private static int cloudTexCount = 0;
    private static long cloudTexTimeIdx = -1;

    public static void update() {
        long time = MinecraftClient.getInstance().world.getTime();
        if (time > lastTime) {
            lastTime = time;

            update(time);

            long update = time / 600;

            if (update > timeIdx) {
                timeIdx = update;
                updateImage(time);
                updateCloudTexCount();
            }
        }
    }

    public static void update(long time) {
        long idx = time / 12000;

        if (idx > cloudIdx) {
            cloudIdx = idx;
        }
    }

    public static void updateImage(long time) {
        Random random = new Random(time);
        NativeImage texture = cloudsTexture.getImage();

        if (MinecraftClient.getInstance().world.isRaining()) {
            int count = random.nextInt(4000) + 4000;

            for (int i = 0; i < count; i++) {
                texture.setPixelColor(random.nextInt(256), random.nextInt(256), COLOR);
            }

            count = random.nextInt(500) + 500;

            for (int i = 0; i < count; i++) {
                texture.setPixelColor(random.nextInt(256), random.nextInt(256), 0);
            }
        } else {
            int count = random.nextInt(4000) + 4000;

            for (int i = 0; i < count; i++) {
                texture.setPixelColor(random.nextInt(256), random.nextInt(256), 0);
            }

            count = random.nextInt(500) + 500;

            for (int i = 0; i < count; i++) {
                texture.setPixelColor(random.nextInt(256), random.nextInt(256), COLOR);
            }
        }

        cloudsTexture.upload();
    }

    public static void setTexture(NativeImageBackedTexture texture) {
        cloudsTexture = texture;

        updateCloudTexCount();
    }

    private static void updateCloudTexCount() {
        if (cloudsTexture == null || cloudsTexture.getImage() == null) {
            cloudTexCount = 0;

            return;
        }

        int nonEmptyPixels = 0;

        for (int x = 0; x < 256; x++) {
            for (int z = 0; z < 256; z++) {
                // Returns fully opaque (0xFFFFFFFF) pixels as -1.
                if (cloudsTexture.getImage().getPixelColor(x, z) < 0) {
                    nonEmptyPixels++;
                }
            }
        }

        cloudTexCount = nonEmptyPixels;
    }
}