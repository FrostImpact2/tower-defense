package com.towerdefense.entity.tower;

import com.towerdefense.ability.MultiShotAbility;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Archer Tower - A ranged DPS tower
 * 
 * Role: DPS (Damage Per Second)
 * - High attack speed
 * - Medium damage
 * - Low aggro limit (1)
 * - Medium range
 * 
 * Appearance: Leather armor with bow
 * Special Ability: Multi-Shot - Fire multiple arrows at once
 */
public class ArcherTowerEntity extends BaseTowerEntity {

    public ArcherTowerEntity(EntityType<? extends ArcherTowerEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected TowerStats createDefaultStats() {
        return new TowerStats(
            8.0f,   // damage
            12.0f,  // range
            1.5f,   // attacks per second
            80.0f,  // max health
            1       // aggro limit (low - archer can only block 1 enemy)
        );
    }

    @Override
    protected void initializeAbilities() {
        abilities.add(new MultiShotAbility());
    }

    @Override
    protected void setupAppearance() {
        // Leather armor for archer look
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.LEATHER_CHESTPLATE));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.LEATHER_LEGGINGS));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.LEATHER_BOOTS));
        
        // Bow in main hand
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
    }

    @Override
    public String getTowerName() {
        return "Archer Tower";
    }

    @Override
    public String getTowerRole() {
        return "DPS";
    }

    public static AttributeSupplier.Builder createAttributes() {
        return BaseTowerEntity.createAttributes()
                .add(Attributes.MAX_HEALTH, 80.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D);
    }
}
