package ru.purpir.enchantment;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class VeinMinerEnchantment {
    private static final int MAX_BLOCKS = 32; // Максимум блоков за раз
    
    public static void onBlockBroken(World world, BlockPos pos, BlockState state, PlayerEntity player, ItemStack tool) {
        if (world.isClient() || !hasVeinMiner(tool)) {
            return;
        }
        
        // Проверяем что это руда
        if (!isOre(state)) {
            return;
        }
        
        // Ищем и ломаем соседние руды того же типа
        breakVein(world, pos, state.getBlock(), player, tool);
    }
    
    private static boolean hasVeinMiner(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        
        var enchantments = stack.getEnchantments();
        for (var entry : enchantments.getEnchantments()) {
            String id = entry.getIdAsString();
            if (id != null && id.equals("caveborn:vein_miner")) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean isOre(BlockState state) {
        // Ванильные руды
        if (state.isIn(BlockTags.COAL_ORES) ||
            state.isIn(BlockTags.COPPER_ORES) ||
            state.isIn(BlockTags.IRON_ORES) ||
            state.isIn(BlockTags.GOLD_ORES) ||
            state.isIn(BlockTags.DIAMOND_ORES) ||
            state.isIn(BlockTags.EMERALD_ORES) ||
            state.isIn(BlockTags.LAPIS_ORES) ||
            state.isIn(BlockTags.REDSTONE_ORES)) {
            return true;
        }
        
        // Проверяем по имени блока (для модовых руд)
        String blockId = state.getBlock().getTranslationKey();
        return blockId.contains("_ore");
    }
    
    private static void breakVein(World world, BlockPos startPos, Block targetBlock, PlayerEntity player, ItemStack tool) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> toCheck = new LinkedList<>();
        int brokenCount = 0;
        
        toCheck.add(startPos);
        visited.add(startPos);
        
        while (!toCheck.isEmpty() && brokenCount < MAX_BLOCKS) {
            BlockPos current = toCheck.poll();
            
            // Проверяем все 6 соседних блоков
            for (BlockPos neighbor : getNeighbors(current)) {
                if (visited.contains(neighbor)) {
                    continue;
                }
                
                visited.add(neighbor);
                BlockState neighborState = world.getBlockState(neighbor);
                
                // Если это тот же тип руды
                if (neighborState.getBlock() == targetBlock && isOre(neighborState)) {
                    // Ломаем блок
                    if (breakBlock(world, neighbor, neighborState, player, tool)) {
                        brokenCount++;
                        toCheck.add(neighbor);
                    }
                }
            }
        }
    }
    
    private static BlockPos[] getNeighbors(BlockPos pos) {
        return new BlockPos[] {
            pos.up(),
            pos.down(),
            pos.north(),
            pos.south(),
            pos.east(),
            pos.west()
        };
    }
    
    private static boolean breakBlock(World world, BlockPos pos, BlockState state, PlayerEntity player, ItemStack tool) {
        // Проверяем что инструмент может сломать блок
        if (!tool.isSuitableFor(state)) {
            return false;
        }
        
        // Уменьшаем прочность инструмента
        tool.damage(1, player, player.getPreferredEquipmentSlot(tool));
        
        // Ломаем блок с дропом
        state.getBlock().onBreak(world, pos, state, player);
        world.breakBlock(pos, true, player);
        
        return true;
    }
}
