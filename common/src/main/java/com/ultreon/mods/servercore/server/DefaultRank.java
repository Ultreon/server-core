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
        this(rank.getName(), rank.getPrefix(), rank.getPermissions());
    }

    /**
     * Create the default rank instance with some defaults.
     *
     * @param name        the getObjName to use.
     * @param prefix      the prefix to use in chat.
     * @param permissions the permissions to set.
     * @since 0.1.0
     */
    @ApiStatus.Internal
    public DefaultRank(String name, String prefix, Set<Permission> permissions) {
        super("default", name, prefix, 0);

        this.permissions.addAll(permissions);
    }

    @Override
    public void setPriority(int priority) {
        throw new IllegalArgumentException("Priority can't be set on the default rank.");
    }
}
