package org.sz.melon_melon.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.neoforged.fml.loading.FMLPaths;
import org.sz.melon_melon.Melon_melon;

public class TutuConfig {
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("melon_melon-tutu.toml");

    private static boolean debugLog = false;
    private static boolean allowDecayToVanillaDirt = false;
    private static boolean tramplingAffectsMoisture = true;
    private static boolean tramplingDriesOnly = true;
    private static boolean tramplingBreaksPlant = false;
    private static boolean pauseGrowthWhenCovered = true;
    private static boolean hidePlantRenderWhenCovered = false;
    private static boolean enableWhitelist = true;
    private static boolean allowAnyItemWhenWhitelistDisabled = true;
    private static List<String> fallbackUnlockedStages = List.of("starter");
    private static int growthTicks = 24000;
    private static boolean requireWater = true;
    private static int waterRange = 4;
    private static boolean bonemealAllowed = true;
    private static double bonemealGrowthAmount = 0.25D;
    private static boolean preferPlantingOverPlacing = true;
    private static boolean sneakBypassesPlanting = true;
    private static boolean allowPlaceOnSoilWhenCannotPlant = true;
    private static boolean preventPlacementForPlantableItems = true;
    private static String defaultRenderMode = "auto";
    private static boolean cropSeedsUseItemRenderer = true;
    private static boolean blockEntityBlocksUseItemRenderer = true;
    private static boolean fallbackToItemRenderer = true;
    private static List<String> forceItemBillboard = List.of(
            "minecraft:wheat_seeds",
            "minecraft:beetroot_seeds",
            "farmersdelight:rice",
            "farmersdelight:tomato_seeds",
            "farmersdelight:cabbage_seeds",
            "create:cogwheel",
            "create:large_cogwheel"
    );
    private static List<String> forceBlockModel = List.of("minecraft:stone");
    private static boolean enableFeifei = true;
    private static double feifeiGrowthAmount = 0.25D;
    private static boolean feifeiConsumeInCreative = false;
    private static Map<String, TutuPlantStage> stages = Map.of();

