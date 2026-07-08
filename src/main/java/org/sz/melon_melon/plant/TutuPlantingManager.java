package org.sz.melon_melon.plant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.NeoForge;
import org.sz.melon_melon.Melon_melon;
import org.sz.melon_melon.config.TutuConfig;
import org.sz.melon_melon.config.TutuPlantStage;
import org.sz.melon_melon.event.TutuCanPlantEvent;

public class TutuPlantingManager {
    private static final Map<Item, List<String>> ITEM_TO_STAGES = new HashMap<>();
    private static final Set<String> WARNED_ITEM_IDS = new HashSet<>();
    private static boolean builtCache;

    public record PlantingResult(boolean allowed, String stageId, String messageKey, List<String> requiredStages) {
    }

    public static PlantingResult canPlant(@Nullable ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty() || stack.is(Items.AIR)) {
            return new PlantingResult(false, "", "message.melon_melon.plant.not_allowed", List.of());
        }

        if (!TutuConfig.enableWhitelist()) {
            if (!TutuConfig.allowAnyItemWhenWhitelistDisabled()) {
                return postEvent(player, stack, new PlantingResult(false, "", "message.melon_melon.plant.not_allowed", List.of()));
            }
            return postEvent(player, stack, new PlantingResult(true, "unrestricted", "", List.of()));
        }

        ensureCache();
        List<String> matchingStages = ITEM_TO_STAGES.getOrDefault(stack.getItem(), List.of());
        if (matchingStages.isEmpty()) {
            return postEvent(player, stack, new PlantingResult(false, "", "message.melon_melon.plant.not_allowed", List.of()));
        }

        for (String stageId : matchingStages) {
            if (TutuStageManager.isStageUnlocked(player, stageId)) {
                return postEvent(player, stack, new PlantingResult(true, stageId, "", matchingStages));
            }
        }

        return postEvent(player, stack, new PlantingResult(false, matchingStages.get(0), "message.melon_melon.plant.stage_locked", matchingStages));
    }

    public static void rebuildCache() {
        ITEM_TO_STAGES.clear();
        WARNED_ITEM_IDS.clear();
        for (TutuPlantStage stage : TutuConfig.stages().values()) {
            for (String configuredItem : stage.itemIds()) {
                ResourceLocation configuredId = ResourceLocation.tryParse(configuredItem);
                if (configuredId == null) {
                    warnOnce(configuredItem, "Invalid item id '{}' in plant stage '{}'", configuredItem, stage.id());
                    continue;
                }

                if (!BuiltInRegistries.ITEM.containsKey(configuredId)) {
                    warnOnce(configuredItem, "Unknown item id '{}' in plant stage '{}'; skipping", configuredItem, stage.id());
                    continue;
                }

                Item item = BuiltInRegistries.ITEM.get(configuredId);
                ITEM_TO_STAGES.computeIfAbsent(item, ignored -> new ArrayList<>()).add(stage.id());
            }
        }

        builtCache = true;
        Melon_melon.LOGGER.info("Resolved {} plantable item entries from configured stages", ITEM_TO_STAGES.size());
    }

    private static void ensureCache() {
        if (!builtCache) {
            rebuildCache();
        }
    }

    private static void warnOnce(String key, String message, Object... args) {
        if (WARNED_ITEM_IDS.add(key)) {
            Melon_melon.LOGGER.warn(message, args);
        }
    }

    private static PlantingResult postEvent(@Nullable ServerPlayer player, ItemStack stack, PlantingResult initial) {
        TutuCanPlantEvent event = new TutuCanPlantEvent(player, stack, initial.allowed(), initial.stageId(), initial.messageKey());
        NeoForge.EVENT_BUS.post(event);
        return new PlantingResult(event.isAllowed(), event.getStageId(), event.getMessageKey(), initial.requiredStages());
    }

    private TutuPlantingManager() {
    }
}
