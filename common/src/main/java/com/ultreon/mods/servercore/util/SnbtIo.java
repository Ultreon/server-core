package com.ultreon.mods.servercore.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.SnbtPrinterTagVisitor;
import net.minecraft.nbt.TagParser;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SnbtIo {
    public static String write(CompoundTag tag) {
        return new SnbtPrinterTagVisitor().visit(tag);
    }

    public static void write(CompoundTag tag, Writer writer) throws IOException {
        write(tag, writer, true);
    }

    public static void write(CompoundTag tag, Writer writer, boolean flush) throws IOException {
        String visit = new SnbtPrinterTagVisitor().visit(tag);
        writer.write(visit);

        if (flush) writer.flush();
    }

    public static void write(CompoundTag tag, File file) throws IOException {
        write(tag, file, true);
    }

    public static void write(CompoundTag tag, File file, boolean flush) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            write(tag, writer, flush);
        }
    }

    public static CompoundTag read(String tag) throws IOException {
        try {
            return TagParser.parseTag(tag);
        } catch (CommandSyntaxException e) {
            throw new IOException("Error occurred loading tag: " + e.getMessage());
        }
    }

    public static CompoundTag read(File file) throws IOException {
        try(FileReader reader = new FileReader(file)) {
            return read(reader);
        }
    }

    private static CompoundTag read(Reader reader) throws IOException {
        return read(new BufferedReader(reader));
    }

    private static CompoundTag read(BufferedReader reader) throws IOException {
        try {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) lines.add(line);

            reader.close();

            return TagParser.parseTag(String.join("", lines));
        } catch (CommandSyntaxException e) {
            throw new IOException("Error occurred loading tag: " + e.getMessage());
        }
    }
}
