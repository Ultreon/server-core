package com.ultreon.mods.servercore.network;

import com.ultreon.mods.servercore.ServerCore;
import net.minecraft.resources.ResourceLocation;

/**
 * State synchronization IDs.
 *
 * @since 0.1.0
 */
public class StateSyncIds {
    /**
     * Sync ID for setting a permission on the client.
     *
     * @since 0.1.0
     */
    public static final ResourceLocation SET_PERMISSION = ServerCore.res("set_permission");

    /**
     * Sync ID for initializing permissions on the client.
     *
     * @since 0.1.0
     */
    public static final ResourceLocation INIT_PERMISSIONS = ServerCore.res("init_permissions");
}
