package ru.purpir.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.purpir.enchantment.SolarInfusionSystem;

@Mixin(EnderPearlItem.class)
public class EnderPearlItemMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void caveborn$throwSolarEnderPearl(World world, PlayerEntity user, Hand hand,
                                               CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = user.getStackInHand(hand);
        if (!SolarInfusionSystem.isInfused(stack)) {
            return;
        }

        world.playSound(
            null,
            user.getX(),
            user.getY(),
            user.getZ(),
            SoundEvents.ENTITY_ENDER_PEARL_THROW,
            SoundCategory.NEUTRAL,
            0.5F,
            0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F)
        );

        if (world instanceof ServerWorld serverWorld) {
            ProjectileEntity.spawnWithVelocity(
                EnderPearlEntity::new,
                serverWorld,
                stack,
                user,
                0.0F,
                EnderPearlItem.POWER * 1.75F,
                1.0F
            );
        }

        user.incrementStat(Stats.USED.getOrCreateStat((EnderPearlItem) (Object) this));
        stack.decrementUnlessCreative(1, user);
        cir.setReturnValue(ActionResult.SUCCESS);
    }
}
