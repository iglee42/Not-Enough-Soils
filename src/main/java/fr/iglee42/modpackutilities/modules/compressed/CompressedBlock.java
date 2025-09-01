package fr.iglee42.modpackutilities.modules.compressed;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;

public class CompressedBlock {

    private final ResourceLocation block;
    private final Map<Integer, Pair<Block, BlockItem>> tierObjects;
    private ResourceLocation customParent;
    private Map<String,Either<ResourceLocation,String>> textures;
    private Class<? extends Block> customBlockClass;
    private PushReaction pushReaction = null;
    private BlockRenderType renderType = BlockRenderType.SOLID;
    private boolean noOcclusion = false;

    public CompressedBlock(ResourceLocation block) {
        this.block = block;
        this.tierObjects = new HashMap<>();
        this.textures = new HashMap<>();
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
        textures.put("all", Either.left(singleTexture));
        textures.put("particle", Either.right("#all"));
    }

    public boolean isSingleTexture(){
        return textures.containsKey("all") && textures.get("all").map(loc->true,s->false);
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
    public Map<String, Either<ResourceLocation,String>> getTextures() {
        return textures;
    }

    public ResourceLocation getCustomParent() {
        return customParent;
    }

    public void deserialize(JsonObject json) {
        if (json.has("model")) {
            if (json.get("model").isJsonObject()){
                JsonObject model = json.getAsJsonObject("model");
                if (model.has("parent") && model.get("parent").isJsonPrimitive() && model.getAsJsonPrimitive("parent").isString()) {
                    ResourceLocation parentLocation = ResourceLocation.tryParse(model.getAsJsonPrimitive("parent").getAsString());
                    if (parentLocation == null) {
                        LogUtils.getLogger().error("parent in the model object for {} isn't a valid resource location, ignoring it...", getBlock());
                        return;
                    }
                    this.customParent = parentLocation;
                }
                if (model.has("textures") && model.get("textures").isJsonObject()) {
                    JsonObject textures = model.getAsJsonObject("textures");
                    for (String key : textures.keySet()) {
                        if (textures.get(key).isJsonPrimitive() && textures.getAsJsonPrimitive(key).isString()) {
                            String strValue = textures.getAsJsonPrimitive(key).getAsString();
                            if (strValue.startsWith("#")) {
                                this.textures.put(key, Either.right(strValue));
                            } else {
                                ResourceLocation value = ResourceLocation.tryParse(strValue);
                                if (value == null) {
                                    LogUtils.getLogger().error("texture {} in the model object for {} isn't a valid resource location, ignoring it...", key, getBlock());
                                } else {
                                    this.textures.put(key, Either.left(value));
                                }
                            }
                        } else {
                            LogUtils.getLogger().error("texture {} in the model object for {} isn't a string, ignoring it...", key, getBlock());
                        }
                    }
                }
            } else {
                LogUtils.getLogger().error("model in the compressed object for {} isn't a json object, ignoring it...", getBlock());
            }
        }
        if (json.has("texture")) {
            if (customParent == null) {
                if (json.get("texture").isJsonPrimitive() && json.getAsJsonPrimitive("texture").isString()) {
                    ResourceLocation location = ResourceLocation.tryParse(json.getAsJsonPrimitive("texture").getAsString());
                    if (location == null) {
                        LogUtils.getLogger().error("texture in the compressed object for {} isn't a valid resource location, ignoring it...", getBlock());
                        return;
                    }
                    setSingleTexture(location);
                } else {
                    LogUtils.getLogger().error("texture in the compressed object for {} isn't a string, ignoring it...", getBlock());
                }
            } else {
                LogUtils.getLogger().error("You can't have a model key and a texture key in a compressed object");
            }
        }
        if (json.has("renderType")) {
            if (json.get("renderType").isJsonPrimitive() && json.getAsJsonPrimitive("renderType").isString()) {
                try  {
                    BlockRenderType type = BlockRenderType.valueOf(json.getAsJsonPrimitive("renderType").getAsString().toUpperCase());
                    if (type == null) {
                        return;
                    }
                    this.renderType = type;
                } catch (IllegalArgumentException e){
                    LogUtils.getLogger().error("renderType in the compressed object for {} isn't a valid BlockRenderType, ignoring it...", getBlock());
                }

            } else {
                LogUtils.getLogger().error("renderType in the compressed object for {} isn't a string, ignoring it...", getBlock());
            }
        }
        if (json.has("class")) {
            if (json.get("class").isJsonPrimitive() && json.getAsJsonPrimitive("class").isString()) {
                try {
                    Class<?> clazz = Class.forName(json.getAsJsonPrimitive("class").getAsString());
                    if (!Block.class.isAssignableFrom(clazz)) {
                        LogUtils.getLogger().error("class in the compressed object for {} isn't a Block class, ignoring it...", getBlock());
                        return;
                    }
                    try {
                        Class<? extends Block> blockClass = (Class<? extends Block>) clazz;
                        if (blockClass.getConstructor(BlockBehaviour.Properties.class) == null){
                            return;
                        }
                        this.customBlockClass = blockClass;
                    } catch (Exception e){
                        LogUtils.getLogger().error("class in the compressed object for {} doesn't have a valid constructor, ignoring it...", getBlock(),e);
                    }
                } catch (ClassNotFoundException e) {
                    LogUtils.getLogger().error("class in the compressed object for {} doesn't exist, ignoring it...", getBlock(),e);
                }
            } else {
                LogUtils.getLogger().error("class in the compressed object for {} isn't a string, ignoring it...", getBlock());
            }
        }
        if (json.has("pushReaction")) {
            if (json.get("pushReaction").isJsonPrimitive() && json.getAsJsonPrimitive("pushReaction").isString()) {
                try {
                    PushReaction reaction = PushReaction.valueOf(json.getAsJsonPrimitive("pushReaction").getAsString().toUpperCase());
                    if (reaction == null) {
                        return;
                    }
                    this.pushReaction = reaction;
                }  catch (IllegalArgumentException e){
                    LogUtils.getLogger().error("pushReaction in the compressed object for {} isn't a valid PushReaction, ignoring it...", getBlock());
                }

            } else {
                LogUtils.getLogger().error("pushReaction in the compressed object for {} isn't a string, ignoring it...", getBlock());
            }
        }

        noOcclusion = json.has("noOcclusion") && json.get("noOcclusion").isJsonPrimitive() && json.getAsJsonPrimitive("noOcclusion").isBoolean() && json.getAsJsonPrimitive("noOcclusion").getAsBoolean();
    }

    public Class<? extends Block> getCustomBlockClass() {
        return customBlockClass;
    }

    public PushReaction getPushReaction() {
        return pushReaction;
    }

    public BlockRenderType getRenderType() {
        return renderType;
    }

    public boolean hasNoOcclusion() {
        return noOcclusion;
    }

    public enum BlockRenderType {
        SOLID,
        CUTOUT,
        CUTOUT_MIPPED,
        TRANSLUCENT
    }
}
