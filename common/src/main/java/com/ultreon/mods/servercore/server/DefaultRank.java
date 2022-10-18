package com.ultreon.mods.servercore.server;

import org.jetbrains.annotations.ApiStatus;

import java.util.Set;

/**
 * Default rank.
 *
 * @since 0.1.0
 */
@ApiStatus.Internal
public final class DefaultRank extends Rank {
    /**
     * Create the default rank instance.
     *
     * @param rank the rank to base it off.
     * @since 0.1.0
     */
    @ApiStatus.Internal
    public DefaultRank(Rank rank) {
        this(rank.getName(), rank.getPermissions());
    }

    /**
     * Create the default rank instance with some defaults.
     *
     * @param name        the name to use.
     * @param permissions the permissions to set.
     * @since 0.1.0
     */
    @ApiStatus.Internal
    public DefaultRank(String name, Set<Permission> permissions) {
        super("default", name);

        this.permissions.addAll(permissions);
    }
}
