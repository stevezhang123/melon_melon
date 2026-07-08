package org.sz.melon_melon.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.sz.melon_melon.block.entity.TutuSoilBlockEntity;
import org.sz.melon_melon.config.TutuConfig;
import org.sz.melon_melon.item.StrangeFertilizerItem;
import org.sz.melon_melon.plant.TutuPlantingManager;
import org.sz.melon_melon.registry.ModBlockEntities;
import org.sz.melon_melon.registry.ModItems;

public class StrangeSoilBlock extends FarmBlock implements EntityBlock {
    public static final BooleanProperty HAS_PLANT = BooleanProperty.create("has_plant");

    public StrangeSoilBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(HAS_PLANT, false));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TutuSoilBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide || blockEntityType != ModBlockEntities.TUTU_SOIL.get()
                ? null
                : (tickerLevel, pos, tickerState, blockEntity) -> TutuSoilBlockEntity.serverTick(tickerLevel, pos, tickerState, (TutuSoilBlockEntity) blockEntity);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof TutuSoilBlockEntity soil)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (stack.is(ModItems.STRANGE_FERTILIZER.get())) {
            StrangeFertilizerItem.applyToTutuSoil(level, pos, player, stack, soil);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        if (soil.hasPlant()) {
            return handleHarvestOrNotMature(level, pos, player, soil);
        }

        if (stack.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        ServerPlayer serverPlayer = player instanceof ServerPlayer sp ? sp : null;
        TutuPlantingManager.PlantingResult result = TutuPlantingManager.canPlant(serverPlayer, stack);
        if (!result.allowed()) {
            if (!level.isClientSide && !result.messageKey().isBlank()) {
                player.displayClientMessage(Component.translatable(result.messageKey(), result.stageId()), true);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!level.isClientSide) {
            soil.plant(stack.copyWithCount(1), result.stageId(), level.getGameTime());
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            level.levelEvent(2001, pos.above(), Block.getId(state));
        }

        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected net.minecraft.world.InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof TutuSoilBlockEntity soil && soil.hasPlant()) {
            ItemInteractionResult result = handleHarvestOrNotMature(level, pos, player, soil);
            return result.consumesAction() ? net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide) : net.minecraft.world.InteractionResult.PASS;
        }

        return net.minecraft.world.InteractionResult.PASS;
    }

    private ItemInteractionResult handleHarvestOrNotMature(Level level, BlockPos pos, Player player, TutuSoilBlockEntity soil) {
        if (!soil.isMature()) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.melon_melon.plant.not_mature"), true);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!level.isClientSide) {
            ItemStack drop = soil.getPlantedStack().copy();
            if (!drop.isEmpty()) {
                drop.setCount(1);
                popResource(level, pos.above(), drop);
            }
            soil.resetAfterHarvest(level.getGameTime());
            level.levelEvent(2001, pos.above(), Block.getId(level.getBlockState(pos)));
        }

        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof TutuSoilBlockEntity soil && soil.hasPlant()) {
            if (!level.isClientSide) {
                ItemStack removed = soil.clearPlant();
                if (!removed.isEmpty()) {
                    popResource(level, pos.above(), removed);
                }
                level.levelEvent(2001, pos.above(), Block.getId(state));
            }
            return;
        }

        super.attack(state, level, pos, player);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int moisture = state.getValue(MOISTURE);
        if (isNearWater(level, pos, TutuConfig.waterRange()) || level.isRainingAt(pos.above())) {
            if (moisture < MAX_MOISTURE) {
                level.setBlock(pos, state.setValue(MOISTURE, MAX_MOISTURE), Block.UPDATE_CLIENTS);
            }
        } else if (moisture > 0) {
            level.setBlock(pos, state.setValue(MOISTURE, moisture - 1), Block.UPDATE_CLIENTS);
        }
    }

    public static boolean isMoist(BlockState state) {
        return state.hasProperty(MOISTURE) && state.getValue(MOISTURE) > 0;
    }

    public static boolean isNearWater(LevelReader level, BlockPos pos, int range) {
        for (BlockPos waterPos : BlockPos.betweenClosed(pos.offset(-range, 0, -range), pos.offset(range, 1, range))) {
            if (level.getFluidState(waterPos).is(FluidTags.WATER)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState above = level.getBlockState(pos.above());
        return !above.isSolid();
    }

    @Override
    protected boolean isPathfindable(BlockState state, net.minecraft.world.level.pathfinder.PathComputationType pathComputationType) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HAS_PLANT);
    }
}
