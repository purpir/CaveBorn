package ru.purpir.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.WindChargeEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.purpir.enchantment.SolarInfusionSystem;

@Mixin(WindChargeEntity.class)
public class WindChargeEntityMixin {
    
    @Unique
    private boolean isSolarInfused = false;
    
    @Inject(method = "<init>(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/World;DDD)V", at = @At("RETURN"))
    private void onPlayerConstruct(PlayerEntity player, World world, double x, double y, double z, CallbackInfo ci) {
        // Проверяем, есть ли у игрока зачарованный заряд ветра
        ItemStack mainHand = player.getMainHandStack();
        ItemStack offHand = player.getOffHandStack();
        
        if (mainHand.isOf(Items.WIND_CHARGE) && SolarInfusionSystem.isInfused(mainHand)) {
            isSolarInfused = true;
        } else if (offHand.isOf(Items.WIND_CHARGE) && SolarInfusionSystem.isInfused(offHand)) {
            isSolarInfused = true;
        }
    }
    
    @Inject(method = "createExplosion", at = @At("HEAD"))
    private void onSolarWindChargeExplosion(Vec3d pos, CallbackInfo ci) {
        if (!isSolarInfused) {
            return;
        }
        
        // Проверка конфигурации
        if (!ru.purpir.config.SolarAbilityConfig.getInstance().isAbilityEnabled("wind_charge")) {
            return;
        }
        
        WindChargeEntity windCharge = (WindChargeEntity) (Object) this;
        World world = windCharge.getEntityWorld();
        
        if (world.isClient()) {
            return;
        }
        
        // Создаем дополнительный взрыв с уроном в точке столкновения
        world.createExplosion(
            windCharge,
            pos.getX(),
            pos.getY(),
            pos.getZ(),
            2.0F, // Сила взрыва
            World.ExplosionSourceType.MOB
        );
    }
}
