package retr0.travellerstoasts.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import static retr0.travellerstoasts.network.ModUsagePacket.NOTIFY_MOD_USAGE_ID;
import static retr0.travellerstoasts.network.TrackInhabitedTimeC2SPacket.INHABITED_TIME_TRACK_REQUEST_ID;
import static retr0.travellerstoasts.network.TrackInhabitedTimeS2CPacket.INHABITED_TIME_TRACK_RESPONSE_ID;
import static retr0.travellerstoasts.network.UpdateVisitedBiomesS2CPacket.UPDATE_VISITED_BIOMES_ID;

public class PacketRegistry {
    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(INHABITED_TIME_TRACK_REQUEST_ID, TrackInhabitedTimeC2SPacket::receive);
        ServerLoginNetworking.registerGlobalReceiver(NOTIFY_MOD_USAGE_ID, ModUsagePacket::receive);
    }

    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(INHABITED_TIME_TRACK_RESPONSE_ID, TrackInhabitedTimeS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(UPDATE_VISITED_BIOMES_ID, UpdateVisitedBiomesS2CPacket::receive);
        ClientLoginNetworking.registerGlobalReceiver(NOTIFY_MOD_USAGE_ID, ModUsagePacket::receive);
    }
}
