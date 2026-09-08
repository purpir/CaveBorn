package ru.purpir.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.purpir.event.SolarBronzeAxeHandler;
import ru.purpir.minecart.MinecartTransportHandler;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "handleFallDamage", at = @At("HEAD"), cancellable = true)
    private void caveborn$cancelBronzeAxeDoubleJumpFallDamage(double fallDistance, float damagePerDistance, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ServerPlayerEntity player && SolarBronzeAxeHandler.consumeFallDamageProtection(player)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "interactAt", at = @At("HEAD"), cancellable = true)
    private void caveborn$chainMinecartInteract(PlayerEntity player, Vec3d hitPos, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (!MinecartTransportHandler.ENABLED) {
            return;
        }

        if (!((Object) this instanceof AbstractMinecartEntity cart)) {
            return;
        }

        ItemStack stack = player.getStackInHand(hand);
        if (!stack.isOf(Items.IRON_CHAIN)) {
            return;
        }

        ActionResult result = MinecartTransportHandler.handleEntityInteract(cart, player, hand, hitPos);
        if (result != ActionResult.PASS) {
            cir.setReturnValue(result);
        }
    }
}
