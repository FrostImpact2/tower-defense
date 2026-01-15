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
 * Packet to command a tower to move to a specific block position
 */
public record TowerMovePacket(int towerId, BlockPos target) implements CustomPacketPayload {

    public static final Type<TowerMovePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TowerDefenseMod.MOD_ID, "tower_move"));

    public static final StreamCodec<FriendlyByteBuf, TowerMovePacket> STREAM_CODEC = StreamCodec.of(
            TowerMovePacket::encode,
            TowerMovePacket::decode
    );

    private static void encode(FriendlyByteBuf buf, TowerMovePacket packet) {
        buf.writeInt(packet.towerId);
        buf.writeBlockPos(packet.target);
    }

    private static TowerMovePacket decode(FriendlyByteBuf buf) {
        int towerId = buf.readInt();
        BlockPos target = buf.readBlockPos();
        return new TowerMovePacket(towerId, target);
    }

    public static void handle(TowerMovePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                Entity entity = player.level().getEntity(packet.towerId);
                if (entity instanceof BaseTowerEntity tower) {
                    // Check distance - tower shouldn't be too far from player
                    if (tower.distanceToSqr(player) > 256.0) {
                        return;
                    }
                    
                    // Check target distance - target shouldn't be too far from tower
                    double targetDist = tower.distanceToSqr(
                        packet.target.getX() + 0.5,
                        packet.target.getY(),
                        packet.target.getZ() + 0.5
                    );
                    if (targetDist > 2500.0) { // Max 50 blocks
                        return;
                    }
                    
                    tower.startMovingTo(packet.target);
                }
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
