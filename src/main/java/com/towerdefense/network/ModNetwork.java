package com.towerdefense.network;

import com.towerdefense.TowerDefenseMod;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Network handler for mod packets
 */
public class ModNetwork {

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ModNetwork::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(TowerDefenseMod.MOD_ID);
        
        // Register tower action packet (client -> server)
        registrar.playToServer(
            TowerActionPacket.TYPE,
            TowerActionPacket.STREAM_CODEC,
            TowerActionPacket::handle
        );
        
        // Register tower move packet (client -> server)
        registrar.playToServer(
            TowerMovePacket.TYPE,
            TowerMovePacket.STREAM_CODEC,
            TowerMovePacket::handle
        );
        
        // Register tower selection packet (client -> server)
        registrar.playToServer(
            TowerSelectionPacket.TYPE,
            TowerSelectionPacket.STREAM_CODEC,
            TowerSelectionPacket::handle
        );
    }

    /**
     * Send a packet to the server
     */
    public static void sendToServer(CustomPacketPayload packet) {
        net.minecraft.client.Minecraft.getInstance().getConnection().send(packet);
    }
}
