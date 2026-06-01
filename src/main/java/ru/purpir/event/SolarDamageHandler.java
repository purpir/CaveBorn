package ru.purpir.event;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import ru.purpir.enchantment.SolarInfusionSystem;
import ru.purpir.item.ModItems;

public class SolarDamageHandler {
    
    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient() && entity instanceof LivingEntity target) {
                ItemStack weapon = player.getStackInHand(hand);
                
                if (weapon.isOf(ModItems.VACUUMITE_MAGNET)) {
                    return ActionResult.PASS;
                }

                if (SolarInfusionSystem.isInfused(weapon)) {
                    float additionalDamage = SolarInfusionSystem.getAdditionalDamage(weapon);
                    if (additionalDamage > 0) {
                        DamageSource damageSource = player.getDamageSources().playerAttack(player);
                        target.damage((ServerWorld) world, damageSource, additionalDamage);
                    }
                }

                ServerWorld serverWorld = (ServerWorld) world;
                float totemDamage = SolarTotemHandler.getAttackBonus(player, serverWorld);
                if (totemDamage > 0) {
                    target.damage(serverWorld, player.getDamageSources().playerAttack(player), totemDamage);
                    SolarTotemHandler.applyFireHit(player, serverWorld, target);
                }
            }
            
            return ActionResult.PASS;
        });
    }
}
