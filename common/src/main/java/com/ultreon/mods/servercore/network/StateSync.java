package com.ultreon.mods.servercore.network;

import com.ultreon.mods.servercore.ServerCore;
import com.ultreon.mods.servercore.server.Permission;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

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
     * Sync ID for setting multiple permission at once on the client.
     *
     * @since 0.1.0
     */
    public static final ResourceLocation SET_MULTI_PERMISSIONS = ServerCore.res("set_multi_permission");

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

    /**
     * Create a NBT tag for the set multiple permissions state sync.
     *
     * @param permissions the permissions to set.
     * @param enable      whether to enable or disable.
     * @return the NBT tag.
     */
    public static CompoundTag setMultiPermission(Set<Permission> permissions, boolean enable) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (Permission permission : permissions) {
            list.add(StringTag.valueOf(permission.id()));
        }
        tag.put("Permissions", list);
        tag.putBoolean("enable", enable);
        return null;
    }
}
