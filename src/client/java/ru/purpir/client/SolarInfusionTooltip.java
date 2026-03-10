package ru.purpir.client;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ru.purpir.enchantment.SolarInfusionSystem;

public class SolarInfusionTooltip {
    
    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            if (SolarInfusionSystem.isInfused(stack)) {
                lines.add(1, Text.translatable("tooltip.caveborn.solar_infused")
                    .formatted(Formatting.GOLD, Formatting.ITALIC));
            }
        });
    }
}
