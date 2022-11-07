package com.ultreon.mods.servercore.server.config;

import com.ultreon.mods.servercore.util.SnbtIo;
import dev.architectury.platform.Platform;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.ApiStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Config {
    //****************************//
    //     Constants & setup.     //
    //****************************//
    private static final Path CONFIG_FOLDER = Platform.getConfigFolder().resolve("servercore");
    private static final Path CONFIG_FILE = CONFIG_FOLDER.resolve("generic.snbt");
    private static final Object LOCK = new Object();
    private static boolean initialized = false;

    //**************//
    //     Data     //
    //**************//
    private static int teleportDelay = 20 * 10; // 10 seconds.
    private static int teleportTimeout = 20 * 15; // 15 seconds.

    public static int getTeleportDelay() {
        return teleportDelay;
    }

    public static void setTeleportDelay(int teleportDelay) {
        Config.teleportDelay = teleportDelay;
    }

    public static int getTeleportTimeout() {
        return teleportTimeout;
    }

    public static void setTeleportTimeout(int teleportTimeout) {
        Config.teleportTimeout = teleportTimeout;
    }

    //************************//
    //     Internal stuff     //
    //************************//
    @ApiStatus.Internal
    public static void load() {
        if (CONFIG_FILE.toFile().exists()) {
            try {
                load(SnbtIo.read(CONFIG_FILE.toFile()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @ApiStatus.Internal
    public static void save() {
        try {
            File file = CONFIG_FOLDER.toFile();
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    throw new IOException("Couldn't create config directory.");
                }
            }

            Files.writeString(CONFIG_FILE, SnbtIo.write(save(new CompoundTag())),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @ApiStatus.Internal
    public static void init() {
        synchronized (LOCK) {
            if (!initialized) {
                load();
                save();
                initialized = true;
            } else {
                throw new IllegalStateException("Initializing config while already initialized.");
            }
        }
    }

    @ApiStatus.Internal
    private static CompoundTag save(CompoundTag nbt) {
        CompoundTag teleportation = new CompoundTag();

        teleportation.putInt("delay", teleportDelay);
        teleportation.putInt("requestTimeout", teleportTimeout);
        nbt.put("Teleportation", teleportation);

        return nbt;
    }

    @ApiStatus.Internal
    private static void load(CompoundTag nbt) {
        CompoundTag teleportation = nbt.getCompound("Teleportation");
        teleportDelay = teleportation.getInt("delay");
        teleportTimeout = teleportation.getInt("requestTimeout");
    }
}
