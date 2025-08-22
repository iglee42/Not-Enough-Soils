package fr.iglee42.notenoughsoils;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@Mod(NotEnoughSoils.MODID)
public class NotEnoughSoils {

    public static final String MODID = "snes";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static HashMap<Block,List<Block>> SOILS;
    private static File configFile;

    public NotEnoughSoils() {
        SOILS = new HashMap<>();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        MinecraftForge.EVENT_BUS.addListener(this::cropGrow);
        MinecraftForge.EVENT_BUS.addListener(this::addReloadListener);

    }


    protected static void reloadConfig() throws IOException, JsonParseException {
        SOILS.clear();
        configFile = new File(FMLPaths.CONFIGDIR.get().toFile(),"snes-soils.json");
        if (configFile.exists()){
            JsonObject config = new Gson().fromJson(new FileReader(configFile),JsonObject.class);
            config.keySet().forEach(k->{
                ResourceLocation key = ResourceLocation.parse(k);
                Optional<Holder<Block>> optional = ForgeRegistries.BLOCKS.getHolder(key);
                if (optional.isEmpty()) throw new JsonParseException("Invalid block key in SNES config file : " + key);

                if (config.get(k).isJsonArray()){

                    ArrayList<Block> blocks = new ArrayList<>();
                    for (JsonElement j : config.getAsJsonArray(k)) {
                        if (j.isJsonPrimitive() && j.getAsJsonPrimitive().isString()){
                            ResourceLocation value = ResourceLocation.parse( j.getAsJsonPrimitive().getAsString());
                            Optional<Holder<Block>> optionalValue = ForgeRegistries.BLOCKS.getHolder(value);
                            if (optionalValue.isEmpty())
                                LOGGER.error("Invalid block value for {} in SNES config file : {}", key, value);
                            else blocks.add(optionalValue.get().get());
                        } else {
                            LOGGER.error("A value in the array for {} isn't a string, ignoring it...", key);
                        }
                    }
                    SOILS.put(optional.get().get(), blocks);
                } else if (config.get(k).isJsonPrimitive() && config.get(k).getAsJsonPrimitive().isString()){
                    ResourceLocation value = ResourceLocation.parse( config.get(k).getAsJsonPrimitive().getAsString());
                    Optional<Holder<Block>> optionalValue = ForgeRegistries.BLOCKS.getHolder(value);
                    if (optionalValue.isEmpty()) throw new JsonParseException("Invalid block value for "+key+" in SNES config file : " + value);
                    SOILS.put(optional.get().get(), List.of(optionalValue.get().get()));
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

    private void cropGrow(final BlockEvent.CropGrowEvent.Pre event) {
        if (!SOILS.containsKey(event.getState().getBlock())) return;

        List<Block> requiredSoil = SOILS.get(event.getState().getBlock());
        if (!requiredSoil.contains(event.getLevel().getBlockState(event.getPos().below()).getBlock())) {
            event.setResult(Event.Result.DENY);
        }
    }


}
