package ru.purpir.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

public class TargetFinder {
    
    /**
     * Находит ближайшую живую сущность в радиусе от игрока
     * @param world Мир
     * @param player Игрок, от которого ищем
     * @param radius Радиус поиска
     * @return Ближайшая сущность или пусто
     */
    public static Optional<LivingEntity> findNearestEntity(World world, PlayerEntity player, double radius) {
        Box searchBox = new Box(
            player.getX() - radius, player.getY() - radius, player.getZ() - radius,
            player.getX() + radius, player.getY() + radius, player.getZ() + radius
        );
        
        List<LivingEntity> entities = world.getEntitiesByClass(
            LivingEntity.class,
            searchBox,
            entity -> entity != player && entity.isAlive()
        );
        
        if (entities.isEmpty()) {
            return Optional.empty();
        }
        
        // Находим ближайшую сущность
        LivingEntity nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (LivingEntity entity : entities) {
            double distance = player.squaredDistanceTo(entity);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = entity;
            }
        }
        
        return Optional.ofNullable(nearest);
    }
    
    /**
     * Находит ближайшую живую сущность определенного класса в радиусе от любой сущности
     * @param world Мир
     * @param source Сущность, от которой ищем
     * @param entityClass Класс искомых сущностей
     * @param radius Радиус поиска
     * @param <T> Тип сущности
     * @return Ближайшая сущность или пусто
     */
    public static <T extends LivingEntity> Optional<T> findNearestEntityOfType(
            World world, 
            Entity source, 
            Class<T> entityClass, 
            double radius) {
        
        Box searchBox = new Box(
            source.getX() - radius, source.getY() - radius, source.getZ() - radius,
            source.getX() + radius, source.getY() + radius, source.getZ() + radius
        );
        
        List<T> entities = world.getEntitiesByClass(
            entityClass,
            searchBox,
            entity -> entity != source && entity.isAlive()
        );
        
        if (entities.isEmpty()) {
            return Optional.empty();
        }
        
        // Находим ближайшую сущность
        T nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (T entity : entities) {
            double distance = source.squaredDistanceTo(entity);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = entity;
            }
        }
        
        return Optional.ofNullable(nearest);
    }
}
