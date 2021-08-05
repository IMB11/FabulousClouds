package net.misterslime.fabulousclouds.clouds;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.world.gen.SimpleRandom;
import net.misterslime.fabulousclouds.util.EnumUtil;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class CloudTexture {

    public List<PixelCoordinate> pixels = new LinkedList<PixelCoordinate>() {};

    public SimplexNoiseSampler noise;
    public NativeImageBackedTexture cloudsTexture;
    public Identifier identifier;
    public double cloudiness;

    private SkyCoverTypes skyCover;

    public CloudTexture(Identifier identifier) {
        Random random = new Random();

        this.identifier = identifier;
        randomizeSkyCover(random);
    }

    public void updateImage(long time) {
        Random random = new Random(time);

        switch (skyCover) {
            case CLEAR  -> SkyCoverGenerators.clearSkyUpdate(random, noise, cloudsTexture.getImage(), pixels, cloudiness);
            case NORMAL -> SkyCoverGenerators.normalSkyUpdate(random, noise, cloudsTexture.getImage(), pixels, cloudiness);
            case CLOUDY -> SkyCoverGenerators.cloudySkyUpdate(random, noise, cloudsTexture.getImage(), pixels, cloudiness);
        }
    }

    public void updatePixels() {
        Iterator<PixelCoordinate> pixelIterator = pixels.iterator();
        while (pixelIterator.hasNext()) {
            PixelCoordinate pixel = pixelIterator.next();
            if (!fadePixel(cloudsTexture.getImage(), pixel.posX, pixel.posZ, pixel.fading)) {
                pixelIterator.remove();
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

        if (alpha <= 0) {
            image.setPixelColor(x, z, 0);
            return false;
        } else if (alpha >= 255) {
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

        cloudiness = random.nextDouble();

        switch (skyCover) {
            case CLEAR  -> SkyCoverGenerators.clearSkyGenerator(noise, image, cloudiness);
            case NORMAL -> SkyCoverGenerators.normalSkyGenerator(noise, image, cloudiness);
            case CLOUDY -> SkyCoverGenerators.cloudySkyGenerator(noise, image, cloudiness);
        }

        return new NativeImageBackedTexture(image);
    }

    public void randomizeSkyCover(Random random) {
        this.skyCover = EnumUtil.randomEnum(SkyCoverTypes.class);
        this.cloudiness = random.nextDouble();
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

    public enum SkyCoverTypes {
        NORMAL, CLOUDY, CLEAR
    }
}
