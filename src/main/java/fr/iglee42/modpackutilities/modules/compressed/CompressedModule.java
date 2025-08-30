package fr.iglee42.modpackutilities.modules.compressed;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
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
                cBlock.setSingleTexture(e.getValue().getAsString());
            } else if (e.getValue().isJsonObject()){

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
                    Block block = Registry.register(BuiltInRegistries.BLOCK,ResourceLocation.fromNamespaceAndPath(getName(),"compressed_" + c.getBlock().getPath() + "_"+finalI),new Block(BlockBehaviour.Properties.ofFullCopy(BuiltInRegistries.BLOCK.get(c.getBlock()))));
                    c.setBlockForTier(finalI, block);
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
}
