package retr0.travellerstoasts.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

import static retr0.travellerstoasts.TravellersToasts.MOD_ID;

public class PacketRegistry {
    public static final Identifier INHABITED_TIME_TRACK_REQUEST_ID = new Identifier(MOD_ID, "inhabited_time_track_request");
    public static final Identifier INHABITED_TIME_TRACK_RESPONSE_ID = new Identifier(MOD_ID, "inhabited_time_track_response");
    public static final Identifier NOTIFY_MOD_USAGE_ID = new Identifier(MOD_ID, "notify_mod_usage");

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(INHABITED_TIME_TRACK_REQUEST_ID, TrackInhabitedTimeC2SPacket::receive);
    }

    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(INHABITED_TIME_TRACK_RESPONSE_ID, TrackInhabitedTimeS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(NOTIFY_MOD_USAGE_ID, ModUsageS2CPacket::receive);
    }
}
