package com.ultreon.mods.servercore.server;

import com.google.errorprone.annotations.CheckReturnValue;
import com.ultreon.mods.servercore.mixin.AntiMixin;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.ApiStatus;

import java.util.UUID;

/**
 * Scheduled task.
 *
 * @author Qboi123
 */
@AntiMixin
public final class ScheduledTask implements SingleRunnable {
    private final int delay;
    private final Runnable task;
    private int remaining;
    private boolean valid = true;
    private final UUID token;
    private boolean accessBlocked;

    /**
     * Create an instance of a scheduled task.
     *
     * @param delay initial delay. Note: the delay can't be negative.
     * @param task the task when the task will be ran.
     */
    @ApiStatus.Internal
    public ScheduledTask(UUID token, int delay, Runnable task) {
        if (delay < 0) throw new IllegalArgumentException("Delay can't be negative.");
        this.delay = delay;
        this.remaining = delay;
        this.task = task;
        this.token = token;
    }

    /**
     * Tick the task, and run if there's no remaining ticks.
     *
     * @return whether the task can be removed.
     */
    @Override
    @CheckReturnValue
    @ApiStatus.Internal
    public synchronized boolean run() {
        // If invalid it should be removed.
        if (!valid) return false;

        accessBlocked = true;

        // Check for zero, for safety reasons.
        if (remaining == 0) task.run();

        return --remaining < 0;
    }

    /**
     * Check if the task is still valid.
     *
     * @return whether the task is still valid.
     */
    public boolean stillValid() {
        return remaining >= 0;
    }

    /**
     * Get the remaining ticks.
     * <p>
     * Returns {@code 0} if the task will run the next tick.<br>
     * Returns {@code -1} if the task is done.
     *
     * @return the remaining ticks until execution.
     */
    @IntRange(from = -1)
    public int remaining() {
        return Math.max(remaining, -1);
    }

    /**
     * Invalidate
     * @param token
     * @return
     */
    public boolean invalidate(UUID token) {
        if (this.token == token) {
            valid = false;
            return true;
        }
        return false;
    }

    public UUID token() {
        return token;
    }

    /**
     * Get the delay the task was initiated with.
     *
     * @return the initiated delay.
     */
    @IntRange(from = 0)
    public int delay() {
        return delay;
    }
}
