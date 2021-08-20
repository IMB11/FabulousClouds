package net.misterslime.fabulousclouds.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.misterslime.fabulousclouds.FabulousClouds;
import net.misterslime.fabulousclouds.client.CloudTexture;
import net.misterslime.fabulousclouds.client.NoiseCloudHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugScreenOverlay.class)
public class MixinDebugScreenOverlay {

    private static final ResourceLocation CLOUDS_LOCATION = new ResourceLocation("textures/environment/clouds.png");

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "render", at = @At("TAIL"))
    private void renderCloudTex(PoseStack poseStack, CallbackInfo ci) {
        if (FabulousClouds.getConfig().debug_noise_clouds) {
            int color = 255 << 24 | 255 << 16 | 255 << 8 | 255;
            int pixels = 0;
            for (CloudTexture cloudTexture : NoiseCloudHandler.cloudTextures) {
                pixels += cloudTexture.pixels.size();
            }
            GuiComponent.drawString(poseStack, this.minecraft.font, "Fading Pixels: " + pixels, this.minecraft.getWindow().getGuiScaledWidth() - 128, this.minecraft.getWindow().getGuiScaledHeight() - 140, color);

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, CLOUDS_LOCATION);
            GuiComponent.blit(poseStack, this.minecraft.getWindow().getGuiScaledWidth() - 128, this.minecraft.getWindow().getGuiScaledHeight() - 128, 0.0F, 0.0F, 128, 128, 128, 128);
        }
    }
}
