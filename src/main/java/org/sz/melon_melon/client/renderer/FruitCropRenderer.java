package org.sz.melon_melon.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.sz.melon_melon.block.FruitCropBlock;
import org.sz.melon_melon.block.entity.FruitCropBlockEntity;

public class FruitCropRenderer implements BlockEntityRenderer<FruitCropBlockEntity> {
    public FruitCropRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FruitCropBlockEntity crop, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack stack = crop.getPlantedStack();
        if (stack.isEmpty()) {
            return;
        }

        BlockState state = crop.getBlockState();
        int age = state.hasProperty(FruitCropBlock.AGE) ? state.getValue(FruitCropBlock.AGE) : 0;
        float scale = 0.25F + age * 0.095F;
        float y = 0.12F + age * 0.045F;

        poseStack.pushPose();
        poseStack.translate(0.5F, y, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees((crop.getBlockPos().asLong() & 3L) * 90.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(scale, scale, scale);

        Level level = crop.getLevel();
        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack,
                ItemDisplayContext.FIXED,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                level,
                (int) crop.getBlockPos().asLong()
        );

        poseStack.popPose();
    }
}
