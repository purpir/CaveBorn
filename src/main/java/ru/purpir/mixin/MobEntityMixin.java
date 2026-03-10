package ru.purpir.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.purpir.util.PetMobUtil;

import java.util.Optional;
import java.util.UUID;

@Mixin(MobEntity.class)
public class MobEntityMixin {
    
    /**
     * Предотвращает атаку питомца на своего владельца
     */
    @Inject(method = "canTarget(Lnet/minecraft/entity/EntityType;)Z", at = @At("HEAD"), cancellable = true)
    private void preventAttackingOwner(EntityType<?> targetType, CallbackInfoReturnable<Boolean> cir) {
        MobEntity self = (MobEntity) (Object) this;
        
        // Проверяем текущую цель моба
        LivingEntity currentTarget = self.getTarget();
        if (currentTarget instanceof PlayerEntity player) {
            // Проверяем, является ли этот моб питомцем игрока
            if (PetMobUtil.isPetOf(self, player)) {
                cir.setReturnValue(false);
            }
        }
    }
    
    /**
     * Предотвращает установку владельца как цели
     */
    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void preventTargetingOwner(LivingEntity target, CallbackInfo ci) {
        if (target instanceof PlayerEntity player) {
            MobEntity self = (MobEntity) (Object) this;
            if (PetMobUtil.isPetOf(self, player)) {
                ci.cancel();
            }
        }
    }
    
    /**
     * Синхронизирует цель питомца с целью владельца и заставляет атаковать враждебных мобов
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void syncTargetWithOwner(CallbackInfo ci) {
        MobEntity self = (MobEntity) (Object) this;
        
        // Получаем владельца
        Optional<UUID> ownerUuid = PetMobUtil.getOwner(self);
        if (ownerUuid.isEmpty()) {
            return;
        }
        
        // Находим владельца в мире (только на сервере)
        if (!(self.getEntityWorld() instanceof ServerWorld serverWorld)) {
            return;
        }
        
        PlayerEntity owner = serverWorld.getPlayerByUuid(ownerUuid.get());
        if (owner == null) {
            return;
        }
        
        // Если питомец пытается атаковать владельца - сбрасываем цель
        if (self.getTarget() == owner) {
            self.setTarget(null);
        }
        
        // Проверяем каждые 10 тиков (0.5 секунды)
        if (serverWorld.getTime() % 10 != 0) {
            return;
        }
        
        // Если владелец атакует кого-то, питомец тоже атакует
        LivingEntity ownerTarget = owner.getAttacking();
        if (ownerTarget != null && ownerTarget.isAlive() && ownerTarget != owner) {
            self.setTarget(ownerTarget);
            return;
        }
        
        // Если у питомца нет цели, ищем ближайшего враждебного моба
        if (self.getTarget() == null || !self.getTarget().isAlive()) {
            net.minecraft.entity.ai.TargetPredicate targetPredicate = net.minecraft.entity.ai.TargetPredicate.createAttackable()
                .setBaseMaxDistance(20.0);
            
            LivingEntity nearestHostile = serverWorld.getClosestEntity(
                serverWorld.getEntitiesByClass(
                    HostileEntity.class,
                    self.getBoundingBox().expand(20.0),
                    entity -> entity.isAlive()
                ),
                targetPredicate,
                self,
                self.getX(), self.getY(), self.getZ()
            );
            
            if (nearestHostile != null) {
                self.setTarget(nearestHostile);
            }
        }
    }
}
