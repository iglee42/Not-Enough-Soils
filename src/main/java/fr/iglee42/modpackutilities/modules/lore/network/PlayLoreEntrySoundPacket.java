package fr.iglee42.modpackutilities.modules.lore.network;

import fr.iglee42.modpackutilities.IgleeModpackUtilities;
import fr.iglee42.modpackutilities.modules.lore.ClientLoreModule;
import fr.iglee42.modpackutilities.modules.lore.LoreEntry;
import fr.iglee42.modpackutilities.modules.lore.LoreFile;
import fr.iglee42.modpackutilities.modules.lore.LoreModule;
import fr.iglee42.modpackutilities.modules.lore.client.LoreSoundHandler;
import fr.iglee42.modpackutilities.modules.lore.progress.LoreProgressManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PlayLoreEntrySoundPacket(ResourceLocation fileId, ResourceLocation entryId) implements CustomPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayLoreEntrySoundPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, PlayLoreEntrySoundPacket::fileId,
            ResourceLocation.STREAM_CODEC, PlayLoreEntrySoundPacket::entryId,
            PlayLoreEntrySoundPacket::new
    );

    public static final Type<PlayLoreEntrySoundPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("lore","play_lore_entry_sound"));


    public static void handle(IPayloadContext context, PlayLoreEntrySoundPacket payload) {
        context.enqueueWork(() -> {
            LoreFile file = ClientLoreModule.getInstance().getLoreFile(payload.fileId()).orElse(null);
            if (file == null) {
                return;
            }

            if (file.getEntry(payload.entryId()).isEmpty()) return;
            LoreEntry entry = file.getEntry(payload.entryId()).get();
            SoundEvent sound = entry.voiceLocation().flatMap(BuiltInRegistries.SOUND_EVENT::getOptional).orElse(SoundEvents.EMPTY);
            if (sound != SoundEvents.EMPTY){
                LoreSoundHandler.getInstance().playSound(sound);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

