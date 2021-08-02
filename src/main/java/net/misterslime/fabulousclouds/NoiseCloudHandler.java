package net.misterslime.fabulousclouds;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.world.gen.SimpleRandom;

import java.nio.file.Watchable;
import java.util.Random;

public final class NoiseCloudHandler {

    public static SimplexNoiseSampler noise;
    public static NativeImageBackedTexture cloudsTexture;

    private static long cloudIdx = -1;
    private static long timeIdx = -1;
    private static long lastTime = -1;

    public static void update() {
        long time = MinecraftClient.getInstance().world.getTime();
        if (time > lastTime) {
            lastTime = time;

            update(time);

            long update = time / 600;

            if (update > timeIdx) {
                timeIdx = update;
                updateImage(time);
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

        int COLOR = 255 << 24 | 255 << 16 | 255 << 8 | 255;
        int count = random.nextInt(2000) + 2000;

        for (int i = 0; i < count; i++) {
            cloudsTexture.getImage().setPixelColor(random.nextInt(256), random.nextInt(256), 0);
        }

        for (int i = 0; i < count; i++) {
            cloudsTexture.getImage().setPixelColor(random.nextInt(256), random.nextInt(256), COLOR);
        }

        cloudsTexture.upload();
    }

    public static void setTexture(NativeImageBackedTexture texture) {
        cloudsTexture = texture;
    }

    public static void initNoise(Random random) {
        noise = new SimplexNoiseSampler(new SimpleRandom(random.nextLong()));
    }
}