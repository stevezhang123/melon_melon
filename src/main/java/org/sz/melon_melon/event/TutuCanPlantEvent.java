package org.sz.melon_melon.event;

import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

public class TutuCanPlantEvent extends Event {
    @Nullable
    private final ServerPlayer player;
    private final ItemStack stack;
    private boolean allowed;
    private String stageId;
    private String messageKey;

    public TutuCanPlantEvent(@Nullable ServerPlayer player, ItemStack stack, boolean allowed, String stageId, String messageKey) {
        this.player = player;
        this.stack = stack;
        this.allowed = allowed;
        this.stageId = stageId;
        this.messageKey = messageKey;
    }

    @Nullable
    public ServerPlayer getPlayer() {
        return player;
    }

    public ItemStack getStack() {
        return stack;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public String getStageId() {
        return stageId;
    }

    public void setStageId(String stageId) {
        this.stageId = stageId;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }
}
