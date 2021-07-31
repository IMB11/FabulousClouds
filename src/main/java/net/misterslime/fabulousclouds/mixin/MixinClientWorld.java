package net.misterslime.fabulousclouds.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import net.misterslime.fabulousclouds.FabulousClouds;
import net.misterslime.fabulousclouds.config.FabulousCloudsConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.class)
public class MixinClientWorld {

    @Inject(method = "getCloudsColor(F)Lnet/minecraft/util/math/Vec3d;", at = @At("RETURN"), cancellable = true)
    private void setCloudColor(CallbackInfoReturnable<Vec3d> cir) {
        Vec3d cloudColor = cir.getReturnValue();
        CameraSubmersionType cameraSubmersionType = MinecraftClient.getInstance().gameRenderer.getCamera().getSubmersionType();
        FabulousCloudsConfig config = FabulousClouds.getConfig();

        if (cameraSubmersionType == CameraSubmersionType.NONE && config.vibrant_clouds) {
            float[] fogColor = RenderSystem.getShaderFogColor();

            double[] tint = new double[3];
            tint[0] = (fogColor[0] + cloudColor.x) / 2;
            tint[1] = (fogColor[1] + cloudColor.y) / 2;
            tint[2] = (fogColor[2] + cloudColor.z) / 2;

            cir.setReturnValue(new Vec3d(tint[0], tint[1], tint[2]));
        }
    }
}
