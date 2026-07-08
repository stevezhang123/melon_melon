package org.sz.melon_melon.plant;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import org.sz.melon_melon.Melon_melon;
import org.sz.melon_melon.compat.ftbquests.TutuFtbQuestsCompat;
import org.sz.melon_melon.config.TutuConfig;
import org.sz.melon_melon.config.TutuPlantStage;
import org.sz.melon_melon.event.TutuStageUnlockEvent;

public class TutuStageManager {
    private static final String PERSIST_KEY = "melon_melon:TutuStages";

    public static boolean isStageUnlocked(@Nullable ServerPlayer player, String stageId) {
        TutuPlantStage stage = TutuConfig.stages().get(stageId);
        if (stage == null) {
            return false;
        }

        if (stage.defaultUnlocked() || "always".equals(stage.unlockType())) {
            return true;
        }

        if (player == null) {
            return TutuConfig.fallbackUnlockedStages().contains(stageId);
        }

        if (readManualStages(player).contains(stageId)) {
            return true;
        }

        return switch (stage.unlockType()) {
            case "advancement" -> hasAdvancement(player, stage.unlockId());
            case "ftb_quest" -> TutuFtbQuestsCompat.isQuestCompleted(player, stage.unlockId());
            case "kubejs", "custom_event" -> false;
            default -> {
                Melon_melon.LOGGER.warn("Unknown unlockType '{}' for plant stage '{}'", stage.unlockType(), stage.id());
                yield false;
            }
        };
    }

    public static Set<String> getUnlockedStages(ServerPlayer player) {
        Set<String> stages = new LinkedHashSet<>();
        for (String stageId : TutuConfig.stages().keySet()) {
            if (isStageUnlocked(player, stageId)) {
                stages.add(stageId);
            }
        }
        stages.addAll(readManualStages(player));
        return stages;
    }

    public static void unlockStage(ServerPlayer player, String stageId) {
        Set<String> stages = readManualStages(player);
        if (stages.add(stageId)) {
            writeManualStages(player, stages);
            NeoForge.EVENT_BUS.post(new TutuStageUnlockEvent(player, stageId, true));
        }
    }

    public static void revokeStage(ServerPlayer player, String stageId) {
        Set<String> stages = readManualStages(player);
        if (stages.remove(stageId)) {
            writeManualStages(player, stages);
            NeoForge.EVENT_BUS.post(new TutuStageUnlockEvent(player, stageId, false));
        }
    }

    private static boolean hasAdvancement(ServerPlayer player, String advancementId) {
        ResourceLocation id = ResourceLocation.tryParse(advancementId);
        if (id == null) {
            Melon_melon.LOGGER.warn("Invalid advancement id '{}'", advancementId);
            return false;
        }

        AdvancementHolder advancement = player.server.getAdvancements().get(id);
        if (advancement == null) {
            Melon_melon.LOGGER.warn("Configured advancement '{}' does not exist", advancementId);
            return false;
        }

        return player.getAdvancements().getOrStartProgress(advancement).isDone();
    }

    private static Set<String> readManualStages(ServerPlayer player) {
        Set<String> stages = new LinkedHashSet<>();
        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.contains(PERSIST_KEY, Tag.TAG_LIST)) {
            return stages;
        }

        ListTag list = persistentData.getList(PERSIST_KEY, Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            stages.add(list.getString(i));
        }
        return stages;
    }

    private static void writeManualStages(ServerPlayer player, Set<String> stages) {
        ListTag list = new ListTag();
        for (String stage : stages) {
            list.add(StringTag.valueOf(stage));
        }
        player.getPersistentData().put(PERSIST_KEY, list);
    }

    private TutuStageManager() {
    }
}