    public static void load() {
        writeDefaultConfigIfMissing();
        appendMissingConfigSections();

        try (CommentedFileConfig config = CommentedFileConfig.builder(CONFIG_PATH).sync().preserveInsertionOrder().build()) {
            config.load();
            debugLog = config.getOrElse("general.debugLog", false);
            allowDecayToVanillaDirt = config.getOrElse("soil.allowDecayToVanillaDirt", false);
            tramplingAffectsMoisture = config.getOrElse("soil.tramplingAffectsMoisture", true);
            tramplingDriesOnly = config.getOrElse("soil.tramplingDriesOnly", true);
            tramplingBreaksPlant = config.getOrElse("soil.tramplingBreaksPlant", false);
            pauseGrowthWhenCovered = config.getOrElse("soil.pauseGrowthWhenCovered", true);
            hidePlantRenderWhenCovered = config.getOrElse("soil.hidePlantRenderWhenCovered", false);
            enableWhitelist = config.getOrElse("planting.enableWhitelist", true);
            allowAnyItemWhenWhitelistDisabled = config.getOrElse("planting.allowAnyItemWhenWhitelistDisabled", true);
            fallbackUnlockedStages = new ArrayList<>(config.getOrElse("planting.fallbackUnlockedStages", List.of("starter")));
            growthTicks = Math.max(1, config.getOrElse("growth.growthTicks", 24000));
            requireWater = config.getOrElse("growth.requireWater", true);
            waterRange = Math.max(0, config.getOrElse("growth.waterRange", 4));
            bonemealAllowed = config.getOrElse("growth.bonemealAllowed", true);
            bonemealGrowthAmount = Math.max(0.0D, config.getOrElse("growth.bonemealGrowthAmount", 0.25D));
            preferPlantingOverPlacing = config.getOrElse("interaction.preferPlantingOverPlacing", true);
            sneakBypassesPlanting = config.getOrElse("interaction.sneakBypassesPlanting", true);
            allowPlaceOnSoilWhenCannotPlant = config.getOrElse("interaction.allowPlaceOnSoilWhenCannotPlant", true);
            preventPlacementForPlantableItems = config.getOrElse("interaction.preventPlacementForPlantableItems", true);
            defaultRenderMode = config.getOrElse("rendering.defaultRenderMode", "auto");
            cropSeedsUseItemRenderer = config.getOrElse("rendering.cropSeedsUseItemRenderer", true);
            blockEntityBlocksUseItemRenderer = config.getOrElse("rendering.blockEntityBlocksUseItemRenderer", true);
            fallbackToItemRenderer = config.getOrElse("rendering.fallbackToItemRenderer", true);
            forceItemBillboard = new ArrayList<>(config.getOrElse("rendering.forceItemBillboard", forceItemBillboard));
            forceBlockModel = new ArrayList<>(config.getOrElse("rendering.forceBlockModel", forceBlockModel));
            enableFeifei = config.getOrElse("feifei.enableFeifei", bonemealAllowed);
            feifeiGrowthAmount = Math.max(0.0D, config.getOrElse("feifei.growthAmount", bonemealGrowthAmount));
            feifeiConsumeInCreative = config.getOrElse("feifei.consumeInCreative", false);

            Map<String, TutuPlantStage> parsedStages = new LinkedHashMap<>();
            List<? extends CommentedFileConfig> ignored = List.of();
            List<?> rawStages = config.getOrElse("stages", ignored);
            for (Object rawStage : rawStages) {
                if (!(rawStage instanceof com.electronwill.nightconfig.core.Config stageConfig)) {
                    Melon_melon.LOGGER.warn("Ignored invalid stage config entry: {}", rawStage);
                    continue;
                }

                String id = stageConfig.getOrElse("id", "");
                if (id.isBlank()) {
                    Melon_melon.LOGGER.warn("Ignored plant stage with empty id");
                    continue;
                }

                if (parsedStages.containsKey(id)) {
                    Melon_melon.LOGGER.warn("Duplicate plant stage id '{}'; later entry overrides earlier entry", id);
                }

                String displayName = stageConfig.getOrElse("displayName", id);
                boolean defaultUnlocked = stageConfig.getOrElse("defaultUnlocked", false);
                String unlockType = stageConfig.getOrElse("unlockType", defaultUnlocked ? "always" : "kubejs");
                String unlockId = stageConfig.getOrElse("unlockId", "");
                List<String> items = new ArrayList<>(stageConfig.getOrElse("items", List.<String>of()));
                parsedStages.put(id, new TutuPlantStage(id, displayName, defaultUnlocked, unlockType, unlockId, items));
            }

            stages = Map.copyOf(parsedStages);
            Melon_melon.LOGGER.info("Loaded {} plant stages; whitelist is {}", stages.size(), enableWhitelist ? "enabled" : "disabled");
        } catch (Exception exception) {
            Melon_melon.LOGGER.error("Failed to load {}. Falling back to built-in starter stage.", CONFIG_PATH, exception);
            stages = Map.of("starter", new TutuPlantStage("starter", "起步阶段", true, "always", "", List.of(
                    "minecraft:wheat", "minecraft:beetroot", "minecraft:melon_slice", "minecraft:carrot", "minecraft:potato"
            )));
        }
    }

    public static boolean enableWhitelist() {
        return enableWhitelist;
    }

    public static boolean debugLog() {
        return debugLog;
    }

    public static boolean allowDecayToVanillaDirt() {
        return allowDecayToVanillaDirt;
    }

    public static boolean tramplingAffectsMoisture() {
        return tramplingAffectsMoisture;
    }

    public static boolean tramplingDriesOnly() {
        return tramplingDriesOnly;
    }

    public static boolean tramplingBreaksPlant() {
        return tramplingBreaksPlant;
    }

    public static boolean pauseGrowthWhenCovered() {
        return pauseGrowthWhenCovered;
    }

    public static boolean hidePlantRenderWhenCovered() {
        return hidePlantRenderWhenCovered;
    }

    public static boolean allowAnyItemWhenWhitelistDisabled() {
        return allowAnyItemWhenWhitelistDisabled;
    }

