package ru.purpir.item;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import ru.purpir.Caveborn;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> NEEDS_NETHERITE_TOOL = createTag("needs_netherite_tool");

        private static TagKey<Block> createTag(String name) {
            return TagKey.of(RegistryKeys.BLOCK, Identifier.of(Caveborn.MOD_ID, name));
        }
    }

    public static class Items {
        public static final TagKey<Item> BRONZE_INGOTS = createTag("bronze_ingots");
        public static final TagKey<Item> TITANIUM_INGOTS = createTag("titanium_ingots");
        public static final TagKey<Item> BRONZE_REPAIR = createTag("bronze_repair");
        public static final TagKey<Item> NETHERITE_TITANIUM_REPAIR = createTag("netherite_titanium_repair");

        private static TagKey<Item> createTag(String name) {
            return TagKey.of(RegistryKeys.ITEM, Identifier.of(Caveborn.MOD_ID, name));
        }
    }
}
