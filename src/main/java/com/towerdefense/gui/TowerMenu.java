package com.towerdefense.gui;

import com.towerdefense.entity.tower.BaseTowerEntity;
import com.towerdefense.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * Container menu for Tower GUI
 * Handles tower interaction logic like upgrade, sell, and move
 */
public class TowerMenu extends AbstractContainerMenu {

    private final BaseTowerEntity tower;
    private final Player player;

    // Client-side constructor (from network)
    public TowerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getTowerFromBuf(playerInventory.player, extraData));
    }

    // Server-side constructor
    public TowerMenu(int containerId, Inventory playerInventory, BaseTowerEntity tower) {
        super(ModMenuTypes.TOWER_MENU.get(), containerId);
        this.player = playerInventory.player;
        this.tower = tower;
    }

    private static BaseTowerEntity getTowerFromBuf(Player player, FriendlyByteBuf buf) {
        int entityId = buf.readInt();
        Entity entity = player.level().getEntity(entityId);
        if (entity instanceof BaseTowerEntity towerEntity) {
            return towerEntity;
        }
        return null;
    }

    /**
     * Called when player clicks the Upgrade button
     */
    public void handleUpgrade() {
        if (tower != null && !player.level().isClientSide()) {
            tower.upgrade();
        }
    }

    /**
     * Called when player clicks the Sell button
     */
    public void handleSell() {
        if (tower != null && !player.level().isClientSide()) {
            tower.sell();
            player.closeContainer();
        }
    }

    /**
     * Called when player clicks the Move button
     * This puts the GUI in "move mode" - next block click will be the destination
     */
    public void handleMoveMode() {
        // Move mode is handled client-side via network packet
    }

    /**
     * Get the tower entity
     */
    public BaseTowerEntity getTower() {
        return tower;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return tower != null && tower.isAlive() && tower.distanceToSqr(player) < 64.0;
    }
}
