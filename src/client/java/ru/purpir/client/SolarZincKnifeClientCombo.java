package ru.purpir.client;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import ru.purpir.enchantment.SolarInfusionSystem;
import ru.purpir.item.ModItems;

public final class SolarZincKnifeClientCombo {
    private static final int ATTACK_COOLDOWN_TICKS = 7;
    private static final int COMBO_RESET_TICKS = 45;
    private static int step;
    private static long expiresAt;
    private static long nextAttack;

    private SolarZincKnifeClientCombo() {
    }

    public static boolean shouldSwingOffhand(ClientPlayerEntity player, HitResult hitResult) {
        if (!hasDuoInfusedKnives(player) || !(hitResult instanceof EntityHitResult entityHitResult)) {
            return false;
        }

        Entity entity = entityHitResult.getEntity();
        if (!(entity instanceof LivingEntity)) {
            return false;
        }

        long now = player.getEntityWorld().getTime();
        if (nextAttack > now) {
            return false;
        }
        nextAttack = now + ATTACK_COOLDOWN_TICKS;

        int currentStep = expiresAt > now ? step : 0;
        expiresAt = now + COMBO_RESET_TICKS;
        if (currentStep == 0) {
            step = 1;
            return false;
        }
        if (currentStep == 1) {
            step = 2;
            return true;
        }

        step = 0;
        return false;
    }

    private static boolean hasDuoInfusedKnives(ClientPlayerEntity player) {
        return player.getMainHandStack().isOf(ModItems.ZINC_KNIFE)
            && SolarInfusionSystem.isInfused(player.getMainHandStack())
            && player.getOffHandStack().isOf(ModItems.ZINC_KNIFE)
            && SolarInfusionSystem.isInfused(player.getOffHandStack());
    }
}
