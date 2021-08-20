package net.misterslime.fabulousclouds.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.misterslime.fabulousclouds.FabulousClouds;
import net.misterslime.fabulousclouds.client.NoiseCloudHandler;
import net.misterslime.fabulousclouds.config.FabulousCloudsConfig;
import net.misterslime.fabulousclouds.client.CloudTexture;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(LevelRenderer.class)
public final class MixinLevelRenderer {

    @Final
    @Shadow
    private static ResourceLocation CLOUDS_LOCATION;
    @Shadow
    private final int ticks;
    @Final
    @Shadow
    @NotNull
    private final Minecraft minecraft;
    @Shadow
    private int prevCloudX;
    @Shadow
    private int prevCloudY;
    @Shadow
    private int prevCloudZ;
    @Shadow
    @NotNull
    private Vec3 prevCloudColor;
    @Shadow
    @NotNull
    private CloudStatus prevCloudsType;
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
    public void renderClouds(PoseStack poseStack, Matrix4f model, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        FabulousCloudsConfig config = FabulousClouds.getConfig();
        if (FabulousClouds.getConfig().noise_clouds) {
            TextureManager textureManager = Minecraft.getInstance().getTextureManager();
            registerClouds(textureManager);
            NoiseCloudHandler.update();
        }

        if (minecraft.level.dimension() == ClientLevel.OVERWORLD) {
            float cloudHeight = DimensionSpecialEffects.OverworldEffects.CLOUD_LEVEL;
            if (!Float.isNaN(cloudHeight)) {
                int i = 0;
                for(FabulousCloudsConfig.CloudLayer cloudLayer : config.cloud_layers) {
                    CloudTexture cloudTexture = NoiseCloudHandler.cloudTextures.get(i);
                    renderCloudLayer(poseStack, model, tickDelta, cameraX, cameraY, cameraZ, cloudHeight, cloudLayer.offset, cloudLayer.scale, cloudLayer.speed, cloudTexture.resourceLocation);
                    i++;
                }
            }

            if (config.enable_default_cloud_layer) {
                CloudTexture cloudTexture = NoiseCloudHandler.cloudTextures.get(NoiseCloudHandler.cloudTextures.size() - 1);
                renderCloudLayer(poseStack, model, tickDelta, cameraX, cameraY, cameraZ, cloudHeight, 0, 1, 1, cloudTexture.resourceLocation);
            }

            ci.cancel();
        }
    }

    private void registerClouds(TextureManager textureManager) {
        if (!this.initializedClouds) {
            Random random = new Random();

            NoiseCloudHandler.initCloudTextures(CLOUDS_LOCATION);

            for(CloudTexture cloudTexture : NoiseCloudHandler.cloudTextures) {
                cloudTexture.initNoise(random);

                DynamicTexture texture = cloudTexture.getNativeImage();
                textureManager.register(cloudTexture.resourceLocation, texture);
                cloudTexture.setTexture(texture);
            }

            this.initializedClouds = true;
        }
    }

