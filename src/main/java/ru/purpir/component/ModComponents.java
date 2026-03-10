package ru.purpir.component;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import ru.purpir.Caveborn;

public class ModComponents {
    public static final ComponentType<Long> SUN_EXPOSURE_TIME = Registry.register(
        Registries.DATA_COMPONENT_TYPE,
        Identifier.of(Caveborn.MOD_ID, "sun_exposure_time"),
        ComponentType.<Long>builder().codec(Codec.LONG).build()
    );

    public static final ComponentType<Boolean> SOLAR_INFUSED = Registry.register(
        Registries.DATA_COMPONENT_TYPE,
        Identifier.of(Caveborn.MOD_ID, "solar_infused"),
        ComponentType.<Boolean>builder().codec(Codec.BOOL).build()
    );

    public static final ComponentType<CooldownComponent> ABILITY_COOLDOWN = Registry.register(
        Registries.DATA_COMPONENT_TYPE,
        Identifier.of(Caveborn.MOD_ID, "ability_cooldown"),
        ComponentType.<CooldownComponent>builder().codec(CooldownComponent.CODEC).build()
    );

    public static void register() {
        Caveborn.LOGGER.info("Registering mod components");
    }
}
