package ru.purpir.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.purpir.enchantment.SolarInfusionSystem;

@Mixin(net.minecraft.item.RangedWeaponItem.class)
public class RangedWeaponItemMixin {

    @Inject(method = "createArrowEntity", at = @At("RETURN"))
    private void caveborn$boostSolarBowArrow(World world, LivingEntity shooter, ItemStack weaponStack,
                                             ItemStack projectileStack, boolean critical,
                                             CallbackInfoReturnable<ProjectileEntity> cir) {
        ProjectileEntity projectile = cir.getReturnValue();
        if (weaponStack.isOf(Items.BOW) && SolarInfusionSystem.isInfused(weaponStack) &&
                projectile instanceof PersistentProjectileEntity arrow) {
            arrow.setDamage(3.0);
        }
    }
}
