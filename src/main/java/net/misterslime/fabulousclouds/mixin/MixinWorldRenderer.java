package net.misterslime.fabulousclouds.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.misterslime.fabulousclouds.FabulousClouds;
import net.misterslime.fabulousclouds.NoiseCloudHandler;
import net.misterslime.fabulousclouds.config.FabulousCloudsConfig;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(WorldRenderer.class)
public final class MixinWorldRenderer {

    private static final int COLOR = 255 << 24 | 255 << 16 | 255 << 8 | 255;

    @Final
    @Shadow
    private static Identifier CLOUDS;
    @Shadow
    @NotNull
    private final ClientWorld world;
    @Shadow
    private final int ticks;
    @Final
    @Shadow
    @NotNull
    private final MinecraftClient client;
    @Shadow
    private int lastCloudsBlockX;
    @Shadow
    private int lastCloudsBlockY;
    @Shadow
    private int lastCloudsBlockZ;
    @Shadow
    @NotNull
    private Vec3d lastCloudsColor;
    @Shadow
    @NotNull
    private CloudRenderMode lastCloudsRenderMode;
    @Shadow
    private boolean cloudsDirty;
    @Shadow
    @NotNull
    private VertexBuffer cloudsBuffer;
    @Unique
    private boolean initializedClouds = false;

    public MixinWorldRenderer() {
        throw new NullPointerException("Null cannot be cast to non-null type.");
    }

    @Redirect(method = "renderClouds(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Matrix4f;FDDD)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/util/Identifier;)V"))
    private void bindFabulousClouds(int i, Identifier id) {
        if (FabulousClouds.getConfig().noise_clouds) {
            TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
            registerCloudsNoise(textureManager);
            NoiseCloudHandler.update();

            RenderSystem._setShaderTexture(i, id);
        }
    }

    @Inject(method = "renderClouds(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Matrix4f;FDDD)V", at = @At("HEAD"), cancellable = true)
    public void renderClouds(MatrixStack matrices, Matrix4f model, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        FabulousCloudsConfig config = FabulousClouds.getConfig();

        if (world.getRegistryKey() == World.OVERWORLD) {
            SkyProperties properties = this.world.getSkyProperties();
            float cloudHeight = properties.getCloudsHeight();
            if (!Float.isNaN(cloudHeight)) {
                for(FabulousCloudsConfig.CloudLayer cloudLayer : config.cloud_layers) {
                    renderCloudLayer(matrices, model, tickDelta, cameraX, cameraY, cameraZ, cloudHeight, cloudLayer.offset, cloudLayer.scale, cloudLayer.speed);
                }
            }

            if (!config.enable_default_cloud_layer) {
                ci.cancel();
            }
        }
    }

    private void registerCloudsNoise(TextureManager textureManager) {
        if (!this.initializedClouds) {
            NativeImage image = new NativeImage(256, 256, false);

            Random random = new Random();
            NoiseCloudHandler.initNoise(random);

            double cloudiness = random.nextDouble();

            for (int x = 0; x < 256; x++) {
                for (int z = 0; z < 256; z++) {
                    if (NoiseCloudHandler.noise.sample(x / 16.0, 0, z / 16.0) * 2.5 < cloudiness || image.getPixelColor(x, z) != 0) {
                        image.setPixelColor(x, z, (int) (NoiseCloudHandler.noise.sample(x / 16.0, 0, z / 16.0) * 2.5));
                    }
                }
            }

            /*int count = random.nextInt(2000) + 2000;

            for (int i = 0; i < count; i++) {
                int x = random.nextInt(256);
                int z = random.nextInt(256);

                image.setPixelColor(x, z, (int) (NoiseCloudHandler.noise.sample(x / 16.0, 0, z / 16.0) * 2.5));
            }

            count /= 4;

            for (int i = 0; i < count; i++) {
                image.setPixelColor(random.nextInt(256), random.nextInt(256), 0);
            }*/

            NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
            textureManager.registerTexture(CLOUDS, texture);
            NoiseCloudHandler.setTexture(texture);
            this.initializedClouds = true;
        }
    }

    private void renderCloudLayer(MatrixStack matrices, Matrix4f model, float tickDelta, double cameraX, double cameraY, double cameraZ, float cloudHeight, float cloudOffset, float cloudScale, float speedMod) {
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(true);
        double speed = ((this.ticks + tickDelta) * (0.03F * speedMod));
        double posX = (cameraX + speed) / 12.0D;
        double posY = cloudHeight - (float) cameraY + cloudOffset;
        double posZ = cameraZ / 12.0D + 0.33000001311302185D;
        posX -= MathHelper.floor(posX / 2048.0D) * 2048;
        posZ -= MathHelper.floor(posZ / 2048.0D) * 2048;
        float adjustedX = (float) (posX - (double) MathHelper.floor(posX));
        float adjustedY = (float) (posY / 4.0D - (double) MathHelper.floor(posY / 4.0D)) * 4.0F;
        float adjustedZ = (float) (posZ - (double) MathHelper.floor(posZ));
        Vec3d cloudColor = this.world.getCloudsColor(tickDelta);
        int floorX = (int) Math.floor(posX);
        int floorY = (int) Math.floor(posY / 4.0D);
        int floorZ = (int) Math.floor(posZ);
        if (floorX != this.lastCloudsBlockX || floorY != this.lastCloudsBlockY || floorZ != this.lastCloudsBlockZ || this.client.options.getCloudRenderMode() != this.lastCloudsRenderMode || this.lastCloudsColor.squaredDistanceTo(cloudColor) > 2.0E-4D) {
            this.lastCloudsBlockX = floorX;
            this.lastCloudsBlockY = floorY;
            this.lastCloudsBlockZ = floorZ;
            this.lastCloudsColor = cloudColor;
            this.lastCloudsRenderMode = this.client.options.getCloudRenderMode();
            this.cloudsDirty = true;
        }

        if (this.cloudsDirty) {
            this.cloudsDirty = false;
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            if (this.cloudsBuffer != null) this.cloudsBuffer.close();

            this.cloudsBuffer = new VertexBuffer();
            this.renderClouds(bufferBuilder, posX, posY, posZ, cloudColor);
            bufferBuilder.end();
            this.cloudsBuffer.upload(bufferBuilder);
        }

        RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
        RenderSystem.setShaderTexture(0, CLOUDS);
        BackgroundRenderer.setFogBlack();
        matrices.push();
        matrices.scale(12.0F, 1.0F, 12.0F);
        matrices.scale(cloudScale, cloudScale, cloudScale);
        matrices.translate(-adjustedX, adjustedY, -adjustedZ);
        if (this.cloudsBuffer != null) {
            int cloudMainIndex = this.lastCloudsRenderMode == CloudRenderMode.FANCY ? 0 : 1;

            for (int cloudIndex = 1; cloudMainIndex <= cloudIndex; ++cloudMainIndex) {
                if (cloudMainIndex == 0) {
                    RenderSystem.colorMask(false, false, false, false);
                } else {
                    RenderSystem.colorMask(true, true, true, true);
                }

                Shader shader = RenderSystem.getShader();
                this.cloudsBuffer.setShader(matrices.peek().getModel(), model, shader);
            }
        }

        matrices.pop();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    @Shadow
    private void renderClouds(BufferBuilder builder, double x, double y, double z, Vec3d color) {
    }
}
