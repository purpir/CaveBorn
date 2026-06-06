package ru.purpir.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import ru.purpir.Caveborn;

public class ModStatusEffects {
    public static final RegistryEntry<StatusEffect> SOLAR_BURN = Registry.registerReference(
        Registries.STATUS_EFFECT,
        Identifier.of(Caveborn.MOD_ID, "solar_burn"),
        new SolarBurnStatusEffect()
    );

    public static void register() {
        Caveborn.LOGGER.info("Registering mod status effects");
    }
}
