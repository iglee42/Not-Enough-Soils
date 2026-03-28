package fr.iglee42.modpackutilities.modules.compressed;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import fr.iglee42.modpackutilities.IgleeModpackUtilities;
import fr.iglee42.modpackutilities.utils.SoundTypeHelper;
import fr.iglee42.modpackutilities.modules.compressed.blocks.FacingBlock;
import fr.iglee42.modpackutilities.modules.compressed.blocks.HorizontalFacingBlock;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.data.SoundDefinition;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class CompressedBlock {

    private final ResourceLocation block;
    private final Map<Integer, Pair<Block, BlockItem>> tierObjects;
    private ResourceLocation customParent;
    private Map<String,Either<ResourceLocation,String>> textures;
    private Class<? extends Block> customBlockClass;
    private PushReaction pushReaction = null;
    private BlockRenderType renderType = BlockRenderType.SOLID;
    private boolean noOcclusion = false;
    private int light = 0;
    private SoundType soundType;
    private List<ResourceLocation> itemTags;
    private List<ResourceLocation> blockTags;
    private boolean disabled;
    private final String prefix;
    private String displayName = "";
    private Rotation rotation = Rotation.NONE;

    public CompressedBlock(ResourceLocation block, String prefix) {
        this.block = block;
        this.prefix = prefix;
        this.tierObjects = new HashMap<>();
        this.textures = new HashMap<>();
        this.itemTags = new ArrayList<>();
        this.blockTags = new ArrayList<>();
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
        CompressedModule module = IgleeModpackUtilities.getModule(CompressedModule.class);
        if (module != null) {
            if (json.has("model")) {
                if (json.get("model").isJsonObject()) {
                    JsonObject model = json.getAsJsonObject("model");
                    if (model.has("parent") && model.get("parent").isJsonPrimitive() && model.getAsJsonPrimitive("parent").isString()) {
                        ResourceLocation parentLocation = ResourceLocation.tryParse(model.getAsJsonPrimitive("parent").getAsString());
                        if (parentLocation == null) {
                            module.warn("parent in the model object for {} isn't a valid resource location", getBlock());
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
                                        module.warn("texture {} in the model object for {} isn't a valid resource location", key, getBlock());
                                    } else {
                                        this.textures.put(key, Either.left(value));
                                    }
                                }
                            } else {
                                module.warn("texture {} in the model object for {} isn't a string", key, getBlock());
                            }
                        }
                    }
                } else {
                    module.error("model in the compressed object for {} isn't a json object", getBlock());
                }
            }
            if (json.has("texture")) {
                if (customParent == null) {
                    if (json.get("texture").isJsonPrimitive() && json.getAsJsonPrimitive("texture").isString()) {
                        ResourceLocation location = ResourceLocation.tryParse(json.getAsJsonPrimitive("texture").getAsString());
                        if (location == null) {
                            module.warn("texture in the compressed object for {} isn't a valid resource location", getBlock());
                            return;
                        }
                        setSingleTexture(location);
                    } else {
                        module.error("texture in the compressed object for {} isn't a string", getBlock());
                    }
                } else {
                    module.error("You can't have a model key and a texture key in a compressed object");
                }
            }
            if (json.has("renderType")) {
                if (json.get("renderType").isJsonPrimitive() && json.getAsJsonPrimitive("renderType").isString()) {
                    try {
                        BlockRenderType type = BlockRenderType.valueOf(json.getAsJsonPrimitive("renderType").getAsString().toUpperCase());
                        if (type == null) {
                            return;
                        }
                        this.renderType = type;
                    } catch (IllegalArgumentException e) {
                        module.warn("renderType in the compressed object for {} isn't a valid BlockRenderType", getBlock());
                    }

                } else {
                    module.error("renderType in the compressed object for {} isn't a string", getBlock());
                }
            }
            if (json.has("class")) {
                if (json.get("class").isJsonPrimitive() && json.getAsJsonPrimitive("class").isString()) {
                    try {
                        Class<?> clazz = Class.forName(json.getAsJsonPrimitive("class").getAsString());
                        if (!Block.class.isAssignableFrom(clazz)) {
                            module.warn("class in the compressed object for {} isn't a Block class", getBlock());
                            return;
                        }
                        try {
                            Class<? extends Block> blockClass = (Class<? extends Block>) clazz;
                            if (blockClass.getConstructor(BlockBehaviour.Properties.class) == null) {
                                return;
                            }
                            this.customBlockClass = blockClass;
                        } catch (Exception e) {
                            module.warn("class in the compressed object for {} doesn't have a valid constructor", getBlock(), e);
                        }
                    } catch (ClassNotFoundException e) {
                        module.warn("class in the compressed object for {} doesn't exist", getBlock(), e);
                    }
                } else {
                    module.error("class in the compressed object for {} isn't a string", getBlock());
                }
            }
            if (json.has("falling")){
                if (json.get("falling").isJsonPrimitive() && json.getAsJsonPrimitive("falling").isBoolean()) {
                    boolean falling = json.getAsJsonPrimitive("falling").getAsBoolean();
                    if (falling) {
                        this.customBlockClass = SimpleCompressedFallingBlock.class;
                    }
                } else {
                    module.error("falling in the compressed object for {} isn't a boolean", getBlock());
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
                    } catch (IllegalArgumentException e) {
                        module.warn("pushReaction in the compressed object for {} isn't a valid PushReaction", getBlock());
                    }

                } else {
                    module.error("pushReaction in the compressed object for {} isn't a string", getBlock());
                }
            }

            if (json.has("noOcclusion")) {
                if (json.get("noOcclusion").isJsonPrimitive() && json.getAsJsonPrimitive("noOcclusion").isBoolean()) {
                    noOcclusion = json.getAsJsonPrimitive("noOcclusion").getAsBoolean();
                } else {
                    module.error("noOcclusion in the compressed object for {} isn't a boolean", getBlock());
                }
            }

            if (json.has("light")) {
                if (json.get("light").isJsonPrimitive() && json.getAsJsonPrimitive("light").isNumber()) {
                    int l = json.getAsJsonPrimitive("light").getAsInt();
                    if (l < 0 || l > 15) {
                        module.warn("light in the compressed object for {} isn't between 0 and 15", getBlock());
                        return;
                    }
                    this.light = l;
                } else {
                    module.error("light in the compressed object for {} isn't a int", getBlock());
                }
            }
            if (json.has("soundType")) {
                if (json.get("soundType").isJsonPrimitive() && json.getAsJsonPrimitive("soundType").isString()) {
                    String type = json.getAsJsonPrimitive("soundType").getAsString().toLowerCase(Locale.ROOT);
                    SoundType soundType = SoundTypeHelper.INSTANCE.getTypes().getOrDefault(type,null);
                    if (soundType == null) {
                        module.warn("soundType in the compressed object for {} isn't a valid SoundType", getBlock());
                        return;
                    }
                    this.soundType = soundType;
                } else {
                    module.error("soundType in the compressed object for {} isn't a string", getBlock());
                }
            }
            if (json.has("tags")) {
                if (json.get("tags").isJsonObject()) {
                    JsonObject tags = json.getAsJsonObject("tags");
                    if (tags.has("block")) {
                        if (tags.get("block").isJsonArray()) {
                            for (int i = 0; i < tags.getAsJsonArray("block").size(); i++) {
                                if (tags.getAsJsonArray("block").get(i).isJsonPrimitive() && tags.getAsJsonArray("block").get(i).getAsJsonPrimitive().isString()) {
                                    ResourceLocation location = ResourceLocation.tryParse(tags.getAsJsonArray("block").get(i).getAsJsonPrimitive().getAsString());
                                    if (location == null) {
                                        module.warn("tag {} in the tags/block array for {} isn't a valid resource location", tags.getAsJsonArray("block").get(i).getAsJsonPrimitive().getAsString(), getBlock());
                                    } else {
                                        blockTags.add(location);
                                    }
                                } else {
                                    module.error("tag {} in the tags/block array for {} isn't a string", i, getBlock());
                                }
                            }
                        } else {
                            module.error("tags/block in the compressed object for {} isn't a json array", getBlock());
                        }
                    }
                    if (tags.has("item")) {
                        if (tags.get("item").isJsonArray()) {
                            for (int i = 0; i < tags.getAsJsonArray("item").size(); i++) {
                                if (tags.getAsJsonArray("item").get(i).isJsonPrimitive() && tags.getAsJsonArray("item").get(i).getAsJsonPrimitive().isString()) {
                                    ResourceLocation location = ResourceLocation.tryParse(tags.getAsJsonArray("item").get(i).getAsJsonPrimitive().getAsString());
                                    if (location == null) {
                                        module.warn("tag {} in the tags/item array for {} isn't a valid resource location", tags.getAsJsonArray("item").get(i).getAsJsonPrimitive().getAsString(), getBlock());
                                    } else {
                                        itemTags.add(location);
                                    }
                                } else {
                                    module.error("tag {} in the tags/item array for {} isn't a string", i, getBlock());
                                }
                            }
                        } else {
                            module.error("tags/item in the compressed object for {} isn't a json array", getBlock());
                        }
                    }
                }  else {
                    module.error("tags in the compressed object for {} isn't a json object", getBlock());
                }
            }
            if (json.has("displayName")) {
                if (json.get("displayName").isJsonPrimitive() && json.getAsJsonPrimitive("displayName").isString()) {
                    this.displayName = json.getAsJsonPrimitive("displayName").getAsString();
                } else {
                    module.error("displayName in the compressed object for {} isn't a string", getBlock());
                }
            }
            if (json.has("rotation")) {
                if (json.get("rotation").isJsonPrimitive() && json.getAsJsonPrimitive("rotation").isString()) {
                    try {
                        Rotation type = Rotation.valueOf(json.getAsJsonPrimitive("rotation").getAsString().toUpperCase());
                        if (type == null) {
                            return;
                        }
                        this.rotation = type;
                        if (type.getClazz() != null)
                            this.customBlockClass = type.getClazz();
                    } catch (IllegalArgumentException e) {
                        module.warn("rotation in the compressed object for {} isn't a valid Rotation", getBlock());
                    }
                } else {
                    module.error("rotation in the compressed object for {} isn't a string", getBlock());
                }
            }
            if (textures.isEmpty()) {
                module.warn("Block \"{}\" doesn't have any texture." ,getBlock());
            }
        }

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

    public int getLight() {
        return light;
    }

    public SoundType getSoundType() {
        return soundType;
    }

    public List<ResourceLocation> getItemTags() {
        return itemTags;
    }

    public List<ResourceLocation> getBlockTags() {
        return blockTags;
    }

    public boolean isDisabled() {
        return disabled;
    }

    protected void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean hasDisplayName(){
        return !displayName.isEmpty();
    }

    public Rotation getRotation() {
        return rotation;
    }

    public enum BlockRenderType {
        SOLID,
        CUTOUT,
        CUTOUT_MIPPED,
        TRANSLUCENT
    }

    public enum Rotation {
        NONE(null, model->{
            JsonObject variants = new JsonObject();
            JsonObject modelJson = new JsonObject();
            modelJson.addProperty("model",model);
            variants.add("empty",modelJson);
            return variants;
        }),
        ALL(FacingBlock.class,model->{
            JsonObject variants = new JsonObject();
            for (Direction direction : Direction.values()) {
                JsonObject modelJson = new JsonObject();
                modelJson.addProperty("model",model);
                modelJson.addProperty("x",direction == Direction.UP?0 : (direction == Direction.DOWN ? 180 : 90));
                modelJson.addProperty("y",180 - direction.toYRot());
                variants.add("facing="+direction.getSerializedName(),modelJson);
            }
            return variants;
        }),
        HORIZONTAL(HorizontalFacingBlock.class,model->{
            JsonObject variants = new JsonObject();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                JsonObject modelJson = new JsonObject();
                modelJson.addProperty("model",model);
                modelJson.addProperty("y",180 - direction.toYRot());
                variants.add("facing="+direction.getSerializedName(),modelJson);
            }
            return variants;
        }),
        AXIS(RotatedPillarBlock.class,model->{
            JsonObject variants = new JsonObject();
            for (Direction.Axis axis : Direction.Axis.values()) {
                JsonObject modelJson = new JsonObject();
                modelJson.addProperty("model",model);
                modelJson.addProperty("x",axis.isHorizontal()?90 :0);
                modelJson.addProperty("y",axis.equals(Direction.Axis.X) ? 90 : 0);
                variants.add("axis="+axis.getSerializedName(),modelJson);
            }
            return variants;
        });

        private final @Nullable Class<? extends Block> clazz;
        private final Function<String, JsonObject> variants;

        Rotation(@Nullable Class<? extends Block> clazz, Function<String, JsonObject> variants) {
            this.clazz = clazz;
            this.variants = variants;
        }

        public Class<? extends Block> getClazz() {
            return clazz;
        }

        public JsonObject getVariants(String model) {
            return variants.apply(model);
        }
    }
}