    public static List<String> fallbackUnlockedStages() {
        return fallbackUnlockedStages;
    }

    public static int growthTicks() {
        return growthTicks;
    }

    public static boolean requireWater() {
        return requireWater;
    }

    public static int waterRange() {
        return waterRange;
    }

    public static boolean bonemealAllowed() {
        return bonemealAllowed;
    }

    public static double bonemealGrowthAmount() {
        return bonemealGrowthAmount;
    }

    public static boolean preferPlantingOverPlacing() {
        return preferPlantingOverPlacing;
    }

    public static boolean sneakBypassesPlanting() {
        return sneakBypassesPlanting;
    }

    public static boolean allowPlaceOnSoilWhenCannotPlant() {
        return allowPlaceOnSoilWhenCannotPlant;
    }

    public static boolean preventPlacementForPlantableItems() {
        return preventPlacementForPlantableItems;
    }

    public static String defaultRenderMode() {
        return defaultRenderMode;
    }

    public static boolean cropSeedsUseItemRenderer() {
        return cropSeedsUseItemRenderer;
    }

    public static boolean blockEntityBlocksUseItemRenderer() {
        return blockEntityBlocksUseItemRenderer;
    }

    public static boolean fallbackToItemRenderer() {
        return fallbackToItemRenderer;
    }

    public static List<String> forceItemBillboard() {
        return forceItemBillboard;
    }

    public static List<String> forceBlockModel() {
        return forceBlockModel;
    }

    public static boolean enableFeifei() {
        return enableFeifei;
    }

    public static double feifeiGrowthAmount() {
        return feifeiGrowthAmount;
    }

    public static boolean feifeiConsumeInCreative() {
        return feifeiConsumeInCreative;
    }

    public static Map<String, TutuPlantStage> stages() {
        return stages;
    }

