package fr.iglee42.modpackutilities.modules.soils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import fr.iglee42.modpackutilities.IgleeModpackUtilities;
import fr.iglee42.modpackutilities.utils.Module;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class SoilsModule extends Module {
    public HashMap<Block, List<Block>> SOILS;

    public SoilsModule() {
        super("soils", true);
        SOILS = new HashMap<>();
    }

    @Override
    public void init(IEventBus modEventBus, IEventBus forgeEventBus) throws Exception {
        super.init(modEventBus, forgeEventBus);
        forgeEventBus.addListener(this::cropGrow);
        info("Initialized {} module successfully",getName());
    }

    private void cropGrow(final BlockEvent.CropGrowEvent.Pre event) {
        if (!SOILS.containsKey(event.getState().getBlock())) return;

        List<Block> requiredSoil = SOILS.get(event.getState().getBlock());
        if (!requiredSoil.contains(event.getLevel().getBlockState(event.getPos().below()).getBlock())) {
            event.setResult(Event.Result.DENY);
        }
    }


    protected void reloadConfig() throws JsonParseException {
        SOILS.clear();
        JsonObject config = getConfig();
        if (config == null) return;
        config.keySet().forEach(k -> {
            ResourceLocation key = ResourceLocation.parse(k);
            Optional<Holder<Block>> optional = ForgeRegistries.BLOCKS.getHolder(key);
            if (optional.isEmpty()){
                error("Invalid block key in IMU Soils config file : " + key);
                return;
            }

            if (config.get(k).isJsonArray()) {

                ArrayList<Block> blocks = new ArrayList<>();
                for (JsonElement j : config.getAsJsonArray(k)) {
                    if (j.isJsonPrimitive() && j.getAsJsonPrimitive().isString()) {
                        String strValue = j.getAsJsonPrimitive().getAsString();
                        if (strValue.startsWith("#")) {
                            TagKey<Block> blockTagKey = TagKey.create(Registries.BLOCK, ResourceLocation.parse(strValue.substring(1)));
                            List<Block> tagBlocks = new ArrayList<>();
                            BuiltInRegistries.BLOCK.getTagOrEmpty(blockTagKey).forEach(h -> tagBlocks.add(h.value()));
                            if (!tagBlocks.isEmpty()) {
                                blocks.addAll(List.copyOf(tagBlocks));
                            }
                        } else {
                            ResourceLocation value = ResourceLocation.parse(strValue);
                            Optional<Holder<Block>> optionalValue = ForgeRegistries.BLOCKS.getHolder(value);
                            if (optionalValue.isEmpty())
                                warn("Invalid block value for {} in IMU Soils config file : {}", key, value);
                            else blocks.add(optionalValue.get().value());
                        }
                    } else {
                        error("A value in the array for {} isn't a string", key);
                    }
                }
                SOILS.put(optional.get().value(), blocks);
            } else if (config.get(k).isJsonPrimitive() && config.get(k).getAsJsonPrimitive().isString()) {
                String strValue = config.get(k).getAsJsonPrimitive().getAsString();
                if (strValue.startsWith("#")) {
                    TagKey<Block> blockTagKey = TagKey.create(Registries.BLOCK, ResourceLocation.parse(strValue.substring(1)));
                    List<Block> blocks = new ArrayList<>();
                    BuiltInRegistries.BLOCK.getTagOrEmpty(blockTagKey).forEach(h -> blocks.add(h.value()));
                    if (!blocks.isEmpty()) {
                        SOILS.put(optional.get().value(), List.copyOf(blocks));
                    } else {
                        warn("Tag " + blockTagKey.location() + " for " + key + " in IMU Soils config file is empty or doesn't exist");
                    }
                } else {
                    ResourceLocation value = ResourceLocation.parse(strValue);
                    Optional<Holder<Block>> optionalValue = ForgeRegistries.BLOCKS.getHolder(value);
                    if (optionalValue.isEmpty())
                        error("Invalid block value for " + key + " in IMU Soils config file : " + value);
                    SOILS.put(optional.get().value(), List.of(optionalValue.get().value()));
                }

            } else {
                error("Value for " + key + " in IMU Soils config file isn't an array or a string");
            }
        });
    }

    @Override
    protected Consumer<ResourceManager> getReloadListener() {
        return rm->{
            SoilsModule module = IgleeModpackUtilities.getModule(SoilsModule.class);
            if (module == null) return;
            try {
                module.reloadConfig();
                if (ModList.get().isLoaded("mysticalagriculture")) MysticalUtils.reloadCropsSoils();
                info("Successfully reloaded IMU soils and modified soils for {} plants", module.SOILS.size());
            } catch (Exception e) {
                error("Failed to reload IMU soils: ", e);
            }
        };
    }
}
