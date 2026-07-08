package org.sz.melon_melon.registry;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

public class ModCreativeTabs {
    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(ModItems.STRANGE_SOIL);
            event.accept(ModItems.STRANGE_FERTILIZER);
        }
    }

    private ModCreativeTabs() {
    }
}
