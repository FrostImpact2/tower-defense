package com.towerdefense.registry;

import com.towerdefense.TowerDefenseMod;
import com.towerdefense.gui.TowerMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = 
            DeferredRegister.create(BuiltInRegistries.MENU, TowerDefenseMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<TowerMenu>> TOWER_MENU = 
            MENUS.register("tower_menu", () -> IMenuTypeExtension.create(TowerMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
