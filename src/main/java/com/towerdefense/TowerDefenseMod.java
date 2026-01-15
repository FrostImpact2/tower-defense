package com.towerdefense;

import com.towerdefense.registry.ModEntities;
import com.towerdefense.registry.ModItems;
import com.towerdefense.registry.ModMenuTypes;
import com.towerdefense.network.ModNetwork;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(TowerDefenseMod.MOD_ID)
public class TowerDefenseMod {
    public static final String MOD_ID = "towerdefense";
    public static final Logger LOGGER = LoggerFactory.getLogger(TowerDefenseMod.class);

    public TowerDefenseMod(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Tower Defense Mod initializing...");
        
        // Register all deferred registers
        ModItems.register(modEventBus);
        ModEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModNetwork.register(modEventBus);
        
        LOGGER.info("Tower Defense Mod initialized!");
    }
}
