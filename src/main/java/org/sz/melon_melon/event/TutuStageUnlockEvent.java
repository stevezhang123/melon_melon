package org.sz.melon_melon.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

public class TutuStageUnlockEvent extends Event {
    private final ServerPlayer player;
    private final String stageId;
    private final boolean unlock;

    public TutuStageUnlockEvent(ServerPlayer player, String stageId, boolean unlock) {
        this.player = player;
        this.stageId = stageId;
        this.unlock = unlock;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public String getStageId() {
        return stageId;
    }

    public boolean isUnlock() {
        return unlock;
    }
}
