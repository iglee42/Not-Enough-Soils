package fr.iglee42.modpackutilities.modules.compressed;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import fr.iglee42.igleelib.api.utils.ModsUtils;
import fr.iglee42.modpackutilities.resourcepack.generation.TextureKey;
import fr.iglee42.modpackutilities.utils.LangFormatter;
import fr.iglee42.modpackutilities.utils.Module;
import fr.iglee42.modpackutilities.utils.ModuleLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Predicate;

public class CompressedModule extends Module {

    public static final String DEFAULT_LAYER = "compressed:block/layer_{{layer}}";
    private static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath("compressed","main"));

    private final List<CompressedBlock> COMPRESSED;
    private int maxCompressedTiers;
    private float progressiveHardness = 0;
    private String langExpression = "Compressed {{type}}{{?tier>1: x{{tier}}}}";
    private Either<String,Map<Integer,String>> layers = Either.left(DEFAULT_LAYER);
    private boolean generateRecipes = true;
    public CompressedModule(ModuleLoader.Modules type, String name) {
        super(type,name, true);
        COMPRESSED = new ArrayList<>();
    }

    @Override
    public void init(IEventBus modEventBus, IEventBus forgeEventBus) throws Exception {
        super.init(modEventBus, forgeEventBus);
        modEventBus.addListener(this::registerEvent);
        modEventBus.addListener(this::addItemsToCreativeTab);
        JsonObject config = getConfig();
        if (!config.has("maxCompressedTiers")){
            fatal("Missing maxCompressedTiers key in the compressed json");
            return;
        }
        maxCompressedTiers = config.get("maxCompressedTiers").getAsInt();
        if (!config.has("blocks") || !config.get("blocks").isJsonObject()){
            fatal("Missing blocks key in the compressed json or it isn't a json object");
            return;
        }
        JsonObject blocks = config.getAsJsonObject("blocks");
        blocks.entrySet().forEach(e->{
            ResourceLocation block = ResourceLocation.tryParse(e.getKey());
            if (block == null){
                warn("Block \"{}\" in the compressed json isn't a valid ResourceLocation",e.getKey());
                return;
            }
            if (COMPRESSED.stream().anyMatch(c->c.getBlock().equals(block))){
                warn("A compressed block already exist for the \"{}\" block",block);
                return;
            }
            String prefix = "";
            if (COMPRESSED.stream().anyMatch(c->c.getBlock().getPath().equals(block.getPath()))){
                prefix = block.getNamespace();
            }
            CompressedBlock cBlock = new CompressedBlock(block,prefix);
            if (e.getValue().isJsonPrimitive() && e.getValue().getAsJsonPrimitive().isString()){
                cBlock.setSingleTexture(ResourceLocation.parse(e.getValue().getAsString()));
            } else if (e.getValue().isJsonObject()){
                cBlock.deserialize(e.getValue().getAsJsonObject());
            } else {
                warn("Block entry \"{}\" in the compressed json isn't a json object or a string",block);
                return;
            }
            COMPRESSED.add(cBlock);
        });
        if (config.has("progressiveHardness")){
            progressiveHardness = config.get("progressiveHardness").getAsFloat();
        } else {
            progressiveHardness = 0;
        }

        if (config.has("langExpression")){
            if (config.get("langExpression").isJsonPrimitive() && config.getAsJsonPrimitive("langExpression").isString())
                langExpression = config.get("langExpression").getAsString();
            else
                error("langExpression in the compressed json must be a string");
        }
        if (config.has("layers")){
            if (config.get("layers").isJsonPrimitive() && config.getAsJsonPrimitive("layers").isString())
                layers = Either.left(config.get("layers").getAsString());
            else if (config.get("layers").isJsonObject())
            {
                Map<Integer, String> layers = new HashMap<>();
                JsonObject obj = config.getAsJsonObject("layers");
                obj.keySet().stream().filter(k->{
                    try {
                        Integer.parseInt(k);
                        return true;
                    } catch (NumberFormatException e){
                       warn("The key {} in the layers obj in {} config isn't a valid number",k,getName());
                        return false;
                    }
                }).forEach(k->{
                    int layer = Integer.parseInt(k);
                    if (layer < 1 || layer > maxCompressedTiers){
                        warn("The layer {} in the layers obj in {} config is out of bounds (1 -> {})",layer,getName(),maxCompressedTiers);
                        return;
                    }
                    if (obj.get(k).isJsonPrimitive() && obj.getAsJsonPrimitive(k).isString()){
                        String texture = obj.getAsJsonPrimitive(k).getAsString();
                        layers.put(layer,texture);
                    } else {
                        warn("The value of layer {} in the layers obj in {} config isn't a string",layer,getName());
                    }
                });
                this.layers = Either.right(layers);
            }
            else
               error("layers in the compressed json must be a string or an object");
        }
        if (config.has("generateRecipes")){
            if (config.get("generateRecipes").isJsonPrimitive() && config.getAsJsonPrimitive("generateRecipes").isBoolean())
                generateRecipes = config.getAsJsonPrimitive("generateRecipes").getAsBoolean();
            else
                error("generateRecipes in the compressed json must be a boolean (true/false)");
        }
        info("Initialized {} module successfully with {} potentials compressed blocks",getName(),COMPRESSED.size());

    }

    private void addItemsToCreativeTab(BuildCreativeModeTabContentsEvent event){
        if (event.getTabKey().equals(TAB_KEY)){
            COMPRESSED.stream().filter(Predicate.not(CompressedBlock::isDisabled)).forEach(c->{
                for (int i = 1; i <= maxCompressedTiers; i++){
                    if (c.getItemForTier(i) != null)
                        event.accept(c.getItemForTier(i));
                }
            });
        }
    }

    private void registerEvent(RegisterEvent event){
        if (event.getRegistryKey().equals(Registries.CREATIVE_MODE_TAB)){
            Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_KEY, CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.imu.compressed"))
                    .withSearchBar()
                    .icon(Items.REINFORCED_DEEPSLATE::getDefaultInstance)
                    .build());
        } else if (event.getRegistryKey().equals(Registries.BLOCK)){
            for (int i = 1; i <= maxCompressedTiers; i++){
                int finalI = i;
                COMPRESSED.stream().filter(c->c.getBlockForTier(finalI) == null).forEach(c->{
                    try {
                        Constructor<? extends Block> constructor = c.getCustomBlockClass() != null ? c.getCustomBlockClass().getConstructor(BlockBehaviour.Properties.class) : Block.class.getConstructor(BlockBehaviour.Properties.class);
                        BlockBehaviour.Properties props = BlockBehaviour.Properties.ofFullCopy(BuiltInRegistries.BLOCK.get(c.getBlock()));
                        if (c.getPushReaction() != null)
                            props = props.pushReaction(c.getPushReaction());
                        if (c.hasNoOcclusion())
                            props = props.noOcclusion();
                        if (c.getLight() > 0)
                            props = props.lightLevel(bs->c.getLight());
                        if (progressiveHardness != 0){
                            float originalHardness = BlockBehaviour.Properties.ofFullCopy(BuiltInRegistries.BLOCK.get(c.getBlock())).destroyTime;
                            props = props.strength(originalHardness + (progressiveHardness * finalI));
                        }
                        if (c.getSoundType() != null){
                            props = props.sound(c.getSoundType());
                        }
                        Block block = Registry.register(BuiltInRegistries.BLOCK,ResourceLocation.fromNamespaceAndPath(getName(),(!c.getPrefix().isBlank() ? c.getPrefix() +"_" : "") +"compressed_" + c.getBlock().getPath() + "_"+finalI),constructor.newInstance(props));
                        c.setBlockForTier(finalI, block);
                    } catch (NoSuchMethodException  e) {
                        warn("The block class for {} doesn't have a valid constructor",c.getBlock(),e);
                    } catch ( InstantiationException | IllegalAccessException |
                            InvocationTargetException e){
                        warn("Failed to invoke the constructor for the block for {}",c.getBlock(),e);
                    }
                });
            }
        } else if (event.getRegistryKey().equals(Registries.ITEM)){
            for (int i = 1; i <= maxCompressedTiers; i++){
                int finalI = i;
                COMPRESSED.stream().filter(c->c.getItemForTier(finalI) == null && c.getBlockForTier(finalI) != null).forEach(c->{
                    BlockItem it = Registry.register(BuiltInRegistries.ITEM,ResourceLocation.fromNamespaceAndPath(getName(),(!c.getPrefix().isBlank() ? c.getPrefix() +"_" : "") +"compressed_" + c.getBlock().getPath() + "_"+finalI),new BlockItem(c.getBlockForTier(finalI), new Item.Properties()));
                    c.setItemForTier(finalI, it);
                });
            }
        }
    }

    @Override
    protected JsonObject getDefaultConfig() {
        JsonObject obj = new JsonObject();
        obj.addProperty("maxCompressedTiers",9);
        obj.add("blocks",new JsonObject());
        return obj;
    }

    public ResourceLocation getTexture(CompressedBlock c, String key){
        if (c.getTextures().containsKey(key)){
            Either<ResourceLocation,String> either = c.getTextures().get(key);
            if (either.left().isPresent())
                return either.left().get();
            else if (either.right().isPresent())
                return getTexture(c,either.right().get().substring(1));
            else {
                warn("The value of " + key + " is neither a ResourceLocation nor a string.");
                return null;
            }
        } else {
            warn("Missing texture {} in the model object for {}", key, c.getBlock());
            return null;
        }
    }

    @Override
    public void generateAssetsForPack() {
        super.generateAssetsForPack();
        for (int i = 1; i <= maxCompressedTiers; i++){
            int finalI = i;

            COMPRESSED.stream().filter(Predicate.not(CompressedBlock::isDisabled)).forEach(c->{
                String prefix = !c.getPrefix().isBlank() ? c.getPrefix() + "_" : "";
                if (FMLEnvironment.dist == Dist.CLIENT) {
                    blockstate(prefix + "compressed_" + c.getBlock().getPath() + "_" + finalI, c.getRotation().getVariants(getName() + ":block/" + prefix + "compressed_" + c.getBlock().getPath() + "_" + finalI));
                    model("block", prefix + "compressed_" + c.getBlock().getPath() + "_" + finalI, c.isSingleTexture() ? "block/cube_all" : c.getCustomParent().toString(), c.getTextures().keySet().stream().map(
                            k -> {
                                ResourceLocation location = getTexture(c, k);
                                if (location != null) {
                                    String out = location.getNamespace() + "/" + location.getPath() + "/" + finalI;
                                    return new TextureKey(k, getName() + ":block/" + out);
                                }
                                return null;
                            }
                    ).filter(Objects::nonNull).toArray(TextureKey[]::new), c.getRenderType().name().toLowerCase());
                    model("item", prefix + "compressed_" + c.getBlock().getPath() + "_" + finalI, getName() + ":block/" + prefix + "compressed_" + c.getBlock().getPath() + "_" + finalI, new TextureKey[]{}, "");
                    if (c.getBlockForTier(finalI) != null) {
                        Map<String, Object> ctx = Map.of(
                                "type", c.hasDisplayName() ? c.getDisplayName() : ModsUtils.getUpperName(c.getBlock().getPath(), "_"),
                                "tier", finalI
                        );

                        lang(c.getBlockForTier(finalI).getDescriptionId(), LangFormatter.format(langExpression, ctx));
                    }
                }


                if (generateRecipes){
                    {
                        JsonObject shaped = new JsonObject();
                        JsonArray fullPattern = new JsonArray();
                        for (int j = 0; j < 3; j++) {
                            fullPattern.add("###");
                        }
                        shaped.add("pattern", fullPattern);
                        JsonObject keys = new JsonObject();
                        JsonObject key = new JsonObject();
                        key.addProperty("item", (finalI > 1 ? getName()+":"+prefix+"compressed_" + c.getBlock().getPath() + "_" + (finalI - 1) : c.getBlock()).toString());
                        keys.add("#", key);
                        shaped.add("key", keys);
                        JsonObject result = new JsonObject();
                        result.addProperty("id",getName() + ":"+prefix+"compressed_" + c.getBlock().getPath() + "_" + finalI);
                        shaped.add("result",result);
                        recipe(prefix + "compressed_" + c.getBlock().getPath() + "_" + finalI, "minecraft:crafting_shaped", shaped);
                    }
                    {
                        JsonObject shapeless = new JsonObject();
                        JsonArray ingredients = new JsonArray();
                        JsonObject key = new JsonObject();
                        key.addProperty("item", (getName()+":"+prefix+"compressed_" + c.getBlock().getPath() + "_" + finalI));
                        ingredients.add(key);
                        shapeless.add("ingredients", ingredients);
                        JsonObject result = new JsonObject();
                        result.addProperty("id", (finalI > 1 ? getName() + ":"+prefix+"compressed_" + c.getBlock().getPath() + "_" + (finalI - 1) : c.getBlock()).toString());
                        result.addProperty("count", 9);
                        shapeless.add("result",result);
                        recipe(prefix + "compressed_" + c.getBlock().getPath() + "_" + finalI+"_decompress", "minecraft:crafting_shapeless", shapeless);
                    }
                }
                tag(BuiltInRegistries.BLOCK,rl("compressed/"+prefix+c.getBlock().getPath()),"#"+getName()+":compressed/" + prefix + c.getBlock().getPath() + "/" + finalI);
                tag(BuiltInRegistries.BLOCK,rl("compressed/"+prefix+c.getBlock().getPath()+"/"+finalI),(getName()+":"+prefix+"compressed_" + c.getBlock().getPath() + "_" + finalI));
                tag(BuiltInRegistries.BLOCK,rl("compressed"),"#"+getName()+":compressed/"+prefix+c.getBlock().getPath());

                tag(BuiltInRegistries.ITEM,rl("compressed/"+prefix+c.getBlock().getPath()),"#"+getName()+":compressed/" +prefix+ c.getBlock().getPath() + "/" + finalI);
                tag(BuiltInRegistries.ITEM,rl("compressed/"+prefix+c.getBlock().getPath()+"/"+finalI),(getName()+":"+prefix+"compressed_" + c.getBlock().getPath() + "_" + finalI));
                tag(BuiltInRegistries.ITEM,rl("compressed"),"#"+getName()+":compressed/"+prefix+c.getBlock().getPath());

                c.getBlockTags().forEach(rs->{
                    tag(BuiltInRegistries.BLOCK,rs,(getName()+":"+prefix+"compressed_" + c.getBlock().getPath() + "_" + finalI));
                });
                c.getItemTags().forEach(rs->{
                    tag(BuiltInRegistries.ITEM,rs,(getName()+":"+prefix+"compressed_" + c.getBlock().getPath() + "_" + finalI));
                });

                LootTable table = LootTable.lootTable().setParamSet(LootContextParamSets.BLOCK)
                        .setRandomSequence(rl(prefix+"compressed_" + c.getBlock().getPath() + "_" + finalI)).withPool(
                                LootPool.lootPool()
                                        .add(LootItem.lootTableItem(c.getItemForTier(finalI)))
                                        .when(ExplosionCondition.survivesExplosion()))
                        .build();

                lootTable("blocks",prefix+"compressed_" + c.getBlock().getPath() + "_" + finalI,table);
            });
        }
    }
    public List<CompressedBlock> getCompressedBlocks(){
        return List.copyOf(COMPRESSED);
    }

    public int getMaxCompressedTiers() {
        return maxCompressedTiers;
    }

    public Either<String, Map<Integer, String>> getLayers() {
        return layers;
    }

    public void validateBlocks() {
        COMPRESSED.forEach(b->{
            Optional<Holder.Reference<Block>> optionalValue = BuiltInRegistries.BLOCK.getHolder(b.getBlock());
            if (optionalValue.isEmpty()) {
                warn("Block entry \"{}\" isn't a valid block", b.getBlock());
                b.setDisabled(true);
            }
        });

        info("Found {} valid compressed blocks for {} module. {} invalid blocks found !", COMPRESSED.stream().filter(Predicate.not(CompressedBlock::isDisabled)).count(), getName(), COMPRESSED.stream().filter(CompressedBlock::isDisabled).count());
    }
}
