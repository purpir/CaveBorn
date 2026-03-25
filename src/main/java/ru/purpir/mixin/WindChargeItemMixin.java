package ru.purpir.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.WindChargeItem;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.purpir.config.SolarAbilityConfig;
import ru.purpir.enchantment.SolarInfusionSystem;

@Mixin(WindChargeItem.class)
public class WindChargeItemMixin {
    
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void onUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = user.getStackInHand(hand);
        
        // Проверяем, является ли заряд ветра солнечным
        if (SolarInfusionSystem.isInfused(stack)) {
            // Проверяем, включена ли способность
            if (!SolarAbilityConfig.getInstance().isAbilityEnabled("wind_charge")) {
                if (!world.isClient()) {
                    user.sendMessage(
                        Text.translatable("ability.caveborn.disabled")
                            .formatted(Formatting.RED),
                        true
                    );
                }
                // Отменяем использование предмета
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }
}
