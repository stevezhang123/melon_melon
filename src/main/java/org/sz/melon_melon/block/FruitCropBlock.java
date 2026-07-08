package org.sz.melon_melon.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.sz.melon_melon.block.entity.FruitCropBlockEntity;
import org.sz.melon_melon.registry.ModBlocks;

public class FruitCropBlock extends BaseEntityBlock {
    public static final MapCodec<FruitCropBlock> CODEC = simpleCodec(FruitCropBlock::new);
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 7);
    public static final int MAX_AGE = 7;

    private static final VoxelShape[] SHAPES = new VoxelShape[] {
            Block.box(6.0, 0.0, 6.0, 10.0, 4.0, 10.0),
            Block.box(5.0, 0.0, 5.0, 11.0, 6.0, 11.0),
            Block.box(4.5, 0.0, 4.5, 11.5, 8.0, 11.5),
            Block.box(4.0, 0.0, 4.0, 12.0, 10.0, 12.0),
            Block.box(3.5, 0.0, 3.5, 12.5, 12.0, 12.5),
            Block.box(3.0, 0.0, 3.0, 13.0, 14.0, 13.0),
            Block.box(2.5, 0.0, 2.5, 13.5, 15.0, 13.5),
            Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0)
    };

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public FruitCropBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FruitCropBlockEntity(pos, state);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, false);
            return;
        }

        int age = state.getValue(AGE);
        if (age >= MAX_AGE) {
            return;
        }

        BlockState soil = level.getBlockState(pos.below());
        int chance = StrangeSoilBlock.isMoist(soil) ? 3 : 10;
        if (random.nextInt(chance) == 0) {
            grow(level, pos, state, 1);
        }
    }

    public static void grow(ServerLevel level, BlockPos pos, BlockState state, int amount) {
        int nextAge = Math.min(MAX_AGE, state.getValue(AGE) + amount);
        if (nextAge != state.getValue(AGE)) {
            level.setBlock(pos, state.setValue(AGE, nextAge), Block.UPDATE_ALL);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return harvestIfMature(state, level, pos);
    }

    @Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hitResult) {
        InteractionResult result = harvestIfMature(state, level, pos);
        return result.consumesAction() ? net.minecraft.world.ItemInteractionResult.sidedSuccess(level.isClientSide) : net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private InteractionResult harvestIfMature(BlockState state, Level level, BlockPos pos) {
        if (state.getValue(AGE) < MAX_AGE) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof FruitCropBlockEntity crop) {
                ItemStack drop = crop.getPlantedStack().copy();
                if (!drop.isEmpty()) {
                    drop.setCount(2 + level.random.nextInt(2));
                    popResource(level, pos, drop);
                }
            }

            level.destroyBlock(pos, false);
            level.levelEvent(2001, pos, Block.getId(state));
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(ModBlocks.STRANGE_SOIL.get());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(AGE)];
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }
}
