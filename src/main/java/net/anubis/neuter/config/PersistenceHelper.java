package net.anubis.neuter.config;

import net.anubis.neuter.Neuter;
import net.minecraft.nbt.NbtCompound;

import java.util.Optional;

public class PersistenceHelper {
//    private static NbtCompound levelNbt = new NbtCompound();
    public static final String NEUTER_LEVEL_NBT_KEY = "Neuter";

    public static NbtCompound saveLevelNbt() {
        return Neuter.getConfig().toNbt();
    }

    public static void loadLevelNbt(NbtCompound levelNbt) {
        Neuter.updateConfigFromNbt(levelNbt);
    }
}
