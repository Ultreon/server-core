package com.ultreon.mods.servercore.network;

import com.ultreon.mods.servercore.ServerCore;
import com.ultreon.mods.servercore.server.Permission;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

/**
 * State synchronization utilities.
 *
 * @since 0.1.0
 */
public class StateSync {
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

    /**
     * Create a NBT tag for the set permission state sync.
     *
     * @param permission the permission.
     * @param allow      whether to set to allow.
     * @return the NBT tag.
     */
    public static CompoundTag setPermission(Permission permission, boolean allow) {
        CompoundTag tag = new CompoundTag();
        tag.putString("permission", permission.id());
        tag.putBoolean("allow", allow);
        return tag;
    }
}
