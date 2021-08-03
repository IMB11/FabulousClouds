package net.misterslime.fabulousclouds.util;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.world.gen.SimpleRandom;
import net.misterslime.fabulousclouds.NoiseCloudHandler;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class CloudTexture {

    public List<NoiseCloudHandler.PixelCoordinate> pixels = new LinkedList<NoiseCloudHandler.PixelCoordinate>() {};

    public SimplexNoiseSampler noise;
    public NativeImageBackedTexture cloudsTexture;
    public Identifier identifier;

    public CloudTexture(Identifier identifier) {
        this.identifier = identifier;
    }

    public void updateImage(long time) {
        Random random = new Random(time);

        int count = random.nextInt(1000) + 1000;

        for (int i = 0; i < count; i++) {
            int x = random.nextInt(256);
            int z = random.nextInt(256);

            if (noise.sample(x / 16.0, 0, z / 16.0) * 2.5 < random.nextDouble() && cloudsTexture.getImage().getPixelColor(x, z) == 0 && !updatingPixel(x, z)) {
                pixels.add(new NoiseCloudHandler.PixelCoordinate(x, z, false));
            }
        }

        count = random.nextInt(1000) + 1000;

        for (int i = 0; i < count; i++) {
            int x = random.nextInt(256);
            int z = random.nextInt(256);

            if (cloudsTexture.getImage().getPixelColor(x, z) != 0 && !updatingPixel(x, z)) {
                pixels.add(new NoiseCloudHandler.PixelCoordinate(x, z, true));
            }
        }
    }

    public boolean updatingPixel(int x, int z) {
        for (NoiseCloudHandler.PixelCoordinate pixel : pixels) {
            if (pixel.posX == x && pixel.posZ == z) {
                return true;
            }
        }
        return false;
    }

    public void updatePixels() {
        Iterator<NoiseCloudHandler.PixelCoordinate> it = pixels.iterator();
        while (it.hasNext()) {
            NoiseCloudHandler.PixelCoordinate pixel = it.next();
            if (!fadePixel(cloudsTexture.getImage(), pixel.posX, pixel.posZ, pixel.fading)) {
                it.remove();
            }
        }

        cloudsTexture.upload();
    }

    public boolean fadePixel(NativeImage image, int x, int z, boolean fading) {
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

    public void setTexture(NativeImageBackedTexture texture) {
        cloudsTexture = texture;
    }

    public void initNoise(Random random) {
        noise = new SimplexNoiseSampler(new SimpleRandom(random.nextLong()));
    }

    public NativeImageBackedTexture getNativeImage(SimplexNoiseSampler noise) {
        NativeImage image = new NativeImage(256, 256, false);

        Random random = new Random();

        double cloudiness = random.nextDouble();

        for (int x = 0; x < 256; x++) {
            for (int z = 0; z < 256; z++) {
                if (noise.sample(x / 16.0, 0, z / 16.0) * 2.5 < cloudiness || image.getPixelColor(x, z) != 0) {
                    image.setPixelColor(x, z, (int) (noise.sample(x / 16.0, 0, z / 16.0) * 2.5));
                }
            }
        }

        return new NativeImageBackedTexture(image);
    }
}
