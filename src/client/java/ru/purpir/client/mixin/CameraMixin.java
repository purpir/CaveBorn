package ru.purpir.client.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.purpir.effect.ModStatusEffects;

@Mixin(Camera.class)
public class CameraMixin {
    @Shadow
    private Quaternionf rotation;

    @Inject(method = "update", at = @At("TAIL"))
    private void caveborn$rollSolarBurnCamera(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView,
                                              float tickProgress, CallbackInfo ci) {
        if (!(focusedEntity instanceof LivingEntity living) || !living.hasStatusEffect(ModStatusEffects.SOLAR_BURN)) {
            return;
        }

        float time = living.age + tickProgress;
        float degrees = MathHelper.sin(time * 0.22F) * 22.0F + MathHelper.sin(time * 0.57F) * 8.0F;
        this.rotation.rotateZ(degrees * MathHelper.RADIANS_PER_DEGREE);
    }
}
