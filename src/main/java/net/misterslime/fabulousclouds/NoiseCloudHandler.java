package net.misterslime.fabulousclouds;

import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.gen.SimpleRandom;
import net.misterslime.fabulousclouds.mixin.MixinWorldRenderer;

import java.nio.file.Watchable;
import java.util.Random;

public final class NoiseCloudHandler {

    public static SimplexNoiseSampler noise;
    public static NativeImageBackedTexture cloudsTexture;

    private static final int COLOR = 255 << 24 | 255 << 16 | 255 << 8 | 255;

    private static long cloudIdx = -1;
    private static long timeIdx = -1;
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

        int count = random.nextInt(2000) + 2000;

        for (int i = 0; i < count; i++) {
            int x = random.nextInt(256);
            int z = random.nextInt(256);

            if (noise.sample(x / 16.0, 0, z / 16.0) * 2.5 < random.nextDouble()) {
                cloudsTexture.getImage().setPixelColor(random.nextInt(256), random.nextInt(256), COLOR);
            } else {
                cloudsTexture.getImage().setPixelColor(random.nextInt(256), random.nextInt(256), 0);
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

    public static void initNoise(Random random) {
        noise = new SimplexNoiseSampler(new SimpleRandom(random.nextLong()));
    }
}