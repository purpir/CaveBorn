package ru.purpir.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import ru.purpir.util.ColorUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * Утилиты для работы с текстурами (только клиент).
 */
@Environment(EnvType.CLIENT)
public class TextureUtil {
    
    /**
     * Загрузить текстуру из ресурсов.
     */
    @Nullable
    public static NativeImage loadTexture(Identifier identifier) {
        String path = String.format("/assets/%s/textures/%s.png", 
            identifier.getNamespace(), identifier.getPath());
        
        try (InputStream stream = TextureUtil.class.getResourceAsStream(path)) {
            if (stream == null) {
                return null;
            }
            return NativeImage.read(stream);
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
     * Создать текстуру с одним цветом.
     */
    public static NativeImage createSolidColor(int width, int height, int argb) {
        NativeImage image = new NativeImage(width, height, true);
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setColorArgb(x, y, argb);
            }
        }
        
        return image;
    }
    
    /**
     * Наложить цвет на текстуру (multiply blend).
     */
    public static NativeImage applyColorOverlay(NativeImage texture, int color) {
        int width = texture.getWidth();
        int height = texture.getHeight();
        NativeImage result = new NativeImage(width, height, true);
        
        int overlayR = ColorUtil.getRed(color);
        int overlayG = ColorUtil.getGreen(color);
        int overlayB = ColorUtil.getBlue(color);
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int argb = texture.getColorArgb(x, y);
                
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                
                r = (r * overlayR) / 255;
                g = (g * overlayG) / 255;
                b = (b * overlayB) / 255;
                
                int newArgb = (a << 24) | (r << 16) | (g << 8) | b;
                result.setColorArgb(x, y, newArgb);
            }
        }
        
        return result;
    }
    
    /**
     * Изменить яркость текстуры.
     */
    public static NativeImage adjustBrightness(NativeImage texture, float factor) {
        int width = texture.getWidth();
        int height = texture.getHeight();
        NativeImage result = new NativeImage(width, height, true);
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int argb = texture.getColorArgb(x, y);
                
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                
                if (factor > 0) {
                    r = (int) (r + (255 - r) * factor);
                    g = (int) (g + (255 - g) * factor);
                    b = (int) (b + (255 - b) * factor);
                } else if (factor < 0) {
                    float darkFactor = 1 + factor;
                    r = (int) (r * darkFactor);
                    g = (int) (g * darkFactor);
                    b = (int) (b * darkFactor);
                }
                
                r = Math.max(0, Math.min(255, r));
                g = Math.max(0, Math.min(255, g));
                b = Math.max(0, Math.min(255, b));
                
                int newArgb = (a << 24) | (r << 16) | (g << 8) | b;
                result.setColorArgb(x, y, newArgb);
            }
        }
        
        return result;
    }
    
    /**
     * Изменить насыщенность текстуры.
     */
    public static NativeImage adjustSaturation(NativeImage texture, float saturation) {
        int width = texture.getWidth();
        int height = texture.getHeight();
        NativeImage result = new NativeImage(width, height, true);
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int argb = texture.getColorArgb(x, y);
                
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                
                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                
                r = (int) (gray + (r - gray) * saturation);
                g = (int) (gray + (g - gray) * saturation);
                b = (int) (gray + (b - gray) * saturation);
                
                r = Math.max(0, Math.min(255, r));
                g = Math.max(0, Math.min(255, g));
                b = Math.max(0, Math.min(255, b));
                
                int newArgb = (a << 24) | (r << 16) | (g << 8) | b;
                result.setColorArgb(x, y, newArgb);
            }
        }
        
        return result;
    }
    
    /**
     * Применить оттенок к текстуре.
     */
    public static NativeImage applyHue(NativeImage texture, float hue) {
        int width = texture.getWidth();
        int height = texture.getHeight();
        NativeImage result = new NativeImage(width, height, true);
        
        hue = hue % 360;
        if (hue < 0) hue += 360;
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int argb = texture.getColorArgb(x, y);
                
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                
                float[] hsv = rgbToHsv(r, g, b);
                hsv[0] = hue;
                int[] rgb = hsvToRgb(hsv[0], hsv[1], hsv[2]);
                
                int newArgb = (a << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
                result.setColorArgb(x, y, newArgb);
            }
        }
        
        return result;
    }
    
    private static float[] rgbToHsv(int r, int g, int b) {
        float rf = r / 255f;
        float gf = g / 255f;
        float bf = b / 255f;
        
        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float delta = max - min;
        
        float h = 0;
        float s = max == 0 ? 0 : delta / max;
        float v = max;
        
        if (delta != 0) {
            if (max == rf) {
                h = 60 * (((gf - bf) / delta) % 6);
            } else if (max == gf) {
                h = 60 * (((bf - rf) / delta) + 2);
            } else {
                h = 60 * (((rf - gf) / delta) + 4);
            }
        }
        
        if (h < 0) h += 360;
        
        return new float[]{h, s, v};
    }
    
    private static int[] hsvToRgb(float h, float s, float v) {
        float c = v * s;
        float x = c * (1 - Math.abs(((h / 60) % 2) - 1));
        float m = v - c;
        
        float rf, gf, bf;
        
        if (h < 60) {
            rf = c; gf = x; bf = 0;
        } else if (h < 120) {
            rf = x; gf = c; bf = 0;
        } else if (h < 180) {
            rf = 0; gf = c; bf = x;
        } else if (h < 240) {
            rf = 0; gf = x; bf = c;
        } else if (h < 300) {
            rf = x; gf = 0; bf = c;
        } else {
            rf = c; gf = 0; bf = x;
        }
        
        int r = (int) ((rf + m) * 255);
        int g = (int) ((gf + m) * 255);
        int b = (int) ((bf + m) * 255);
        
        return new int[]{r, g, b};
    }
}
