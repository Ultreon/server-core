package com.ultreon.mods.servercore.server.state;

import com.ultreon.mods.servercore.network.Network;
import com.ultreon.mods.servercore.network.StateSync;
import com.ultreon.mods.servercore.server.Permission;
import com.ultreon.mods.servercore.server.Rank;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.ultreon.mods.servercore.network.StateSync.INIT_PERMISSIONS;

/**
 * Server player state.
 * @since 0.1.0
 */
public class ServerPlayerState extends ServerState {
    private final UUID uuid;
    private final ServerStateManager main;
    private final File baseDir;
    private final File genericDataFile;
    private ServerPlayer player;
    private final Set<Rank> ranks = new HashSet<>();
    private final Set<Permission> permissions = new HashSet<>();

    /**
     * Create an instance of the player state class.
     * @param uuid       the player's UUID.
     * @param main       the state manager.
     * @param storageDir the directory where the data is stored.
     * @throws IOException if loading failed.
     * @since 0.1.0
     */
    public ServerPlayerState(UUID uuid, ServerStateManager main, File storageDir) throws IOException {
        super();
        this.uuid = uuid;
        this.main = main;

        this.baseDir = storageDir;
        this.genericDataFile = new File(storageDir, "generic.dat");
        if (!baseDir.exists()) {
            if (!baseDir.mkdirs()) {
                throw new IOException("Failed to create storage directory for player: " + uuid);
            }
        }
        load();
    }

    /**
     * Handle joining of the player.
     * @param player the player joined.
     * @since 0.1.0
     */
    @ApiStatus.Internal
    public void onJoin(ServerPlayer player) {
        this.player = player;

        CompoundTag data = new CompoundTag();
        ListTag permissions = new ListTag();
        this.ranks.forEach(rank -> rank.getPermissions().forEach(perm -> permissions.add(StringTag.valueOf(perm.id()))));
        this.permissions.forEach(perm -> permissions.add(StringTag.valueOf(perm.id())));
        data.put("Permissions", permissions);
        Network.sendStateSync(player, INIT_PERMISSIONS, data);
    }

    /**
     * Handle leaving of the player.
     * @since 0.1.0
     */
    @ApiStatus.Internal
    public void onQuit() {
        this.player = null;
    }

    private void load() throws IOException {
        try {
            CompoundTag genericData = NbtIo.readCompressed(genericDataFile);
            ranks.clear();
            ListTag ranks = genericData.getList("Ranks", Tag.TAG_STRING);
            for (Tag tag : ranks) {
                if (tag instanceof StringTag s) {
                    String rankId = s.getAsString();
                    Rank rank = main.getRank(rankId);
                    if (rank != null) {
                        this.ranks.add(rank);
                    }
                }
            }
            ListTag permissions = genericData.getList("Permissions", Tag.TAG_STRING);
            for (Tag tag : permissions) {
                if (tag instanceof StringTag s) {
                    String id = s.getAsString();
                    this.permissions.add(new Permission(id));
                }
            }
        } catch (FileNotFoundException ignored) {
            // Ignore
        }
    }

    /**
     * Receive a data sync from the client.
     * @param type the type of data.
     * @param data the data to sync.
     */
    public void receive(ResourceLocation type, CompoundTag data) {
    }

    /**
     * The bound player if online.
     * @return the player. (null if offline).
     * @since 0.1.0
     */
    @Nullable
    public ServerPlayer player() {
        return player;
    }

    /**
     * Check if the player is online.
     * @return whether the player is online.
     * @since 0.1.0
     */
    public boolean isOnline() {
        return player != null;
    }

    /**
     * Check if the player is offline.
     * @return whether the player is offline.
     * @since 0.1.0
     */
    public boolean isOffline() {
        return player == null;
    }

    /**
     * Add a permission to the player.
     * @param permission the permission to add.
     * @since 0.1.0
     */
    public void addPermission(String permission) {
        this.addPermission(new Permission(permission));
    }

    /**
     * Add a permission to the player.
     * @param permission the permission to add.
     * @since 0.1.0
     */
    public void addPermission(Permission permission) {
        boolean hadBefore = hasPermission(permission);
        permissions.add(permission);

        if (!hadBefore) {
            sendAddPermission(permission);
        }
    }

    /**
     * Remove a permission to the player.
     * @param permission the permission to remove.
     * @since 0.1.0
     */
    public void removePermission(String permission) {
        this.removePermission(new Permission(permission));
    }

    /**
     * Remove a permission to the player.
     * @param permission the permission to remove.
     * @since 0.1.0
     */
    public void removePermission(Permission permission) {
        boolean hadBefore = hasPermission(permission);
        permissions.remove(permission);

        if (!hasPermission(permission) && hadBefore) {
            sendRemovePermission(permission);
        }
    }

    /**
     * Check if the player has a permission.
     * @param permission the permission to check for.
     * @return whether the player has that permission.
     * @since 0.1.0
     */
    public boolean hasPermission(String permission) {
        return hasPermission(new Permission(permission));
    }

    /**
     * Check if the player has a permission.
     * @param permission the permission to check for.
     * @return whether the player has that permission.
     * @since 0.1.0
     */
    public boolean hasPermission(Permission permission) {
        return ranks.stream().anyMatch(rank -> rank.hasPermission(permission))
                || permissions.stream().anyMatch(perm -> perm.isChild(permission) || perm.equals(permission));
    }

    /**
     * Get all the permissions the player has.
     * @return all the permissions.
     * @since 0.1.0
     */
    public Set<Permission> getPermissions() {
        return permissions;
    }

    /**
     * Get the bound player's UUID.
     * @return the UUID.
     * @since 0.1.0
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Get the base directory of where the data is stored/
     * @return the base storage directory.
     * @since 0.1.0
     */
    public File getBaseDir() {
        return baseDir;
    }

    /**
     * Add a rank to the player.
     * @param rank the rank to add.
     * @since 0.1.0
     */
    public void addRank(Rank rank) {
        this.ranks.add(rank);
    }

    /**
     * Add a rank from an ID.
     *
     * @param id the ID.
     * @since 0.1.0
     */
    public void addRank(String id) {
        Rank rank = main.getRank(id);
        if (rank == null) return;
        this.ranks.add(rank);
    }

    /**
     * Check if the player has a certain rank.
     *
     * @param rank the rank to check for.
     * @return whether the player has that rank.
     * @since 0.1.0
     */
    public boolean hasRank(Rank rank) {
        return this.ranks.contains(rank);
    }

    /**
     * Send permission adding to client.
     *
     * @param permission permission to send the adding for.
     * @since 0.1.0
     */
    @ApiStatus.Internal
    public void sendAddPermission(Permission permission) {
        ServerPlayer player = this.player;
        if (player != null) {
            CompoundTag data = StateSync.setPermission(permission, true);
            Network.sendStateSync(player, StateSync.SET_PERMISSION, data);
        }
    }

    /**
     * Send permission removing to client.
     *
     * @param permission permission to send the removal for.
     * @since 0.1.0
     */
    @ApiStatus.Internal
    public void sendRemovePermission(Permission permission) {
        ServerPlayer player = this.player;
        if (player != null) {
            CompoundTag data = StateSync.setPermission(permission, false);
            Network.sendStateSync(player, StateSync.SET_PERMISSION, data);
        }
    }
}
