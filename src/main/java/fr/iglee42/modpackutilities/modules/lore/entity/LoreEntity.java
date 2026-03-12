package fr.iglee42.modpackutilities.modules.lore.entity;

import fr.iglee42.modpackutilities.modules.lore.ClientLoreModule;
import fr.iglee42.modpackutilities.modules.lore.block.entity.LoreEntityBlockEntity;
import net.minecraft.nbt.CompoundTag;
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

    public LoreEntity(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(FILE_ID, "_empty_");
        this.entityData.define(ENTITY, "minecraft:villager");
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        getEntityData().set(FILE_ID,tag.getString("file_id"));
        getEntityData().set(ENTITY,tag.getString("entity"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putString("file_id",getEntityData().get(FILE_ID));
        tag.putString("entity",getEntityData().get(ENTITY));
    }

    public void setFileId(ResourceLocation fileId){
        getEntityData().set(FILE_ID,fileId.toString());
    }

    public void setEntity(ResourceLocation entity){
        getEntityData().set(ENTITY,entity.toString());
    }

    public ResourceLocation getFileId(){
        return ResourceLocation.parse(getEntityData().get(FILE_ID));
    }

    public EntityType<?> getEntityType(){
        return EntityType.byString(getEntityData().get(ENTITY)).orElse(EntityType.VILLAGER);
    }

    @Override
    public void tick() {
        if (!(level().getBlockEntity(blockPosition()) instanceof LoreEntityBlockEntity be) || !getFileId().equals(be.getFileId())) {
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
        if (player.level().isClientSide && level().getBlockEntity(blockPosition()) instanceof LoreEntityBlockEntity be){
            ClientLoreModule.getInstance().openLoreGui(be.getFileId());
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    @Override
    public EntityDimensions getDimensions(Pose p_19975_) {
        return getEntityType().getDimensions();
    }
}
