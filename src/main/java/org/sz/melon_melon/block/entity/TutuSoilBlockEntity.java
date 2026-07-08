package org.sz.melon_melon.block.entity;

import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.sz.melon_melon.block.StrangeSoilBlock;
import org.sz.melon_melon.config.TutuConfig;
import org.sz.melon_melon.registry.ModBlockEntities;

public class TutuSoilBlockEntity extends BlockEntity {
    private static final String PLANTED_STACK_TAG = "PlantedStack";
    private static final String GROWTH_TAG = "Growth";
    private static final String STAGE_ID_TAG = "StageId";
    private static final String PLANTED_AT_TAG = "PlantedAtGameTime";

    private ItemStack plantedStack = ItemStack.EMPTY;
    private float growth;
    private String stageId = "";
    private long plantedAtGameTime;

    public TutuSoilBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.TUTU_SOIL.get(), pos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TutuSoilBlockEntity soil) {
        if (!(level instanceof ServerLevel serverLevel) || !soil.hasPlant() || soil.isMature()) {
            return;
        }

        if (TutuConfig.requireWater() && !StrangeSoilBlock.isNearWater(level, pos, TutuConfig.waterRange()) && !level.isRainingAt(pos.above())) {
            return;
        }

        if (TutuConfig.pauseGrowthWhenCovered() && !level.isEmptyBlock(pos.above())) {
            return;
        }

        soil.addGrowth(1.0F / TutuConfig.growthTicks());
    }

    public boolean hasPlant() {
        return !this.plantedStack.isEmpty();
    }

    public boolean isMature() {
        return this.growth >= 1.0F;
    }

    public ItemStack getPlantedStack() {
        return this.plantedStack;
    }

    public float getGrowth() {
        return this.growth;
    }

    public String getStageId() {
        return this.stageId;
    }

    public void plant(ItemStack stack, String stageId, long gameTime) {
        this.plantedStack = stack.copyWithCount(1);
        this.growth = 0.0F;
        this.stageId = stageId;
        this.plantedAtGameTime = gameTime;
        this.syncPlantState();
    }

    public ItemStack clearPlant() {
        ItemStack removed = this.plantedStack.copyWithCount(1);
        this.plantedStack = ItemStack.EMPTY;
        this.growth = 0.0F;
        this.stageId = "";
        this.plantedAtGameTime = 0L;
        this.syncPlantState();
        return removed;
    }

    public void resetAfterHarvest(long gameTime) {
        if (!this.plantedStack.isEmpty()) {
            this.plantedStack = this.plantedStack.copyWithCount(1);
            this.growth = 0.0F;
            this.plantedAtGameTime = gameTime;
            this.syncPlantState();
        }
    }

    public void addGrowth(float amount) {
        if (!this.hasPlant() || amount <= 0.0F) {
            return;
        }

        float nextGrowth = Math.min(1.0F, this.growth + amount);
        if (nextGrowth != this.growth) {
            this.growth = nextGrowth;
            this.syncPlantState();
        }
    }

    private void syncPlantState() {
        this.setChanged();
        if (this.level != null) {
            BlockState state = this.level.getBlockState(this.worldPosition);
            if (state.hasProperty(StrangeSoilBlock.HAS_PLANT) && state.getValue(StrangeSoilBlock.HAS_PLANT) != this.hasPlant()) {
                this.level.setBlock(this.worldPosition, state.setValue(StrangeSoilBlock.HAS_PLANT, this.hasPlant()), Block.UPDATE_ALL);
                state = this.level.getBlockState(this.worldPosition);
            }

            if (!this.level.isClientSide) {
                this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_ALL);
            }
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
        tag.putFloat(GROWTH_TAG, this.growth);
        tag.putString(STAGE_ID_TAG, this.stageId);
        tag.putLong(PLANTED_AT_TAG, this.plantedAtGameTime);
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
        this.growth = tag.getFloat(GROWTH_TAG);
        this.stageId = tag.getString(STAGE_ID_TAG);
        this.plantedAtGameTime = tag.getLong(PLANTED_AT_TAG);
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
