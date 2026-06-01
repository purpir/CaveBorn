package ru.purpir.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.purpir.event.SolarTotemHandler;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Unique
    private boolean caveborn$hadSolarTotem;

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void caveborn$cancelSolarTotemDamage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof PlayerEntity player && SolarTotemHandler.isImmortal(player, world)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "tryUseDeathProtector", at = @At("HEAD"))
    private void caveborn$rememberSolarTotem(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        this.caveborn$hadSolarTotem = SolarTotemHandler.hasSolarTotem((LivingEntity) (Object) this);
    }

    @Inject(method = "tryUseDeathProtector", at = @At("RETURN"))
    private void caveborn$applySolarTotem(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && this.caveborn$hadSolarTotem) {
            SolarTotemHandler.onSolarTotemUsed((LivingEntity) (Object) this);
        }
        this.caveborn$hadSolarTotem = false;
    }
}
