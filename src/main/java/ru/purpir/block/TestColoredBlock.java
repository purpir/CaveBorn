package ru.purpir.block;

import net.minecraft.block.Block;

/**
 * Тестовый блок для проверки системы динамических текстур.
 */
public class TestColoredBlock extends Block implements IColoredBlock {
    
    public TestColoredBlock(Settings settings) {
        super(settings);
    }
    
    @Override
    public int getColor(int tintIndex) {
        return 0xFF0000; // Красный цвет
    }
}
