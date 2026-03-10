package ru.purpir.util;

/**
 * Утилиты для работы с цветами.
 */
public class ColorUtil {
    
    /**
     * Преобразовать RGB в int (формат 0xRRGGBB).
     */
    public static int rgb(int r, int g, int b) {
        return ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }
    
    /**
     * Преобразовать ARGB в int (формат 0xAARRGGBB).
     */
    public static int argb(int a, int r, int g, int b) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }
    
    /**
     * Получить красный компонент из цвета.
     */
    public static int getRed(int color) {
        return (color >> 16) & 0xFF;
    }
    
    /**
     * Получить зелёный компонент из цвета.
     */
    public static int getGreen(int color) {
        return (color >> 8) & 0xFF;
    }
    
    /**
     * Получить синий компонент из цвета.
     */
    public static int getBlue(int color) {
        return color & 0xFF;
    }
    
    /**
     * Получить альфа компонент из цвета.
     */
    public static int getAlpha(int color) {
        return (color >> 24) & 0xFF;
    }
    
    /**
     * Смешать два цвета с заданным соотношением.
     * 
     * @param color1 Первый цвет
     * @param color2 Второй цвет
     * @param ratio Соотношение (0.0 = только color1, 1.0 = только color2)
     * @return Смешанный цвет
     */
    public static int blend(int color1, int color2, float ratio) {
        ratio = Math.max(0, Math.min(1, ratio));
        
        int r1 = getRed(color1);
        int g1 = getGreen(color1);
        int b1 = getBlue(color1);
        
        int r2 = getRed(color2);
        int g2 = getGreen(color2);
        int b2 = getBlue(color2);
        
        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);
        
        return rgb(r, g, b);
    }
    
    /**
     * Применить тональность к цвету (сделать светлее или темнее).
     * 
     * @param color Исходный цвет
     * @param factor Фактор тональности (-1.0 = чёрный, 0.0 = без изменений, 1.0 = белый)
     * @return Цвет с изменённой тональностью
     */
    public static int adjustBrightness(int color, float factor) {
        int r = getRed(color);
        int g = getGreen(color);
        int b = getBlue(color);
        
        if (factor > 0) {
            // Осветление (смешивание с белым)
            r = (int) (r + (255 - r) * factor);
            g = (int) (g + (255 - g) * factor);
            b = (int) (b + (255 - b) * factor);
        } else if (factor < 0) {
            // Затемнение (смешивание с чёрным)
            float darkFactor = 1 + factor;
            r = (int) (r * darkFactor);
            g = (int) (g * darkFactor);
            b = (int) (b * darkFactor);
        }
        
        return rgb(
            Math.max(0, Math.min(255, r)),
            Math.max(0, Math.min(255, g)),
            Math.max(0, Math.min(255, b))
        );
    }
    
    /**
     * Применить насыщенность к цвету.
     * 
     * @param color Исходный цвет
     * @param saturation Насыщенность (0.0 = серый, 1.0 = полная насыщенность)
     * @return Цвет с изменённой насыщенностью
     */
    public static int adjustSaturation(int color, float saturation) {
        int r = getRed(color);
        int g = getGreen(color);
        int b = getBlue(color);
        
        // Вычислить яркость (grayscale)
        int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
        
        // Интерполировать между серым и исходным цветом
        r = (int) (gray + (r - gray) * saturation);
        g = (int) (gray + (g - gray) * saturation);
        b = (int) (gray + (b - gray) * saturation);
        
        return rgb(
            Math.max(0, Math.min(255, r)),
            Math.max(0, Math.min(255, g)),
            Math.max(0, Math.min(255, b))
        );
    }
    
    /**
     * Наложить цвет на другой цвет (multiply blend mode).
     * 
     * @param base Базовый цвет
     * @param overlay Накладываемый цвет
     * @return Результат наложения
     */
    public static int multiply(int base, int overlay) {
        int r = (getRed(base) * getRed(overlay)) / 255;
        int g = (getGreen(base) * getGreen(overlay)) / 255;
        int b = (getBlue(base) * getBlue(overlay)) / 255;
        
        return rgb(r, g, b);
    }
    
    /**
     * Преобразовать hex строку в цвет.
     * 
     * @param hex Hex строка (например, "#FF5733" или "FF5733")
     * @return Цвет в формате int
     */
    public static int fromHex(String hex) {
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        return Integer.parseInt(hex, 16);
    }
    
    /**
     * Преобразовать цвет в hex строку.
     * 
     * @param color Цвет
     * @return Hex строка (например, "FF5733")
     */
    public static String toHex(int color) {
        return String.format("%06X", color & 0xFFFFFF);
    }
}
