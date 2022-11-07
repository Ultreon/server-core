package com.ultreon.mods.servercore.server.teleport;

import com.ultreon.mods.servercore.server.ServerHooks;
import com.ultreon.mods.servercore.server.TaskManager;
import com.ultreon.mods.servercore.server.config.Config;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Teleport<T> {
    private final UUID sender;
    private final TeleportDestination<T> destination;
    private final int delay;
    private int countdown;
    private final List<Runnable> onFail = new ArrayList<>();
    private final List<Runnable> onSuccess = new ArrayList<>();
    private boolean cancelled = true;

    public Teleport(UUID sender, TeleportDestination<T> destination) {
        this(sender, destination, Config.getTeleportDelay());
    }

    public Teleport(UUID sender, TeleportDestination<T> destination, int delay) {
        this.sender = sender;
        this.destination = destination;
        this.delay = delay;
        this.countdown = delay;
    }

    public void prepare() {
        TaskManager.INSTANCE.schedule(this::tick, 0);
    }

    private void tick() {
        if (cancelled) {
            onFail.forEach(Runnable::run);
            return;
        }

        if (countdown-- <= 0) {
            if (countdown < 0) throw new InternalError("Counted down to negative");

            if (!execute()) onFail.forEach(Runnable::run);
            else onSuccess.forEach(Runnable::run);

            countdown = 0;
        } else {
            TaskManager.INSTANCE.schedule(this::tick, 1);
        }
    }

    public int getCountdown() {
        return countdown;
    }

    public int getDelay() {
        return delay;
    }

    public boolean execute() {
        Entity sender = ServerHooks.entity(this.sender);
        if (sender == null) return false;

        this.destination.teleportHere(sender);

        return true;
    }

    public void onFail(Runnable onFail) {
        this.onFail.add(onFail);
    }

    public void onSuccess(Runnable onSuccess) {
        this.onSuccess.add(onSuccess);
    }

    public void cancel() {
        cancelled = true;
    }
}
