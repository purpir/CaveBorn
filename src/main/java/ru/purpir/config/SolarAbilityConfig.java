package ru.purpir.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import ru.purpir.Caveborn;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class SolarAbilityConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("caveborn-solar-abilities.json");
    
    private static SolarAbilityConfig INSTANCE;
    
    // Настройки для каждой способности
    public Map<String, Boolean> abilities = new HashMap<>();
    
    public SolarAbilityConfig() {
        // По умолчанию все способности включены
        abilities.put("wooden_sword", true);
        abilities.put("stone_sword", true);
        abilities.put("copper_sword", true);
        abilities.put("golden_sword", true);
        abilities.put("iron_sword", true);
        abilities.put("diamond_sword", true);
        abilities.put("netherite_sword", true);
        abilities.put("bronze_sword", true);
        abilities.put("mace", true);
        abilities.put("wind_charge", true);
    }
    
    public static SolarAbilityConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }
    
    public boolean isAbilityEnabled(String abilityName) {
        return abilities.getOrDefault(abilityName, true);
    }
    
    public static SolarAbilityConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                SolarAbilityConfig config = GSON.fromJson(json, SolarAbilityConfig.class);
                Caveborn.LOGGER.info("Loaded solar abilities config from {}", CONFIG_PATH);
                return config;
            } catch (IOException e) {
                Caveborn.LOGGER.error("Failed to load solar abilities config", e);
            }
        }
        
        SolarAbilityConfig config = new SolarAbilityConfig();
        config.save();
        return config;
    }
    
    public static void reload() {
        INSTANCE = load();
        Caveborn.LOGGER.info("Reloaded solar abilities config");
    }
    
    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            String json = GSON.toJson(this);
            Files.writeString(CONFIG_PATH, json);
            Caveborn.LOGGER.info("Saved solar abilities config to {}", CONFIG_PATH);
        } catch (IOException e) {
            Caveborn.LOGGER.error("Failed to save solar abilities config", e);
        }
    }
}
