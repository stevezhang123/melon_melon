package org.sz.melon_melon.config;

import java.util.List;

public record TutuPlantStage(
        String id,
        String displayName,
        boolean defaultUnlocked,
        String unlockType,
        String unlockId,
        List<String> itemIds
) {
}
