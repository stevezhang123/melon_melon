package org.sz.melon_melon.compat.ftbquests;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import org.sz.melon_melon.Melon_melon;

public class TutuFtbQuestsCompat {
    private static Boolean available;

    public static boolean isQuestCompleted(ServerPlayer player, String questId) {
        if (!isAvailable()) {
            return false;
        }

        // TODO: wire this to FTB Quests' public API with a compileOnly dependency or reflection once the target version is fixed.
        // The soft compat point is intentionally safe: no FTB Quests classes are referenced directly.
        return false;
    }

    public static boolean isAvailable() {
        if (available == null) {
            available = ModList.get().isLoaded("ftbquests");
            Melon_melon.LOGGER.info("FTB Quests compat is {}", available ? "available but not yet wired" : "disabled; ftbquests is not loaded");
        }

        return available;
    }

    private TutuFtbQuestsCompat() {
    }
}
