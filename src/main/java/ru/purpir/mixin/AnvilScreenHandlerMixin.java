package ru.purpir.mixin;

import net.minecraft.entity.player.PlayerEntity;
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
import ru.purpir.item.ModItems;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin {

    @Shadow @Final private Property levelCost;
    @Shadow private int repairItemUsage;

    private boolean caveborn$singleItemSolarInfusionResult;

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private void onUpdateResult(CallbackInfo ci) {
        AnvilScreenHandler handler = (AnvilScreenHandler) (Object) this;
        this.caveborn$singleItemSolarInfusionResult = false;

        ItemStack leftStack = handler.getSlot(0).getStack();
        ItemStack rightStack = handler.getSlot(1).getStack();

        if (SolarInfusionSystem.canInfuse(leftStack, rightStack)) {
            ItemStack result = SolarInfusionSystem.infuseSword(leftStack, rightStack);

            if (!result.isEmpty()) {
                handler.getSlot(2).setStack(result);
                this.levelCost.set(5);
                this.repairItemUsage = 1;
                this.caveborn$singleItemSolarInfusionResult = result.isOf(ModItems.RUSTED_MINER_KEY);
                ci.cancel();
            }
        }
    }

    @Inject(method = "onTakeOutput", at = @At("HEAD"), cancellable = true)
    private void onTakeSolarInfusionOutput(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (!this.caveborn$singleItemSolarInfusionResult) {
            return;
        }

        AnvilScreenHandler handler = (AnvilScreenHandler) (Object) this;
        if (!player.getAbilities().creativeMode) {
            player.addExperienceLevels(-this.levelCost.get());
        }

        ItemStack leftStack = handler.getSlot(0).getStack();
        if (!leftStack.isEmpty()) {
            leftStack.decrement(1);
            handler.getSlot(0).setStack(leftStack);
        }

        ItemStack rightStack = handler.getSlot(1).getStack();
        if (!rightStack.isEmpty()) {
            rightStack.decrement(this.repairItemUsage);
            handler.getSlot(1).setStack(rightStack);
        }

        handler.getSlot(2).setStack(ItemStack.EMPTY);
        handler.sendContentUpdates();
        this.caveborn$singleItemSolarInfusionResult = false;
        ci.cancel();
    }
}
