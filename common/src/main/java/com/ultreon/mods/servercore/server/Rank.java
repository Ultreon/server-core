package com.ultreon.mods.servercore.server;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Server rank class.
 *
 * @since 0.1.0
 */
public class Rank {
    private final String id;
    private String name;
    private final Set<Permission> permissions = new HashSet<>();

    /**
     * Create a rank from NBT data.
     *
     * @param tag the compound tag that contains the data.
     * @since 0.1.0
     */
    public Rank(CompoundTag tag) {
        this.id = tag.getString("id");
        this.name = tag.getString("name");

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
     * Create a rank from an id and name.
     *
     * @param id   the id of the rank.
     * @param name the display name of the rank.
     * @since 0.1.0
     */
    public Rank(String id, String name) {
        this.id = id;
        this.name = name;
        if (id.contains(" ")) {
            throw new IllegalArgumentException("Rank contains space.");
        }
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
     * Get the rank name.
     *
     * @return the name.
     * @since 0.1.0
     */
    public String getName() {
        return name;
    }

    /**
     * Get the rank name.
     *
     * @param name the name to set.
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
        permissions.add(new Permission(permission));
    }

    /**
     * Add a permission to the rank.
     *
     * @param permission the permission to add.
     * @since 0.1.0
     */
    public void addPermission(Permission permission) {
        permissions.add(permission);
    }

    /**
     * Remove a permission to the rank.
     *
     * @param permission the permission to remove.
     * @since 0.1.0
     */
    public void removePermission(String permission) {
        permissions.remove(new Permission(permission));
    }

    /**
     * Remove a permission to the rank.
     *
     * @param permission the permission to remove.
     * @since 0.1.0
     */
    public void removePermission(Permission permission) {
        permissions.remove(permission);
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
        return permissions.stream().anyMatch(perm -> perm.isParent(permission) || perm.equals(permission));
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
        tag.putString("name", name);
        tag.putString("id", id);
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
}
