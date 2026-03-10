package ru.purpir.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import ru.purpir.Caveborn;
import ru.purpir.util.ColorUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Генератор динамических текстур (только клиент).
 */
@Environment(EnvType.CLIENT)
public class DynamicTextureGenerator {
    
    private static final Map<String, Identifier> CACHED_TEXTURES = new HashMap<>();
    
    /**
     * Создать текстуру с одним цветом.
     */
    public static Identifier createSolidColorTexture(String name, int color, int size) {
        String cacheKey = "solid_" + name + "_" + Integer.toHexString(color);
        
        if (CACHED_TEXTURES.containsKey(cacheKey)) {
            return CACHED_TEXTURES.get(cacheKey);
        }
        
        int argb = ColorUtil.argb(255, 
            ColorUtil.getRed(color), 
            ColorUtil.getGreen(color), 
            ColorUtil.getBlue(color));
        
        NativeImage image = TextureUtil.createSolidColor(size, size, argb);
        Identifier identifier = registerTexture(name, image);
        
        CACHED_TEXTURES.put(cacheKey, identifier);
        return identifier;
    }
    
    /**
     * Создать текстуру с наложением цвета.
     */
    public static Identifier createColoredTexture(String name, Identifier baseTexture, int overlayColor) {
        String cacheKey = "colored_" + name + "_" + baseTexture + "_" + Integer.toHexString(overlayColor);
        
        if (CACHED_TEXTURES.containsKey(cacheKey)) {
            return CACHED_TEXTURES.get(cacheKey);
        }
        
        NativeImage base = TextureUtil.loadTexture(baseTexture);
        if (base == null) {
            Caveborn.LOGGER.error("Failed to load base texture: " + baseTexture);
            return baseTexture;
        }
        
        NativeImage colored = TextureUtil.applyColorOverlay(base, overlayColor);
        base.close();
        
        Identifier identifier = registerTexture(name, colored);
        CACHED_TEXTURES.put(cacheKey, identifier);
        return identifier;
    }
    
    /**
     * Создать текстуру с изменённой яркостью.
     */
    public static Identifier createBrightnessAdjustedTexture(String name, Identifier baseTexture, float brightness) {
        String cacheKey = "brightness_" + name + "_" + baseTexture + "_" + brightness;
        
        if (CACHED_TEXTURES.containsKey(cacheKey)) {
            return CACHED_TEXTURES.get(cacheKey);
        }
        
        NativeImage base = TextureUtil.loadTexture(baseTexture);
        if (base == null) {
            Caveborn.LOGGER.error("Failed to load base texture: " + baseTexture);
            return baseTexture;
        }
        
        NativeImage adjusted = TextureUtil.adjustBrightness(base, brightness);
        base.close();
        
        Identifier identifier = registerTexture(name, adjusted);
        CACHED_TEXTURES.put(cacheKey, identifier);
        return identifier;
    }
    
    /**
     * Зарегистрировать текстуру.
     */
    private static Identifier registerTexture(String name, NativeImage image) {
        Identifier identifier = Identifier.of(Caveborn.MOD_ID, "dynamic/" + name);
        
        MinecraftClient.getInstance().execute(() -> {
            NativeImageBackedTexture texture = new NativeImageBackedTexture(() -> identifier.toString(), image);
            MinecraftClient.getInstance().getTextureManager().registerTexture(identifier, texture);
        });
        
        return identifier;
    }
    
    /**
     * Очистить кэш.
     */
    public static void clearCache() {
        CACHED_TEXTURES.clear();
    }
}
