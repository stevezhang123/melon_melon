package org.sz.melon_melon.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.sz.melon_melon.Melon_melon;
import org.sz.melon_melon.block.FruitCropBlock;
import org.sz.melon_melon.block.StrangeSoilBlock;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Melon_melon.MODID);

    public static final DeferredBlock<StrangeSoilBlock> STRANGE_SOIL = BLOCKS.register(
            "strange_soil",
            () -> new StrangeSoilBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(0.6F)
                    .sound(SoundType.GRAVEL)
                    .randomTicks()));

    public static final DeferredBlock<FruitCropBlock> FRUIT_CROP = BLOCKS.register(
            "fruit_crop",
            () -> new FruitCropBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.CROP)
                    .pushReaction(PushReaction.DESTROY)));

    private ModBlocks() {
    }
}
