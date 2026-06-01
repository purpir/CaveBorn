package ru.purpir.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.purpir.enchantment.SolarInfusionSystem;

@Mixin(TridentEntity.class)
public abstract class TridentEntityMixin {

    @Shadow public int returnTimer;
    @Shadow public abstract ItemStack getWeaponStack();

    @Inject(method = "onEntityHit", at = @At("TAIL"))
    private void caveborn$applySolarTridentHit(EntityHitResult hitResult, CallbackInfo ci) {
        TridentEntity trident = (TridentEntity) (Object) this;
        ItemStack stack = getWeaponStack();
        if (!stack.isOf(Items.TRIDENT) || !SolarInfusionSystem.isInfused(stack) ||
                !(trident.getEntityWorld() instanceof ServerWorld serverWorld) ||
                !(hitResult.getEntity() instanceof LivingEntity target)) {
            return;
        }

        if (caveborn$isAquatic(target)) {
            target.damage(serverWorld, serverWorld.getDamageSources().trident(trident, trident.getOwner()), 3.0F);
        }

        if (target.isTouchingWaterOrRain()) {
            caveborn$steamBurst(serverWorld, trident, target);
        } else {
            target.setOnFireFor(5.0F);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void caveborn$boostSolarLoyaltyReturn(CallbackInfo ci) {
        TridentEntity trident = (TridentEntity) (Object) this;
        if (returnTimer <= 0 || !SolarInfusionSystem.isInfused(getWeaponStack())) {
            return;
        }

        Entity owner = trident.getOwner();
        if (owner == null) {
            return;
        }

        Vec3d pull = owner.getEyePos().subtract(caveborn$pos(trident));
        if (pull.lengthSquared() > 0.0001D) {
            trident.setVelocity(trident.getVelocity().add(pull.normalize().multiply(0.08D)));
        }
    }

    @Unique
    private static void caveborn$steamBurst(ServerWorld world, TridentEntity trident, LivingEntity target) {
        Vec3d targetPos = caveborn$pos(target);
        Vec3d center = targetPos.add(0.0D, target.getHeight() * 0.5D, 0.0D);
        world.spawnParticles(ParticleTypes.CLOUD, center.x, center.y, center.z, 36, 1.0D, 0.6D, 1.0D, 0.08D);

        Box box = Box.of(center, 5.0D, 3.0D, 5.0D);
        for (Entity entity : world.getOtherEntities(trident, box, entity -> entity instanceof LivingEntity)) {
            if (!(entity instanceof LivingEntity living)) {
                continue;
            }

            Vec3d livingPos = caveborn$pos(living);
            double distance = livingPos.distanceTo(targetPos);
            if (distance > 2.5D) {
                continue;
            }

            if (living != target) {
                living.damage(world, world.getDamageSources().trident(trident, trident.getOwner()), 4.0F);
            }

            Vec3d knockback = livingPos.subtract(targetPos);
            if (knockback.lengthSquared() < 0.001D) {
                knockback = trident.getVelocity();
            }

            if (knockback.lengthSquared() > 0.001D) {
                living.addVelocity(knockback.normalize().multiply(0.65D).add(0.0D, 0.25D, 0.0D));
                living.velocityModified = true;
            }
        }
    }

    @Unique
    private static boolean caveborn$isAquatic(LivingEntity entity) {
        EntityType<?> type = entity.getType();
        return type == EntityType.DROWNED ||
            type == EntityType.GUARDIAN ||
            type == EntityType.ELDER_GUARDIAN ||
            type == EntityType.SQUID ||
            type == EntityType.GLOW_SQUID ||
            type == EntityType.DOLPHIN ||
            type == EntityType.TURTLE ||
            type == EntityType.AXOLOTL ||
            type == EntityType.COD ||
            type == EntityType.SALMON ||
            type == EntityType.PUFFERFISH ||
            type == EntityType.TROPICAL_FISH;
    }

    @Unique
    private static Vec3d caveborn$pos(Entity entity) {
        return new Vec3d(entity.getX(), entity.getY(), entity.getZ());
    }
}
