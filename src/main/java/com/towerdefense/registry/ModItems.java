package com.towerdefense.registry;

import com.towerdefense.TowerDefenseMod;
import com.towerdefense.item.TowerPlacerItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, TowerDefenseMod.MOD_ID);

    // Tower placer items
    public static final DeferredHolder<Item, Item> ARCHER_TOWER_PLACER =
            ITEMS.register("archer_tower_placer", () -> new TowerPlacerItem(
                    () -> ModEntities.ARCHER_TOWER.get(),
                    "Archer Tower",
                    List.of(
                            Component.literal("§6Role: §fDPS"),
                            Component.literal("§7High attack speed"),
                            Component.literal("§7Medium damage"),
                            Component.literal("§7Range: 12 blocks")
                    )
            ));

    public static final DeferredHolder<Item, Item> TANK_TOWER_PLACER =
            ITEMS.register("tank_tower_placer", () -> new TowerPlacerItem(
                    () -> ModEntities.TANK_TOWER.get(),
                    "Tank Tower",
                    List.of(
                            Component.literal("§6Role: §fTank"),
                            Component.literal("§7Very high health"),
                            Component.literal("§7Can block 5 enemies"),
                            Component.literal("§7Range: 10 blocks")
                    )
            ));

    public static final DeferredHolder<Item, Item> MAGE_TOWER_PLACER =
            ITEMS.register("mage_tower_placer", () -> new TowerPlacerItem(
                    () -> ModEntities.MAGE_TOWER.get(),
                    "Mage Tower",
                    List.of(
                            Component.literal("§6Role: §fMage"),
                            Component.literal("§7AoE damage"),
                            Component.literal("§7Special abilities"),
                            Component.literal("§7Range: 15 blocks")
                    )
            ));

    public static final DeferredHolder<Item, Item> AILYON_TOWER_PLACER =
            ITEMS.register("ailyon_tower_placer", () -> new TowerPlacerItem(
                    () -> ModEntities.AILYON_TOWER.get(),
                    "Ailyon, the Skirmisher",
                    List.of(
                            Component.literal("§6Role: §fAssassin"),
                            Component.literal("§7Blink teleport movement"),
                            Component.literal("§7Spectral charge system"),
                            Component.literal("§7Attack while moving"),
                            Component.literal("§7Range: 8 blocks")
                    )
            ));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}