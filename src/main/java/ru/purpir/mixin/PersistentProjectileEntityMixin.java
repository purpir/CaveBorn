package ru.purpir.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import net.minecraft.entity.SpawnReason;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.purpir.enchantment.SolarInfusionSystem;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin {

    @Shadow public abstract ItemStack getWeaponStack();
    @Shadow public abstract ItemStack getItemStack();

    @Unique private int caveborn$solarBrokenBlocks;

    @Inject(method = "onEntityHit", at = @At("TAIL"))
    private void caveborn$applySolarArrowHit(EntityHitResult entityHitResult, CallbackInfo ci) {
        ItemStack weaponStack = getWeaponStack();
        if (weaponStack != null && weaponStack.isOf(Items.BOW) && SolarInfusionSystem.isInfused(weaponStack) &&
                entityHitResult.getEntity() instanceof LivingEntity target) {
            target.setOnFireFor(5.0F);
        }

        ItemStack arrowStack = getItemStack();
        if ((arrowStack.isOf(Items.ARROW) || arrowStack.isOf(Items.SPECTRAL_ARROW)) &&
                SolarInfusionSystem.isInfused(arrowStack) && entityHitResult.getEntity() instanceof LivingEntity target) {
            caveborn$summonLightning(target);
        }
    }

    @Inject(method = "onBlockHit", at = @At("HEAD"), cancellable = true)
    private void caveborn$breakBlockWithSolarSpectralArrow(BlockHitResult hitResult, CallbackInfo ci) {
        ItemStack arrowStack = getItemStack();
        PersistentProjectileEntity projectile = (PersistentProjectileEntity) (Object) this;
        World world = projectile.getEntityWorld();

        if (!arrowStack.isOf(Items.SPECTRAL_ARROW) || !SolarInfusionSystem.isInfused(arrowStack) ||
                caveborn$solarBrokenBlocks >= 2 || !(world instanceof ServerWorld serverWorld)) {
            return;
        }

        BlockPos pos = hitResult.getBlockPos();
        BlockState state = world.getBlockState(pos);
        float hardness = state.getHardness(world, pos);

        if (hardness < 0.0F || hardness > 1.5F || state.isAir()) {
            return;
        }

        if (serverWorld.breakBlock(pos, true, projectile.getOwner())) {
            caveborn$solarBrokenBlocks++;
            projectile.setPosition(new Vec3d(projectile.getX(), projectile.getY(), projectile.getZ())
                .add(projectile.getVelocity().normalize().multiply(0.25)));
            ci.cancel();
        }
    }

    @Unique
    private static void caveborn$summonLightning(LivingEntity target) {
        if (!(target.getEntityWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(serverWorld, SpawnReason.TRIGGERED);
        if (lightning != null) {
            lightning.refreshPositionAfterTeleport(target.getX(), target.getY(), target.getZ());
            serverWorld.spawnEntity(lightning);
        }
    }
}
