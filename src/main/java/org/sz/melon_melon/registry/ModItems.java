package org.sz.melon_melon.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.sz.melon_melon.Melon_melon;
import org.sz.melon_melon.item.StrangeFertilizerItem;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Melon_melon.MODID);

    public static final DeferredItem<BlockItem> STRANGE_SOIL = ITEMS.registerSimpleBlockItem("strange_soil", ModBlocks.STRANGE_SOIL);

    public static final DeferredItem<Item> STRANGE_FERTILIZER = ITEMS.register(
            "strange_fertilizer",
            () -> new StrangeFertilizerItem(new Item.Properties()));

    private ModItems() {
    }
}
