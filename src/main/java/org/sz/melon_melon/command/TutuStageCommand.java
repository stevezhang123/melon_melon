package org.sz.melon_melon.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.sz.melon_melon.config.TutuConfig;
import org.sz.melon_melon.plant.TutuStageManager;

public class TutuStageCommand {
    public static void register(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tutu")
                .then(Commands.literal("stage")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("unlock")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("stageId", StringArgumentType.word())
                                                .executes(context -> unlock(context.getSource(), EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "stageId"))))))
                        .then(Commands.literal("revoke")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("stageId", StringArgumentType.word())
                                                .executes(context -> revoke(context.getSource(), EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "stageId"))))))
                        .then(Commands.literal("list")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> list(context.getSource(), EntityArgument.getPlayer(context, "player")))))));
    }

    private static int unlock(CommandSourceStack source, ServerPlayer player, String stageId) {
        TutuStageManager.unlockStage(player, stageId);
        source.sendSuccess(() -> Component.translatable("message.melon_melon.stage.unlocked", player.getGameProfile().getName(), stageId), true);
        return 1;
    }

    private static int revoke(CommandSourceStack source, ServerPlayer player, String stageId) {
        TutuStageManager.revokeStage(player, stageId);
        source.sendSuccess(() -> Component.translatable("message.melon_melon.stage.revoked", player.getGameProfile().getName(), stageId), true);
        return 1;
    }

    private static int list(CommandSourceStack source, ServerPlayer player) {
        String stages = String.join(", ", TutuStageManager.getUnlockedStages(player));
        if (stages.isBlank()) {
            stages = "-";
        }
        String stageText = stages;
        source.sendSuccess(() -> Component.translatable("message.melon_melon.stage.list", player.getGameProfile().getName(), stageText), false);
        return TutuConfig.stages().size();
    }

    private TutuStageCommand() {
    }
}
