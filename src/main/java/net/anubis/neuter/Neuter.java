package net.anubis.neuter;

import net.anubis.neuter.command.ConfigCommands;
import net.anubis.neuter.config.BehaviourEnum;
import net.anubis.neuter.config.Config;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Neuter implements ModInitializer {
    private static final Config defaultConfig = new Config.Builder()
            .setDefaultBehaviour(BehaviourEnum.NEUTRAL)
            .addRule("minecraft:ender_dragon", BehaviourEnum.HOSTILE)
            .addRule("minecraft:wither", BehaviourEnum.HOSTILE)
            .create();
    // TODO - defaultConfig should load from gamerules
    private static final Config config = new Config(defaultConfig);
    public static final Logger LOGGER = LoggerFactory.getLogger("neuter");
    public static final int TICKS_IN_SECOND = 20;

    public static void defaultConfig() {
        config.copyFrom(defaultConfig);
    }

    public static Config getConfig() {
        return config;
    }

    public static void setConfigAngerSeconds(int seconds) {
        config.setAngrySeconds(seconds);
    }

    public static void setConfigDefaultBehaviour(BehaviourEnum behaviour) {
        config.setDefaultBehaviour(behaviour);
    }

    public static void addConfigCustomRule(String entityId, BehaviourEnum behaviour) {
        config.addCustomRule(entityId, behaviour);
    }

    public static void updateConfigFromNbt(NbtCompound nbt) {
        config.fromNbt(nbt, defaultConfig);
    }

    @Override
    public void onInitialize() {
//        ServerWorldEvents.LOAD.register((server, world) -> {
//            LOGGER.warn("World loaded: " + world);
//        });
//        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
//            dispatcher.register(ConfigCommands.configCommandsBuilder);
//        });
    }
}
