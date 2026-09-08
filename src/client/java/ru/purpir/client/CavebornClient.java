package ru.purpir.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.entity.EmptyEntityRenderer;
import net.minecraft.client.render.item.tint.TintSourceTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import ru.purpir.Caveborn;
import ru.purpir.block.ModBlocks;
import ru.purpir.client.render.BlockTintSource;
import ru.purpir.client.render.ChaosRiftEntityRenderer;
import ru.purpir.client.render.MinecartSpeedHud;
import ru.purpir.client.render.MinecartChainRenderer;
import ru.purpir.client.render.RootBindingChainRenderer;
import ru.purpir.client.render.SolarBurnOverlay;
import ru.purpir.client.render.SolarPointsHud;
import ru.purpir.client.screen.BagScreen;
import ru.purpir.client.screen.CrusherScreen;
import ru.purpir.client.screen.EventAltarScreen;
import ru.purpir.client.screen.SolarInfusionGuideScreen;
import ru.purpir.client.util.SceneFadeOverlay;
import ru.purpir.entity.ModEntities;
import ru.purpir.item.ModItems;
import ru.purpir.network.ModPackets;
import ru.purpir.minecart.MinecartTransportHandler;
import ru.purpir.screen.ModScreenHandlers;

public class CavebornClient implements ClientModInitializer {
    private static final int BRONZE_AXE_DOUBLE_JUMP_MIN_AIR_TICKS = 3;

    private boolean caveborn$bronzeAxeAirJumpUsed;
    private boolean caveborn$jumpWasDown;
    private int caveborn$bronzeAxeAirTicks;

    @Override
    public void onInitializeClient() {
        // Регистрируем свой TintSource для блоков
        TintSourceTypes.ID_MAPPER.put(
            Identifier.of(Caveborn.MOD_ID, "block"),
            BlockTintSource.CODEC
        );
        
        // Инициализация динамических текстур
        TextureInitializer.initialize();
        
        // Регистрируем тултип для солнечной инфузии
        SolarInfusionTooltip.register();
        SolarPointsHud.register();
        SolarBurnOverlay.register();
        if (MinecartTransportHandler.ENABLED) {
            MinecartSpeedHud.register();
        }
        SceneFadeOverlay.register();
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.SolarPointsPayload.ID,
            (payload, context) -> SolarPointsClientState.setPoints(payload.points()));
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.OpenAltarScreenPayload.ID,
            (payload, context) -> context.client().setScreen(new EventAltarScreen(payload)));
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.AltarScenePayload.ID,
            (payload, context) -> {
                context.client().setScreen(null);
                SceneFadeOverlay.start(payload.data());
            });
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.RootBindingChainsPayload.ID,
            (payload, context) -> RootBindingClientState.setLinks(payload.entityLinks()));
        if (MinecartTransportHandler.ENABLED) {
            ClientPlayNetworking.registerGlobalReceiver(ModPackets.MinecartChainsPayload.ID,
                (payload, context) -> MinecartChainClientState.setLinks(payload.entityLinks()));
        }
        
        // Регистрируем экран сумки
        HandledScreens.register(ModScreenHandlers.BAG_SCREEN_HANDLER, BagScreen::new);
        HandledScreens.register(ModScreenHandlers.CRUSHER_SCREEN_HANDLER, CrusherScreen::new);
        EntityRendererRegistry.register(ModEntities.CAVE_FIREFLY, EmptyEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.SOLAR_SOUL, EmptyEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.CHAOS_RIFT, ChaosRiftEntityRenderer::new);
        RootBindingChainRenderer.register();
        if (MinecartTransportHandler.ENABLED) {
            MinecartChainRenderer.register();
        }
        ClientTickEvents.END_CLIENT_TICK.register(this::tickClientSystems);

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClient()) {
                return ActionResult.PASS;
            }

            if (player.getStackInHand(hand).isOf(ModItems.SOLAR_INFUSION_GUIDE)) {
                MinecraftClient.getInstance().setScreen(new SolarInfusionGuideScreen());
                return ActionResult.SUCCESS;
            }

            return ActionResult.PASS;
        });
        
        // Регистрируем прозрачный рендер для сорняков
        BlockRenderLayerMap.putBlock(ModBlocks.WEED, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.WEED_TOP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.HOGWEED, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.HOGWEED_PASTE, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.SOLAR_IRIS, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.VOID_EYE_PLANT, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.VOID_LANTERN, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.CRYSTAL_GROWTH, BlockRenderLayer.CUTOUT);
        
        // Прозрачность для титановых блоков
        BlockRenderLayerMap.putBlock(ModBlocks.TITANIUM_GRATE, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.TITANIUM_BARS, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.TITANIUM_DOOR, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.TITANIUM_TRAPDOOR, BlockRenderLayer.CUTOUT);
    }

    private void tickBronzeAxeDoubleJump(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            caveborn$bronzeAxeAirJumpUsed = false;
            caveborn$jumpWasDown = false;
            caveborn$bronzeAxeAirTicks = 0;
            return;
        }

        boolean jumpDown = client.options.jumpKey.isPressed();
        if (client.player.isOnGround()) {
            caveborn$bronzeAxeAirJumpUsed = false;
            caveborn$jumpWasDown = jumpDown;
            caveborn$bronzeAxeAirTicks = 0;
            return;
        }

        caveborn$bronzeAxeAirTicks++;
        if (caveborn$bronzeAxeAirTicks >= BRONZE_AXE_DOUBLE_JUMP_MIN_AIR_TICKS &&
            jumpDown && !caveborn$jumpWasDown && !caveborn$bronzeAxeAirJumpUsed && isHoldingInfusedBronzeAxe(client)) {
            caveborn$bronzeAxeAirJumpUsed = true;
            ClientPlayNetworking.send(new ModPackets.BronzeAxeDoubleJumpPayload());
        }
        caveborn$jumpWasDown = jumpDown;
    }

    private void tickClientSystems(MinecraftClient client) {
        tickBronzeAxeDoubleJump(client);
        tickMinecartControls(client);
    }

    private void tickMinecartControls(MinecraftClient client) {
        if (!MinecartTransportHandler.ENABLED) {
            return;
        }

        if (client.player == null || !(client.player.getVehicle() instanceof net.minecraft.entity.vehicle.AbstractMinecartEntity)) {
            return;
        }

        ClientPlayNetworking.send(new ModPackets.MinecartControlPayload(
            client.options.forwardKey.isPressed(),
            client.options.backKey.isPressed()
        ));
    }

    private boolean isHoldingInfusedBronzeAxe(MinecraftClient client) {
        return isInfusedBronzeAxe(client.player.getMainHandStack()) || isInfusedBronzeAxe(client.player.getOffHandStack());
    }

    private boolean isInfusedBronzeAxe(net.minecraft.item.ItemStack stack) {
        return stack.isOf(ModItems.BRONZE_AXE) && ru.purpir.enchantment.SolarInfusionSystem.isInfused(stack);
    }
}
