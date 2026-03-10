package ru.purpir.event;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import ru.purpir.enchantment.SolarInfusionSystem;

public class SolarDamageHandler {
    
    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient() && entity instanceof LivingEntity target) {
                ItemStack weapon = player.getStackInHand(hand);
                
                if (SolarInfusionSystem.isInfused(weapon)) {
                    float additionalDamage = SolarInfusionSystem.getAdditionalDamage(weapon);
                    if (additionalDamage > 0) {
                        DamageSource damageSource = player.getDamageSources().playerAttack(player);
                        target.damage((ServerWorld) world, damageSource, additionalDamage);
                    }
                }
            }
            
            return ActionResult.PASS;
        });
    }
}
