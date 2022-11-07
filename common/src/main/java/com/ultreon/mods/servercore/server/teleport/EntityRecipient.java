package com.ultreon.mods.servercore.server.teleport;

import com.ultreon.mods.servercore.server.ServerHooks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * @author Qboi123
 * @param <T>
 */
public interface EntityRecipient<T extends Entity> extends RequestRecipient<T> {
    @Override
    default Vec3 getObjPos() {
        System.out.println("Entity recipient obj pos.");
        System.out.println("true = " + true);
        Entity recipient = this.recipient();
        System.out.println("recipient = " + recipient);
        return recipient != null ? recipient.position() : null;
    }

    @Override
    default TeleportDestination<Entity> asDestination() {
        return TeleportDestination.entity(recipient());
    }

    /**
     * Get the recipient from an entity uuid.
     * @param uuid the uuid of the entity.
     * @return the recipient.
     */
    static EntityRecipient<?> entity(UUID uuid) {
        Entity entity = ServerHooks.entity(uuid);
        if (entity instanceof EntityRecipient<?> recipient) {
            return recipient;
        }
        return null;
    }
}
