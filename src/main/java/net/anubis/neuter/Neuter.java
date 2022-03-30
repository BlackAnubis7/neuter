package net.anubis.neuter;

import net.anubis.neuter.command.ConfigCommands;
import net.anubis.neuter.config.BehaviourEnum;
import net.anubis.neuter.config.Config;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.nbt.NbtCompound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

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

    public static int getConfigAngerSeconds() {
        return config.getAngrySeconds();
    }

    public static BehaviourEnum getConfigDefaultBehaviour() {
        return config.getDefaultBehaviour();
    }

    public static Map<String, BehaviourEnum> getConfigCustomRules() {
        return config.getCustomRules();
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

    public static void removeConfigCustomRule(String entityId) {
        config.removeCustomRule(entityId);
    }

    public static void resetConfigCustomRule() {
        config.copyCustomRules(defaultConfig);
    }

    public static void updateConfigFromNbt(NbtCompound nbt) {
        config.fromNbt(nbt, defaultConfig);
    }

    @Override
    public void onInitialize() {
//        ServerWorldEvents.LOAD.register((server, world) -> {
//            LOGGER.warn("World loaded: " + world);
//        });
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(ConfigCommands.configCommandsBuilder));
    }
}
