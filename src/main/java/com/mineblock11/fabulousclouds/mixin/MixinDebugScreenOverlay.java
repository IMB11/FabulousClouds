package com.mineblock11.fabulousclouds.mixin;

import com.mineblock11.fabulousclouds.client.CloudTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import com.mineblock11.fabulousclouds.FabulousClouds;
import com.mineblock11.fabulousclouds.client.NoiseCloudHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugHud.class)
public class MixinDebugScreenOverlay {

    private static final Identifier CLOUDS_LOCATION = new Identifier("textures/environment/clouds.png");

    @Shadow
    @Final
    private MinecraftClient minecraft;

    @Inject(method = "render", at = @At("TAIL"))
    private void renderCloudTex(MatrixStack poseStack, CallbackInfo ci) {
        if (FabulousClouds.getConfig().debug_noise_clouds) {
            int color = 255 << 24 | 255 << 16 | 255 << 8 | 255;
            int pixels = 0;
            for (CloudTexture cloudTexture : NoiseCloudHandler.cloudTextures) {
                pixels += cloudTexture.pixels.size();
            }
            DrawableHelper.drawStringWithShadow(poseStack, this.minecraft.textRenderer, "Fading Pixels: " + pixels, this.minecraft.getWindow().getScaledWidth() - 128, this.minecraft.getWindow().getScaledHeight() - 140, color);

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, CLOUDS_LOCATION);
            DrawableHelper.drawTexture(poseStack, this.minecraft.getWindow().getScaledWidth() - 128, this.minecraft.getWindow().getScaledHeight() - 128, 0.0F, 0.0F, 128, 128, 128, 128);
        }
    }
}
