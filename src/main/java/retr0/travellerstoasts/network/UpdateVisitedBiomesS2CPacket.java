package retr0.travellerstoasts.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import retr0.travellerstoasts.util.BiomeToastManager;

import java.util.ArrayList;
import java.util.Collection;

import static retr0.travellerstoasts.TravellersToasts.MOD_ID;

/**
 * A packet to synchronize already-explored biomes from the server to the client. This exists as a fallback to the client-side
 * method present in {@link BiomeToastManager}.
 */
public class UpdateVisitedBiomesS2CPacket {
    public static final Identifier UPDATE_VISITED_BIOMES_ID = new Identifier(MOD_ID, "update_visited_biomes");

    public static void send(Collection<String> visited, ServerPlayerEntity player) {
        var buf = PacketByteBufs.create();
        buf.writeInt(visited.size());
        visited.forEach(id -> buf.writeIdentifier(new Identifier(id)));

        ServerPlayNetworking.send(player, UPDATE_VISITED_BIOMES_ID, buf);
    }



    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender)
    {
        var numVisited = buf.readInt();
        var visitedBiomes = new ArrayList<Identifier>();
        for (var i = 0; i < numVisited; ++i)
            visitedBiomes.add(i, buf.readIdentifier());

        client.execute(() -> BiomeToastManager.getInstance().addVisitedBiomes(visitedBiomes));
    }
}
