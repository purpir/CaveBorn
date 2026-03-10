package ru.purpir.client.mixin;

import net.minecraft.block.Block;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.purpir.block.IColoredBlock;

@Mixin(BlockColors.class)
public class BlockColorsMixin {
    
    @Inject(method = "create", at = @At("RETURN"))
    private static void onCreateBlockColors(CallbackInfoReturnable<BlockColors> cir) {
        BlockColors blockColors = cir.getReturnValue();
        
        // Автоматически регистрируем все блоки, реализующие IColoredBlock
        for (Block block : Registries.BLOCK) {
            if (block instanceof IColoredBlock coloredBlock) {
                blockColors.registerColorProvider(
                    (state, world, pos, tintIndex) -> coloredBlock.getColor(tintIndex),
                    block
                );
            }
        }
    }
}
