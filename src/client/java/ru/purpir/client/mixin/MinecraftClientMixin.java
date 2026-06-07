package ru.purpir.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.purpir.client.SolarZincKnifeClientCombo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow
    public ClientPlayerEntity player;

    @Shadow
    public HitResult crosshairTarget;

    @Redirect(
        method = "doAttack",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;swingHand(Lnet/minecraft/util/Hand;)V"
        )
    )
    private void caveborn$useOffhandSwingForSecondZincKnifeHit(ClientPlayerEntity player, Hand hand) {
        if (this.player == player && SolarZincKnifeClientCombo.shouldSwingOffhand(player, this.crosshairTarget)) {
            player.swingHand(Hand.OFF_HAND);
            return;
        }

        player.swingHand(hand);
    }
}
