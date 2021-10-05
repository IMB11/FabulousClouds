package net.misterslime.fabulousclouds.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;

import net.misterslime.fabulousclouds.FabulousClouds;
import net.misterslime.fabulousclouds.config.FabulousCloudsConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public class MixinClientLevel {

    @Inject(method = "getCloudColor(F)Lnet/minecraft/world/phys/Vec3;", at = @At("RETURN"), cancellable = true)
    private void setCloudColor(float tickDelta, CallbackInfoReturnable<Vec3> cir) {
        FabulousCloudsConfig config = FabulousClouds.getConfig();

        if (config.vibrant_clouds) {
            Vec3 cloudColor = cir.getReturnValue();
            float vibranceIntensity = config.vibrance_intensity;

            double[] tint = new double[3];
            tint[0] = ((AccessorFogRenderer.getFogRed() * vibranceIntensity) + cloudColor.x) / 2;
            tint[1] = ((AccessorFogRenderer.getFogGreen() * vibranceIntensity) + cloudColor.y) / 2;
            tint[2] = ((AccessorFogRenderer.getFogBlue() * vibranceIntensity) + cloudColor.z) / 2;

            cir.setReturnValue(new Vec3(tint[0], tint[1], tint[2]));
        }
    }
}
