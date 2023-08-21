package com.mineblock11.fabulousclouds.client;

import com.mineblock11.fabulousclouds.util.EnumUtil;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.util.math.random.ChunkRandom;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class CloudTexture {

    public List<PixelCoordinate> pixels = new LinkedList<PixelCoordinate>() {
    };

    public SimplexNoiseSampler noise;
    public NativeImageBackedTexture cloudsTexture;
    public Identifier resourceLocation;
    public double cloudiness;

    private SkyCoverTypes skyCover;

    public CloudTexture(Identifier resourceLocation) {
        this.resourceLocation = resourceLocation;
        randomizeSkyCover();
    }

    public void updateImage(long time) {
        Random random = new Random(time);

        switch (this.skyCover) {
            case CLEAR ->
                    SkyCoverGenerators.clearSkyUpdate(random, this.noise, this.cloudsTexture.getImage(), pixels, this.cloudiness);
            case NORMAL ->
                    SkyCoverGenerators.normalSkyUpdate(random, this.noise, this.cloudsTexture.getImage(), pixels, this.cloudiness);
            case CLOUDY ->
                    SkyCoverGenerators.cloudySkyUpdate(random, this.noise, this.cloudsTexture.getImage(), pixels, this.cloudiness);
        }
    }

    public void updatePixels() {
        pixels.removeIf(pixel -> !fadePixel(Objects.requireNonNull(this.cloudsTexture.getImage()), pixel.posX, pixel.posZ, pixel.fading));

        this.cloudsTexture.upload();
    }

    public boolean fadePixel(NativeImage image, int x, int z, boolean fading) {
        int color = image.getColor(x, z);
        int alpha = (color >> 24) & 0xFF;
        //int alpha = image.getLuminanceOrAlpha(x, z) + 128;

        if (fading) alpha -= 5;
        else alpha += 5;

        int newColor = alpha << 24 | 255 << 16 | 255 << 8 | 255;
        //int newColor = NativeImage.combine(alpha, 255, 255, 255);
        image.setColor(x, z, newColor);

        if (alpha <= 0) {
            image.setColor(x, z, 0);
            return false;
        } else return alpha < 255;
    }

    public void setTexture(NativeImageBackedTexture texture) {
        this.cloudsTexture = texture;
    }

    public void initNoise(Random random) {
        this.noise = new SimplexNoiseSampler(ChunkRandom.RandomProvider.LEGACY.create(random.nextLong()));
    }

    public NativeImageBackedTexture getNativeImage() {
        NativeImage image = new NativeImage(256, 256, false);

        Random random = new Random();

        this.cloudiness = random.nextDouble();

        switch (skyCover) {
            case CLEAR -> SkyCoverGenerators.clearSkyGenerator(this.noise, image, this.cloudiness);
            case NORMAL -> SkyCoverGenerators.normalSkyGenerator(this.noise, image, this.cloudiness);
            case CLOUDY -> SkyCoverGenerators.cloudySkyGenerator(this.noise, image, this.cloudiness);
        }

        return new NativeImageBackedTexture(image);
    }

    public void randomizeSkyCover() {
        this.skyCover = EnumUtil.randomEnum(SkyCoverTypes.class);
    }

    public enum SkyCoverTypes {
        NORMAL, CLOUDY, CLEAR
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
