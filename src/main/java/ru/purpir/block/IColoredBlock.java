package ru.purpir.block;

/**
 * Интерфейс для блоков с динамической окраской.
 * Блоки, реализующие этот интерфейс, автоматически получат цветовую окраску.
 */
public interface IColoredBlock {
    
    /**
     * Возвращает цвет блока в формате RGB (0xRRGGBB).
     * 
     * @param tintIndex индекс окраски из модели
     * @return цвет в формате RGB
     */
    int getColor(int tintIndex);
}
