package com.ultreon.mods.servercore.server;

import com.ultreon.mods.servercore.server.state.ServerPlayerState;
import com.ultreon.mods.servercore.server.state.ServerStateManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Server rank class.
 *
 * @since 0.1.0
 */
public class Rank {
    /**
     * All the permissions the rank has.
     *
     * @since 0.1.0
     */
    protected final Set<Permission> permissions = new HashSet<>();
    private final String id;
    private String prefix;
    private String name;
    private int priority;

    /**
     * Create a rank from NBT data.
     *
     * @param tag the compound tag that contains the data.
     * @since 0.1.0
     */
    public Rank(CompoundTag tag) {
        this.id = tag.getString("id").toLowerCase(Locale.ROOT);
        this.name = tag.getString("getObjName");
        this.prefix = tag.getString("prefix");
        this.priority = tag.getInt("priority");

        ListTag permissions = tag.getList("Permissions", Tag.TAG_STRING);
        permissions.forEach(elem -> {
            if (elem instanceof StringTag s) {
                this.permissions.add(new Permission(s.getAsString()));
            }
        });

        if (this.id.contains(" ")) {
            throw new IllegalArgumentException("Loaded rank contains space.");
        }
    }

    /**
     * Create a rank from an id and getObjName.
     *
     * @param id   the id of the rank.
     * @param name the display getObjName of the rank.
     * @since 0.1.0
     * @deprecated use {@link #Rank(String, String, String, int)} instead.
     */
    @Deprecated
    public Rank(String id, String name) {
        this(id, name, "&l[UNKNOWN] ", 0);
    }

    /**
     * Create a rank from an id and getObjName.
     *
     * @param id       the id of the rank.
     * @param name     the display getObjName of the rank.
     * @param prefix   the chat prefix of the rank.
     * @param priority the index priority of the rank.
     * @since 0.1.0
     */
    public Rank(String id, String name, String prefix, int priority) {
        this.id = id.toLowerCase(Locale.ROOT);
        this.name = name;
        if (id.contains(" ")) {
            throw new IllegalArgumentException("Rank contains space.");
        }

        this.prefix = prefix;
    }

    /**
     * Get the rank id.
     *
     * @return the id.
     * @since 0.1.0
     */
    public String getId() {
        return id;
    }

    /**
     * Get the rank getObjName.
     *
     * @return the getObjName.
     * @since 0.1.0
     */
    public String getName() {
        return name;
    }

    /**
     * Get the rank getObjName.
     *
     * @param name the getObjName to set.
     * @since 0.1.0
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Add a permission to the rank.
     *
     * @param permission the permission to add.
     * @since 0.1.0
     */
    public void addPermission(String permission) {
        this.addPermission(new Permission(permission));
    }

    /**
     * Add a permission to the rank.
     *
     * @param permission the permission to add.
     * @since 0.1.0
     */
    public void addPermission(Permission permission) {
        permissions.add(permission);
        ServerStateManager manager = ServerStateManager.get();
        if (manager != null) {
            manager.getOnlinePlayersWith(this).forEach(player -> manager.player(player).sendAddPermission(permission));
        }
    }

    /**
     * Remove a permission to the rank.
     *
     * @param permission the permission to remove.
     * @since 0.1.0
     */
    public void removePermission(String permission) {
        this.removePermission(new Permission(permission));
    }

    /**
     * Remove a permission to the rank.
     *
     * @param permission the permission to remove.
     * @since 0.1.0
     */
    public void removePermission(Permission permission) {
        permissions.remove(permission);
        ServerStateManager manager = ServerStateManager.get();
        if (manager != null) {
            manager.getOnlinePlayersWith(this).forEach(player -> {
                ServerPlayerState playerState = manager.player(player);
                if (!playerState.hasPermission(permission)) {
                    playerState.sendRemovePermission(permission);
                }
            });
        }
    }

    /**
     * Check if the rank has a permission.
     *
     * @param permission the permission to check for.
     * @return whether the rank has that permission.
     * @since 0.1.0
     */
    public boolean hasPermission(String permission) {
        return hasPermission(new Permission(permission));
    }

    /**
     * Check if the rank has a permission.
     *
     * @param permission the permission to check for.
     * @return whether the rank has that permission.
     * @since 0.1.0
     */
    public boolean hasPermission(Permission permission) {
        return permissions.stream().anyMatch(perm -> perm.isChild(permission) || perm.equals(permission));
    }

    /**
     * Get all the permissions the rank has.
     *
     * @return all the permissions.
     * @since 0.1.0
     */
    public Set<Permission> getPermissions() {
        return permissions;
    }

    /**
     * Save the rank data as NBT.
     *
     * @return the compound tag.
     * @since 0.1.0
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag permissions = new ListTag();
        this.permissions.forEach(permission -> permissions.add(StringTag.valueOf(permission.id())));
        tag.put("Permissions", permissions);
        tag.putString("getObjName", name);
        tag.putString("id", id);
        tag.putString("prefix", prefix);
        tag.putInt("priority", priority);
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rank rank = (Rank) o;
        return getId().equals(rank.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    /**
     * Get the index priority of the rank.
     *
     * @return the priority.
     * @since 0.1.0
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Set the index priority for the rank.
     *
     * @param priority the priority to set.
     * @since 0.1.0
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Get the chat prefix.
     *
     * @return the prefix.
     * @since 0.1.0
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Set the chat prefix.
     *
     * @param prefix the prefix to set.
     * @since 0.1.0
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
