package fr.iglee42.modpackutilities.modules.lore.network;

import fr.iglee42.modpackutilities.modules.lore.ClientLoreModule;
import fr.iglee42.modpackutilities.modules.lore.LoreFile;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record SyncLoreFilesPacket(List<LoreFile> files) implements CustomPacketPayload{

    public static final StreamCodec<RegistryFriendlyByteBuf,SyncLoreFilesPacket> STREAM_CODEC = StreamCodec.composite(
            LoreFile.STREAM_CODEC.apply(ByteBufCodecs.list()),SyncLoreFilesPacket::files,
            SyncLoreFilesPacket::new
    );

    public static final Type<SyncLoreFilesPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("lore","sync_lore_files"));

    public static void handle(IPayloadContext context, SyncLoreFilesPacket payload) {
        context.enqueueWork(() -> {
            ClientLoreModule.getInstance().syncFromServer(new ArrayList<>(payload.files()));
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
