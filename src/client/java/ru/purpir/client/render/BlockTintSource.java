package ru.purpir.client.render;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.client.render.item.tint.TintSource;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import ru.purpir.block.IColoredBlock;

/**
 * TintSource для блоков, реализующих IColoredBlock
 */
public record BlockTintSource(int tintIndex) implements TintSource {
    
    public static final MapCodec<BlockTintSource> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Codec.INT.optionalFieldOf("default", 0).forGetter(BlockTintSource::tintIndex)
        ).apply(instance, BlockTintSource::new)
    );
    
    @Override
    public int getTint(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity user) {
        Block block = Block.getBlockFromItem(stack.getItem());
        if (block instanceof IColoredBlock coloredBlock) {
            return coloredBlock.getColor(this.tintIndex);
        }
        return -1; // Белый цвет по умолчанию
    }
    
    @Override
    public MapCodec<? extends TintSource> getCodec() {
        return CODEC;
    }
}
