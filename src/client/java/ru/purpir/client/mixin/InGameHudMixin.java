package ru.purpir.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.purpir.minecart.MinecartTransportHandler;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Shadow
    private MinecraftClient client;

    @Inject(method = "shouldShowExperienceBar", at = @At("HEAD"), cancellable = true)
    private void caveborn$hideExperienceBarForMinecarts(CallbackInfoReturnable<Boolean> cir) {
        if (!MinecartTransportHandler.ENABLED) {
            return;
        }

        if (client.player != null && client.player.getVehicle() instanceof AbstractMinecartEntity) {
            cir.setReturnValue(false);
        }
    }
}
