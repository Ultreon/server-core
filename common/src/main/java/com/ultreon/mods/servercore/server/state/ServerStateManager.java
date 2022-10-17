package com.ultreon.mods.servercore.server.state;

import com.ultreon.mods.servercore.server.Permission;
import com.ultreon.mods.servercore.server.Rank;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.File;
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
    private final File baseFile;
    private final Map<String, Rank> ranks = new HashMap<>();

    private ServerStateManager(MinecraftServer server) {
        this.server = server;
        this.baseFile = server.getWorldPath(LEVEL_RESOURCE).toFile();
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
        instance = new ServerStateManager(server);
    }

    /**
     * Stop the state manager.
     *
     * @since 0.1.0
     */
    @ApiStatus.Internal
    public static void stop() {
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
            return new ServerPlayerState(uuid, this, new File(baseFile, "players/" + uuid.toString()));
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
     * @param name the name of the rank.
     * @since 0.1.0
     */
    public void createRank(String id, String name) {
        this.createRank(new Rank(id, name));
    }

    /**
     * Create from an already instantiated rank.
     *
     * @param rank the rank.
     * @since 0.1.0
     */
    public void createRank(Rank rank) {
        this.ranks.put(rank.getId(), rank);
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
