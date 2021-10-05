package net.misterslime.fabulousclouds.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.FogRenderer;

@Mixin(FogRenderer.class)
public interface AccessorFogRenderer {
	@Accessor("fogRed")
	static float getFogRed() {
		throw new AssertionError();
	}

	@Accessor("fogGreen")
	static float getFogGreen() {
		throw new AssertionError();
	}

	@Accessor("fogBlue")
	static float getFogBlue() {
		throw new AssertionError();
	}
}
