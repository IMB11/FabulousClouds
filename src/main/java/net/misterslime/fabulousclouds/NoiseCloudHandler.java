package net.misterslime.fabulousclouds;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.world.gen.SimpleRandom;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public final class NoiseCloudHandler {

    public static final List<PixelCoordinate> pixels = new LinkedList<PixelCoordinate>() {};

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

            updatePixels();
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

        int count = random.nextInt(1000) + 1000;

        int color = 255 << 24 | 255 << 16 | 255 << 8 | 255;

        for (int i = 0; i < count; i++) {
            int x = random.nextInt(256);
            int z = random.nextInt(256);

            if (noise.sample(x / 16.0, 0, z / 16.0) * 2.5 < random.nextDouble() && cloudsTexture.getImage().getPixelColor(x, z) == 0 && !updatingPixel(x, z)) {
                pixels.add(new PixelCoordinate(x, z, false));
            }
        }

        count = random.nextInt(1000) + 1000;

        for (int i = 0; i < count; i++) {
            int x = random.nextInt(256);
            int z = random.nextInt(256);

            if (cloudsTexture.getImage().getPixelColor(x, z) != 0 && !updatingPixel(x, z)) {
                pixels.add(new PixelCoordinate(x, z, true));
            }
        }
    }

    public static boolean updatingPixel(int x, int z) {
        for (PixelCoordinate pixel : pixels) {
            if (pixel.posX == x && pixel.posZ == z) {
                return true;
            }
        }
        return false;
    }

    public static void updatePixels() {
        Iterator<PixelCoordinate> it = pixels.iterator();
        while (it.hasNext()) {
            PixelCoordinate pixel = it.next();
            if (!fadePixel(cloudsTexture.getImage(), pixel.posX, pixel.posZ, pixel.fading)) {
                it.remove();
            }
        }

        cloudsTexture.upload();
    }

    public static boolean fadePixel(NativeImage image, int x, int z, boolean fading) {
        int color = image.getPixelColor(x, z);
        int alpha = (color >> 24) & 0xFF;

        if (fading) alpha -= 5;
        else alpha += 5;

        int newColor = alpha << 24 | 255 << 16 | 255 << 8 | 255;
        image.setPixelColor(x, z, newColor);

        if (alpha >= 255 || alpha <= 0) {
            if (alpha <= 0) {
                image.setPixelColor(x, z, 0);
            }
            return false;
        }

        return true;
    }

    public static void setTexture(NativeImageBackedTexture texture) {
        cloudsTexture = texture;
    }

    public static void initNoise(Random random) {
        noise = new SimplexNoiseSampler(new SimpleRandom(random.nextLong()));
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