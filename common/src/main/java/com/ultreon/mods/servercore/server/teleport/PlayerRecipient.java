package com.ultreon.mods.servercore.server.teleport;

import com.ultreon.mods.servercore.server.ServerHooks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public interface PlayerRecipient extends EntityRecipient<Player> {
    /**
     * Get the recipient from an entity uuid.
     * @param uuid the uuid of the entity.
     * @return the recipient.
     */
    static PlayerRecipient player(UUID uuid) {
        Entity entity = ServerHooks.player(uuid);
        if (entity instanceof PlayerRecipient recipient) {
            return recipient;
        }
        return null;
    }
}
