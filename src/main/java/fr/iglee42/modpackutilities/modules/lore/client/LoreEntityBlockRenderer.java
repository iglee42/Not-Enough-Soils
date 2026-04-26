package fr.iglee42.modpackutilities.modules.lore.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import fr.iglee42.modpackutilities.modules.lore.block.entity.LoreEntityBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.Vec3;

public class LoreEntityBlockRenderer implements BlockEntityRenderer<LoreEntityBlockEntity> {

    public LoreEntityBlockRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public  void render(LoreEntityBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        render(blockEntity, poseStack, bufferSource, packedLight, packedOverlay);
    }

    private <R extends Entity> void render(LoreEntityBlockEntity blockEntity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay){
        if (blockEntity.getFileId() == null) {
            return;
        }

        poseStack.pushPose();

        // necessary transforms to make models render in the right place
        poseStack.translate(0.5, 2.02 + blockEntity.getYOffset(), 0.5);
        poseStack.scale(1f, -1f, -1f);

        // face the player
        poseStack.mulPose(Axis.YP.rotationDegrees(180 + Minecraft.getInstance().gameRenderer.getMainCamera().getYRot()));
        EntityType<R> type = (EntityType<R>) EntityType.byString(blockEntity.getModelId().toString()).orElse(EntityType.VILLAGER);
        R entity = type.create(blockEntity.getLevel());
        entity.setPos(Vec3.ZERO);
        entity.setXRot(0);
        entity.setYRot(0);
        if (entity instanceof Mob lv)lv.setNoAi(true);
        if (entity instanceof AgeableMob age){
            age.setBaby(false);
        }
        EntityRenderer<R> renderer = (EntityRenderer<R>) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
        if (!(renderer instanceof LivingEntityRenderer<?,?> lvrenderer)) {
            poseStack.popPose();
            return;
        }

        EntityModel<?> model = lvrenderer.getModel();
        VertexConsumer vertexBuilder = bufferSource.getBuffer(RenderType.entityTranslucent(renderer.getTextureLocation(entity)));
        model.renderToBuffer(poseStack, vertexBuilder, LightTexture.FULL_BRIGHT,packedOverlay, FastColor.ARGB32.colorFromFloat(9/16f,1f,1f,9/16f));

        poseStack.popPose();
    }

}