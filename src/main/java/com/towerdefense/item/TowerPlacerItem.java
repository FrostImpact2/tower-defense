package com.towerdefense.item;

import com.towerdefense.entity.tower.BaseTowerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Supplier;

/**
 * Base class for tower placement items
 * Right-click on ground to spawn a tower
 */
public class TowerPlacerItem extends Item {
    
    private final Supplier<EntityType<? extends BaseTowerEntity>> towerType;
    private final String towerName;
    private final List<Component> description;
    
    public TowerPlacerItem(Supplier<EntityType<? extends BaseTowerEntity>> towerType, String towerName, List<Component> description) {
        super(new Properties().stacksTo(16));
        this.towerType = towerType;
        this.towerName = towerName;
        this.description = description;
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        
        BlockPos pos = context.getClickedPos().above();
        
        // Create and spawn tower entity
        BaseTowerEntity tower = towerType.get().create(level);
        if (tower != null) {
            tower.moveTo(
                pos.getX() + 0.5,
                pos.getY(),
                pos.getZ() + 0.5,
                context.getRotation(),
                0.0F
            );
            
            level.addFreshEntity(tower);
            
            // Consume item
            if (!context.getPlayer().isCreative()) {
                context.getItemInHand().shrink(1);
            }
            
            // Show message
            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(
                    Component.literal("Placed " + towerName),
                    true
                );
            }
            
            return InteractionResult.CONSUME;
        }
        
        return InteractionResult.FAIL;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag flag) {
        tooltipComponents.add(Component.literal("§7Right-click to place tower"));
        tooltipComponents.addAll(description);
    }
}
