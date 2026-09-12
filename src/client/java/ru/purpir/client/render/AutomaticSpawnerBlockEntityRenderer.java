package ru.purpir.client.render;

import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.MobSpawnerBlockEntityRenderer;
import net.minecraft.client.render.block.entity.state.MobSpawnerBlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.math.Vec3d;
import ru.purpir.block.entity.AutomaticSpawnerBlockEntity;

public final class AutomaticSpawnerBlockEntityRenderer implements BlockEntityRenderer<AutomaticSpawnerBlockEntity, MobSpawnerBlockEntityRenderState> {
    private final EntityRenderManager entityRenderDispatcher;
    private Entity displayEntity;
    private EntityType<?> displayType;

    public AutomaticSpawnerBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.entityRenderDispatcher = context.entityRenderDispatcher();
    }

    @Override
    public MobSpawnerBlockEntityRenderState createRenderState() {
        return new MobSpawnerBlockEntityRenderState();
    }

    @Override
    public void updateRenderState(AutomaticSpawnerBlockEntity blockEntity, MobSpawnerBlockEntityRenderState state,
                                  float tickDelta, Vec3d cameraPos,
                                  ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay) {
        BlockEntityRenderer.super.updateRenderState(blockEntity, state, tickDelta, cameraPos, crumblingOverlay);
        state.displayEntityRenderState = null;

        if (blockEntity.getWorld() == null) return;
        ItemStack egg = blockEntity.getStack(0);
        if (!(egg.getItem() instanceof SpawnEggItem spawnEgg)) {
            displayEntity = null;
            displayType = null;
            return;
        }

        EntityType<?> type = spawnEgg.getEntityType(egg);
        if (displayEntity == null || displayType != type || displayEntity.getEntityWorld() != blockEntity.getWorld()) {
            displayEntity = type.create(blockEntity.getWorld(), SpawnReason.SPAWN_ITEM_USE);
            displayType = type;
        }
        if (displayEntity == null) return;

        float rotation = (blockEntity.getWorld().getTime() + tickDelta) * 3.0F;
        displayEntity.refreshPositionAndAngles(
            blockEntity.getPos().getX() + 0.5,
            blockEntity.getPos().getY() + 0.35,
            blockEntity.getPos().getZ() + 0.5,
            rotation,
            0.0F
        );
        state.displayEntityRotation = rotation;
        state.displayEntityScale = 0.45F;
        state.displayEntityRenderState = entityRenderDispatcher.getAndUpdateRenderState(displayEntity, tickDelta);
    }

    @Override
    public void render(MobSpawnerBlockEntityRenderState state, MatrixStack matrices,
                       OrderedRenderCommandQueue queue, CameraRenderState camera) {
        if (state.displayEntityRenderState != null) {
            MobSpawnerBlockEntityRenderer.renderDisplayEntity(
                matrices, queue, state.displayEntityRenderState, entityRenderDispatcher,
                state.displayEntityRotation, state.displayEntityScale, camera
            );
        }
    }
}
