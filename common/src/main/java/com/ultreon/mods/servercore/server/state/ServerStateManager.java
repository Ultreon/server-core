package com.ultreon.mods.servercore.server.state;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.authlib.GameProfile;
import com.ultreon.mods.servercore.ServerCore;
import com.ultreon.mods.servercore.server.DefaultRank;
import com.ultreon.mods.servercore.server.Permission;
import com.ultreon.mods.servercore.server.Rank;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * Server state manager.
 *
 * @since 0.1.0
 */
public class ServerStateManager {
    private static final LevelResource LEVEL_RESOURCE = new LevelResource("data/servercore");
    private static ServerStateManager instance;
    private final MinecraftServer server;
    private final Map<UUID, ServerPlayerState> playerStates = new HashMap<>();
    private final File baseDir;
    private final Map<String, Rank> ranks = new HashMap<>();
    private final File globalDataFile;
    private final Set<Permission> globalPermissions = new HashSet<>();
    private final Rank defaultRank;

    private ServerStateManager(MinecraftServer server) {
        this.server = server;
        this.baseDir = server.getWorldPath(LEVEL_RESOURCE).toFile();
        this.globalDataFile = new File(this.baseDir, "global.dat");

        List<Resource> resourceStack = server.getResourceManager().getResourceStack(ServerCore.res("sc/permissions.json"));
        Gson gson = new Gson();
        resourceStack.forEach(resource -> {
            try {
                BufferedReader bufferedReader = resource.openAsReader();
                JsonObject jsonObject = gson.fromJson(bufferedReader, JsonObject.class);
                for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                    String permissionId = entry.getKey();
                    if (entry.getValue() instanceof JsonPrimitive primitive) {
                        if (primitive.isBoolean() && primitive.getAsBoolean()) {
                            this.globalPermissions.add(new Permission(permissionId));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        try {
            loadLocal();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Rank loadedDefRank = getRank("default");
        this.defaultRank = loadedDefRank != null
                ? new DefaultRank(loadedDefRank)
                : new DefaultRank("Default", "&8[&7Default&8] ", this.globalPermissions);
        ranks.put(this.defaultRank.getId(), this.defaultRank);
    }

    /**
     * Save local data to NBT.
     *
     * @throws IOException when an I/O error occurs/
     * @since 0.1.0
     */
    public void loadLocal() throws IOException {
        try {
            CompoundTag genericData = NbtIo.readCompressed(globalDataFile);
            ranks.clear();
            ListTag ranks = genericData.getList("Ranks", Tag.TAG_COMPOUND);
            for (Tag tag : ranks) {
                if (tag instanceof CompoundTag compoundTag) {
                    Rank rank = new Rank(compoundTag);
                    this.ranks.put(rank.getId(), rank);
                }
            }
        } catch (FileNotFoundException ignored) {
            // Ignore
        }
    }

    /**
     * Get all globally enabled permissions.
     *
     * @return all the enabled permissions.
     */
    public Set<Permission> getGlobalPermissions() {
        return Collections.unmodifiableSet(globalPermissions);
    }

    /**
     * Load local data from NBT.
     *
     * @since 0.1.0
     */
    public void saveLocal() {
        try {
            // Create base directory if non-existent.
            if (!baseDir.exists() && !baseDir.mkdirs())
                throw new IOException("Failed to create directories: " + baseDir.getPath());

            // Create NBT tag.
            CompoundTag tag = new CompoundTag();

            // Ranks
            ListTag ranks = new ListTag();
            for (Rank rank : this.ranks.values()) {
                ranks.add(rank.save());
            }
            tag.put("Ranks", ranks);

            // Write data.
            NbtIo.writeCompressed(tag, globalDataFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the instance of the server state manager.
     *
     * @return the instance.
     * @since 0.1.0
     */
    @Nullable
    public static ServerStateManager get() {
        return instance;
    }

    /**
     * Get the server where the manager is bound to.
     *
     * @return the server.
     * @since 0.1.0
     */
    public MinecraftServer server() {
        return server;
    }

    /**
     * Start the state manager.
     *
     * @param server the server.
     * @since 0.1.0
     */
    @ApiStatus.Internal
    public static void start(MinecraftServer server) {
        ServerCore.LOGGER.info("Starting the server-side state manager.");
        instance = new ServerStateManager(server);
    }

    /**
     * Stop the state manager.
     *
     * @since 0.1.0
     */
    @ApiStatus.Internal
    public static void stop() {
        ServerCore.LOGGER.info("Stopping the server-side state manager.");
        instance = null;
    }

    /**
     * Get the state of the player.
     *
     * @param player the player.
     * @return the state.
     * @since 0.1.0
     */
    public ServerPlayerState player(Player player) {
        return player(player.getUUID());
    }

    /**
     * Get the player state from game profile. (For offline use)
     *
     * @param profile the game profile.
     * @return the player state.
     * @since 0.1.0
     */
    @Nullable
    public ServerPlayerState player(GameProfile profile) {
        UUID id = profile.getId();
        if (id != null) {
            return player(id);
        }
        return null;
    }

    /**
     * Get the state of a player using a UUID.
     *
     * @param uuid the player's UUID.
     * @return the state.
     * @since 0.1.0
     */
    public ServerPlayerState player(UUID uuid) {
        return playerStates.computeIfAbsent(uuid, this::loadPlayer);
    }

    private ServerPlayerState loadPlayer(UUID uuid) {
        try {
            return new ServerPlayerState(uuid, this, new File(baseDir, "players/" + uuid.toString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the existing rank.
     *
     * @param id the id of the rank.
     * @return the rank.
     * @since 0.1.0
     */
    public Rank getRank(String id) {
        return this.ranks.get(id);
    }

    /**
     * Create a rank.
     *
     * @param id   the id of the rank.
     * @param name the getObjName of the rank.
     * @since 0.1.0
     * @deprecated use {@link #addRank(String, String, String, int)} instead.
     */
    @Deprecated
    public void addRank(String id, String name) {
        addRank(new Rank(id, name));
    }

    /**
     * Create a rank.
     *
     * @param id       the id of the rank.
     * @param name     the display getObjName of the rank.
     * @param prefix   the chat prefix of the rank.
     * @param priority the index priority of the rank.
     * @since 0.1.0
     */
    public void addRank(String id, String name, String prefix, int priority) {
        this.addRank(new Rank(id, name, prefix, priority));
    }

    /**
     * Add a rank from an already instantiated one.
     *
     * @param rank the rank.
     * @since 0.1.0
     */
    public void addRank(Rank rank) {
        Rank original = ranks.get(rank.getId());
        if (rank instanceof DefaultRank) throw new IllegalArgumentException("Default Rank is for internal usage.");
        if (original != null) throw new IllegalArgumentException("Can't overwrite original rank.");
        this.ranks.put(rank.getId(), rank);
    }

    /**
     * Remove a rank.
     *
     * @param id the id of the rank.
     * @since 0.1.0
     */
    public void removeRank(String id) {
        if (ranks.get(id) instanceof DefaultRank) throw new IllegalArgumentException("Can't remove default Rank.");
        ranks.remove(id);
        for (ServerPlayerState state : playerStates.values()) {
            state.removeRank(id);
        }
    }

    /**
     * Check if a rank exists.
     *
     * @param id the id of the rank.
     * @return whether it exists.
     */
    public boolean hasRank(String id) {
        return ranks.containsKey(id);
    }

    /**
     * Get all the registered ranks
     *
     * @return all the ranks.
     * @since 0.1.0
     */
    public Collection<Rank> getRanks() {
        return ranks.values();
    }

    /**
     * Get the default rank
     *
     * @return the default rank.
     * @since 0.1.0
     */
    public Rank getDefaultRank() {
        return defaultRank;
    }

    /**
     * Check if a command source stack has a certain permission.
     *
     * @param commandSourceStack the command source stack.
     * @param permission         the permission.
     * @return whether it has permission.
     */
    public boolean hasPermission(CommandSourceStack commandSourceStack, String permission) {
        return hasPermission(commandSourceStack, new Permission(permission));
    }

    /**
     * Check if a command source stack has a certain permission.
     *
     * @param commandSourceStack the command source stack.
     * @param permission         the permission.
     * @return whether it has permission.
     */
    public boolean hasPermission(CommandSourceStack commandSourceStack, Permission permission) {
        if (isServer(commandSourceStack) || isOp(commandSourceStack)) {
            return true;
        } else if (commandSourceStack.getEntity() instanceof Player player) {
            return player(player).hasPermission(permission);
        }
        return false;
    }

    private boolean isOp(CommandSourceStack commandSourceStack) {
        return commandSourceStack.hasPermission(commandSourceStack.getServer().getOperatorUserPermissionLevel());
    }

    private boolean isServer(CommandSourceStack commandSourceStack) {
        return commandSourceStack.getEntity() == null && Objects.equals(commandSourceStack.getTextName(), "Server")
                && commandSourceStack.getRotation().equals(Vec2.ZERO) && commandSourceStack.hasPermission(4);
    }

    /**
     * Get all players with a specific rank.
     *
     * @param rank the rank to get the players for.
     * @return the online players with that rank.
     * @since 0.1.0
     */
    public List<Player> getOnlinePlayersWith(Rank rank) {
        if (getRank(rank.getId()) == null) return new ArrayList<>();

        List<Player> players = new ArrayList<>();
        for (ServerPlayerState state : playerStates.values()) {
            if (state.isOffline()) continue;
            if (state.hasRank(rank)) {
                players.add(state.player());
            }
        }

        return players;
    }
}
