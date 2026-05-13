package fr.iglee42.modpackutilities.modules.lore.network;

import fr.iglee42.modpackutilities.IgleeModpackUtilities;
import fr.iglee42.modpackutilities.modules.lore.LoreFile;
import fr.iglee42.modpackutilities.modules.lore.LoreModule;
import fr.iglee42.modpackutilities.modules.lore.block.entity.LoreEntityBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ChangeLoreFilePacket(ResourceLocation fileId,BlockPos bePos) implements CustomPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf,ChangeLoreFilePacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,ChangeLoreFilePacket::fileId,
            BlockPos.STREAM_CODEC,ChangeLoreFilePacket::bePos,
            ChangeLoreFilePacket::new
    );

    public static final Type<ChangeLoreFilePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("lore","change_lore_file"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(IPayloadContext context, ChangeLoreFilePacket payload) {
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

            if (player.isCreative() && player.hasPermissions(2) && player.level().getBlockEntity(payload.bePos()) instanceof LoreEntityBlockEntity be){
                be.setFileId(file.id());
                player.level().sendBlockUpdated(payload.bePos(),be.getBlockState(),be.getBlockState(), Block.UPDATE_ALL);
            }
        });
    }

}

