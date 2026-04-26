package fr.iglee42.modpackutilities.modules.lore.entity;

import dev.ftb.mods.ftblibrary.util.NBTUtils;
import fr.iglee42.modpackutilities.modules.lore.ClientLoreModule;
import fr.iglee42.modpackutilities.modules.lore.block.entity.LoreEntityBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class LoreEntity extends Entity {

    private static final EntityDataAccessor<String> FILE_ID
            = SynchedEntityData.defineId(LoreEntity.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<String> ENTITY
            = SynchedEntityData.defineId(LoreEntity.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<BlockPos> BE_POS
            = SynchedEntityData.defineId(LoreEntity.class, EntityDataSerializers.BLOCK_POS);

    public LoreEntity(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(FILE_ID,"_empty_");
        builder.define(ENTITY,"minecraft:villager");
        builder.define(BE_POS,BlockPos.ZERO);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        getEntityData().set(FILE_ID,tag.getString("file_id"));
        getEntityData().set(ENTITY,tag.getString("entity"));
        getEntityData().set(BE_POS, NbtUtils.readBlockPos(tag,"be_pos").orElse(BlockPos.ZERO));
        refreshDimensions();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putString("file_id",getEntityData().get(FILE_ID));
        tag.putString("entity",getEntityData().get(ENTITY));
        tag.put("be_pos",NbtUtils.writeBlockPos(getEntityData().get(BE_POS)));
    }

    public void setFileId(ResourceLocation fileId){
        getEntityData().set(FILE_ID,fileId.toString());
    }

    public void setEntity(ResourceLocation entity) {
        String newEntity = entity.toString();
        if (!newEntity.equals(getEntityData().get(ENTITY))) {
            getEntityData().set(ENTITY,newEntity);
            refreshDimensions();
        }
    }

    public ResourceLocation getFileId(){
        return ResourceLocation.parse(getEntityData().get(FILE_ID));
    }

    public EntityType<?> getEntityType(){
        return EntityType.byString(getEntityData().get(ENTITY)).orElse(EntityType.VILLAGER);
    }

    public BlockPos getBEPos() {
        return getEntityData().get(BE_POS);
    }

    public void setBEPos(BlockPos bePos) {
        getEntityData().set(BE_POS, bePos);
    }


    @Override
    public void tick() {
        if (!(level().getBlockEntity(getBEPos()) instanceof LoreEntityBlockEntity be) || !getFileId().equals(be.getFileId())) {
            discard();
        }
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.level().isClientSide && level().getBlockEntity(getBEPos()) instanceof LoreEntityBlockEntity be){
            ClientLoreModule.getInstance().openLoreGui(be.getFileId());
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    @Override
    public EntityDimensions getDimensions(Pose p_19975_) {
        return getEntityType().getDimensions();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (ENTITY.equals(key)) {
            refreshDimensions();
        }
    }
}
