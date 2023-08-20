package com.mineblock11.fabulousclouds.mixin;

import com.mineblock11.fabulousclouds.config.FabulousCloudsConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import com.mineblock11.fabulousclouds.FabulousClouds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.class)
public class MixinClientLevel {

    @Inject(method = "getCloudColor(F)Lnet/minecraft/world/phys/Vec3;", at = @At("RETURN"), cancellable = true)
    private void setCloudColor(float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
        FabulousCloudsConfig config = FabulousClouds.getConfig();

        if (config.vibrant_clouds) {
            Vec3d cloudColor = cir.getReturnValue();
            float[] fogColor = RenderSystem.getShaderFogColor();
            float vibranceIntensity = config.vibrance_intensity;

            double[] tint = new double[3];
            tint[0] = ((fogColor[0] * vibranceIntensity) + cloudColor.x) / 2;
            tint[1] = ((fogColor[1] * vibranceIntensity) + cloudColor.y) / 2;
            tint[2] = ((fogColor[2] * vibranceIntensity) + cloudColor.z) / 2;

            cir.setReturnValue(new Vec3d(tint[0], tint[1], tint[2]));
        }
    }
}
