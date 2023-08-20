package com.mineblock11.fabulousclouds.client;

import java.util.List;
import java.util.Random;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.math.noise.SimplexNoiseSampler;

public class SkyCoverGenerators {

    public static final int COLOR = NativeImage.packColor(255, 255, 255, 255);

    public static void clearSkyGenerator(SimplexNoiseSampler noiseSampler, NativeImage image, double cloudiness) {
        for (int x = 0; x < 256; x++) {
            for (int z = 0; z < 256; z++) {
                if (noiseSampler.sample(x / 16.0, 0, z / 16.0) * 2.5 < cloudiness || image.getColor(x, z) != 0) {
                    image.setColor(x, z, 0);
                }
            }
        }
    }

    public static void clearSkyUpdate(Random random, SimplexNoiseSampler noiseSampler, NativeImage image, List<CloudTexture.PixelCoordinate> pixels, double cloudiness) {
        int count = random.nextInt(1000) + 1000;

        for (int i = 0; i < count; i++) {
            int x = random.nextInt(256);
            int z = random.nextInt(256);

            if (noiseSampler.sample(x / 16.0, 0, z / 16.0) * 2.5 < cloudiness && image.getColor(x, z) == 0 && !updatingPixel(x, z, pixels)) {
                pixels.add(new CloudTexture.PixelCoordinate(x, z, false));
            }
        }

        count = random.nextInt(1000) + 1000;

        for (int i = 0; i < count; i++) {
            int x = random.nextInt(256);
            int z = random.nextInt(256);

            if (image.getColor(x, z) != 0 && !updatingPixel(x, z, pixels)) {
                pixels.add(new CloudTexture.PixelCoordinate(x, z, true));
            }
        }
    }

    public static void normalSkyGenerator(SimplexNoiseSampler noiseSampler, NativeImage image, double cloudiness) {
        for (int x = 0; x < 256; x++) {
            for (int z = 0; z < 256; z++) {
                if (noiseSampler.sample(x / 16.0, 0, z / 16.0) * 2.5 < cloudiness || image.getColor(x, z) != 0) {
                    //image.setPixelRGBA(x, z, (int) (noiseSampler.getValue(x / 16.0, 0, z / 16.0) * 2.5));
                    image.setColor(x, z, COLOR);
                }
            }
        }
    }

    public static void normalSkyUpdate(Random random, SimplexNoiseSampler noiseSampler, NativeImage image, List<CloudTexture.PixelCoordinate> pixels, double cloudiness) {
        int count = random.nextInt(2000) + 2000;

        for (int i = 0; i < count; i++) {
            int x = random.nextInt(256);
            int z = random.nextInt(256);

            if (noiseSampler.sample(x / 16.0, 0, z / 16.0) * 2.5 < cloudiness && image.getColor(x, z) == 0 && !updatingPixel(x, z, pixels)) {
                if ((int) (noiseSampler.sample(x / 16.0, 0, z / 16.0) * 2.5) != 0) {
                    pixels.add(new CloudTexture.PixelCoordinate(x, z, false));
                }
            }
        }

        count = random.nextInt(1000) + 1000;

        for (int i = 0; i < count; i++) {
            int x = random.nextInt(256);
            int z = random.nextInt(256);

            if (image.getColor(x, z) != 0 && !updatingPixel(x, z, pixels)) {
                pixels.add(new CloudTexture.PixelCoordinate(x, z, true));
            }
        }
    }

    public static void cloudySkyGenerator(SimplexNoiseSampler noiseSampler, NativeImage image, double cloudiness) {
        for (int x = 0; x < 256; x++) {
            for (int z = 0; z < 256; z++) {
                image.setColor(x, z, COLOR);
                if (noiseSampler.sample(x / 16.0, 0, z / 16.0) * 2.5 >= cloudiness || image.getColor(x, z) != 0) {
                    if ((int) (noiseSampler.sample(x / 16.0, 0, z / 16.0) * 2.5) != 0) {
                        image.setColor(x, z, 0);
                    }
                }
            }
        }
    }

    public static void cloudySkyUpdate(Random random, SimplexNoiseSampler noiseSampler, NativeImage image, List<CloudTexture.PixelCoordinate> pixels, double cloudiness) {
        int count = random.nextInt(4000) + 4000;

        for (int i = 0; i < count; i++) {
            int x = random.nextInt(256);
            int z = random.nextInt(256);

            if (!updatingPixel(x, z, pixels)) {
                if (noiseSampler.sample(x / 16.0, 0, z / 16.0) * 2.5 < cloudiness && image.getColor(x, z) == 0) {
                    if ((int) (noiseSampler.sample(x / 16.0, 0, z / 16.0) * 2.5) != 0) {
                        pixels.add(new CloudTexture.PixelCoordinate(x, z, true));
                    } else {
                        pixels.add(new CloudTexture.PixelCoordinate(x, z, false));
                    }
                } else {
                    pixels.add(new CloudTexture.PixelCoordinate(x, z, false));
                }
            }
        }
    }

    /*public static void overcastSkyGenerator(SimplexNoiseSampler noiseSampler, NativeImage image, double cloudiness) {
        int color = NativeImage.getAbgrColor(255, 255, 255, 255);

        for (int x = 0; x < 256; x++) {
            for (int z = 0; z < 256; z++) {
                image.setPixelColor(x, z, color);
            }
        }
    }

    public static void overcastSkyUpdate(Random random, SimplexNoiseSampler noiseSampler, NativeImage image, List<CloudTexture.PixelCoordinate> pixels, double cloudiness) {
        int count = random.nextInt(500) + 500;

        for (int i = 0; i < count; i++) {
            int x = random.nextInt(256);
            int z = random.nextInt(256);

            if (noiseSampler.sample(x / 16.0, 0, z / 16.0) * 2.5 < cloudiness && image.getPixelColor(x, z) != 0 && !updatingPixel(x, z, pixels)) {
                pixels.add(new CloudTexture.PixelCoordinate(x, z, true));
            }
        }

        count = random.nextInt(4000) + 4000;

        for (int i = 0; i < count; i++) {
            int x = random.nextInt(256);
            int z = random.nextInt(256);

            if (image.getPixelColor(x, z) == 0 && !updatingPixel(x, z, pixels)) {
                pixels.add(new CloudTexture.PixelCoordinate(x, z, false));
            }
        }
    }*/

    public static boolean updatingPixel(int x, int z, List<CloudTexture.PixelCoordinate> pixels) {
        for (CloudTexture.PixelCoordinate pixel : pixels) {
            if (pixel.posX == x && pixel.posZ == z) {
                return true;
            }
        }
        return false;
    }
}
