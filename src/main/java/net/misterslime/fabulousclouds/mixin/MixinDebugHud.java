package net.misterslime.fabulousclouds.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugHud.class)
public class MixinDebugHud {

    private static final Identifier CLOUDS = new Identifier("textures/environment/clouds.png");

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "render", at = @At("TAIL"))
    private void renderCloudTex(MatrixStack matrices, CallbackInfo ci) {
        if (!this.client.options.debugProfilerEnabled) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, CLOUDS);
            DrawableHelper.drawTexture(matrices, this.client.getWindow().getScaledWidth() - 128, this.client.getWindow().getScaledHeight() - 128, 0.0F, 0.0F, 128, 128, 128, 128);
        }
    }
}
