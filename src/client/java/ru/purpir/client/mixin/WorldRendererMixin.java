package ru.purpir.client.mixin;

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
import ru.purpir.multiblock.IMultiblock;
import ru.purpir.multiblock.MultiblockManager;

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
     * Для мультиблоков переопределяет форму и предотвращает дублирование.
     */
    @Inject(method = "drawBlockOutline", at = @At("HEAD"), cancellable = true)
    private void onDrawBlockOutline(MatrixStack matrices, VertexConsumer vertexConsumer,
                                     double x, double y, double z,
                                     OutlineRenderState state, int color, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;
        
        BlockPos pos = state.pos();
        BlockState blockState = client.world.getBlockState(pos);
        
        // Проверка через MultiblockManager
        MultiblockManager manager = MultiblockManager.getInstance();
        if (manager.isPartOfStructure(pos)) {
            BlockPos originPos = manager.getOriginPos(pos);
            
            // Если уже рисовали outline для этой структуры в этом кадре, пропускаем
            if (originPos != null && originPos.equals(lastMultiblockOrigin)) {
                ci.cancel();
                return;
            }
            
            lastMultiblockOrigin = originPos;
            VoxelShape shape = manager.getOutlineShape(pos);
            
            if (shape != null && originPos != null) {
                // Рисуем outline относительно origin позиции
                VertexRendering.drawOutline(matrices, vertexConsumer, shape,
                    (double)originPos.getX() - x, 
                    (double)originPos.getY() - y, 
                    (double)originPos.getZ() - z, 
                    color);
                ci.cancel();
            }
        }
        // Проверка через интерфейс IMultiblock (для блоков, которые сами управляют структурой)
        else if (blockState.getBlock() instanceof IMultiblock multiblock) {
            BlockPos originPos = multiblock.getOriginPos(client.world, pos, blockState);
            
            if (originPos != null) {
                // Если уже рисовали outline для этой структуры в этом кадре, пропускаем
                if (originPos.equals(lastMultiblockOrigin)) {
                    ci.cancel();
                    return;
                }
                
                lastMultiblockOrigin = originPos;
                VoxelShape shape = multiblock.getFullOutlineShape(client.world, originPos, blockState);
                
                // Рисуем outline относительно origin позиции
                VertexRendering.drawOutline(matrices, vertexConsumer, shape,
                    (double)originPos.getX() - x, 
                    (double)originPos.getY() - y, 
                    (double)originPos.getZ() - z, 
                    color);
                ci.cancel();
            }
        }
    }

    /**
     * Сбрасывает флаг мультиблока в начале каждого кадра.
     */
    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderStart(CallbackInfo ci) {
        lastMultiblockOrigin = null;
    }
}
