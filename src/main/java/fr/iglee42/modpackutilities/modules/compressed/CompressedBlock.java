package fr.iglee42.modpackutilities.modules.compressed;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompressedBlock {

    private final ResourceLocation block;
    private final Map<Integer, Pair<Block, BlockItem>> tierObjects;
    private ResourceLocation singleTexture = null;
    private ResourceLocation customBaseModel;

    public CompressedBlock(ResourceLocation block) {
        this.block = block;
        this.tierObjects = new HashMap<>();
    }

    public Pair<Block,BlockItem> getPairForTier(int tier){
        return tierObjects.getOrDefault(tier,null);
    }

    public Block getBlockForTier(int tier) {
        if (getPairForTier(tier) == null) return null;
        return getPairForTier(tier).getFirst();
    }
    public BlockItem getItemForTier(int tier) {
        if (getPairForTier(tier) == null) return null;
        return getPairForTier(tier).getSecond();
    }

    public void setSingleTexture(ResourceLocation singleTexture) {
        this.singleTexture = singleTexture;
    }

    public boolean isSingleTexture(){
        return singleTexture != null;
    }

    public void setBlockForTier(int tier,Block block){
        Pair<Block,BlockItem> pair = tierObjects.getOrDefault(tier,new Pair<>(null,null));
        if (pair.getFirst() == null) {
            pair = new Pair<>(block,pair.getSecond());
            tierObjects.put(tier,pair);
        }
    }

    public void setItemForTier(int tier,BlockItem item){
        Pair<Block,BlockItem> pair = tierObjects.getOrDefault(tier,new Pair<>(null,null));
        if (pair.getSecond() == null) {
            pair = new Pair<>(pair.getFirst(),item);
            tierObjects.put(tier,pair);
        }
    }

    public ResourceLocation getBlock() {
        return block;
    }


    @OnlyIn(Dist.CLIENT)
    public Map<String, Either<Material,String>> getTextures() {
        if (isSingleTexture()) return Map.of("all",Either.left(new Material(TextureAtlas.LOCATION_BLOCKS,singleTexture)),"particle",Either.right("#all"));
        UnbakedModel unbakedModel = Minecraft.getInstance().getModelManager().getModelBakery().getModel(customBaseModel);
        if (unbakedModel instanceof BlockModel model) return Map.copyOf(model.textureMap);
        return Map.of();
    }

    public void deserialize(JsonObject json) {
        if (json.has("customModel")) {
            if (json.get("customModel").isJsonPrimitive() && json.getAsJsonPrimitive("customModel").isString()){
                ResourceLocation location = ResourceLocation.tryParse(json.getAsJsonPrimitive("customModel").getAsString());
                if (location == null){
                    LogUtils.getLogger().error("customModel in the compressed object for {} isn't a valid resource location, ignoring it...", getBlock());
                    return;
                }
                customBaseModel = location;
            } else {
                LogUtils.getLogger().error("customModel in the compressed object for {} isn't a string, ignoring it...", getBlock());
            }
        }
    }
}
