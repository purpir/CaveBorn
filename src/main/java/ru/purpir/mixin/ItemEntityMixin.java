package ru.purpir.mixin;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.purpir.component.ModComponents;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    
    @Shadow
    public abstract ItemStack getStack();
    
    @Inject(method = "onPlayerCollision", at = @At("HEAD"))
    private void onPickup(PlayerEntity player, CallbackInfo ci) {
        ItemStack stack = this.getStack();
        // Очищаем компонент времени экспозиции при подборе
        if (stack.contains(ModComponents.SUN_EXPOSURE_TIME)) {
            stack.remove(ModComponents.SUN_EXPOSURE_TIME);
        }
    }
}
