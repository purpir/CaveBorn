package ru.purpir.client.render;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import ru.purpir.Caveborn;
import ru.purpir.client.RootBindingClientState;

public final class RootBindingChainRenderer {
    private static final Identifier TEXTURE = Identifier.of(Caveborn.MOD_ID, "textures/entity/root_binding_chain.png");
    private static final RenderLayer LAYER = RenderLayer.getEntityTranslucent(TEXTURE);
    private static final float HALF_WIDTH = 0.07F;

    private RootBindingChainRenderer() {
    }

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(RootBindingChainRenderer::render);
    }

    private static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            RootBindingClientState.clear();
            return;
        }

        Camera camera = client.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();
        Vector3f horizontalPlane = camera.getHorizontalPlane();
        Vec3d cameraForward = new Vec3d(horizontalPlane.x, horizontalPlane.y, horizontalPlane.z);
        VertexConsumer vertices = context.consumers().getBuffer(LAYER);
        MatrixStack.Entry entry = context.matrices().peek();

        for (RootBindingClientState.Link link : RootBindingClientState.getLinks()) {
            Entity from = client.world.getEntityById(link.fromEntityId());
            Entity to = client.world.getEntityById(link.toEntityId());
            if (from == null || to == null || !from.isAlive() || !to.isAlive()) {
                continue;
            }

            Vec3d start = anchor(from);
            Vec3d end = anchor(to);
            Vec3d delta = end.subtract(start);
            double length = delta.length();
            if (length < 0.001D) {
                continue;
            }

            Vec3d along = delta.normalize();
            Vec3d side = along.crossProduct(cameraForward);
            if (side.lengthSquared() < 1.0E-6D) {
                side = along.crossProduct(new Vec3d(0.0D, 1.0D, 0.0D));
            }
            if (side.lengthSquared() < 1.0E-6D) {
                side = new Vec3d(1.0D, 0.0D, 0.0D);
            }

            side = side.normalize().multiply(HALF_WIDTH);
            Vec3d p1 = start.add(side).subtract(cameraPos);
            Vec3d p2 = start.subtract(side).subtract(cameraPos);
            Vec3d p3 = end.subtract(side).subtract(cameraPos);
            Vec3d p4 = end.add(side).subtract(cameraPos);
            float vMax = (float) (length * 2.0D);

            vertex(vertices, entry, p1, 0.0F, 0.0F);
            vertex(vertices, entry, p2, 1.0F, 0.0F);
            vertex(vertices, entry, p3, 1.0F, vMax);
            vertex(vertices, entry, p4, 0.0F, vMax);
        }
    }

    private static Vec3d anchor(Entity entity) {
        double y = entity.getY() + MathHelper.clamp(entity.getHeight() * 0.45D, 0.35D, 1.0D);
        return new Vec3d(entity.getX(), y, entity.getZ());
    }

    private static void vertex(VertexConsumer vertices, MatrixStack.Entry entry, Vec3d pos, float u, float v) {
        vertices.vertex(entry, (float) pos.x, (float) pos.y, (float) pos.z)
            .color(255, 255, 255, 255)
            .texture(u, v)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
            .normal(entry, 0.0F, 1.0F, 0.0F);
    }
}
