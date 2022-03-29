package net.anubis.neuter;

import net.anubis.neuter.config.BehaviourEnum;
import net.anubis.neuter.config.Config;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Neuter implements ModInitializer {
    private static final Config defaultConfig = new Config.Builder()
            .setDefaultBehaviour(BehaviourEnum.NEUTRAL)
            .addRule("minecraft:ender_dragon", BehaviourEnum.HOSTILE)
            .addRule("minecraft:wither", BehaviourEnum.HOSTILE)
            .create();
    private static final Config config = new Config(defaultConfig);
    public static final Logger LOGGER = LoggerFactory.getLogger("neuter");
    public static final int TICKS_IN_SECOND = 20;

    public static void defaultConfig() {
        config.copyFrom(defaultConfig);
    }



    @Override
    public void onInitialize() {
        ServerWorldEvents.LOAD.register((server, world) -> {
            LOGGER.error("World loaded: " + world);
        });
    }
}
