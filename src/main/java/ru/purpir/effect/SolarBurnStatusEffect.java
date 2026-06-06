package ru.purpir.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.world.ServerWorld;

public class SolarBurnStatusEffect extends StatusEffect {
    public SolarBurnStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0xffc43b);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        entity.setOnFireFor(2.0F);
        return true;
    }
}
