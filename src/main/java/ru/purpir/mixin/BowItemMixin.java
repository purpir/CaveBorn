package ru.purpir.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.purpir.enchantment.SolarInfusionSystem;

@Mixin(BowItem.class)
public class BowItemMixin {

    @Inject(method = "shoot", at = @At("HEAD"), cancellable = true)
    private void caveborn$shootSolarArrow(LivingEntity shooter, ProjectileEntity projectile, int index,
                                          float speed, float divergence, float yaw,
                                          LivingEntity target, CallbackInfo ci) {
        if (!isUsingSolarBow(shooter)) {
            return;
        }

        projectile.setVelocity(shooter, shooter.getPitch(), shooter.getYaw() + yaw, 0.0F, speed * 1.5F, divergence);
        ci.cancel();
    }

    private static boolean isUsingSolarBow(LivingEntity shooter) {
        ItemStack activeStack = shooter.getActiveItem();
        if (activeStack.isOf(Items.BOW) && SolarInfusionSystem.isInfused(activeStack)) {
            return true;
        }

        ItemStack mainHand = shooter.getMainHandStack();
        ItemStack offHand = shooter.getOffHandStack();
        return (mainHand.isOf(Items.BOW) && SolarInfusionSystem.isInfused(mainHand)) ||
            (offHand.isOf(Items.BOW) && SolarInfusionSystem.isInfused(offHand));
    }
}
