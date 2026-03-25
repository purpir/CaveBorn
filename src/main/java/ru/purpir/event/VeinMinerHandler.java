package ru.purpir.event;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ru.purpir.enchantment.VeinMinerEnchantment;

public class VeinMinerHandler {
    
    public static void register() {
        PlayerBlockBreakEvents.AFTER.register(VeinMinerHandler::onBlockBroken);
    }
    
    private static void onBlockBroken(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        VeinMinerEnchantment.onBlockBroken(world, pos, state, player, player.getMainHandStack());
    }
}
