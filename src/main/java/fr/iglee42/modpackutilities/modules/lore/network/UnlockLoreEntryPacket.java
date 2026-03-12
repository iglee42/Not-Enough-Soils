package fr.iglee42.modpackutilities.modules.lore.network;

import fr.iglee42.modpackutilities.IgleeModpackUtilities;
import fr.iglee42.modpackutilities.modules.lore.LoreEntry;
import fr.iglee42.modpackutilities.modules.lore.LoreFile;
import fr.iglee42.modpackutilities.modules.lore.LoreModule;
import fr.iglee42.modpackutilities.modules.lore.progress.LoreProgress;
import fr.iglee42.modpackutilities.modules.lore.progress.LoreProgressManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class UnlockLoreEntryPacket {

    private final ResourceLocation fileId;
    private final ResourceLocation entryId;

    public UnlockLoreEntryPacket(ResourceLocation fileId, ResourceLocation entryId) {
        this.fileId = fileId;
        this.entryId = entryId;
    }

    public UnlockLoreEntryPacket(FriendlyByteBuf buf) {
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
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            var module = IgleeModpackUtilities.getModule(LoreModule.class);
            if (module == null) {
                return;
            }

            LoreFile file = module.getFiles().get(fileId);
            if (file == null) {
                return;
            }

            LoreProgressManager manager = LoreProgressManager.get();
            manager.tryUnlockEntry(player,file,entryId);
        });
        return true;
    }

}

