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

    private static boolean enableWhitelist = true;
    private static List<String> fallbackUnlockedStages = List.of("starter");
    private static int growthTicks = 24000;
    private static boolean requireWater = true;
    private static int waterRange = 4;
    private static boolean bonemealAllowed = true;
    private static double bonemealGrowthAmount = 0.25D;
    private static boolean enableFeifei = true;
    private static double feifeiGrowthAmount = 0.25D;
    private static boolean feifeiConsumeInCreative = false;
    private static Map<String, TutuPlantStage> stages = Map.of();

    public static void load() {
        writeDefaultConfigIfMissing();
        appendFeifeiConfigIfMissing();

        try (CommentedFileConfig config = CommentedFileConfig.builder(CONFIG_PATH).sync().preserveInsertionOrder().build()) {
            config.load();
            enableWhitelist = config.getOrElse("planting.enableWhitelist", true);
            fallbackUnlockedStages = new ArrayList<>(config.getOrElse("planting.fallbackUnlockedStages", List.of("starter")));
            growthTicks = Math.max(1, config.getOrElse("growth.growthTicks", 24000));
            requireWater = config.getOrElse("growth.requireWater", true);
            waterRange = Math.max(0, config.getOrElse("growth.waterRange", 4));
            bonemealAllowed = config.getOrElse("growth.bonemealAllowed", true);
            bonemealGrowthAmount = Math.max(0.0D, config.getOrElse("growth.bonemealGrowthAmount", 0.25D));
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

    private static void appendFeifeiConfigIfMissing() {
        try {
            String existingConfig = Files.readString(CONFIG_PATH);
            if (!existingConfig.contains("[feifei]")) {
                Files.writeString(CONFIG_PATH, existingConfig + "\n" + DEFAULT_FEIFEI_CONFIG);
            }
        } catch (Exception exception) {
            Melon_melon.LOGGER.warn("Failed to append default Feifei config section to {}", CONFIG_PATH, exception);
        }
    }

    private static final String DEFAULT_CONFIG = """
            [planting]
            # 是否启用可种植物品白名单。
            # true：只有当前玩家已解锁阶段里的物品可以种植。
            # false：忽略下面所有阶段与物品列表，允许所有物品尝试通用种植逻辑。
            enableWhitelist = true

            # 没有玩家上下文时使用的默认阶段，例如未来自动化机器尝试种植时。
            fallbackUnlockedStages = ["starter"]

            [growth]
            # 一个植株从刚种下到成熟需要多少 tick。20 tick = 1 秒。
            growthTicks = 24000

            # 是否要求土土附近有水才能成长。
            requireWater = true

            # 检测水源的水平范围。4 类似原版耕地。
            waterRange = 4

            # 是否允许“？肥肥？”催熟。普通骨粉不会催熟土土植株。
            bonemealAllowed = true

            # 每次“？肥肥？”增加的成长比例，0.25 表示增加 25%。
            bonemealGrowthAmount = 0.25

            [feifei]
            # 是否允许“？肥肥？”催熟土土上的种植物。
            enableFeifei = true

            # 每次使用“？肥肥？”增加多少成长进度。
            # 0.25 表示增加 25%，4 次可以从 0 催熟到成熟。
            growthAmount = 0.25

            # 创造模式玩家使用“？肥肥？”时是否消耗物品。
            consumeInCreative = false

            # 阶段 starter：默认解锁，适合放原版基础农作物。
            [[stages]]
            id = "starter"
            displayName = "起步阶段"
            defaultUnlocked = true
            # 解锁方式：always / advancement / ftb_quest / kubejs / custom_event
            unlockType = "always"
            unlockId = ""
            # 可种植物品白名单。可以写其他模组物品；若未安装对应模组，会 warn 并跳过。
            # 方块类物品会尝试用方块模型直立显示，普通物品会用竖直 item 方式显示。
            items = [
              "minecraft:wheat",
              "minecraft:beetroot",
              "minecraft:melon_slice",
              "minecraft:carrot",
              "minecraft:potato"
            ]

            # 示例：机械动力阶段。请按整合包实际进度 id 修改 unlockId。
            [[stages]]
            id = "create"
            displayName = "机械动力阶段"
            defaultUnlocked = false
            unlockType = "advancement"
            unlockId = "create:andesite_age"
            items = [
              "create:cogwheel",
              "create:large_cogwheel",
              "create:water_wheel"
            ]

            # 示例：KubeJS / 命令控制阶段。
            # 可用 /tutu stage unlock <player> mekanism 解锁。
            [[stages]]
            id = "mekanism"
            displayName = "通用机械阶段"
            defaultUnlocked = false
            unlockType = "kubejs"
            unlockId = "mekanism"
            items = [
              "mekanism:steel_casing",
              "mekanism:metallurgic_infuser"
            ]

            # 示例：FTB Quests 阶段。未安装 FTB Quests 时不会崩溃，只会视为未解锁。
            [[stages]]
            id = "ae2"
            displayName = "应用能源阶段"
            defaultUnlocked = false
            unlockType = "ftb_quest"
            unlockId = "1234567890ABCDEF"
            items = [
              "ae2:certus_quartz_crystal",
              "ae2:charger",
              "ae2:inscriber"
            ]
            """;

    private static final String DEFAULT_FEIFEI_CONFIG = """
            [feifei]
            # 是否允许“？肥肥？”催熟土土上的种植物。
            enableFeifei = true

            # 每次使用“？肥肥？”增加多少成长进度。
            # 0.25 表示增加 25%，4 次可以从 0 催熟到成熟。
            growthAmount = 0.25

            # 创造模式玩家使用“？肥肥？”时是否消耗物品。
            consumeInCreative = false
            """;

    private TutuConfig() {
    }
}
