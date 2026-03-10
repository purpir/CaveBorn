package ru.purpir.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.BreezeEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.purpir.util.PetMobUtil;
import ru.purpir.util.TargetFinder;

import java.util.Optional;
import java.util.UUID;

@Mixin(BreezeEntity.class)
public class BreezeEntityMixin {
    
    /**
     * Переопределяем canTarget для вихрей-питомцев
     */
    @Inject(method = "canTarget(Lnet/minecraft/entity/EntityType;)Z", at = @At("RETURN"), cancellable = true)
    private void modifyTargeting(EntityType<?> type, CallbackInfoReturnable<Boolean> cir) {
        BreezeEntity self = (BreezeEntity) (Object) this;
        
        // Проверяем, является ли вихрь питомцем
        Optional<UUID> ownerUuid = PetMobUtil.getOwner(self);
        if (ownerUuid.isEmpty()) {
            return;
        }
        
        // Для питомцев разрешаем атаковать всех кроме владельца
        if (type == EntityType.PLAYER) {
            Brain<BreezeEntity> brain = self.getBrain();
            Optional<LivingEntity> attackTarget = brain.getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET);
            if (attackTarget.isPresent() && attackTarget.get() instanceof PlayerEntity player) {
                if (PetMobUtil.isPetOf(self, player)) {
                    cir.setReturnValue(false);
                    return;
                }
            }
        }
        
        // Проверяем, не является ли цель другим питомцем того же владельца
        Brain<BreezeEntity> brain = self.getBrain();
        Optional<LivingEntity> attackTarget = brain.getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET);
        if (attackTarget.isPresent()) {
            LivingEntity target = attackTarget.get();
            Optional<UUID> targetOwnerUuid = PetMobUtil.getOwner(target);
            if (targetOwnerUuid.isPresent() && targetOwnerUuid.get().equals(ownerUuid.get())) {
                // Цель - питомец того же владельца
                cir.setReturnValue(false);
                return;
            }
        }
        
        // Разрешаем атаковать всех остальных
        cir.setReturnValue(true);
    }
    
    /**
     * Управляем поведением через mobTick
     */
    @Inject(method = "mobTick", at = @At("HEAD"))
    private void managePetBehavior(ServerWorld world, CallbackInfo ci) {
        BreezeEntity self = (BreezeEntity) (Object) this;
        
        Optional<UUID> ownerUuid = PetMobUtil.getOwner(self);
        if (ownerUuid.isEmpty()) {
            return;
        }
        
        PlayerEntity owner = world.getPlayerByUuid(ownerUuid.get());
        if (owner == null) {
            return;
        }
        
        Brain<BreezeEntity> brain = self.getBrain();
        
        // Сбрасываем цель если это владелец
        Optional<LivingEntity> currentTarget = brain.getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET);
        if (currentTarget.isPresent() && currentTarget.get() == owner) {
            brain.forget(MemoryModuleType.ATTACK_TARGET);
        }
        
        // Каждые 20 тиков ищем цель
        if (world.getTime() % 20 != 0) {
            return;
        }
        
        // Если владелец атакует кого-то
        LivingEntity ownerTarget = owner.getAttacking();
        if (ownerTarget != null && ownerTarget.isAlive()) {
            brain.remember(MemoryModuleType.ATTACK_TARGET, ownerTarget);
            brain.remember(MemoryModuleType.ANGRY_AT, ownerTarget.getUuid());
            return;
        }
        
        // Ищем ближайшего враждебного моба используя TargetFinder
        if (currentTarget.isEmpty() || !currentTarget.get().isAlive()) {
            Optional<HostileEntity> nearestHostile = TargetFinder.findNearestEntityOfType(
                world,
                self,
                HostileEntity.class,
                20.0
            );
            
            // Проверяем, что найденный моб не является питомцем того же владельца
            if (nearestHostile.isPresent()) {
                HostileEntity hostile = nearestHostile.get();
                
                // Проверяем, не является ли цель питомцем того же владельца
                Optional<UUID> targetOwnerUuid = PetMobUtil.getOwner(hostile);
                if (targetOwnerUuid.isPresent() && targetOwnerUuid.get().equals(ownerUuid.get())) {
                    // Это питомец того же владельца, не атакуем
                    return;
                }
                
                brain.remember(MemoryModuleType.ATTACK_TARGET, hostile);
                brain.remember(MemoryModuleType.ANGRY_AT, hostile.getUuid());
            }
        }
    }
}
