package ru.purpir.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.biome.source.util.VanillaBiomeParameters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.purpir.world.ModBiomes;

import java.util.function.Consumer;

@Mixin(VanillaBiomeParameters.class)
public class VanillaBiomeParametersMixin {

    @Inject(method = "writeOverworldBiomeParameters", at = @At("TAIL"))
    private void caveborn$addAmethystPlains(
            Consumer<Pair<MultiNoiseUtil.NoiseHypercube, RegistryKey<Biome>>> parameters,
            CallbackInfo ci) {
        add(parameters, ModBiomes.AMETHYST_PLAINS, 0.10f, 0.42f, -0.35f, 0.05f, 0.18f, 0.48f, -0.35f, 0.10f, -0.25f, 0.20f);
        add(parameters, ModBiomes.AMETHYST_PLAINS, 0.25f, 0.58f, -0.50f, -0.12f, 0.36f, 0.68f, -0.55f, -0.18f, 0.25f, 0.58f);
    }

    private static void add(Consumer<Pair<MultiNoiseUtil.NoiseHypercube, RegistryKey<Biome>>> parameters,
                            RegistryKey<Biome> biome,
                            float minTemperature, float maxTemperature,
                            float minHumidity, float maxHumidity,
                            float minContinentalness, float maxContinentalness,
                            float minErosion, float maxErosion,
                            float minWeirdness, float maxWeirdness) {
        parameters.accept(Pair.of(
            MultiNoiseUtil.createNoiseHypercube(
                MultiNoiseUtil.ParameterRange.of(minTemperature, maxTemperature),
                MultiNoiseUtil.ParameterRange.of(minHumidity, maxHumidity),
                MultiNoiseUtil.ParameterRange.of(minContinentalness, maxContinentalness),
                MultiNoiseUtil.ParameterRange.of(minErosion, maxErosion),
                MultiNoiseUtil.ParameterRange.of(0.0f),
                MultiNoiseUtil.ParameterRange.of(minWeirdness, maxWeirdness),
                0.0f
            ),
            biome
        ));
    }
}
