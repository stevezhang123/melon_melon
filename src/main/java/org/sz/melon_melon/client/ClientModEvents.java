package org.sz.melon_melon.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.sz.melon_melon.Melon_melon;
import org.sz.melon_melon.client.renderer.FruitCropRenderer;
import org.sz.melon_melon.client.renderer.TutuSoilBlockEntityRenderer;
import org.sz.melon_melon.registry.ModBlockEntities;

@EventBusSubscriber(modid = Melon_melon.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.FRUIT_CROP.get(), FruitCropRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TUTU_SOIL.get(), TutuSoilBlockEntityRenderer::new);
    }
}
