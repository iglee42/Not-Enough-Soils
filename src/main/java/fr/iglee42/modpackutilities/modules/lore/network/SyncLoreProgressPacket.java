package fr.iglee42.modpackutilities.modules.lore.network;

import fr.iglee42.modpackutilities.modules.lore.ClientLoreModule;
import fr.iglee42.modpackutilities.modules.lore.LoreFile;
import fr.iglee42.modpackutilities.modules.lore.progress.LoreProgress;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class SyncLoreProgressPacket {
    private final LoreProgress progress;

    public SyncLoreProgressPacket(LoreProgress progress) {
        this.progress = progress;
    }

    public SyncLoreProgressPacket(FriendlyByteBuf buf) {
        this.progress = LoreProgress.read(buf);
    }

    public void toBytes(FriendlyByteBuf buf) {
        progress.write(buf);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientLoreModule.getInstance().syncProgressFromServer(progress);
        });
        return true;
    }
}
