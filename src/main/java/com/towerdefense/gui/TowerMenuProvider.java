package com.towerdefense.gui;

import com.towerdefense.entity.tower.BaseTowerEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Menu provider for the Tower GUI
 */
public class TowerMenuProvider implements MenuProvider {

    private final BaseTowerEntity tower;

    public TowerMenuProvider(BaseTowerEntity tower) {
        this.tower = tower;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal(tower.getTowerName());
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new TowerMenu(containerId, playerInventory, tower);
    }
}
