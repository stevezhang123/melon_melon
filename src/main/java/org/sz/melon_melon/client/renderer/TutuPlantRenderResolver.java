package org.sz.melon_melon.client.renderer;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.GrowingPlantBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.sz.melon_melon.config.TutuConfig;

public final class TutuPlantRenderResolver {
    public static TutuRenderMode resolve(ItemStack stack) {
        if (stack.isEmpty()) {
            return TutuRenderMode.ITEM_BILLBOARD;
        }

        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if (TutuConfig.forceItemBillboard().contains(itemId)) {
            return TutuRenderMode.ITEM_BILLBOARD;
        }
        if (TutuConfig.forceBlockModel().contains(itemId)) {
            return TutuRenderMode.BLOCK_MODEL;
        }

        String defaultMode = TutuConfig.defaultRenderMode().trim().toLowerCase();
        if ("item".equals(defaultMode) || "item_billboard".equals(defaultMode)) {
            return TutuRenderMode.ITEM_BILLBOARD;
        }
        if ("block".equals(defaultMode) || "block_model".equals(defaultMode)) {
            return TutuRenderMode.BLOCK_MODEL;
        }

        if (TutuConfig.cropSeedsUseItemRenderer() && isCropSeedOrPlantItem(stack)) {
            return TutuRenderMode.ITEM_BILLBOARD;
        }

        if (stack.getItem() instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            if (TutuConfig.blockEntityBlocksUseItemRenderer() && block instanceof EntityBlock) {
                return TutuRenderMode.ITEM_BILLBOARD;
            }
            return TutuRenderMode.BLOCK_MODEL;
        }

        return TutuRenderMode.ITEM_BILLBOARD;
    }

    public static BlockState stableStateFor(Block block) {
        BlockState state = block.defaultBlockState();
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH);
        }
        if (state.hasProperty(BlockStateProperties.FACING)) {
            try {
                state = state.setValue(BlockStateProperties.FACING, Direction.SOUTH);
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (state.hasProperty(BlockStateProperties.AXIS)) {
            try {
                state = state.setValue(BlockStateProperties.AXIS, Direction.Axis.Y);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return state;
    }

    private static boolean isCropSeedOrPlantItem(ItemStack stack) {
        if (stack.getItem() instanceof ItemNameBlockItem) {
            return true;
        }
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }

        Block block = blockItem.getBlock();
        return block instanceof CropBlock
                || block instanceof BushBlock
                || block instanceof StemBlock
                || block instanceof AttachedStemBlock
                || block instanceof GrowingPlantBlock;
    }

    private TutuPlantRenderResolver() {
    }
}
