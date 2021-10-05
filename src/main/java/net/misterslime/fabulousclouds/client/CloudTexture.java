package net.misterslime.fabulousclouds.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.misterslime.fabulousclouds.util.EnumUtil;

import java.util.*;
import java.util.List;

public class CloudTexture {

    public List<PixelCoordinate> pixels = new LinkedList<PixelCoordinate>() {};

    public SimplexNoise noise;
    public DynamicTexture cloudsTexture;
    public ResourceLocation resourceLocation;
    public double cloudiness;

    private SkyCoverTypes skyCover;

    public CloudTexture(ResourceLocation resourceLocation) {
        this.resourceLocation = resourceLocation;
        randomizeSkyCover();
    }

    public void updateImage(long time) {
        Random random = new Random(time);

        switch (this.skyCover) {
            case CLEAR: SkyCoverGenerators.clearSkyUpdate(random, this.noise, this.cloudsTexture.getPixels(), pixels, this.cloudiness);
            case NORMAL: SkyCoverGenerators.normalSkyUpdate(random, this.noise, this.cloudsTexture.getPixels(), pixels, this.cloudiness);
            case CLOUDY: SkyCoverGenerators.cloudySkyUpdate(random, this.noise, this.cloudsTexture.getPixels(), pixels, this.cloudiness);
        }
    }

    public void updatePixels() {
        pixels.removeIf(pixel -> !fadePixel(Objects.requireNonNull(this.cloudsTexture.getPixels()), pixel.posX, pixel.posZ, pixel.fading));

        this.cloudsTexture.upload();
    }

    public boolean fadePixel(NativeImage image, int x, int z, boolean fading) {
        int color = image.getPixelRGBA(x, z);
        int alpha = (color >> 24) & 0xFF;
        //int alpha = image.getLuminanceOrAlpha(x, z) + 128;

        if (fading) alpha -= 5;
        else alpha += 5;

        int newColor = alpha << 24 | 255 << 16 | 255 << 8 | 255;
        //int newColor = NativeImage.combine(alpha, 255, 255, 255);
        image.setPixelRGBA(x, z, newColor);

        if (alpha <= 0) {
            image.setPixelRGBA(x, z, 0);
            return false;
        } else return alpha < 255;
    }

    public void setTexture(DynamicTexture texture) {
        this.cloudsTexture = texture;
    }

    public void initNoise(Random random) {
        this.noise = new SimplexNoise(new WorldgenRandom(random.nextLong()));
    }

    public DynamicTexture getNativeImage() {
        NativeImage image = new NativeImage(256, 256, false);

        Random random = new Random();

        this.cloudiness = random.nextDouble();

        switch (skyCover) {
            case CLEAR: SkyCoverGenerators.clearSkyGenerator(this.noise, image, this.cloudiness);
            case NORMAL: SkyCoverGenerators.normalSkyGenerator(this.noise, image, this.cloudiness);
            case CLOUDY: SkyCoverGenerators.cloudySkyGenerator(this.noise, image, this.cloudiness);
        }

        return new DynamicTexture(image);
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
