package com.towerdefense.network;

import com.towerdefense.TowerDefenseMod;
import com.towerdefense.entity.tower.BaseTowerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet for tower actions (Upgrade, Sell, Start Move)
 */
public record TowerActionPacket(int towerId, Action action, BlockPos moveTarget, int abilityIndex) implements CustomPacketPayload {

    public static final Type<TowerActionPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TowerDefenseMod.MOD_ID, "tower_action"));

    public static final StreamCodec<FriendlyByteBuf, TowerActionPacket> STREAM_CODEC = StreamCodec.of(
            TowerActionPacket::encode,
            TowerActionPacket::decode
    );

    public enum Action {
        UPGRADE,
        SELL,
        START_MOVE,
        USE_ABILITY
    }

    private static void encode(FriendlyByteBuf buf, TowerActionPacket packet) {
        buf.writeInt(packet.towerId);
        buf.writeEnum(packet.action);
        buf.writeBoolean(packet.moveTarget != null);
        if (packet.moveTarget != null) {
            buf.writeBlockPos(packet.moveTarget);
        }
        buf.writeInt(packet.abilityIndex);
    }

    private static TowerActionPacket decode(FriendlyByteBuf buf) {
        int towerId = buf.readInt();
        Action action = buf.readEnum(Action.class);
        BlockPos moveTarget = null;
        if (buf.readBoolean()) {
            moveTarget = buf.readBlockPos();
        }
        int abilityIndex = buf.readInt();
        return new TowerActionPacket(towerId, action, moveTarget, abilityIndex);
    }

    public static void handle(TowerActionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                Entity entity = player.level().getEntity(packet.towerId);
                if (entity instanceof BaseTowerEntity tower) {
                    // Check distance
                    if (tower.distanceToSqr(player) > 64.0) {
                        return;
                    }

                    switch (packet.action) {
                        case UPGRADE -> tower.upgrade();
                        case SELL -> tower.sell();
                        case START_MOVE -> {
                            // Move mode is tracked client-side
                            // Actual move is sent via TowerMovePacket
                        }
                        case USE_ABILITY -> {
                            // Activate ability at the given index
                            if (packet.abilityIndex >= 0 && packet.abilityIndex < tower.getAbilities().size()) {
                                tower.getAbilities().get(packet.abilityIndex).activate(tower, null);
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
