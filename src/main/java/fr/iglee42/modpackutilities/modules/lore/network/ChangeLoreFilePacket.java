package fr.iglee42.modpackutilities.modules.lore.network;

import fr.iglee42.modpackutilities.IgleeModpackUtilities;
import fr.iglee42.modpackutilities.modules.lore.LoreFile;
import fr.iglee42.modpackutilities.modules.lore.LoreModule;
import fr.iglee42.modpackutilities.modules.lore.block.entity.LoreEntityBlockEntity;
import fr.iglee42.modpackutilities.modules.lore.progress.LoreProgressManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ChangeLoreFilePacket {

    private final ResourceLocation fileId;
    private final BlockPos bePos;

    public ChangeLoreFilePacket(ResourceLocation fileId, BlockPos bePos) {
        this.fileId = fileId;
        this.bePos = bePos;
    }

    public ChangeLoreFilePacket(FriendlyByteBuf buf) {
        this.fileId = buf.readResourceLocation();
        this.bePos = buf.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeResourceLocation(fileId);
        buf.writeBlockPos(bePos);
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

            if (player.isCreative() && player.hasPermissions(2) && player.level().getBlockEntity(bePos) instanceof LoreEntityBlockEntity be){
                be.setFileId(file.id());
                player.level().sendBlockUpdated(bePos,be.getBlockState(),be.getBlockState(), Block.UPDATE_ALL);
            }
        });
        return true;
    }

}

