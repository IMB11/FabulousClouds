package com.mineblock11.fabulousclouds.mixin;

import com.mineblock11.fabulousclouds.client.CloudTexture;
import com.mineblock11.fabulousclouds.client.NoiseCloudHandler;
import com.mineblock11.fabulousclouds.config.FabulousCloudsConfig;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.message.MessageType;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import com.mineblock11.fabulousclouds.FabulousClouds;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(WorldRenderer.class)
public final class MixinLevelRenderer {

    @Final
    @Shadow
    private static Identifier CLOUDS_LOCATION;
    @Shadow
    private final int ticks;
    @Final
    @Shadow
    @NotNull
    private final MinecraftClient minecraft;
    @Shadow
    private int prevCloudX;
    @Shadow
    private int prevCloudY;
    @Shadow
    private int prevCloudZ;
    @Shadow
    @NotNull
    private Vec3d prevCloudColor;
    @Shadow
    @NotNull
    private CloudRenderMode prevCloudsType;
    @Shadow
    private boolean generateClouds;
    @Shadow
    @NotNull
    private VertexBuffer cloudBuffer;
    @Unique
    private boolean initializedClouds = false;

    public MixinLevelRenderer() {
        throw new NullPointerException("Null cannot be cast to non-null type.");
    }

    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
    public void renderClouds(MatrixStack poseStack, Matrix4f model, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        FabulousCloudsConfig config = FabulousClouds.getConfig();

        TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
        registerClouds(textureManager);
        NoiseCloudHandler.update();

        if (minecraft.world.getRegistryKey() == ClientWorld.OVERWORLD) {
            float cloudHeight = DimensionEffects.Overworld.CLOUDS_HEIGHT;
            if (!Float.isNaN(cloudHeight)) {
                int i = 0;
                for (FabulousCloudsConfig.CloudLayer cloudLayer : config.cloud_layers) {
                    CloudTexture cloudTexture = NoiseCloudHandler.cloudTextures.get(i);
                    renderCloudLayer(poseStack, model, tickDelta, cameraX, cameraY, cameraZ, cloudHeight, cloudLayer.offset, cloudLayer.scale, cloudLayer.speed, cloudTexture.resourceLocation);
                    i++;
                }
            }

            if (config.enable_default_cloud_layer) {
                CloudTexture cloudTexture = NoiseCloudHandler.cloudTextures.get(NoiseCloudHandler.cloudTextures.size() - 1);
                renderCloudLayer(poseStack, model, tickDelta, cameraX, cameraY, cameraZ, cloudHeight, 0, 1, 1, cloudTexture.resourceLocation);
            }
        }

        ci.cancel();
    }

    private void registerClouds(TextureManager textureManager) {
        if (!this.initializedClouds) {
            Random random = new Random();

            NoiseCloudHandler.initCloudTextures(CLOUDS_LOCATION);

            for (CloudTexture cloudTexture : NoiseCloudHandler.cloudTextures) {
                cloudTexture.initNoise(random);

                NativeImageBackedTexture texture = cloudTexture.getNativeImage();
                textureManager.registerTexture(cloudTexture.resourceLocation, texture);
                cloudTexture.setTexture(texture);
            }

            if (FabricLoader.getInstance().isModLoaded("immersive_portals")) {
                MinecraftClient.getInstance().inGameHud.handleChat(MessageType.SYSTEM, new TranslatableTextContent("messages.fabulousclouds.warn_immersive_portals"), MinecraftClient.getInstance().player.getUuid());
            }

            this.initializedClouds = true;
        }
    }

