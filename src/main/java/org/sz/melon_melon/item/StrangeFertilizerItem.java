package org.sz.melon_melon.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.sz.melon_melon.block.entity.TutuSoilBlockEntity;
import org.sz.melon_melon.config.TutuConfig;
import org.sz.melon_melon.block.FruitCropBlock;
import org.sz.melon_melon.registry.ModBlocks;

public class StrangeFertilizerItem extends Item {
    public StrangeFertilizerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (level.getBlockEntity(pos) instanceof TutuSoilBlockEntity soil) {
            return applyToTutuSoil(level, pos, context.getPlayer(), context.getItemInHand(), soil);
        }

        if (!state.is(ModBlocks.FRUIT_CROP.get()) || state.getValue(FruitCropBlock.AGE) >= FruitCropBlock.MAX_AGE) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            FruitCropBlock.grow((ServerLevel) level, pos, state, 1 + level.random.nextInt(2));
            if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
            level.levelEvent(1505, pos, 8);
            level.gameEvent(context.getPlayer(), GameEvent.ITEM_INTERACT_FINISH, pos);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public static InteractionResult applyToTutuSoil(Level level, BlockPos pos, Player player, ItemStack fertilizerStack, TutuSoilBlockEntity soil) {
        if (!TutuConfig.enableFeifei()) {
            sendActionMessage(level, player, "message.melon_melon.feifei.disabled");
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!soil.hasPlant()) {
            sendActionMessage(level, player, "message.melon_melon.feifei.no_plant");
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (soil.isMature()) {
            sendActionMessage(level, player, "message.melon_melon.feifei.already_mature");
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!level.isClientSide) {
            soil.addGrowth((float) TutuConfig.feifeiGrowthAmount());
            if (player == null || !player.getAbilities().instabuild || TutuConfig.feifeiConsumeInCreative()) {
                fertilizerStack.shrink(1);
            }
            level.levelEvent(1505, pos.above(), 8);
            level.gameEvent(player, GameEvent.ITEM_INTERACT_FINISH, pos);
            sendActionMessage(level, player, "message.melon_melon.feifei.success");
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void sendActionMessage(Level level, Player player, String messageKey) {
        if (!level.isClientSide && player != null) {
            player.displayClientMessage(Component.translatable(messageKey), true);
        }
    }
}
