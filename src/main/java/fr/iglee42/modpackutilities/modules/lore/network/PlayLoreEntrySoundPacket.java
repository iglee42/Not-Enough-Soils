package fr.iglee42.modpackutilities.modules.lore.network;

import fr.iglee42.modpackutilities.IgleeModpackUtilities;
import fr.iglee42.modpackutilities.modules.lore.ClientLoreModule;
import fr.iglee42.modpackutilities.modules.lore.LoreEntry;
import fr.iglee42.modpackutilities.modules.lore.LoreFile;
import fr.iglee42.modpackutilities.modules.lore.LoreModule;
import fr.iglee42.modpackutilities.modules.lore.client.LoreSoundHandler;
import fr.iglee42.modpackutilities.modules.lore.progress.LoreProgressManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayLoreEntrySoundPacket {

    private final ResourceLocation fileId;
    private final ResourceLocation entryId;

    public PlayLoreEntrySoundPacket(ResourceLocation fileId, ResourceLocation entryId) {
        this.fileId = fileId;
        this.entryId = entryId;
    }

    public PlayLoreEntrySoundPacket(FriendlyByteBuf buf) {
        this.fileId = buf.readResourceLocation();
        this.entryId = buf.readResourceLocation();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeResourceLocation(fileId);
        buf.writeResourceLocation(entryId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {

            LoreFile file = ClientLoreModule.getInstance().getLoreFile(fileId).orElse(null);
            if (file == null) {
                return;
            }

            if (file.getEntry(entryId).isEmpty()) return;

            LoreEntry entry = file.getEntry(entryId).get();
            SoundEvent sound = entry.voiceLocation().flatMap(BuiltInRegistries.SOUND_EVENT::getOptional).orElse(SoundEvents.EMPTY);
            if (sound != SoundEvents.EMPTY){
                LoreSoundHandler.getInstance().playSound(sound);
            }
        });
        return true;
    }

}

