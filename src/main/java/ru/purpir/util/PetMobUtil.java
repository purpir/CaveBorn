package ru.purpir.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.*;

/**
 * Утилита для управления мобами-питомцами игрока
 */
public class PetMobUtil {
    
    // Хранилище питомцев: UUID игрока -> список UUID питомцев
    private static final Map<UUID, Set<UUID>> playerPets = new HashMap<>();
    
    // Хранилище времени смерти: UUID питомца -> время смерти
    private static final Map<UUID, Long> petDeathTimes = new HashMap<>();
    
    /**
     * Регистрирует моба как питомца игрока
     */
    public static void registerPet(PlayerEntity owner, MobEntity pet) {
        UUID ownerUuid = owner.getUuid();
        UUID petUuid = pet.getUuid();
        
        playerPets.computeIfAbsent(ownerUuid, k -> new HashSet<>()).add(petUuid);
    }
    
    /**
     * Регистрирует моба как временного питомца с автоматической смертью
     */
    public static void registerTemporaryPet(PlayerEntity owner, MobEntity pet, long durationTicks, ServerWorld world) {
        registerPet(owner, pet);
        long deathTime = world.getTime() + durationTicks;
        petDeathTimes.put(pet.getUuid(), deathTime);
    }
    
    /**
     * Удаляет моба из списка питомцев
     */
    public static void unregisterPet(UUID ownerUuid, UUID petUuid) {
        Set<UUID> pets = playerPets.get(ownerUuid);
        if (pets != null) {
            pets.remove(petUuid);
            if (pets.isEmpty()) {
                playerPets.remove(ownerUuid);
            }
        }
        petDeathTimes.remove(petUuid);
    }
    
    /**
     * Проверяет, является ли моб питомцем игрока
     */
    public static boolean isPetOf(LivingEntity mob, PlayerEntity player) {
        Set<UUID> pets = playerPets.get(player.getUuid());
        return pets != null && pets.contains(mob.getUuid());
    }
    
    /**
     * Получает владельца питомца
     */
    public static Optional<UUID> getOwner(LivingEntity mob) {
        UUID mobUuid = mob.getUuid();
        for (Map.Entry<UUID, Set<UUID>> entry : playerPets.entrySet()) {
            if (entry.getValue().contains(mobUuid)) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }
    
    /**
     * Получает всех питомцев игрока
     */
    public static Set<UUID> getPets(PlayerEntity player) {
        return playerPets.getOrDefault(player.getUuid(), Collections.emptySet());
    }
    
    /**
     * Устанавливает цель для питомца (атаковать того, кого атакует владелец)
     */
    public static void setTargetFromOwner(MobEntity pet, LivingEntity target) {
        if (pet.getTarget() != target) {
            pet.setTarget(target);
        }
    }
    
    /**
     * Обновляет временных питомцев - убивает тех, у кого истекло время
     */
    public static void tickTemporaryPets(ServerWorld world) {
        long currentTime = world.getTime();
        List<UUID> toRemove = new ArrayList<>();
        
        for (Map.Entry<UUID, Long> entry : petDeathTimes.entrySet()) {
            if (currentTime >= entry.getValue()) {
                UUID petUuid = entry.getKey();
                
                // Находим и убиваем питомца - используем большой радиус поиска
                double searchRadius = 1000.0;
                List<MobEntity> entities = world.getEntitiesByClass(
                    MobEntity.class,
                    new net.minecraft.util.math.Box(
                        -searchRadius, world.getBottomY(), -searchRadius,
                        searchRadius, 320, searchRadius
                    ),
                    entity -> entity.getUuid().equals(petUuid)
                );
                
                for (MobEntity entity : entities) {
                    entity.kill(world);
                }
                
                toRemove.add(petUuid);
            }
        }
        
        // Удаляем из всех списков
        for (UUID petUuid : toRemove) {
            petDeathTimes.remove(petUuid);
            for (Set<UUID> pets : playerPets.values()) {
                pets.remove(petUuid);
            }
        }
    }
    
    /**
     * Очищает всех питомцев игрока
     */
    public static void clearPets(PlayerEntity player) {
        Set<UUID> pets = playerPets.remove(player.getUuid());
        if (pets != null) {
            for (UUID petUuid : pets) {
                petDeathTimes.remove(petUuid);
            }
        }
    }
}
