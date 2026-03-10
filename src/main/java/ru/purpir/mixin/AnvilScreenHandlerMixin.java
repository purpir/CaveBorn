package ru.purpir.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.purpir.enchantment.SolarInfusionSystem;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin {

    @Shadow @Final private Property levelCost;
    @Shadow private int repairItemUsage;

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private void onUpdateResult(CallbackInfo ci) {
        AnvilScreenHandler handler = (AnvilScreenHandler) (Object) this;
        
        ItemStack leftStack = handler.getSlot(0).getStack();
        ItemStack rightStack = handler.getSlot(1).getStack();
        
        if (SolarInfusionSystem.canInfuse(leftStack, rightStack)) {
            ItemStack result = SolarInfusionSystem.infuseSword(leftStack, rightStack);
            
            if (!result.isEmpty()) {
                handler.getSlot(2).setStack(result);
                this.levelCost.set(5);
                this.repairItemUsage = 1; // Используем только 1 кристалл
                ci.cancel();
            }
        }
    }
}