    private static void writeDefaultConfigIfMissing() {
        if (Files.exists(CONFIG_PATH)) {
            return;
        }

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, DEFAULT_CONFIG);
        } catch (Exception exception) {
            Melon_melon.LOGGER.error("Failed to create default config {}", CONFIG_PATH, exception);
        }
    }

    private static void appendMissingConfigSections() {
        try {
            String existingConfig = Files.readString(CONFIG_PATH);
            StringBuilder additions = new StringBuilder();
            if (!existingConfig.contains("[general]")) {
                additions.append('\n').append(DEFAULT_GENERAL_CONFIG);
            }
            if (!existingConfig.contains("[soil]")) {
                additions.append('\n').append(DEFAULT_SOIL_CONFIG);
            }
            if (!existingConfig.contains("[interaction]")) {
                additions.append('\n').append(DEFAULT_INTERACTION_CONFIG);
            }
            if (!existingConfig.contains("[rendering]")) {
                additions.append('\n').append(DEFAULT_RENDERING_CONFIG);
            }
            if (!existingConfig.contains("[feifei]")) {
                additions.append('\n').append(DEFAULT_FEIFEI_CONFIG);
            }
            if (!additions.isEmpty()) {
                Files.writeString(CONFIG_PATH, existingConfig + additions);
            }
        } catch (Exception exception) {
            Melon_melon.LOGGER.warn("Failed to append missing Tutu config sections to {}", CONFIG_PATH, exception);
        }
    }

    private static final String DEFAULT_CONFIG = """
            # ============================================================
            # 种瓜得瓜 / Melon Melon - Common Config
            # CN: 这个配置文件控制土土的种植、成长、渲染和阶段解锁。
            # EN: This file controls Tutu Soil planting, growth, rendering, and stage unlocks.
            # ============================================================

            [general]
            # CN: 是否启用调试日志。遇到兼容问题时可以打开。
            # EN: Enables debug logs. Useful when diagnosing compatibility issues.
            debugLog = false

            [soil]
            # CN: 土土是否允许像原版耕地一样退化成普通泥土。
            # EN: Whether Tutu Soil can decay into vanilla dirt like farmland.
            allowDecayToVanillaDirt = false

            # CN: 实体踩踏土土时是否影响湿润度。
            # EN: Whether entity trampling affects Tutu Soil moisture.
            tramplingAffectsMoisture = true

            # CN: 被踩踏时只回退为未润湿土土，而不是变成泥土。
            # EN: When trampled, Tutu Soil dries out instead of turning into dirt.
            tramplingDriesOnly = true

            # CN: 被踩踏时是否破坏土土上的植株。
            # EN: Whether trampling breaks the plant on Tutu Soil.
            tramplingBreaksPlant = false

            # CN: 上方有方块时是否暂停成长。
            # EN: Pause growth when the space above Tutu Soil is occupied.
            pauseGrowthWhenCovered = true

            # CN: 上方有方块时是否隐藏种植物渲染。
            # EN: Hide plant rendering when the space above Tutu Soil is occupied.
            hidePlantRenderWhenCovered = false

            [planting]
            # CN: 是否启用可种植物品白名单。true 时只有已解锁阶段里的物品可以种植。
            # EN: Enables the plantable whitelist. When true, only items from unlocked stages can be planted.
            enableWhitelist = true

            # CN: 白名单关闭时，是否允许任意物品尝试种植。
            # EN: Allows any item to be planted when the whitelist is disabled.
            allowAnyItemWhenWhitelistDisabled = true

            # CN: 没有玩家上下文时默认解锁的阶段，例如自动化种植。
            # EN: Stages treated as unlocked when there is no player context, such as automation.
            fallbackUnlockedStages = ["starter"]

            [growth]
            # CN: 从 0 成长到成熟需要多少 tick。20 tick = 1 秒。
            # EN: Ticks required to grow from 0 to mature. 20 ticks = 1 second.
            growthTicks = 24000

            # CN: 是否要求土土附近有水才会成长。
            # EN: Whether nearby water is required for growth.
            requireWater = true

            # CN: 检测水源的范围，4 类似原版耕地。
            # EN: Water search range. 4 behaves similarly to vanilla farmland.
            waterRange = 4

            # CN: 兼容旧配置名：是否允许“？肥肥？”催熟。普通骨粉不会催熟土土植株。
            # EN: Legacy option name: enables ?Feifei? growth. Vanilla bone meal does not affect Tutu plants.
            bonemealAllowed = true

            # CN: 兼容旧配置名：每次“？肥肥？”增加的成长比例，0.25 表示增加 25%。
            # EN: Legacy option name: growth added per ?Feifei? use. 0.25 means 25%.
            bonemealGrowthAmount = 0.25

            [interaction]
            # CN: 右键土土时优先尝试种植，而不是放置方块。
            # EN: Try planting before normal block placement when right-clicking Tutu Soil.
            preferPlantingOverPlacing = true

            # CN: 潜行右键是否允许绕过种植逻辑，按原版方式放置方块。
            # EN: Sneak-right-click bypasses planting and allows normal placement.
            sneakBypassesPlanting = true

            # CN: 当物品不能种植时，是否允许正常放置到土土上方。
            # EN: Allow normal placement above Tutu Soil if the held item cannot be planted.
            allowPlaceOnSoilWhenCannotPlant = true

            # CN: 当物品可以种植时，阻止它被作为方块真实放置。
            # EN: Prevent plantable block items from being placed as real blocks.
            preventPlacementForPlantableItems = true

            [rendering]
            # CN: 默认渲染模式。auto = 自动判断，item = 竖直物品，block = 方块模型。
            # EN: Default render mode. auto = automatic, item = upright item, block = block model.
            defaultRenderMode = "auto"

            # CN: 作物种子是否强制显示为物品材质，而不是作物方块模型。
            # EN: Force crop seeds to render as item sprites instead of crop block models.
            cropSeedsUseItemRenderer = true

            # CN: 方块实体方块是否默认显示为物品模型，以避免特殊 BER/Flywheel 渲染问题。
            # EN: Render block-entity blocks as item models by default to avoid special renderer issues.
            blockEntityBlocksUseItemRenderer = true

            # CN: 方块模型渲染失败时是否回退为物品渲染。
            # EN: Fallback to item rendering when block-model rendering fails.
            fallbackToItemRenderer = true

            # CN: 强制竖直物品渲染的 item id 列表。不存在的模组 id 会被忽略，不会崩溃。
            # EN: Item ids that always use upright item rendering. Missing mod ids are ignored safely.
            forceItemBillboard = [
              "minecraft:wheat_seeds",
              "minecraft:beetroot_seeds",
              "farmersdelight:rice",
              "farmersdelight:tomato_seeds",
              "farmersdelight:cabbage_seeds",
              "create:cogwheel",
              "create:large_cogwheel"
            ]

            # CN: 强制方块模型渲染的 item id 列表。
            # EN: Item ids that try to use block-model rendering.
            forceBlockModel = [
              "minecraft:stone"
            ]

            [feifei]
            # CN: 是否允许“？肥肥？”催熟土土上的种植物。
            # EN: Enables ?Feifei? to accelerate plants on Tutu Soil.
            enableFeifei = true

            # CN: 每次使用增加多少成长进度。0.25 表示 25%。
            # EN: Growth added per use. 0.25 means 25%.
            growthAmount = 0.25

            # CN: 创造模式玩家使用“？肥肥？”时是否消耗物品。
            # EN: Whether creative-mode players consume ?Feifei? when using it.
            consumeInCreative = false

            # CN: 阶段 starter：默认解锁，适合放原版基础农作物。
            # EN: Starter stage: unlocked by default and intended for basic crops.
            [[stages]]
            id = "starter"
            displayName = "Starter / 起步阶段"
            defaultUnlocked = true
            # CN/EN unlockType: always / advancement / ftb_quest / kubejs / custom_event
            unlockType = "always"
            unlockId = ""
            # CN: 可种植物品白名单。可以写其他模组物品；若未安装对应模组，会 warn 并跳过。
            # EN: Plantable whitelist. Missing mod items warn once and are skipped.
            items = [
              "minecraft:wheat",
              "minecraft:beetroot",
              "minecraft:melon_slice",
              "minecraft:carrot",
              "minecraft:potato",
              "farmersdelight:rice"
            ]

            # CN: 示例：机械动力阶段。请按整合包实际进度 id 修改 unlockId。
            # EN: Example Create stage. Adjust unlockId for your pack.
            [[stages]]
            id = "create"
            displayName = "Create / 机械动力阶段"
            defaultUnlocked = false
            unlockType = "kubejs"
            unlockId = "create"
            items = [
              "create:cogwheel",
              "create:large_cogwheel",
              "create:water_wheel"
            ]

            # CN: 示例：KubeJS / 命令控制阶段。可用 /tutu stage unlock <player> mekanism 解锁。
            # EN: Example KubeJS / command-controlled stage. Use /tutu stage unlock <player> mekanism.
            [[stages]]
            id = "mekanism"
            displayName = "Mekanism / 通用机械阶段"
            defaultUnlocked = false
            unlockType = "kubejs"
            unlockId = "mekanism"
            items = [
              "mekanism:steel_casing",
              "mekanism:metallurgic_infuser"
            ]

            # CN: 示例：FTB Quests 阶段。未安装 FTB Quests 时不会崩溃，只会视为未解锁。
            # EN: Example FTB Quests stage. Missing FTB Quests is safe and simply stays locked.
            [[stages]]
            id = "ae2"
            displayName = "AE2 / 应用能源阶段"
            defaultUnlocked = false
            unlockType = "ftb_quest"
            unlockId = "1234567890ABCDEF"
            items = [
              "ae2:certus_quartz_crystal",
              "ae2:charger",
              "ae2:inscriber"
            ]
            """;

    private static final String DEFAULT_GENERAL_CONFIG = """
            [general]
            # CN: 是否启用调试日志。遇到兼容问题时可以打开。
            # EN: Enables debug logs. Useful when diagnosing compatibility issues.
            debugLog = false
            """;

    private static final String DEFAULT_SOIL_CONFIG = """
            [soil]
            # CN: 土土是否允许像原版耕地一样退化成普通泥土。
            # EN: Whether Tutu Soil can decay into vanilla dirt like farmland.
            allowDecayToVanillaDirt = false

            # CN: 实体踩踏土土时是否影响湿润度。
            # EN: Whether entity trampling affects Tutu Soil moisture.
            tramplingAffectsMoisture = true

            # CN: 被踩踏时只回退为未润湿土土，而不是变成泥土。
            # EN: When trampled, Tutu Soil dries out instead of turning into dirt.
            tramplingDriesOnly = true

            # CN: 被踩踏时是否破坏土土上的植株。
            # EN: Whether trampling breaks the plant on Tutu Soil.
            tramplingBreaksPlant = false

            # CN: 上方有方块时是否暂停成长。
            # EN: Pause growth when the space above Tutu Soil is occupied.
            pauseGrowthWhenCovered = true

            # CN: 上方有方块时是否隐藏种植物渲染。
            # EN: Hide plant rendering when the space above Tutu Soil is occupied.
            hidePlantRenderWhenCovered = false
            """;

    private static final String DEFAULT_INTERACTION_CONFIG = """
            [interaction]
            # CN: 右键土土时优先尝试种植，而不是放置方块。
            # EN: Try planting before normal block placement when right-clicking Tutu Soil.
            preferPlantingOverPlacing = true

            # CN: 潜行右键是否允许绕过种植逻辑，按原版方式放置方块。
            # EN: Sneak-right-click bypasses planting and allows normal placement.
            sneakBypassesPlanting = true

            # CN: 当物品不能种植时，是否允许正常放置到土土上方。
            # EN: Allow normal placement above Tutu Soil if the held item cannot be planted.
            allowPlaceOnSoilWhenCannotPlant = true

            # CN: 当物品可以种植时，阻止它被作为方块真实放置。
            # EN: Prevent plantable block items from being placed as real blocks.
            preventPlacementForPlantableItems = true
            """;

    private static final String DEFAULT_RENDERING_CONFIG = """
            [rendering]
            # CN: 默认渲染模式。auto = 自动判断，item = 竖直物品，block = 方块模型。
            # EN: Default render mode. auto = automatic, item = upright item, block = block model.
            defaultRenderMode = "auto"

            # CN: 作物种子是否强制显示为物品材质，而不是作物方块模型。
            # EN: Force crop seeds to render as item sprites instead of crop block models.
            cropSeedsUseItemRenderer = true

            # CN: 方块实体方块是否默认显示为物品模型，以避免特殊 BER/Flywheel 渲染问题。
            # EN: Render block-entity blocks as item models by default to avoid special renderer issues.
            blockEntityBlocksUseItemRenderer = true

            # CN: 方块模型渲染失败时是否回退为物品渲染。
            # EN: Fallback to item rendering when block-model rendering fails.
            fallbackToItemRenderer = true

            # CN: 强制竖直物品渲染的 item id 列表。
            # EN: Item ids that always use upright item rendering.
            forceItemBillboard = [
              "minecraft:wheat_seeds",
              "minecraft:beetroot_seeds",
              "farmersdelight:rice",
              "farmersdelight:tomato_seeds",
              "farmersdelight:cabbage_seeds",
              "create:cogwheel",
              "create:large_cogwheel"
            ]

            # CN: 强制方块模型渲染的 item id 列表。
            # EN: Item ids that try to use block-model rendering.
            forceBlockModel = [
              "minecraft:stone"
            ]
            """;

    private static final String DEFAULT_FEIFEI_CONFIG = """
            [feifei]
            # CN: 是否允许“？肥肥？”催熟土土上的种植物。
            # EN: Enables ?Feifei? to accelerate plants on Tutu Soil.
            enableFeifei = true

            # CN: 每次使用增加多少成长进度。0.25 表示 25%。
            # EN: Growth added per use. 0.25 means 25%.
            growthAmount = 0.25

            # CN: 创造模式玩家使用“？肥肥？”时是否消耗物品。
            # EN: Whether creative-mode players consume ?Feifei? when using it.
            consumeInCreative = false
            """;

    private TutuConfig() {
    }
}
