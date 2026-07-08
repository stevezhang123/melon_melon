package org.sz.melon_melon;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.sz.melon_melon.command.TutuStageCommand;
import org.sz.melon_melon.config.TutuConfig;
import org.sz.melon_melon.plant.TutuPlantingManager;
import org.sz.melon_melon.registry.ModBlockEntities;
import org.sz.melon_melon.registry.ModBlocks;
import org.sz.melon_melon.registry.ModCreativeTabs;
import org.sz.melon_melon.registry.ModItems;

@Mod(Melon_melon.MODID)
public class Melon_melon {
    public static final String MODID = "melon_melon";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Melon_melon(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);

        modEventBus.addListener(ModCreativeTabs::addCreative);
        NeoForge.EVENT_BUS.addListener(TutuStageCommand::register);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        TutuConfig.load();
        TutuPlantingManager.rebuildCache();
    }
}
