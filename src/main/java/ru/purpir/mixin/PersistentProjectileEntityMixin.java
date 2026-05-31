package ru.purpir.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.purpir.enchantment.SolarInfusionSystem;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin {

    @Shadow public abstract ItemStack getWeaponStack();

    @Inject(method = "onEntityHit", at = @At("TAIL"))
    private void caveborn$igniteSolarBowTarget(EntityHitResult entityHitResult, CallbackInfo ci) {
        ItemStack weaponStack = getWeaponStack();
        if (weaponStack != null && weaponStack.isOf(Items.BOW) && SolarInfusionSystem.isInfused(weaponStack) &&
                entityHitResult.getEntity() instanceof LivingEntity target) {
            target.setOnFireFor(5.0F);
        }
    }
}
