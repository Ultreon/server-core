package com.ultreon.mods.servercore.server;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Permission class.
 *
 * @param id the id of the permission.
 * @since 0.1.0
 */
public record Permission(String id) implements Comparable<Permission> {
    private static final Pattern PATTERN = Pattern.compile("[a-z]+(\\.[a-z]+)*");

    /**
     * Instantiate a permission object.
     *
     * @param id the id of the permission.
     * @since 0.1.0
     */
    public Permission {
        if (!PATTERN.matcher(id).find()) {
            if (id.endsWith(".")) throw new IllegalArgumentException("Permission should not end with a dot.");
            if (id.startsWith(".")) throw new IllegalArgumentException("Permission should not start with a dot.");
            if (!id.contains(".")) throw new IllegalArgumentException("Permission should at least one dot.");
            throw new IllegalArgumentException("Permission contains invalid characters.");
        }
    }

    /**
     * Check if another permission is a child of this one.
     *
     * @param permission the other permission.
     * @return whether the permission is a child.
     * @since 0.1.0
     */
    public boolean isChild(Permission permission) {
        return (id + ".").startsWith(permission.id);
    }

    /**
     * Check if another permission is a parent of this one.
     *
     * @param permission the other permission.
     * @return whether the permission is a parent.
     * @since 0.1.0
     */
    public boolean isParent(Permission permission) {
        return (permission.id).startsWith(id + ".");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(@NotNull Permission o) {
        return id.compareTo(o.id);
    }
}
