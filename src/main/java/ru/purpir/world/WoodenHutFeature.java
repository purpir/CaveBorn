package ru.purpir.world;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import ru.purpir.Caveborn;

/**
 * Генерирует деревянную будку на полянах
 */
public class WoodenHutFeature extends Feature<DefaultFeatureConfig> {
    
    public WoodenHutFeature(Codec<DefaultFeatureConfig> configCodec) {
        super(configCodec);
    }
    
    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess world = context.getWorld();
        BlockPos origin = context.getOrigin();
        Random random = context.getRandom();
        
        // Проверяем, что это подходящее место (трава)
        BlockPos groundPos = origin.down();
        BlockState groundState = world.getBlockState(groundPos);
        
        if (!groundState.isOf(Blocks.GRASS_BLOCK)) {
            return false;
        }
        
        // Проверяем, что вокруг достаточно места (5x5 область)
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos checkPos = origin.add(x, 0, z);
                if (!world.getBlockState(checkPos).isAir() && !world.getBlockState(checkPos).isReplaceable()) {
                    return false;
                }
                
                BlockPos checkGround = checkPos.down();
                if (!world.getBlockState(checkGround).isOf(Blocks.GRASS_BLOCK)) {
                    return false;
                }
            }
        }
        
        // Строим будку 3x3x3
        buildHut(world, origin, random);
        
        return true;
    }
    
    private void buildHut(StructureWorldAccess world, BlockPos origin, Random random) {
        // Пол из дубовых досок
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                world.setBlockState(origin.add(x, 0, z), Blocks.OAK_PLANKS.getDefaultState(), 3);
            }
        }
        
        // Стены из дубовых бревен (углы) и досок
        // Передняя стена (с дверью)
        world.setBlockState(origin.add(-1, 1, -1), Blocks.OAK_LOG.getDefaultState(), 3);
        world.setBlockState(origin.add(0, 1, -1), Blocks.OAK_DOOR.getDefaultState(), 3);
        world.setBlockState(origin.add(1, 1, -1), Blocks.OAK_LOG.getDefaultState(), 3);
        
        world.setBlockState(origin.add(-1, 2, -1), Blocks.OAK_LOG.getDefaultState(), 3);
        world.setBlockState(origin.add(0, 2, -1), Blocks.OAK_PLANKS.getDefaultState(), 3);
        world.setBlockState(origin.add(1, 2, -1), Blocks.OAK_LOG.getDefaultState(), 3);
        
        // Задняя стена
        world.setBlockState(origin.add(-1, 1, 1), Blocks.OAK_LOG.getDefaultState(), 3);
        world.setBlockState(origin.add(0, 1, 1), Blocks.OAK_PLANKS.getDefaultState(), 3);
        world.setBlockState(origin.add(1, 1, 1), Blocks.OAK_LOG.getDefaultState(), 3);
        
        world.setBlockState(origin.add(-1, 2, 1), Blocks.OAK_LOG.getDefaultState(), 3);
        world.setBlockState(origin.add(0, 2, 1), Blocks.OAK_PLANKS.getDefaultState(), 3);
        world.setBlockState(origin.add(1, 2, 1), Blocks.OAK_LOG.getDefaultState(), 3);
        
        // Левая стена
        world.setBlockState(origin.add(-1, 1, 0), Blocks.OAK_PLANKS.getDefaultState(), 3);
        world.setBlockState(origin.add(-1, 2, 0), Blocks.OAK_PLANKS.getDefaultState(), 3);
        
        // Правая стена (с окном)
        world.setBlockState(origin.add(1, 1, 0), Blocks.GLASS_PANE.getDefaultState(), 3);
        world.setBlockState(origin.add(1, 2, 0), Blocks.OAK_PLANKS.getDefaultState(), 3);
        
        // Крыша из дубовых ступенек
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                world.setBlockState(origin.add(x, 3, z), Blocks.OAK_STAIRS.getDefaultState(), 3);
            }
        }
        
        // Добавляем декор внутри
        // Стол для крафта в углу
        world.setBlockState(origin.add(-1, 1, 1), Blocks.CRAFTING_TABLE.getDefaultState(), 3);
        
        // Сундук с лутом
        world.setBlockState(origin.add(1, 1, 1), Blocks.CHEST.getDefaultState(), 3);
        
        // Заполняем сундук лутом
        BlockEntity blockEntity = world.getBlockEntity(origin.add(1, 1, 1));
        if (blockEntity instanceof ChestBlockEntity chestBlockEntity) {
            RegistryKey<LootTable> lootTableKey = RegistryKey.of(RegistryKeys.LOOT_TABLE, 
                Identifier.of(Caveborn.MOD_ID, "chests/wooden_hut"));
            chestBlockEntity.setLootTable(lootTableKey, random.nextLong());
        }
        
        // Кровать
        if (random.nextBoolean()) {
            world.setBlockState(origin.add(0, 1, 0), Blocks.RED_BED.getDefaultState(), 3);
        }
        
        // Факел для освещения
        world.setBlockState(origin.add(0, 2, 0), Blocks.TORCH.getDefaultState(), 3);
        
        // Небольшой сад вокруг будки
        for (int i = 0; i < 3; i++) {
            int x = random.nextInt(7) - 3;
            int z = random.nextInt(7) - 3;
            
            if (Math.abs(x) > 1 || Math.abs(z) > 1) { // Не на самой будке
                BlockPos flowerPos = origin.add(x, 1, z);
                if (world.getBlockState(flowerPos).isAir() && 
                    world.getBlockState(flowerPos.down()).isOf(Blocks.GRASS_BLOCK)) {
                    
                    // Случайные цветы
                    if (random.nextBoolean()) {
                        world.setBlockState(flowerPos, Blocks.DANDELION.getDefaultState(), 3);
                    } else {
                        world.setBlockState(flowerPos, Blocks.POPPY.getDefaultState(), 3);
                    }
                }
            }
        }
    }
}