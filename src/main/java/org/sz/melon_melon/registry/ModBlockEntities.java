package org.sz.melon_melon.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.sz.melon_melon.Melon_melon;
import org.sz.melon_melon.block.entity.FruitCropBlockEntity;
import org.sz.melon_melon.block.entity.TutuSoilBlockEntity;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Melon_melon.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FruitCropBlockEntity>> FRUIT_CROP = BLOCK_ENTITY_TYPES.register(
            "fruit_crop",
            () -> BlockEntityType.Builder.of(FruitCropBlockEntity::new, ModBlocks.FRUIT_CROP.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TutuSoilBlockEntity>> TUTU_SOIL = BLOCK_ENTITY_TYPES.register(
            "tutu_soil",
            () -> BlockEntityType.Builder.of(TutuSoilBlockEntity::new, ModBlocks.STRANGE_SOIL.get()).build(null));

    private ModBlockEntities() {
    }
}
