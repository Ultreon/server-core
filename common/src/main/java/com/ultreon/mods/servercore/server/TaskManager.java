package com.ultreon.mods.servercore.server;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.mods.servercore.mixin.AntiMixin;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@AntiMixin
public class TaskManager {
    public static final TaskManager INSTANCE = new TaskManager();
    private final Map<UUID, ScheduledTask> tasks = new ConcurrentHashMap<>();
    private final Object lock = new Object();
    private boolean ticking;

    @ApiStatus.Internal
    void tick() {
        synchronized (lock) {
            ticking = true;
            tasks.values().stream().filter(ScheduledTask::run).forEach(key -> tasks.remove(key.token()));
            ticking = false;
        }
    }

    @CanIgnoreReturnValue
    public ScheduledTask schedule(Runnable task, int afterTicks) {
        synchronized (lock) {
            if (ticking) {
                afterTicks--;
                if (afterTicks == -1) {
                    UUID uuid;
                    do {
                        uuid = UUID.randomUUID();
                    } while (!tasks.containsKey(uuid));

                    ScheduledTask scheduledTask = new ScheduledTask(uuid, 0, task);
                    if (!scheduledTask.run()) throw new InternalError("Scheduled task isn't run at a 0 tick delay.");
                    return scheduledTask;
                }
            }
            UUID uuid = UUID.randomUUID();
            ScheduledTask scheduledTask = new ScheduledTask(uuid, afterTicks, task);
            tasks.put(uuid, scheduledTask);
            return scheduledTask;
        }
    }

    public void cancelTask(UUID token) {
        ScheduledTask remove = this.tasks.remove(token);
        if (!remove.invalidate(token)) {
            throw new InternalError("Token of removed task doesn't match requested task token.");
        }
    }
}
