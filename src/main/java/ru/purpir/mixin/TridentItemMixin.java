package ru.purpir.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.TridentItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.purpir.enchantment.SolarInfusionSystem;

@Mixin(TridentItem.class)
public class TridentItemMixin {

    @Unique private static final ThreadLocal<Boolean> caveborn$throwingSolarTrident = ThreadLocal.withInitial(() -> false);

    @Inject(method = "onStoppedUsing", at = @At("HEAD"))
    private void caveborn$rememberSolarTrident(ItemStack stack, World world, LivingEntity user, int remainingUseTicks,
                                               CallbackInfoReturnable<Boolean> cir) {
        caveborn$throwingSolarTrident.set(stack.isOf(Items.TRIDENT) && SolarInfusionSystem.isInfused(stack));
    }

    @ModifyArg(
        method = "onStoppedUsing",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/projectile/ProjectileEntity;spawnWithVelocity(Lnet/minecraft/entity/projectile/ProjectileEntity$ProjectileCreator;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;FFF)Lnet/minecraft/entity/projectile/ProjectileEntity;"
        ),
        index = 5
    )
    private float caveborn$boostSolarTridentThrow(float speed) {
        return caveborn$throwingSolarTrident.get() ? speed * 1.25F : speed;
    }

    @Inject(method = "onStoppedUsing", at = @At("RETURN"))
    private void caveborn$forgetSolarTrident(ItemStack stack, World world, LivingEntity user, int remainingUseTicks,
                                             CallbackInfoReturnable<Boolean> cir) {
        caveborn$throwingSolarTrident.remove();
    }
}
