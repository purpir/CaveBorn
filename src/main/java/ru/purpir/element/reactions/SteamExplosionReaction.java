package ru.purpir.element.reactions;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import ru.purpir.element.elements.FireElement;
import ru.purpir.element.elements.IceElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SteamExplosionReaction extends Reaction {
    public static final SteamExplosionReaction INSTANCE = new SteamExplosionReaction();
    
    private static final float EXPLOSION_RADIUS = 4.0F;
    private static final float HP_DAMAGE_PERCENT = 0.1F; // 10% от текущего HP
    private static final float KNOCKBACK_STRENGTH = 1.5F;
    private static final int BLINDNESS_DURATION = 60; // 3 секунды
    private static final int REACTION_INTERVAL_TICKS = 300; // 15 секунд
    
    private final Map<UUID, Integer> entityTimers = new HashMap<>();

    private SteamExplosionReaction() {
        super("steam_explosion", Text.translatable("reaction.caveborn.steam_explosion"),
            List.of(FireElement.INSTANCE, IceElement.INSTANCE));
    }

    @Override
    public void onActivate(Entity entity) {
        // Немедленно запускаем взрыв при активации
        triggerExplosion(entity);
        // Устанавливаем таймер для следующего взрыва
        entityTimers.put(entity.getUuid(), REACTION_INTERVAL_TICKS);
    }

    @Override
    public void onDeactivate(Entity entity) {
        // Убираем таймер когда реакция деактивируется
        entityTimers.remove(entity.getUuid());
    }
    
    public void tick(Entity entity) {
        UUID uuid = entity.getUuid();
        Integer timer = entityTimers.get(uuid);
        
        if (timer == null) {
            return;
        }
        
        if (timer <= 0) {
            // Время для взрыва!
            triggerExplosion(entity);
            entityTimers.put(uuid, REACTION_INTERVAL_TICKS);
        } else {
            // Уменьшаем таймер
            entityTimers.put(uuid, timer - 1);
        }
    }
    
    private void triggerExplosion(Entity entity) {
        if (!(entity.getEntityWorld() instanceof ServerWorld world)) {
            return;
        }
        
        // Урон и слепота самой сущности с эффектом (10% от текущего HP)
        if (entity instanceof LivingEntity living) {
            float currentHealth = living.getHealth();
            float damage = currentHealth * HP_DAMAGE_PERCENT;
            living.damage(world, world.getDamageSources().magic(), damage);
            
            // Ослепление сущности с эффектом
            living.addStatusEffect(new StatusEffectInstance(
                StatusEffects.BLINDNESS,
                BLINDNESS_DURATION,
                0,
                false,
                true
            ));
        }
        
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        
        // Звук шипящего пара
        world.playSound(null, x, y, z, 
            SoundEvents.BLOCK_FIRE_EXTINGUISH, 
            SoundCategory.HOSTILE, 
            2.0F, 
            0.5F + world.random.nextFloat() * 0.2F);

        // Партиклы пара
        for (int i = 0; i < 100; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * EXPLOSION_RADIUS;
            double offsetY = world.random.nextDouble() * EXPLOSION_RADIUS;
            double offsetZ = (world.random.nextDouble() - 0.5) * EXPLOSION_RADIUS;
            
            world.spawnParticles(
                ParticleTypes.CLOUD,
                x + offsetX,
                y + offsetY,
                z + offsetZ,
                1,
                0.0, 0.2, 0.0,
                0.05
            );
        }

        // Добавляем эффект облака пара
        for (int i = 0; i < 50; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * EXPLOSION_RADIUS * 0.5;
            double offsetY = world.random.nextDouble() * EXPLOSION_RADIUS * 0.5;
            double offsetZ = (world.random.nextDouble() - 0.5) * EXPLOSION_RADIUS * 0.5;
            
            world.spawnParticles(
                ParticleTypes.WHITE_SMOKE,
                x + offsetX,
                y + offsetY,
                z + offsetZ,
                1,
                0.0, 0.1, 0.0,
                0.02
            );
        }

        // Находим всех существ в радиусе
        Box box = Box.of(new Vec3d(x, y, z), EXPLOSION_RADIUS * 2, EXPLOSION_RADIUS * 2, EXPLOSION_RADIUS * 2);
        List<LivingEntity> entities = world.getEntitiesByClass(
            LivingEntity.class, 
            box, 
            e -> e != entity && e.isAlive()
        );

        for (LivingEntity target : entities) {
            double targetX = target.getX();
            double targetY = target.getY();
            double targetZ = target.getZ();
            double distance = Math.sqrt(Math.pow(x - targetX, 2) + Math.pow(y - targetY, 2) + Math.pow(z - targetZ, 2));
            
            if (distance > EXPLOSION_RADIUS) {
                continue;
            }

            // Только отбрасывание окружающих (без урона и слепоты)
            double dirX = targetX - x;
            double dirY = targetY - y;
            double dirZ = targetZ - z;
            double length = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
            if (length > 0) {
                dirX /= length;
                dirY /= length;
                dirZ /= length;
            }
            
            double knockbackMultiplier = 1.0 - (distance / EXPLOSION_RADIUS);
            target.setVelocity(
                dirX * KNOCKBACK_STRENGTH * knockbackMultiplier,
                0.4 * knockbackMultiplier,
                dirZ * KNOCKBACK_STRENGTH * knockbackMultiplier
            );
            target.velocityModified = true;
        }
    }
}
