package fr.iglee42.modpackutilities.modules.lore.block.entity;

import fr.iglee42.modpackutilities.modules.lore.LoreRegistries;
import fr.iglee42.modpackutilities.modules.lore.entity.LoreEntity;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class LoreEntityBlockEntity extends BlockEntity {

    private UUID entityId = Util.NIL_UUID;
    private ResourceLocation modelId = ResourceLocation.withDefaultNamespace("villager");
    @Nullable
    private ResourceLocation fileId;
    private double yOffset = 0;


    public LoreEntityBlockEntity( BlockPos pos, BlockState state) {
        super(LoreRegistries.BE_TYPE.get(), pos, state);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        if (fileId != null)
            tag.putString("file_id", fileId.toString());
        if (modelId != null)
            tag.putString("model_id", modelId.toString());
        tag.putDouble("y_offset", yOffset);
        return tag;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void loadAdditional(CompoundTag tag,HolderLookup.Provider registries) {
        super.loadAdditional(tag,registries);

        fileId = tag.contains("file_id", CompoundTag.TAG_STRING) ?
                ResourceLocation.parse(tag.getString("file_id")) :
                null;

        modelId = tag.contains("model_id", CompoundTag.TAG_STRING) ?
                ResourceLocation.parse(tag.getString("model_id")) :
                ResourceLocation.withDefaultNamespace("villager");

        entityId = tag.contains("entity_id") ? tag.getUUID("entity_id") : Util.NIL_UUID;
        yOffset = tag.contains("y_offset") ? tag.getDouble("y_offset") : 0;
    }

    @Override
    public void saveAdditional(CompoundTag tag,HolderLookup.Provider registries) {
        super.saveAdditional(tag,registries);

        if (fileId != null) {
            tag.putString("file_id", fileId.toString());
        }
        if (modelId != null) {
            tag.putString("model_id", modelId.toString());
        }
        if (entityId != Util.NIL_UUID) {
            tag.putUUID("entity_id", entityId);
        }

        tag.putDouble("y_offset", yOffset);
    }

    @Override
    public void setRemoved() {
        if (!entityId.equals(Util.NIL_UUID) && level instanceof ServerLevel slevel){
            Entity entity = slevel.getEntity(entityId);
            if (entity != null) entity.discard();
        }
    }


    public @Nullable ResourceLocation getFileId() {
        return fileId;
    }

    public ResourceLocation getModelId() {
        return modelId;
    }

    public double getYOffset() {
        return yOffset;
    }

    public void tickServer(ServerLevel level){
        Entity entity = level.getEntity(entityId);

        if (entity instanceof LoreEntity e){
            e.setEntity(getModelId());
            e.setBEPos(getBlockPos());
            e.setPos(Vec3.atCenterOf(getBlockPos()).add(0,getYOffset(),0));
        }
        if (entity == null && getFileId() != null){
            LoreEntity newEntity = new LoreEntity(LoreRegistries.ENTITY.get(), level);
            newEntity.setPos(Vec3.atCenterOf(getBlockPos()).add(0,getYOffset(),0));
            newEntity.setFileId(getFileId());
            newEntity.setEntity(getModelId());
            newEntity.setBEPos(getBlockPos());
            level.addFreshEntity(newEntity);
            entityId = newEntity.getUUID();
            setChanged();
        }
    }

}
