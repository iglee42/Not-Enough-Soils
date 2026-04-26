package fr.iglee42.modpackutilities.modules.lore.network;

import fr.iglee42.modpackutilities.IgleeModpackUtilities;
import fr.iglee42.modpackutilities.modules.lore.LoreFile;
import fr.iglee42.modpackutilities.modules.lore.LoreModule;
import fr.iglee42.modpackutilities.modules.lore.progress.LoreProgressManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UnlockLoreEntryPacket(ResourceLocation fileId,ResourceLocation entryId) implements CustomPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf,UnlockLoreEntryPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,UnlockLoreEntryPacket::fileId,
            ResourceLocation.STREAM_CODEC,UnlockLoreEntryPacket::entryId,
            UnlockLoreEntryPacket::new
    );

    public static final Type<UnlockLoreEntryPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("lore","unlock_lore_entry"));


    public static void handle(IPayloadContext context,UnlockLoreEntryPacket payload) {
        context.enqueueWork(() -> {
            Player unsafePlayer = context.player();
            if (!(unsafePlayer instanceof ServerPlayer player)) {
                return;
            }

            var module = IgleeModpackUtilities.getModule(LoreModule.class);
            if (module == null) {
                return;
            }

            LoreFile file = module.getFiles().get(payload.fileId());
            if (file == null) {
                return;
            }

            LoreProgressManager manager = LoreProgressManager.get();
            manager.tryUnlockEntry(player,file,payload.entryId());
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

