package org.sz.melon_melon.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.sz.melon_melon.block.entity.TutuSoilBlockEntity;

public class TutuSoilBlockEntityRenderer implements BlockEntityRenderer<TutuSoilBlockEntity> {
    public TutuSoilBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TutuSoilBlockEntity soil, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack stack = soil.getPlantedStack();
        if (stack.isEmpty()) {
            return;
        }

        float growth = Math.max(0.0F, Math.min(1.0F, soil.getGrowth()));
        float scale = 0.25F + growth * 0.75F;

        if (stack.getItem() instanceof BlockItem blockItem) {
            renderBlockItem(soil, blockItem.getBlock(), poseStack, bufferSource, packedLight, scale);
        } else {
            renderVerticalItem(soil, stack, poseStack, bufferSource, packedLight, scale);
        }
    }

    private static void renderBlockItem(TutuSoilBlockEntity soil, Block block, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float scale) {
        try {
            BlockState renderedState = block.defaultBlockState();
            poseStack.pushPose();
            poseStack.translate(0.5D, 1.0D, 0.5D);
            poseStack.scale(scale, scale, scale);
            poseStack.translate(-0.5D, 0.0D, -0.5D);

            BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
            dispatcher.renderSingleBlock(renderedState, poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        } catch (Exception exception) {
            poseStack.popPose();
            renderVerticalItem(soil, soil.getPlantedStack(), poseStack, bufferSource, packedLight, scale);
        }
    }

    private static void renderVerticalItem(TutuSoilBlockEntity soil, ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float scale) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 1.02D + scale * 0.35D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(scale, scale, scale);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack,
                ItemDisplayContext.FIXED,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                soil.getLevel(),
                (int) soil.getBlockPos().asLong()
        );
        poseStack.popPose();
    }
}
