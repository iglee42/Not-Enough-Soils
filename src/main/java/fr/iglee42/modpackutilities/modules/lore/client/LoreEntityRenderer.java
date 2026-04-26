package fr.iglee42.modpackutilities.modules.lore.client;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.iglee42.modpackutilities.modules.lore.entity.LoreEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

public class LoreEntityRenderer extends EntityRenderer<LoreEntity> {
    public LoreEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public ResourceLocation getTextureLocation(LoreEntity p_114482_) {
        return MissingTextureAtlasSprite.getLocation();
    }

    @Override
    public void render(LoreEntity p_114485_, float p_114486_, float p_114487_, PoseStack p_114488_, MultiBufferSource p_114489_, int p_114490_) {}
}
