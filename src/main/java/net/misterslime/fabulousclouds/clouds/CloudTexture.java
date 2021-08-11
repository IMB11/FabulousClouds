package net.misterslime.fabulousclouds.clouds;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.world.gen.SimpleRandom;
import net.misterslime.fabulousclouds.util.EnumUtil;

import java.util.*;

public class CloudTexture {

    public List<PixelCoordinate> pixels = new LinkedList<PixelCoordinate>() {};

    public SimplexNoiseSampler noise;
    public NativeImageBackedTexture cloudsTexture;
    public Identifier identifier;
    public double cloudiness;

    private SkyCoverTypes skyCover;

    public CloudTexture(Identifier identifier) {
        this.identifier = identifier;
        randomizeSkyCover();
    }

    public void updateImage(long time) {
        Random random = new Random(time);

        switch (skyCover) {
            case CLEAR  -> SkyCoverGenerators.clearSkyUpdate(random, noise, this.cloudsTexture.getImage(), pixels, cloudiness);
            case NORMAL -> SkyCoverGenerators.normalSkyUpdate(random, noise, this.cloudsTexture.getImage(), pixels, cloudiness);
            case CLOUDY -> SkyCoverGenerators.cloudySkyUpdate(random, noise, this.cloudsTexture.getImage(), pixels, cloudiness);
        }
    }

    public void updatePixels() {
        pixels.removeIf(pixel -> !fadePixel(Objects.requireNonNull(this.cloudsTexture.getImage()), pixel.posX, pixel.posZ, pixel.fading));

        this.cloudsTexture.upload();
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
        } else return alpha < 255;
    }

    public void setTexture(NativeImageBackedTexture texture) {
        this.cloudsTexture = texture;
    }

    public void initNoise(Random random) {
        this.noise = new SimplexNoiseSampler(new SimpleRandom(random.nextLong()));
    }

    public NativeImageBackedTexture getNativeImage(SimplexNoiseSampler noise) {
        NativeImage image = new NativeImage(256, 256, false);

        Random random = new Random();

        this.cloudiness = random.nextDouble();

        switch (skyCover) {
            case CLEAR  -> SkyCoverGenerators.clearSkyGenerator(noise, image, cloudiness);
            case NORMAL -> SkyCoverGenerators.normalSkyGenerator(noise, image, cloudiness);
            case CLOUDY -> SkyCoverGenerators.cloudySkyGenerator(noise, image, cloudiness);
        }

        return new NativeImageBackedTexture(image);
    }

    public void randomizeSkyCover() {
        this.skyCover = EnumUtil.randomEnum(SkyCoverTypes.class);
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
