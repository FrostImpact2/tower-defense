package com.towerdefense.entity.tower;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.towerdefense.ability.MultiShotAbility;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * Archer Tower - A ranged DPS tower
 * 
 * Role: DPS (Damage Per Second)
 * - High attack speed
 * - Medium damage
 * - Low aggro limit (1)
 * - Medium range
 * 
 * Appearance: Dark green leather armor with custom skull head and bow
 * Special Ability: Multi-Shot - Fire multiple arrows at once
 */
public class ArcherTowerEntity extends BaseTowerEntity {

    // Base64 encoded skull texture for archer tower head
    // This is a custom player head texture showing an archer/ranger face
    // Source: Minecraft player head texture from the texture database
    private static final String ARCHER_SKULL_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzMxNmVhZmE1YTgzMWI2YTRiOWRlNDNiMDA2NDkwNDJmNGZhOGYwYWU2MjY1YWMyNTE1YWQxZGJkYzE1MTc1MyJ9fX0=";

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
        abilities.add(new com.towerdefense.ability.RapidFireAbility());
        abilities.add(new com.towerdefense.ability.CriticalStrikeAbility());
    }

    @Override
    protected void setupAppearance() {
        // Dark green leather armor for archer look
        ItemStack helmet = createCustomSkullHead();
        ItemStack chest = new ItemStack(Items.LEATHER_CHESTPLATE);
        ItemStack legs = new ItemStack(Items.LEATHER_LEGGINGS);
        ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);

        // Apply dark green color to leather armor (RGB: 34, 139, 34 = Forest Green)
        // Minecraft uses integer color format: (R << 16) | (G << 8) | B
        int darkGreen = (34 << 16) | (139 << 8) | 34; // 0x228B22

        chest.set(DataComponents.DYED_COLOR, new net.minecraft.world.item.component.DyedItemColor(darkGreen, true));
        legs.set(DataComponents.DYED_COLOR, new net.minecraft.world.item.component.DyedItemColor(darkGreen, true));
        boots.set(DataComponents.DYED_COLOR, new net.minecraft.world.item.component.DyedItemColor(darkGreen, true));

        this.setItemSlot(EquipmentSlot.HEAD, helmet);
        this.setItemSlot(EquipmentSlot.CHEST, chest);
        this.setItemSlot(EquipmentSlot.LEGS, legs);
        this.setItemSlot(EquipmentSlot.FEET, boots);
        
        // Bow in main hand
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
    }

    /**
     * Create a player head with custom texture
     */
    private ItemStack createCustomSkullHead() {
        ItemStack skull = new ItemStack(Items.PLAYER_HEAD);
        
        // Create a game profile with the texture
        GameProfile profile = new GameProfile(UUID.randomUUID(), "ArcherTower");
        profile.getProperties().put("textures", new Property("textures", ARCHER_SKULL_TEXTURE));
        
        // Set the profile on the skull
        skull.set(DataComponents.PROFILE, new ResolvableProfile(profile));
        
        return skull;
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
