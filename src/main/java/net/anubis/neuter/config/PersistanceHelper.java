package net.anubis.neuter.config;

import net.minecraft.nbt.NbtCompound;

import java.util.Optional;

public class PersistanceHelper {
    private static NbtCompound levelNbt = new NbtCompound();
    public static final String NEUTER_LEVEL_NBT_KEY = "Neuter";

    public static NbtCompound getLevelNbt() {
        return levelNbt;
    }

    public static void setLevelNbt(NbtCompound levelNbt) {
        PersistanceHelper.levelNbt = levelNbt.copy();
    }
}
