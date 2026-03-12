package fr.iglee42.modpackutilities.modules.lore.network;

import com.google.common.collect.Sets;
import fr.iglee42.modpackutilities.modules.lore.ClientLoreModule;
import fr.iglee42.modpackutilities.modules.lore.LoreEntry;
import fr.iglee42.modpackutilities.modules.lore.LoreFile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Supplier;

public class SyncLoreFilesPacket {
    private final List<LoreFile> files;

    public SyncLoreFilesPacket(List<LoreFile> files) {
        this.files = files;
    }

    public SyncLoreFilesPacket(FriendlyByteBuf buf) {
        this.files = buf.readList(LoreFile::read);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeCollection(files, (b,f)->f.write(b));
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientLoreModule.getInstance().syncFromServer(files);
        });
        return true;
    }
}
