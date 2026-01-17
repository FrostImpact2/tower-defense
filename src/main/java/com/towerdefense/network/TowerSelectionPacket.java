package com.towerdefense.network;

import com.towerdefense.TowerDefenseMod;
import com.towerdefense.entity.tower.BaseTowerEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet to notify server when a tower is selected or deselected
 */
public record TowerSelectionPacket(int towerId, boolean selected) implements CustomPacketPayload {

    public static final Type<TowerSelectionPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TowerDefenseMod.MOD_ID, "tower_selection"));

    public static final StreamCodec<FriendlyByteBuf, TowerSelectionPacket> STREAM_CODEC = StreamCodec.of(
            TowerSelectionPacket::encode,
            TowerSelectionPacket::decode
    );

    private static void encode(FriendlyByteBuf buf, TowerSelectionPacket packet) {
        buf.writeInt(packet.towerId);
        buf.writeBoolean(packet.selected);
    }

    private static TowerSelectionPacket decode(FriendlyByteBuf buf) {
        int towerId = buf.readInt();
        boolean selected = buf.readBoolean();
        return new TowerSelectionPacket(towerId, selected);
    }

    public static void handle(TowerSelectionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                Entity entity = player.level().getEntity(packet.towerId);
                if (entity instanceof BaseTowerEntity tower) {
                    // Check distance - tower shouldn't be too far from player
                    if (tower.distanceToSqr(player) > 1024.0) {
                        return;
                    }
                    
                    tower.setSelected(packet.selected);
                }
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
