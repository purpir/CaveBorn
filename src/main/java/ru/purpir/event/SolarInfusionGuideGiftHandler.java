package ru.purpir.event;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import ru.purpir.item.ModItems;

public class SolarInfusionGuideGiftHandler {
    private static final String GUIDE_GIVEN_TAG = "caveborn_solar_infusion_guide_given";

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
            giveGuideIfNeeded(handler.player)
        );
    }

    private static void giveGuideIfNeeded(ServerPlayerEntity player) {
        if (player.getCommandTags().contains(GUIDE_GIVEN_TAG)) {
            return;
        }

        ItemStack guide = new ItemStack(ModItems.SOLAR_INFUSION_GUIDE);
        if (!player.getInventory().insertStack(guide)) {
            player.dropItem(guide, false);
        }

        player.addCommandTag(GUIDE_GIVEN_TAG);
    }
}
