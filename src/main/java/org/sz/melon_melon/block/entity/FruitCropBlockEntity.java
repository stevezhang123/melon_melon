package org.sz.melon_melon.block.entity;

import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.sz.melon_melon.registry.ModBlockEntities;

public class FruitCropBlockEntity extends BlockEntity {
    private static final String PLANTED_STACK_TAG = "PlantedStack";

    private ItemStack plantedStack = ItemStack.EMPTY;

    public FruitCropBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.FRUIT_CROP.get(), pos, blockState);
    }

    public ItemStack getPlantedStack() {
        return this.plantedStack;
    }

    public void setPlantedStack(ItemStack stack) {
        this.plantedStack = stack.copyWithCount(1);
        this.setChanged();

        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.plantedStack.isEmpty()) {
            ItemStack.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), this.plantedStack)
                    .resultOrPartial(error -> {})
                    .ifPresent(encoded -> tag.put(PLANTED_STACK_TAG, encoded));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains(PLANTED_STACK_TAG)) {
            DataResult<ItemStack> result = ItemStack.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get(PLANTED_STACK_TAG));
            this.plantedStack = result.result().orElse(ItemStack.EMPTY);
        } else {
            this.plantedStack = ItemStack.EMPTY;
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
