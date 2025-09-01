package fr.iglee42.modpackutilities.modules.compressed;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import fr.iglee42.modpackutilities.resourcepack.generation.TextureKey;
import fr.iglee42.modpackutilities.utils.Module;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CompressedModule extends Module {

    private final List<CompressedBlock> COMPRESSED;
    private int maxCompressedTiers;
    public CompressedModule() {
        super("compressed", true);
        COMPRESSED = new ArrayList<>();
    }

    @Override
    public void init(IEventBus modEventBus, IEventBus forgeEventBus) throws Exception {
        super.init(modEventBus, forgeEventBus);
        modEventBus.addListener(this::registerEvent);
        JsonObject config = getConfig();
        if (!config.has("maxCompressedTiers")){
            throw new JsonParseException("Missing maxCompressedTiers key in the compressed json");
        }
        maxCompressedTiers = config.get("maxCompressedTiers").getAsInt();
        if (!config.has("blocks") || !config.get("blocks").isJsonObject()){
            throw new JsonParseException("Missing blocks key in the compressed json or it isn't a json object");
        }
        JsonObject blocks = config.getAsJsonObject("blocks");
        blocks.entrySet().forEach(e->{
            ResourceLocation block = ResourceLocation.tryParse(e.getKey());
            if (block == null){
                throw new JsonParseException(String.format("Block \"%s\" in the compressed json isn't a valid resourcelocation",e.getKey()));
            }
            Optional<Holder.Reference<Block>> optionalValue = BuiltInRegistries.BLOCK.getHolder(block);
            if (optionalValue.isEmpty())
                throw new JsonParseException(String.format("Block entry \"%s\" isn't a valid block",block));
            CompressedBlock cBlock = new CompressedBlock(block);
            if (e.getValue().isJsonPrimitive() && e.getValue().getAsJsonPrimitive().isString()){
                cBlock.setSingleTexture(ResourceLocation.parse(e.getValue().getAsString()));
            } else if (e.getValue().isJsonObject()){
                cBlock.deserialize(e.getValue().getAsJsonObject());
            } else {
                throw new JsonParseException(String.format("Block entry \"%s\" in the compressed json isn't a json object or a string",block));
            }
            COMPRESSED.add(cBlock);
        });
    }

    private void registerEvent(RegisterEvent event){
        if (event.getRegistryKey().equals(Registries.BLOCK)){
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
                        Block block = Registry.register(BuiltInRegistries.BLOCK,ResourceLocation.fromNamespaceAndPath(getName(),"compressed_" + c.getBlock().getPath() + "_"+finalI),constructor.newInstance(props));
                        c.setBlockForTier(finalI, block);
                    } catch (NoSuchMethodException  e) {
                        LogUtils.getLogger().error("The block class for {} doesn't have a valid constructor, skipping it...",c.getBlock(),e);
                    } catch ( InstantiationException | IllegalAccessException |
                            InvocationTargetException e){
                        LogUtils.getLogger().error("Failed to invoke the constructor for the block for {}, skipping it...",c.getBlock(),e);
                    }
                });
            }
        } else if (event.getRegistryKey().equals(Registries.ITEM)){
            for (int i = 1; i <= maxCompressedTiers; i++){
                int finalI = i;
                COMPRESSED.stream().filter(c->c.getItemForTier(finalI) == null && c.getBlockForTier(finalI) != null).forEach(c->{
                    BlockItem it = Registry.register(BuiltInRegistries.ITEM,ResourceLocation.fromNamespaceAndPath(getName(),"compressed_" + c.getBlock().getPath() + "_"+finalI),new BlockItem(c.getBlockForTier(finalI), new Item.Properties()));
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

    @Override
    public void generateAssetsForPack() {
        super.generateAssetsForPack();
        for (int i = 1; i <= maxCompressedTiers; i++){
            int finalI = i;
            COMPRESSED.forEach(c->{
                blockstate("compressed_" + c.getBlock().getPath() + "_"+finalI,getName() + ":block/"+"compressed_" + c.getBlock().getPath() + "_"+finalI);
                model("block","compressed_" + c.getBlock().getPath() + "_"+finalI,c.isSingleTexture() ? "block/cube_all": c.getCustomParent().toString(),c.getTextures().entrySet().stream().map(
                        e-> new TextureKey(e.getKey(), e.getValue().map(ResourceLocation::toString, r->r))
                ).toArray(TextureKey[]::new),c.getRenderType().name().toLowerCase());
                model("item","compressed_" + c.getBlock().getPath() + "_"+finalI,getName() + ":block/"+"compressed_" + c.getBlock().getPath() + "_"+finalI,new TextureKey[]{},"");
            });
        }
    }
}
