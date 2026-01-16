package com.towerdefense.registry;

import com.towerdefense.TowerDefenseMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = 
            DeferredRegister.create(BuiltInRegistries.MENU, TowerDefenseMod.MOD_ID);

    // Menu registration removed - using side GUI instead of center screen GUI

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
