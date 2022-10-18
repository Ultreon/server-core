package com.ultreon.mods.servercore.server.state;

import com.ultreon.mods.servercore.ServerCore;
import com.ultreon.mods.servercore.network.Network;
import com.ultreon.mods.servercore.network.StateSync;
import com.ultreon.mods.servercore.server.DefaultRank;
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
import java.util.*;
import java.util.stream.Collectors;

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
    private final Map<String, Rank> ranks = new HashMap<>();
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

        this.ranks.put(main.getDefaultRank().getId(), main.getDefaultRank());
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
        this.ranks.values().forEach(rank -> rank.getPermissions().forEach(perm -> permissions.add(StringTag.valueOf(perm.id()))));
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
                        this.ranks.put(rankId, rank);
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
        } catch (FileNotFoundException e) {
            ServerCore.LOGGER.info("Player state for " + uuid + " not loaded because file didn't exists.");
        }
    }

    /**
     * Save the player state to NBT.
     *
     * @since 0.1.0
     */
    public void save() {
        try {
            // Create base directory if non-existent.
            if (!baseDir.exists() && !baseDir.mkdirs())
                throw new IOException("Failed to create directories: " + baseDir.getPath());

            // Create NBT tag.
            CompoundTag tag = new CompoundTag();

            // Ranks
            ListTag ranks = new ListTag();
            for (Rank rank : this.ranks.values()) {
                ranks.add(StringTag.valueOf(rank.getId()));
            }
            tag.put("Ranks", ranks);

            // Permissions
            ListTag permissions = new ListTag();
            for (Permission permission : this.permissions) {
                permissions.add(StringTag.valueOf(permission.id()));
            }
            tag.put("Permissions", permissions);

            // Write data.
            NbtIo.writeCompressed(tag, genericDataFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Receive a data sync from the client.
     *
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
        return ranks.values().stream().anyMatch(rank -> rank.hasPermission(permission))
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
        Rank official = main.getRank(rank.getId());
        if (official == null) throw new IllegalArgumentException("Rank doesn't exist: " + rank.getId());
        if (official instanceof DefaultRank) throw new IllegalArgumentException("Can't add an default Rank.");
        if (ranks.containsKey(rank.getId())) return;
        this.sendBulkAddPermission(official.getPermissions());
        this.ranks.put(official.getId(), official);
    }

    /**
     * Add a rank from an ID.
     *
     * @param id the ID.
     * @since 0.1.0
     */
    public void addRank(String id) {
        Rank official = main.getRank(id);
        if (official == null) throw new IllegalArgumentException("Rank doesn't exist: " + id);
        if (official instanceof DefaultRank) throw new IllegalArgumentException("Can't add an default Rank.");
        if (ranks.containsKey(id)) return;
        this.sendBulkAddPermission(official.getPermissions());
        this.ranks.put(id, official);
    }

    /**
     * Remove a rank to the player.
     *
     * @param rank the rank to remove.
     * @since 0.1.0
     */
    public void removeRank(Rank rank) {
        Rank official = ranks.get(rank.getId());
        if (official instanceof DefaultRank) return;
        if (!ranks.containsKey(rank.getId())) return;
        this.sendBulkRemovePermission(official.getPermissions().stream().filter(permission -> !hasPermission(permission)).collect(Collectors.toSet()));
        this.ranks.remove(rank.getId());
    }

    /**
     * Remove a rank from an ID.
     *
     * @param id the ID.
     * @since 0.1.0
     */
    public void removeRank(String id) {
        Rank official = main.getRank(id);
        if (official instanceof DefaultRank) return;
        if (!ranks.containsKey(id)) return;
        this.sendBulkRemovePermission(official.getPermissions().stream().filter(permission -> !hasPermission(permission)).collect(Collectors.toSet()));
        this.ranks.remove(id);
    }

    /**
     * Check if the player has a certain rank.
     *
     * @param rank the rank to check for.
     * @return whether the player has that rank.
     * @since 0.1.0
     */
    public boolean hasRank(Rank rank) {
        return this.ranks.containsKey(rank.getId());
    }

    /**
     * Check if the player has a certain rank.
     *
     * @param id the id of the rank to check for.
     * @return whether the player has that rank.
     * @since 0.1.0
     */
    public boolean hasRank(String id) {
        return this.ranks.containsKey(id);
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

    @ApiStatus.Internal
    private void sendBulkAddPermission(Set<Permission> permissions) {
        ServerPlayer player = this.player;
        if (player != null) {
            CompoundTag data = StateSync.setMultiPermission(permissions, true);
            Network.sendStateSync(player, StateSync.SET_MULTI_PERMISSIONS, data);
        }
    }

    @ApiStatus.Internal
    private void sendBulkRemovePermission(Set<Permission> permissions) {
        ServerPlayer player = this.player;
        if (player != null) {
            CompoundTag data = StateSync.setMultiPermission(permissions, false);
            Network.sendStateSync(player, StateSync.SET_MULTI_PERMISSIONS, data);
        }
    }
}
