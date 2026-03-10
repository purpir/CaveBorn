package ru.purpir.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import ru.purpir.client.util.DynamicTextureGenerator;

/**
 * Инициализация динамических текстур на клиенте.
 */
@Environment(EnvType.CLIENT)
public class TextureInitializer {
    
    public static void initialize() {
        // Создать красную текстуру для тестового блока
        DynamicTextureGenerator.createSolidColorTexture(
            "test_colored_block", 
            0xFF3333, // Красный цвет
            16
        );
        
        // Создать синюю версию алмаза
        DynamicTextureGenerator.createColoredTexture(
            "blue_diamond",
            Identifier.of("minecraft", "item/diamond"),
            0x3333FF
        );
        
        // Создать тёмную версию камня
        DynamicTextureGenerator.createBrightnessAdjustedTexture(
            "dark_stone",
            Identifier.of("minecraft", "block/stone"),
            -0.4f
        );
        
        // Создать золотую версию железной руды
        DynamicTextureGenerator.createColoredTexture(
            "golden_iron_ore",
            Identifier.of("minecraft", "block/iron_ore"),
            0xFFD700
        );
    }
}

