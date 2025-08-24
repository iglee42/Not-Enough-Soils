package fr.iglee42.notenoughsoils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Mod(NotEnoughSoils.MODID)
public class NotEnoughSoils {

    public static final String MODID = "snes";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static HashMap<Block,List<Block>> SOILS;
    private static File configFile;

    public NotEnoughSoils(IEventBus modEventBus) {
        SOILS = new HashMap<>();

        NeoForge.EVENT_BUS.addListener(this::cropGrow);
        NeoForge.EVENT_BUS.addListener(this::addReloadListener);

    }


    protected static void reloadConfig() throws IOException, JsonParseException {
        SOILS.clear();
        configFile = new File(FMLPaths.CONFIGDIR.get().toFile(),"snes-soils.json");
        if (configFile.exists()){
            JsonObject config = new Gson().fromJson(new FileReader(configFile),JsonObject.class);
            config.keySet().forEach(k->{
                ResourceLocation key = ResourceLocation.parse(k);
                Optional<Holder.Reference<Block>> optional = BuiltInRegistries.BLOCK.getHolder(key);
                if (optional.isEmpty()) throw new JsonParseException("Invalid block key in SNES config file : " + key);

                if (config.get(k).isJsonArray()){

                    ArrayList<Block> blocks = new ArrayList<>();
                    for (JsonElement j : config.getAsJsonArray(k)) {
                        if (j.isJsonPrimitive() && j.getAsJsonPrimitive().isString()){
                            ResourceLocation value = ResourceLocation.parse( j.getAsJsonPrimitive().getAsString());
                            Optional<Holder.Reference<Block>> optionalValue = BuiltInRegistries.BLOCK.getHolder(value);
                            if (optionalValue.isEmpty())
                                LOGGER.error("Invalid block value for {} in SNES config file : {}", key, value);
                            else blocks.add(optionalValue.get().value());
                        } else {
                            LOGGER.error("A value in the array for {} isn't a string, ignoring it...", key);
                        }
                    }
                    SOILS.put(optional.get().value(), blocks);
                } else if (config.get(k).isJsonPrimitive() && config.get(k).getAsJsonPrimitive().isString()){
                    ResourceLocation value = ResourceLocation.parse( config.get(k).getAsJsonPrimitive().getAsString());
                    Optional<Holder.Reference<Block>> optionalValue = BuiltInRegistries.BLOCK.getHolder(value);
                    if (optionalValue.isEmpty()) throw new JsonParseException("Invalid block value for "+key+" in SNES config file : " + value);
                    SOILS.put(optional.get().value(), List.of(optionalValue.get().value()));
                } else {
                    throw new JsonParseException("Value for " + key + " in SNES config file isn't an array or a string");
                }
            });
        } else {
            FileWriter writer = new FileWriter(configFile);
            writer.write(new Gson().toJson(new JsonObject()));
            writer.close();
        }
    }

    private void addReloadListener(AddReloadListenerEvent event){
        event.addListener(new SNESReloadListener());
    }

    private void cropGrow(final CropGrowEvent.Pre event) {
        if (!SOILS.containsKey(event.getState().getBlock())) return;

        List<Block> requiredSoil = SOILS.get(event.getState().getBlock());
        if (!requiredSoil.contains(event.getLevel().getBlockState(event.getPos().below()).getBlock())) {
            event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);
        }
    }


}
