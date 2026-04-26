package fr.iglee42.modpackutilities.modules.lore.network;

import fr.iglee42.modpackutilities.modules.lore.ClientLoreModule;
import fr.iglee42.modpackutilities.modules.lore.progress.LoreProgress;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncLoreProgressPacket(LoreProgress progress) implements CustomPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf,SyncLoreProgressPacket> STREAM_CODEC = StreamCodec.composite(
            LoreProgress.STREAM_CODEC,SyncLoreProgressPacket::progress,
            SyncLoreProgressPacket::new
    );

    public static final Type<SyncLoreProgressPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("lore","sync_lore_progress"));


    public static void handle(IPayloadContext context,SyncLoreProgressPacket payload) {
        context.enqueueWork(() -> {
            ClientLoreModule.getInstance().syncProgressFromServer(payload.progress());
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
