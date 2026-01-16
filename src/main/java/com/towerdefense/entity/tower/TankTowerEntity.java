package com.towerdefense.entity.tower;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Tank Tower - A defensive tower
 * 
 * Role: Tank/Defender
 * - Very high health
 * - Low damage
 * - Slow attack speed
 * - High aggro limit (can block many enemies)
 * - Medium range
 * 
 * Appearance: Iron armor with shield
 */
public class TankTowerEntity extends BaseTowerEntity {

    public TankTowerEntity(EntityType<? extends TankTowerEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected TowerStats createDefaultStats() {
        return new TowerStats(
            4.0f,   // damage (low)
            10.0f,  // range (medium)
            0.5f,   // attacks per second (slow)
            200.0f, // max health (very high)
            5       // aggro limit (high - tank can block 5 enemies)
        );
    }

    @Override
    protected void initializeAbilities() {
        // Tank abilities: defensive and supportive
        abilities.add(new com.towerdefense.ability.ShieldAbility());
        abilities.add(new com.towerdefense.ability.HealAuraAbility());
    }

    @Override
    protected void setupAppearance() {
        // Iron armor for tank look
        ItemStack helmet = new ItemStack(Items.IRON_HELMET);
        ItemStack chest = new ItemStack(Items.IRON_CHESTPLATE);
        ItemStack legs = new ItemStack(Items.IRON_LEGGINGS);
        ItemStack boots = new ItemStack(Items.IRON_BOOTS);

        this.setItemSlot(EquipmentSlot.HEAD, helmet);
        this.setItemSlot(EquipmentSlot.CHEST, chest);
        this.setItemSlot(EquipmentSlot.LEGS, legs);
        this.setItemSlot(EquipmentSlot.FEET, boots);
        
        // Shield in main hand, sword in offhand
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.SHIELD));
        this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.IRON_SWORD));
    }

    @Override
    public String getTowerName() {
        return "Tank Tower";
    }

    @Override
    public String getTowerRole() {
        return "Tank";
    }

    public static AttributeSupplier.Builder createAttributes() {
        return BaseTowerEntity.createAttributes()
                .add(Attributes.MAX_HEALTH, 200.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.20D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D);
    }
}
