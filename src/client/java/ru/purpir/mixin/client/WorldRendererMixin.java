package ru.purpir.mixin.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.state.OutlineRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Миксин для кастомного рендера outline мультиблоков.
 * Позволяет рисовать единый хитбокс для блоков, занимающих несколько позиций.
 */
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Unique
    private static BlockPos lastMultiblockOrigin = null;

    /**
     * Перехватывает рисование outline блока.
     * Для мультиблоков можно переопределить форму и предотвратить дублирование.
     */
    @Inject(method = "drawBlockOutline", at = @At("HEAD"), cancellable = true)
    private void onDrawBlockOutline(MatrixStack matrices, VertexConsumer vertexConsumer,
                                     double x, double y, double z,
                                     OutlineRenderState state, int color, CallbackInfo ci) {
        // TODO: Добавить проверку на мультиблоки здесь
        // Пример использования:
        // BlockPos pos = state.pos();
        // BlockState blockState = MinecraftClient.getInstance().world.getBlockState(pos);
        // if (blockState.getBlock() instanceof IMultiblock multiblock) {
        //     BlockPos originPos = multiblock.getOriginPos(pos, blockState);
        //     if (originPos.equals(lastMultiblockOrigin)) {
        //         ci.cancel();
        //         return;
        //     }
        //     lastMultiblockOrigin = originPos;
        //     VoxelShape shape = multiblock.getFullOutlineShape(blockState);
        //     VertexRendering.drawOutline(matrices, vertexConsumer, shape,
        //         (double)pos.getX() - x, (double)pos.getY() - y, (double)pos.getZ() - z, color);
        //     ci.cancel();
        // }
    }

    /**
     * Сбрасывает флаг мультиблока в начале каждого кадра.
     */
    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderStart(CallbackInfo ci) {
        lastMultiblockOrigin = null;
    }
}
