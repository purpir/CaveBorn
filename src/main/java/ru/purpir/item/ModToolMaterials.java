package ru.purpir.item;

import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.BlockTags;

public class ModToolMaterials {
    // Bronze - same as diamond
    public static final ToolMaterial BRONZE = new ToolMaterial(
        BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1561, 8.0f, 3.0f, 10, ModTags.Items.BRONZE_REPAIR);

    // Netherite Titanium - 2x netherite
    public static final ToolMaterial NETHERITE_TITANIUM = new ToolMaterial(
        BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 4062, 18.0f, 8.0f, 20, ModTags.Items.NETHERITE_TITANIUM_REPAIR);
}
