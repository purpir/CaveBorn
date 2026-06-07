package ru.purpir.client.render;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import ru.purpir.Caveborn;
import ru.purpir.entity.ChaosRiftEntity;

public class ChaosRiftEntityRenderer extends EntityRenderer<ChaosRiftEntity, EntityRenderState> {
    private static final Identifier TEXTURE = Identifier.of(Caveborn.MOD_ID, "textures/block/chaos_rift.png");

    public ChaosRiftEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(EntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        matrices.push();
        matrices.multiply(cameraState.orientation);
        queue.submitCustom(matrices, RenderLayer.getEntityTranslucentEmissive(TEXTURE), (entry, vertices) -> {
            float halfWidth = 1.5F;
            float height = 3.0F;
            vertex(vertices, entry, -halfWidth, 0.0F, 0.0F, 0.0F, 1.0F, state.light);
            vertex(vertices, entry, halfWidth, 0.0F, 0.0F, 1.0F, 1.0F, state.light);
            vertex(vertices, entry, halfWidth, height, 0.0F, 1.0F, 0.0F, state.light);
            vertex(vertices, entry, -halfWidth, height, 0.0F, 0.0F, 0.0F, state.light);
        });
        matrices.pop();
        super.render(state, matrices, queue, cameraState);
    }

    private static void vertex(VertexConsumer vertices, MatrixStack.Entry entry, float x, float y, float z, float u, float v, int light) {
        vertices.vertex(entry, x, y, z)
            .color(255, 255, 255, 230)
            .texture(u, v)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(light)
            .normal(entry, 0.0F, 1.0F, 0.0F);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