    private void renderCloudLayer(PoseStack poseStack, Matrix4f model, float tickDelta, double cameraX, double cameraY, double cameraZ, float cloudHeight, float cloudOffset, float cloudScale, float speedMod, ResourceLocation resourceLocation) {
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
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
        Vec3 cloudColor = minecraft.level.getCloudColor(tickDelta);
        int floorX = (int) Math.floor(posX);
        int floorY = (int) Math.floor(posY / 4.0D);
        int floorZ = (int) Math.floor(posZ);
        if (floorX != this.prevCloudX || floorY != this.prevCloudY || floorZ != this.prevCloudZ || this.minecraft.options.getCloudsType() != this.prevCloudsType || this.prevCloudColor.distanceToSqr(cloudColor) > 2.0E-4D) {
            this.prevCloudX = floorX;
            this.prevCloudY = floorY;
            this.prevCloudZ = floorZ;
            this.prevCloudColor = cloudColor;
            this.prevCloudsType = this.minecraft.options.getCloudsType();
            this.generateClouds = true;
        }

        RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
        if (this.generateClouds) {
            this.generateClouds = false;
            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuilder();
            if (this.cloudBuffer != null) this.cloudBuffer.close();

            this.cloudBuffer = new VertexBuffer();
            this.buildCloudLayer(bufferBuilder, posX, posY, posZ, cloudScale, cloudColor);
            bufferBuilder.end();
            this.cloudBuffer.upload(bufferBuilder);
        }
        RenderSystem.setShaderTexture(0, resourceLocation);
        FogRenderer.levelFogColor();
        poseStack.pushPose();
        poseStack.scale(scale, cloudScale, scale);
        poseStack.translate(-adjustedX, adjustedY, -adjustedZ);
        if (this.cloudBuffer != null) {
            int cloudMainIndex = this.prevCloudsType == CloudStatus.FANCY ? 0 : 1;
          
            for (int cloudIndex = 1; cloudMainIndex <= cloudIndex; ++cloudMainIndex) {
                if (cloudMainIndex == 0) {
                    RenderSystem.colorMask(false, false, false, false);
                } else {
                    RenderSystem.colorMask(true, true, true, true);
                }

                ShaderInstance shader = RenderSystem.getShader();
                this.cloudBuffer.drawWithShader(poseStack.last().pose(), model, shader);
            }
        }

        poseStack.popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void buildCloudLayer(BufferBuilder bufferBuilder, double cloudX, double cloudY, double cloudZ, float scale, Vec3 color) {
        float lowpFracAccur = (float) Math.pow(2.0, -8);
        float mediumpFracAccur = (float) Math.pow(2.0, -10);
        float viewDistance = 8;
        float cloudThickness = 4.0f;
        float adjustedCloudX = (float)Math.floor(cloudX) * lowpFracAccur;
        float adjustedCloudZ = (float)Math.floor(cloudZ) * lowpFracAccur;
        float redTop = (float)color.x;
        float greenTop = (float)color.y;
        float blueTop = (float)color.z;
        float redEW = redTop * 0.9f;
        float greenEW = greenTop * 0.9f;
        float blueEW = blueTop * 0.9f;
        float redBottom = redTop * 0.7f;
        float greenBottom = greenTop * 0.7f;
        float blueBottom = blueTop * 0.7f;
        float redNS = redTop * 0.8f;
        float greenNS = greenTop * 0.8f;
        float blueNS = blueTop * 0.8f;
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
        float adjustedCloudY = (float)Math.floor(cloudY / cloudThickness) * cloudThickness;
        if (this.prevCloudsType == CloudStatus.FANCY) {
            int scaledViewDistance = (int) (viewDistance / scale) / 2;

            for (int x = -scaledViewDistance; x <= scaledViewDistance; ++x) {
                for (int z = -scaledViewDistance; z <= scaledViewDistance; ++z) {
                    int n3;
                    float scaledX = x * viewDistance;
                    float scaledZ = z * viewDistance;
                    if (adjustedCloudY > -5.0f) {
                        bufferBuilder.vertex(scaledX + 0.0f, adjustedCloudY + 0.0f, scaledZ + 8.0f).uv((scaledX + 0.0f) * lowpFracAccur + adjustedCloudX, (scaledZ + 8.0f) * lowpFracAccur + adjustedCloudZ).color(redBottom, greenBottom, blueBottom, 0.8f).normal(0.0f, -1.0f, 0.0f).endVertex();
                        bufferBuilder.vertex(scaledX + 8.0f, adjustedCloudY + 0.0f, scaledZ + 8.0f).uv((scaledX + 8.0f) * lowpFracAccur + adjustedCloudX, (scaledZ + 8.0f) * lowpFracAccur + adjustedCloudZ).color(redBottom, greenBottom, blueBottom, 0.8f).normal(0.0f, -1.0f, 0.0f).endVertex();
                        bufferBuilder.vertex(scaledX + 8.0f, adjustedCloudY + 0.0f, scaledZ + 0.0f).uv((scaledX + 8.0f) * lowpFracAccur + adjustedCloudX, (scaledZ + 0.0f) * lowpFracAccur + adjustedCloudZ).color(redBottom, greenBottom, blueBottom, 0.8f).normal(0.0f, -1.0f, 0.0f).endVertex();
                        bufferBuilder.vertex(scaledX + 0.0f, adjustedCloudY + 0.0f, scaledZ + 0.0f).uv((scaledX + 0.0f) * lowpFracAccur + adjustedCloudX, (scaledZ + 0.0f) * lowpFracAccur + adjustedCloudZ).color(redBottom, greenBottom, blueBottom, 0.8f).normal(0.0f, -1.0f, 0.0f).endVertex();
                    }
                    if (adjustedCloudY <= 5.0f) {
                        bufferBuilder.vertex(scaledX + 0.0f, adjustedCloudY + cloudThickness - mediumpFracAccur, scaledZ + 8.0f).uv((scaledX + 0.0f) * lowpFracAccur + adjustedCloudX, (scaledZ + 8.0f) * lowpFracAccur + adjustedCloudZ).color(redTop, greenTop, blueTop, 0.8f).normal(0.0f, 1.0f, 0.0f).endVertex();
                        bufferBuilder.vertex(scaledX + 8.0f, adjustedCloudY + cloudThickness - mediumpFracAccur, scaledZ + 8.0f).uv((scaledX + 8.0f) * lowpFracAccur + adjustedCloudX, (scaledZ + 8.0f) * lowpFracAccur + adjustedCloudZ).color(redTop, greenTop, blueTop, 0.8f).normal(0.0f, 1.0f, 0.0f).endVertex();
                        bufferBuilder.vertex(scaledX + 8.0f, adjustedCloudY + cloudThickness - mediumpFracAccur, scaledZ + 0.0f).uv((scaledX + 8.0f) * lowpFracAccur + adjustedCloudX, (scaledZ + 0.0f) * lowpFracAccur + adjustedCloudZ).color(redTop, greenTop, blueTop, 0.8f).normal(0.0f, 1.0f, 0.0f).endVertex();
                        bufferBuilder.vertex(scaledX + 0.0f, adjustedCloudY + cloudThickness - mediumpFracAccur, scaledZ + 0.0f).uv((scaledX + 0.0f) * lowpFracAccur + adjustedCloudX, (scaledZ + 0.0f) * lowpFracAccur + adjustedCloudZ).color(redTop, greenTop, blueTop, 0.8f).normal(0.0f, 1.0f, 0.0f).endVertex();
                    }
                    if (x > -1) {
                        for (n3 = 0; n3 < 8; ++n3) {
                            bufferBuilder.vertex(scaledX + (float)n3 + 0.0f, adjustedCloudY + 0.0f, scaledZ + 8.0f).uv((scaledX + (float)n3 + 0.5f) * lowpFracAccur + adjustedCloudX, (scaledZ + 8.0f) * lowpFracAccur + adjustedCloudZ).color(redEW, greenEW, blueEW, 0.8f).normal(-1.0f, 0.0f, 0.0f).endVertex();
                            bufferBuilder.vertex(scaledX + (float)n3 + 0.0f, adjustedCloudY + cloudThickness, scaledZ + 8.0f).uv((scaledX + (float)n3 + 0.5f) * lowpFracAccur + adjustedCloudX, (scaledZ + 8.0f) * lowpFracAccur + adjustedCloudZ).color(redEW, greenEW, blueEW, 0.8f).normal(-1.0f, 0.0f, 0.0f).endVertex();
                            bufferBuilder.vertex(scaledX + (float)n3 + 0.0f, adjustedCloudY + cloudThickness, scaledZ + 0.0f).uv((scaledX + (float)n3 + 0.5f) * lowpFracAccur + adjustedCloudX, (scaledZ + 0.0f) * lowpFracAccur + adjustedCloudZ).color(redEW, greenEW, blueEW, 0.8f).normal(-1.0f, 0.0f, 0.0f).endVertex();
                            bufferBuilder.vertex(scaledX + (float)n3 + 0.0f, adjustedCloudY + 0.0f, scaledZ + 0.0f).uv((scaledX + (float)n3 + 0.5f) * lowpFracAccur + adjustedCloudX, (scaledZ + 0.0f) * lowpFracAccur + adjustedCloudZ).color(redEW, greenEW, blueEW, 0.8f).normal(-1.0f, 0.0f, 0.0f).endVertex();
                        }
                    }
                    if (x <= 1) {
                        for (n3 = 0; n3 < 8; ++n3) {
                            bufferBuilder.vertex(scaledX + (float)n3 + 1.0f - mediumpFracAccur, adjustedCloudY + 0.0f, scaledZ + 8.0f).uv((scaledX + (float)n3 + 0.5f) * lowpFracAccur + adjustedCloudX, (scaledZ + 8.0f) * lowpFracAccur + adjustedCloudZ).color(redEW, greenEW, blueEW, 0.8f).normal(1.0f, 0.0f, 0.0f).endVertex();
                            bufferBuilder.vertex(scaledX + (float)n3 + 1.0f - mediumpFracAccur, adjustedCloudY + cloudThickness, scaledZ + 8.0f).uv((scaledX + (float)n3 + 0.5f) * lowpFracAccur + adjustedCloudX, (scaledZ + 8.0f) * lowpFracAccur + adjustedCloudZ).color(redEW, greenEW, blueEW, 0.8f).normal(1.0f, 0.0f, 0.0f).endVertex();
                            bufferBuilder.vertex(scaledX + (float)n3 + 1.0f - mediumpFracAccur, adjustedCloudY + cloudThickness, scaledZ + 0.0f).uv((scaledX + (float)n3 + 0.5f) * lowpFracAccur + adjustedCloudX, (scaledZ + 0.0f) * lowpFracAccur + adjustedCloudZ).color(redEW, greenEW, blueEW, 0.8f).normal(1.0f, 0.0f, 0.0f).endVertex();
                            bufferBuilder.vertex(scaledX + (float)n3 + 1.0f - mediumpFracAccur, adjustedCloudY + 0.0f, scaledZ + 0.0f).uv((scaledX + (float)n3 + 0.5f) * lowpFracAccur + adjustedCloudX, (scaledZ + 0.0f) * lowpFracAccur + adjustedCloudZ).color(redEW, greenEW, blueEW, 0.8f).normal(1.0f, 0.0f, 0.0f).endVertex();
                        }
                    }
                    if (z > -1) {
                        for (n3 = 0; n3 < 8; ++n3) {
                            bufferBuilder.vertex(scaledX + 0.0f, adjustedCloudY + cloudThickness, scaledZ + (float)n3 + 0.0f).uv((scaledX + 0.0f) * lowpFracAccur + adjustedCloudX, (scaledZ + (float)n3 + 0.5f) * lowpFracAccur + adjustedCloudZ).color(redNS, greenNS, blueNS, 0.8f).normal(0.0f, 0.0f, -1.0f).endVertex();
                            bufferBuilder.vertex(scaledX + 8.0f, adjustedCloudY + cloudThickness, scaledZ + (float)n3 + 0.0f).uv((scaledX + 8.0f) * lowpFracAccur + adjustedCloudX, (scaledZ + (float)n3 + 0.5f) * lowpFracAccur + adjustedCloudZ).color(redNS, greenNS, blueNS, 0.8f).normal(0.0f, 0.0f, -1.0f).endVertex();
                            bufferBuilder.vertex(scaledX + 8.0f, adjustedCloudY + 0.0f, scaledZ + (float)n3 + 0.0f).uv((scaledX + 8.0f) * lowpFracAccur + adjustedCloudX, (scaledZ + (float)n3 + 0.5f) * lowpFracAccur + adjustedCloudZ).color(redNS, greenNS, blueNS, 0.8f).normal(0.0f, 0.0f, -1.0f).endVertex();
                            bufferBuilder.vertex(scaledX + 0.0f, adjustedCloudY + 0.0f, scaledZ + (float)n3 + 0.0f).uv((scaledX + 0.0f) * lowpFracAccur + adjustedCloudX, (scaledZ + (float)n3 + 0.5f) * lowpFracAccur + adjustedCloudZ).color(redNS, greenNS, blueNS, 0.8f).normal(0.0f, 0.0f, -1.0f).endVertex();
                        }
                    }
                    if (z > 1) continue;
                    for (n3 = 0; n3 < 8; ++n3) {
                        bufferBuilder.vertex(scaledX + 0.0f, adjustedCloudY + cloudThickness, scaledZ + (float)n3 + 1.0f - mediumpFracAccur).uv((scaledX + 0.0f) * lowpFracAccur + adjustedCloudX, (scaledZ + (float)n3 + 0.5f) * lowpFracAccur + adjustedCloudZ).color(redNS, greenNS, blueNS, 0.8f).normal(0.0f, 0.0f, 1.0f).endVertex();
                        bufferBuilder.vertex(scaledX + 8.0f, adjustedCloudY + cloudThickness, scaledZ + (float)n3 + 1.0f - mediumpFracAccur).uv((scaledX + 8.0f) * lowpFracAccur + adjustedCloudX, (scaledZ + (float)n3 + 0.5f) * lowpFracAccur + adjustedCloudZ).color(redNS, greenNS, blueNS, 0.8f).normal(0.0f, 0.0f, 1.0f).endVertex();
                        bufferBuilder.vertex(scaledX + 8.0f, adjustedCloudY + 0.0f, scaledZ + (float)n3 + 1.0f - mediumpFracAccur).uv((scaledX + 8.0f) * lowpFracAccur + adjustedCloudX, (scaledZ + (float)n3 + 0.5f) * lowpFracAccur + adjustedCloudZ).color(redNS, greenNS, blueNS, 0.8f).normal(0.0f, 0.0f, 1.0f).endVertex();
                        bufferBuilder.vertex(scaledX + 0.0f, adjustedCloudY + 0.0f, scaledZ + (float)n3 + 1.0f - mediumpFracAccur).uv((scaledX + 0.0f) * lowpFracAccur + adjustedCloudX, (scaledZ + (float)n3 + 0.5f) * lowpFracAccur + adjustedCloudZ).color(redNS, greenNS, blueNS, 0.8f).normal(0.0f, 0.0f, 1.0f).endVertex();
                    }
                }
            }
        } else {
            int scaled32Chunks = (int) (32 / scale);
            for (int x = -scaled32Chunks; x < scaled32Chunks; x += scaled32Chunks) {
                for (int z = -scaled32Chunks; z < scaled32Chunks; z += scaled32Chunks) {
                    bufferBuilder.vertex(x, adjustedCloudY, z + scaled32Chunks).uv((float)x * lowpFracAccur + adjustedCloudX, (float)(z + scaled32Chunks) * lowpFracAccur + adjustedCloudZ).color(redTop, greenTop, blueTop, 0.8f).normal(0.0f, -1.0f, 0.0f).endVertex();
                    bufferBuilder.vertex(x + scaled32Chunks, adjustedCloudY, z + scaled32Chunks).uv((float)(x + scaled32Chunks) * lowpFracAccur + adjustedCloudX, (float)(z + scaled32Chunks) * lowpFracAccur + adjustedCloudZ).color(redTop, greenTop, blueTop, 0.8f).normal(0.0f, -1.0f, 0.0f).endVertex();
                    bufferBuilder.vertex(x + scaled32Chunks, adjustedCloudY, z).uv((float)(x + scaled32Chunks) * lowpFracAccur + adjustedCloudX, (float)z * lowpFracAccur + adjustedCloudZ).color(redTop, greenTop, blueTop, 0.8f).normal(0.0f, -1.0f, 0.0f).endVertex();
                    bufferBuilder.vertex(x, adjustedCloudY, z).uv((float)x * lowpFracAccur + adjustedCloudX, (float)z * lowpFracAccur + adjustedCloudZ).color(redTop, greenTop, blueTop, 0.8f).normal(0.0f, -1.0f, 0.0f).endVertex();
                }
            }
        }
    }
}
