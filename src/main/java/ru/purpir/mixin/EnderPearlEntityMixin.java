package ru.purpir.mixin;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.purpir.enchantment.SolarInfusionSystem;

@Mixin(EnderPearlEntity.class)
public class EnderPearlEntityMixin {
    @ModifyArg(
        method = "onCollision",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerPlayerEntity;damage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z"
        ),
        index = 2
    )
    private float caveborn$removeSolarPearlDamage(float damage) {
        EnderPearlEntity pearl = (EnderPearlEntity) (Object) this;
        return SolarInfusionSystem.isInfused(((ThrownItemEntity) pearl).getStack()) ? 0.0F : damage;
    }

    @Inject(method = "onCollision", at = @At("TAIL"))
    private void caveborn$solarPearlFlash(HitResult hitResult, CallbackInfo ci) {
        EnderPearlEntity pearl = (EnderPearlEntity) (Object) this;
        if (!SolarInfusionSystem.isInfused(((ThrownItemEntity) pearl).getStack())) {
            return;
        }

        if (!(pearl.getEntityWorld() instanceof ServerWorld world)) {
            return;
        }

        Entity owner = pearl.getOwner();
        Vec3d center = pearl.getLastRenderPos();
        if (owner != null) {
            center = new Vec3d(owner.getX(), owner.getY() + owner.getHeight() * 0.5, owner.getZ());
        }

        world.spawnParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y, center.z, 36, 0.75, 0.45, 0.75, 0.08);
        world.spawnParticles(ParticleTypes.END_ROD, center.x, center.y, center.z, 24, 0.65, 0.35, 0.65, 0.08);
        world.playSound(null, center.x, center.y, center.z, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 0.45F, 1.8F);

        List<LivingEntity> targets = world.getEntitiesByClass(
            LivingEntity.class,
            Box.of(center, 5.0, 3.0, 5.0),
            target -> target.isAlive() && target != owner
        );

        for (LivingEntity target : targets) {
            Vec3d push = new Vec3d(target.getX(), target.getY(), target.getZ()).subtract(center);
            if (push.lengthSquared() < 0.001) {
                push = new Vec3d(0.0, 0.0, 1.0);
            }
            Vec3d velocity = push.normalize().multiply(0.9).add(0.0, 0.25, 0.0);
            target.addVelocity(velocity.x, velocity.y, velocity.z);
            target.velocityModified = true;
        }
    }
}
