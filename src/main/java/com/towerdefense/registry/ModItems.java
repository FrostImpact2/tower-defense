package com.towerdefense.registry;

import com.towerdefense.TowerDefenseMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = 
            DeferredRegister.create(BuiltInRegistries.ITEM, TowerDefenseMod.MOD_ID);

    // Tower spawner items
    public static final DeferredHolder<Item, Item> ARCHER_TOWER_SPAWNER = 
            ITEMS.register("archer_tower_spawner", () -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
