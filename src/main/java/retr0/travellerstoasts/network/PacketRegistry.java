package retr0.travellerstoasts.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import static retr0.travellerstoasts.network.ModUsageS2CPacket.NOTIFY_MOD_USAGE_ID;
import static retr0.travellerstoasts.network.TrackInhabitedTimeC2SPacket.INHABITED_TIME_TRACK_REQUEST_ID;
import static retr0.travellerstoasts.network.TrackInhabitedTimeS2CPacket.INHABITED_TIME_TRACK_RESPONSE_ID;

public class PacketRegistry {
    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(INHABITED_TIME_TRACK_REQUEST_ID, TrackInhabitedTimeC2SPacket::receive);
    }

    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(INHABITED_TIME_TRACK_RESPONSE_ID, TrackInhabitedTimeS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(NOTIFY_MOD_USAGE_ID, ModUsageS2CPacket::receive);
    }
}
