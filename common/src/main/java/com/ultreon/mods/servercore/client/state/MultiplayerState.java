package com.ultreon.mods.servercore.client.state;

import com.ultreon.mods.servercore.server.Permission;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashSet;
import java.util.Set;

import static com.ultreon.mods.servercore.network.StateSync.*;

/**
 * Multiplayer client state.
 *
 * @since 0.1.0
 */
public class MultiplayerState extends ClientState {
    private LocalPlayer player;
    private final Set<Permission> permissions = new HashSet<>();

    /**
     * Create the multiplayer state for the client-side player.
     *
     * @since 0.1.0
     */
    @ApiStatus.Internal
    public MultiplayerState() {

    }

    /**
     * Handle joining of multiplayer servers..
     *
     * @param player the player that joined.
     * @since 0.1.0
     */
    public void onJoin(LocalPlayer player) {
        this.player = player;
    }

    /**
     * Get the local player instance.
     *
     * @return the local player.
     * @since 0.1.0
     */
    public LocalPlayer getPlayer() {
        return player;
    }

    /**
     * Receive a data sync from the server.
     *
     * @param type the type of sync.
     * @param data the data synced.
     * @since 0.1.0
     */
    @ApiStatus.Internal
    public void receive(ResourceLocation type, CompoundTag data) {
        if (type.equals(SET_PERMISSION)) {
            String id = data.getString("permission");
            boolean allow = data.getBoolean("allow");
            if (allow) permissions.add(new Permission(id));
            else permissions.remove(new Permission(id));
        } else if (type.equals(SET_MULTI_PERMISSIONS)) {
            System.out.println("data = " + data);
            boolean allow = data.getBoolean("allow");
            ListTag permissions = data.getList("Permissions", Tag.TAG_STRING);
            permissions.forEach(tag -> {
                if (tag instanceof StringTag s) {
                    if (allow) this.permissions.add(new Permission(s.getAsString()));
                    else this.permissions.remove(new Permission(s.getAsString()));
                }
            });
        } else if (type.equals(INIT_PERMISSIONS)) {
            System.out.println("data = " + data);
            ListTag permissions = data.getList("Permissions", Tag.TAG_STRING);
            this.permissions.clear();
            permissions.forEach(tag -> {
                if (tag instanceof StringTag s) {
                    this.permissions.add(new Permission(s.getAsString()));
                }
            });
        }
    }

    /**
     * Check if the player can walk.
     *
     * @return whether the player can walk.
     * @since 0.1.0
     */
    public boolean canWalk() {
        return hasPermission("minecraft.interaction.walk");
    }

    /**
     * Check if the player can jump.
     *
     * @return whether the player can jump.
     * @since 0.1.0
     */
    public boolean canJump() {
        return hasPermission("minecraft.interaction.jump");
    }

    /**
     * Check if the player can sprint.
     *
     * @return whether the player can sprint.
     * @since 0.1.0
     */
    public boolean canSprint() {
        return hasPermission("minecraft.interaction.sprint");
    }

    /**
     * Check if the player has a permission.
     *
     * @param permission the permission to check for.
     * @return whether the player has that permission.
     * @since 0.1.0
     */
    public boolean hasPermission(String permission) {
        return hasPermission(new Permission(permission));
    }

    /**
     * Check if the player has a permission.
     *
     * @param permission the permission to check for.
     * @return whether the player has that permission.
     * @since 0.1.0
     */
    public boolean hasPermission(Permission permission) {
        return permissions.stream().anyMatch(perm -> perm.isChild(permission) || perm.equals(permission));
    }

    /**
     * Get all the permissions the player has.
     *
     * @return all the permissions.
     * @since 0.1.0
     */
    public Set<Permission> getPermissions() {
        return permissions;
    }
}