    private void renderCloudLayer(MatrixStack poseStack, Matrix4f model, float tickDelta, double cameraX, double cameraY, double cameraZ, float cloudHeight, float cloudOffset, float cloudScale, float speedMod, Identifier resourceLocation) {
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(true);
        float scale = 12.0F * cloudScale;
        double speed = ((this.ticks + tickDelta) * (0.03F * speedMod));
        double posX = (cameraX + speed) / scale;
        double posY = (cloudHeight - (float) cameraY + cloudOffset) / cloudScale;
        double posZ = cameraZ / scale + 0.33000001311302185D;
        posX -= Math.floor(posX / 2048.0D) * 2048;
        posZ -= Math.floor(posZ / 2048.0D) * 2048;
        float adjustedX = (float) (posX - (double) Math.floor(posX));
        float adjustedY = (float) (posY / 4.0D - (double) Math.floor(posY / 4.0D)) * 4.0F;
        float adjustedZ = (float) (posZ - (double) Math.floor(posZ));
        Vec3d cloudColor = minecraft.world.getCloudsColor(tickDelta);
        int floorX = (int) Math.floor(posX);
        int floorY = (int) Math.floor(posY / 4.0D);
        int floorZ = (int) Math.floor(posZ);
        if (floorX != this.prevCloudX || floorY != this.prevCloudY || floorZ != this.prevCloudZ || this.minecraft.options.getCloudRenderModeValue() != this.prevCloudsType || this.prevCloudColor.squaredDistanceTo(cloudColor) > 2.0E-4D) {
            this.prevCloudX = floorX;
            this.prevCloudY = floorY;
            this.prevCloudZ = floorZ;
            this.prevCloudColor = cloudColor;
            this.prevCloudsType = this.minecraft.options.getCloudRenderModeValue();
            this.generateClouds = true;
        }

        RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
        if (this.generateClouds) {
            this.generateClouds = false;
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            if (this.cloudBuffer != null) this.cloudBuffer.close();

            this.cloudBuffer = new VertexBuffer();
            this.buildCloudLayer(bufferBuilder, posX, posY, posZ, cloudOffset, cloudScale, cloudColor);
            bufferBuilder.end();
            this.cloudBuffer.upload(bufferBuilder);
        }
        if (FabricLoader.getInstance().isModLoaded("immersive_portals")) {
            RenderSystem.setShaderTexture(0, CLOUDS_LOCATION);
        } else {
            RenderSystem.setShaderTexture(0, resourceLocation);
        }
        BackgroundRenderer.setFogBlack();
        poseStack.push();
        poseStack.scale(scale, cloudScale, scale);
        poseStack.translate(-adjustedX, adjustedY, -adjustedZ);
        if (this.cloudBuffer != null) {
            int cloudMainIndex = this.prevCloudsType == CloudRenderMode.FANCY ? 0 : 1;

            for (int cloudIndex = 1; cloudMainIndex <= cloudIndex; ++cloudMainIndex) {
                if (cloudMainIndex == 0) {
                    RenderSystem.colorMask(false, false, false, false);
                } else {
                    RenderSystem.colorMask(true, true, true, true);
                }

                Shader shader = RenderSystem.getShader();
                this.cloudBuffer.draw(poseStack.peek().getPositionMatrix(), model, shader);
            }
        }

        poseStack.pop();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void buildCloudLayer(BufferBuilder bufferBuilder, double cloudX, double cloudY, double cloudZ, float offset, float scale, Vec3d color) {
        float lowpFracAccur = (float) Math.pow(2.0, -8);
        float mediumpFracAccur = (float) Math.pow(2.0, -10);
        float viewDistance = 8;
        float cloudThickness = 4.0f;
        float adjustedCloudX = (float) Math.floor(cloudX) * lowpFracAccur;
        float adjustedCloudZ = (float) Math.floor(cloudZ) * lowpFracAccur;
        float redTop = (float) color.x;
        float greenTop = (float) color.y;
        float blueTop = (float) color.z;
        float redEW = redTop * 0.9f;
        float greenEW = greenTop * 0.9f;
        float blueEW = blueTop * 0.9f;
        float redBottom = redTop * 0.7f;
        float greenBottom = greenTop * 0.7f;
        float blueBottom = blueTop * 0.7f;
        float redNS = redTop * 0.8f;
        float greenNS = greenTop * 0.8f;
        float blueNS = blueTop * 0.8f;
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_NORMAL);
        float adjustedCloudY = (float) Math.floor(cloudY / cloudThickness) * cloudThickness;
        boolean offsetCloudRendering = FabulousClouds.getConfig().offset_cloud_rendering;

        if (this.prevCloudsType == CloudRenderMode.FANCY) {
            int scaledViewDistance = (int) ((minecraft.options.viewDistance / 4) / scale) / 2;

            if (offsetCloudRendering) {
                float cloudHeightOffset = offset + DimensionEffects.Overworld.CLOUDS_HEIGHT - 63;
                float cloudHeightSeaLevel = DimensionEffects.Overworld.CLOUDS_HEIGHT - 63;

                if (cloudHeightOffset > cloudHeightSeaLevel) {
                    float offsetScale = cloudHeightOffset / cloudHeightSeaLevel;

                    scaledViewDistance *= offsetScale;
                }
            }

            for (int x = -scaledViewDistance - 1; x <= scaledViewDistance; ++x) {
                for (int z = -scaledViewDistance - 1; z <= scaledViewDistance; ++z) {
                    int n3;
                    float scaledX = x * viewDistance;
                    float scaledZ = z * viewDistance;
                    if (adjustedCloudY > -5.0f) {
                        bufferBuilder.vertex(scaledX + 0.0f, adjustedCloudY + 0.0f, scaledZ + 8.0f).texture((scaledX + 0.0f) * lowpFracAccur + adjustedCloudX, (scaledZ + 8.0f) * lowpFracAccur + adjustedCloudZ).color(redBottom, greenBottom, blueBottom, 0.8f).normal(0.0f, -1.0f, 0.0f).next();
                        bufferBuilder.vertex(scaledX + 8.0f, adjustedCloudY + 0.0f, scaledZ + 8.0f).texture((scaledX + 8.0f) * lowpFracAccur + adjustedCloudX, (scaledZ + 8.0f) * lowpFracAccur + adjustedCloudZ).color(redBottom, greenBottom, blueBottom, 0.8f).normal(0.0f, -1.0f, 0.0f).next();
                        bufferBuilder.vertex(scaledX + 8.0f, adjustedCloudY + 0.0f, scaledZ + 0.0f).texture((scaledX + 8.0f) * lowpFracAccur + adjustedCloudX, (scaledZ + 0.0f) * lowpFracAccur + adjustedCloudZ).color(redBottom, greenBottom, blueBottom, 0.8f).normal(0.0f, -1.0f, 0.0f).next();
                        bufferBuilder.vertex(scaledX + 0.0f, adjustedCloudY + 0.0f, scaledZ + 0.0f).texture((scaledX + 0.0f) * lowpFracAccur + adjustedCloudX, (scaledZ + 0.0f) * lowpFracAccur + adjustedCloudZ).color(redBottom, greenBottom, blueBottom, 0.8f).normal(0.0f, -1.0f, 0.0f).next();
                    }
                    if (adjustedCloudY <= 5.0f) {
                        bufferBuilder.vertex(scaledX + 0.0f, adjustedCloudY + cloudThickness - mediumpFracAccur, scaledZ + 8.0f).texture((scaledX + 0.0f) * lowpFracAccur + adjustedCloudX, (scaledZ + 8.0f) * lowpFracAccur + adjustedCloudZ).color(redTop, greenTop, blueTop, 0.8f).normal(0.0f, 1.0f, 0.0f).next();
                        bufferBuilder.vertex(scaledX + 8.0f, adjustedCloudY + cloudThickness - mediumpFracAccur, scaledZ + 8.0f).texture((scaledX + 8.0f) * lowpFracAccur + adjustedCloudX, (scaledZ + 8.0f) * lowpFracAccur + adjustedCloudZ).color(redTop, greenTop, blueTop, 0.8f).normal(0.0f, 1.0f, 0.0f).next();
                        bufferBuilder.vertex(scaledX + 8.0f, adjustedCloudY + cloudThickness - mediumpFracAccur, scaledZ + 0.0f).texture((scaledX + 8.0f) * lowpFracAccur + adjustedCloudX, (scaledZ + 0.0f) * lowpFracAccur + adjustedCloudZ).color(redTop, greenTop, blueTop, 0.8f).normal(0.0f, 1.0f, 0.0f).next();
                        bufferBuilder.vertex(scaledX + 0.0f, adjustedCloudY + cloudThickness - mediumpFracAccur, scaledZ + 0.0f).texture((scaledX + 0.0f) * lowpFracAccur + adjustedCloudX, (scaledZ + 0.0f) * lowpFracAccur + adjustedCloudZ).color(redTop, greenTop, blueTop, 0.8f).normal(0.0f, 1.0f, 0.0f).next();
                    }
                    if (x > -1) {
                        for (n3 = 0; n3 < 8; ++n3) {
                            bufferBuilder.vertex(scaledX + (float) n3 + 0.0f, adjustedCloudY + 0.0f, scaledZ + 8.0f).texture((scaledX + (float) n3 + 0.5f) * lowpFracAccur + adjustedCloudX, (scaledZ + 8.0f) * lowpFracAccur + adjustedCloudZ).color(redEW, greenEW, blueEW, 0.8f).normal(-1.0f, 0.0f, 0.0f).next();
                            bufferBuilder.vertex(scaledX + (float) n3 + 0.0f, adjustedCloudY + cloudThickness, scaledZ + 8.0f).texture((scaledX + (float) n3 + 0.5f) * lowpFracAccur + adjustedCloudX, (scaledZ + 8.0f) * lowpFracAccur + adjustedCloudZ).color(redEW, greenEW, blueEW, 0.8f).normal(-1.0f, 0.0f, 0.0f).next();
                            bufferBuilder.vertex(scaledX + (float) n3 + 0.0f, adjustedCloudY + cloudThickness, scaledZ + 0.0f).texture((scaledX + (float) n3 + 0.5f) * lowpFracAccur + adjustedCloudX, (scaledZ + 0.0f) * lowpFracAccur + adjustedCloudZ).color(redEW, greenEW, blueEW, 0.8f).normal(-1.0f, 0.0f, 0.0f).next();
                            bufferBuilder.vertex(scaledX + (float) n3 + 0.0f, adjustedCloudY + 0.0f, scaledZ + 0.0f).texture((scaledX + (float) n3 + 0.5f) * lowpFracAccur + adjustedCloudX, (scaledZ + 0.0f) * lowpFracAccur + adjustedCloudZ).color(redEW, greenEW, blueEW, 0.8f).normal(-1.0f, 0.0f, 0.0f).next();
                        }
                    }
                    if (x <= 1) {
                        for (n3 = 0; n3 < 8; ++n3) {
                            bufferBuilder.vertex(scaledX + (float) n3 + 1.0f - mediumpFracAccur, adjustedCloudY + 0.0f, scaledZ + 8.0f).texture((scaledX + (float) n3 + 0.5f) * lowpFracAccur + adjustedCloudX, (scaledZ + 8.0f) * lowpFracAccur + adjustedCloudZ).color(redEW, greenEW, blueEW, 0.8f).normal(1.0f, 0.0f, 0.0f).next();
                            bufferBuilder.vertex(scaledX + (float) n3 + 1.0f - mediumpFracAccur, adjustedCloudY + cloudThickness, scaledZ + 8.0f).texture((scaledX + (float) n3 + 0.5f) * lowpFracAccur + adjustedCloudX, (scaledZ + 8.0f) * lowpFracAccur + adjustedCloudZ).color(redEW, greenEW, blueEW, 0.8f).normal(1.0f, 0.0f, 0.0f).next();
                            bufferBuilder.vertex(scaledX + (float) n3 + 1.0f - mediumpFracAccur, adjustedCloudY + cloudThickness, scaledZ + 0.0f).texture((scaledX + (float) n3 + 0.5f) * lowpFracAccur + adjustedCloudX, (scaledZ + 0.0f) * lowpFracAccur + adjustedCloudZ).color(redEW, greenEW, blueEW, 0.8f).normal(1.0f, 0.0f, 0.0f).next();
                            bufferBuilder.vertex(scaledX + (float) n3 + 1.0f - mediumpFracAccur, adjustedCloudY + 0.0f, scaledZ + 0.0f).texture((scaledX + (float) n3 + 0.5f) * lowpFracAccur + adjustedCloudX, (scaledZ + 0.0f) * lowpFracAccur + adjustedCloudZ).color(redEW, greenEW, blueEW, 0.8f).normal(1.0f, 0.0f, 0.0f).next();
                        }
                    }
                    if (z > -1) {
                        for (n3 = 0; n3 < 8; ++n3) {
                            bufferBuilder.vertex(scaledX + 0.0f, adjustedCloudY + cloudThickness, scaledZ + (float) n3 + 0.0f).texture((scaledX + 0.0f) * lowpFracAccur + adjustedCloudX, (scaledZ + (float) n3 + 0.5f) * lowpFracAccur + adjustedCloudZ).color(redNS, greenNS, blueNS, 0.8f).normal(0.0f, 0.0f, -1.0f).next();
                            bufferBuilder.vertex(scaledX + 8.0f, adjustedCloudY + cloudThickness, scaledZ + (float) n3 + 0.0f).texture((scaledX + 8.0f) * lowpFracAccur + adjustedCloudX, (scaledZ + (float) n3 + 0.5f) * lowpFracAccur + adjustedCloudZ).color(redNS, greenNS, blueNS, 0.8f).normal(0.0f, 0.0f, -1.0f).next();
                            bufferBuilder.vertex(scaledX + 8.0f, adjustedCloudY + 0.0f, scaledZ + (float) n3 + 0.0f).texture((scaledX + 8.0f) * lowpFracAccur + adjustedCloudX, (scaledZ + (float) n3 + 0.5f) * lowpFracAccur + adjustedCloudZ).color(redNS, greenNS, blueNS, 0.8f).normal(0.0f, 0.0f, -1.0f).next();
                            bufferBuilder.vertex(scaledX + 0.0f, adjustedCloudY + 0.0f, scaledZ + (float) n3 + 0.0f).texture((scaledX + 0.0f) * lowpFracAccur + adjustedCloudX, (scaledZ + (float) n3 + 0.5f) * lowpFracAccur + adjustedCloudZ).color(redNS, greenNS, blueNS, 0.8f).normal(0.0f, 0.0f, -1.0f).next();
                        }
                    }
                    if (z > 1) continue;
                    for (n3 = 0; n3 < 8; ++n3) {
                        bufferBuilder.vertex(scaledX + 0.0f, adjustedCloudY + cloudThickness, scaledZ + (float) n3 + 1.0f - mediumpFracAccur).texture((scaledX + 0.0f) * lowpFracAccur + adjustedCloudX, (scaledZ + (float) n3 + 0.5f) * lowpFracAccur + adjustedCloudZ).color(redNS, greenNS, blueNS, 0.8f).normal(0.0f, 0.0f, 1.0f).next();
                        bufferBuilder.vertex(scaledX + 8.0f, adjustedCloudY + cloudThickness, scaledZ + (float) n3 + 1.0f - mediumpFracAccur).texture((scaledX + 8.0f) * lowpFracAccur + adjustedCloudX, (scaledZ + (float) n3 + 0.5f) * lowpFracAccur + adjustedCloudZ).color(redNS, greenNS, blueNS, 0.8f).normal(0.0f, 0.0f, 1.0f).next();
                        bufferBuilder.vertex(scaledX + 8.0f, adjustedCloudY + 0.0f, scaledZ + (float) n3 + 1.0f - mediumpFracAccur).texture((scaledX + 8.0f) * lowpFracAccur + adjustedCloudX, (scaledZ + (float) n3 + 0.5f) * lowpFracAccur + adjustedCloudZ).color(redNS, greenNS, blueNS, 0.8f).normal(0.0f, 0.0f, 1.0f).next();
                        bufferBuilder.vertex(scaledX + 0.0f, adjustedCloudY + 0.0f, scaledZ + (float) n3 + 1.0f - mediumpFracAccur).texture((scaledX + 0.0f) * lowpFracAccur + adjustedCloudX, (scaledZ + (float) n3 + 0.5f) * lowpFracAccur + adjustedCloudZ).color(redNS, greenNS, blueNS, 0.8f).normal(0.0f, 0.0f, 1.0f).next();
                    }
                }
            }
        } else {
            int scaledRenderDistance = (int) (minecraft.options.viewDistance / scale);

            if (offsetCloudRendering) {
                float cloudHeightOffset = offset + DimensionEffects.Overworld.CLOUDS_HEIGHT - 63;
                float cloudHeightSeaLevel = DimensionEffects.Overworld.CLOUDS_HEIGHT - 63;

                if (cloudHeightOffset > cloudHeightSeaLevel) {
                    float offsetScale = cloudHeightOffset / cloudHeightSeaLevel;

                    scaledRenderDistance *= offsetScale;
                }
            }

            for (int x = -scaledRenderDistance; x < scaledRenderDistance; x += scaledRenderDistance) {
                for (int z = -scaledRenderDistance; z < scaledRenderDistance; z += scaledRenderDistance) {
                    bufferBuilder.vertex(x, adjustedCloudY, z + scaledRenderDistance).texture((float) x * lowpFracAccur + adjustedCloudX, (float) (z + scaledRenderDistance) * lowpFracAccur + adjustedCloudZ).color(redTop, greenTop, blueTop, 0.8f).normal(0.0f, -1.0f, 0.0f).next();
                    bufferBuilder.vertex(x + scaledRenderDistance, adjustedCloudY, z + scaledRenderDistance).texture((float) (x + scaledRenderDistance) * lowpFracAccur + adjustedCloudX, (float) (z + scaledRenderDistance) * lowpFracAccur + adjustedCloudZ).color(redTop, greenTop, blueTop, 0.8f).normal(0.0f, -1.0f, 0.0f).next();
                    bufferBuilder.vertex(x + scaledRenderDistance, adjustedCloudY, z).texture((float) (x + scaledRenderDistance) * lowpFracAccur + adjustedCloudX, (float) z * lowpFracAccur + adjustedCloudZ).color(redTop, greenTop, blueTop, 0.8f).normal(0.0f, -1.0f, 0.0f).next();
                    bufferBuilder.vertex(x, adjustedCloudY, z).texture((float) x * lowpFracAccur + adjustedCloudX, (float) z * lowpFracAccur + adjustedCloudZ).color(redTop, greenTop, blueTop, 0.8f).normal(0.0f, -1.0f, 0.0f).next();
                }
            }
        }
    }
}
