package org.sz.melon_melon.tag;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.sz.melon_melon.Melon_melon;

public class ModTags {
    public static class Items {
        public static final TagKey<Item> PLANTABLE_FRUITS = TagKey.create(
                Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(Melon_melon.MODID, "plantable_fruits"));

        private Items() {
        }
    }

    private ModTags() {
    }
}
