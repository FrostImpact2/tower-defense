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
 * Mage Tower - A magical AoE tower
 * 
 * Role: AoE/Support
 * - Medium damage
 * - Medium range
 * - Slow attack speed
 * - Low health
 * - Low aggro limit
 * - Special abilities for AoE and debuffs
 * 
 * Appearance: Golden/enchanted armor with staff
 */
public class MageTowerEntity extends BaseTowerEntity {

    public MageTowerEntity(EntityType<? extends MageTowerEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected TowerStats createDefaultStats() {
        return new TowerStats(
            10.0f,  // damage (medium-high)
            15.0f,  // range (high)
            0.8f,   // attacks per second (slow)
            60.0f,  // max health (low)
            1       // aggro limit (low)
        );
    }

    @Override
    protected void initializeAbilities() {
        // Mage abilities: AoE and crowd control
        abilities.add(new com.towerdefense.ability.ChainLightningAbility());
        abilities.add(new com.towerdefense.ability.ExplosiveShotAbility());
        abilities.add(new com.towerdefense.ability.SlowFieldAbility());
    }

    @Override
    protected void setupAppearance() {
        // Purple/enchanted leather armor for mage look
        ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
        ItemStack chest = new ItemStack(Items.LEATHER_CHESTPLATE);
        ItemStack legs = new ItemStack(Items.LEATHER_LEGGINGS);
        ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);

        // Apply purple color to leather armor (RGB: 138, 43, 226 = Blue Violet)
        int purple = (138 << 16) | (43 << 8) | 226; // 0x8A2BE2

        helmet.set(DataComponents.DYED_COLOR, new net.minecraft.world.item.component.DyedItemColor(purple, true));
        chest.set(DataComponents.DYED_COLOR, new net.minecraft.world.item.component.DyedItemColor(purple, true));
        legs.set(DataComponents.DYED_COLOR, new net.minecraft.world.item.component.DyedItemColor(purple, true));
        boots.set(DataComponents.DYED_COLOR, new net.minecraft.world.item.component.DyedItemColor(purple, true));

        this.setItemSlot(EquipmentSlot.HEAD, helmet);
        this.setItemSlot(EquipmentSlot.CHEST, chest);
        this.setItemSlot(EquipmentSlot.LEGS, legs);
        this.setItemSlot(EquipmentSlot.FEET, boots);
        
        // Blaze rod as staff in main hand
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BLAZE_ROD));
    }

    @Override
    public String getTowerName() {
        return "Mage Tower";
    }

    @Override
    public String getTowerRole() {
        return "Mage";
    }

    public static AttributeSupplier.Builder createAttributes() {
        return BaseTowerEntity.createAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D);
    }
}
