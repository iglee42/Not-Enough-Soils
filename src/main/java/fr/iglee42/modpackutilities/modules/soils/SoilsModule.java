package fr.iglee42.modpackutilities.modules.soils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import fr.iglee42.modpackutilities.utils.Module;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class SoilsModule extends Module {
    public HashMap<Block, List<Block>> SOILS;

    public SoilsModule() {
        super("soils", true);
        SOILS = new HashMap<>();
    }

    @Override
    public void init(IEventBus modEventBus, IEventBus forgeEventBus) {
        super.init(modEventBus, forgeEventBus);
        NeoForge.EVENT_BUS.addListener(this::cropGrow);
        NeoForge.EVENT_BUS.addListener(this::addReloadListener);
    }

    private void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(new SoilsReloadListener());
    }

    private void cropGrow(final CropGrowEvent.Pre event) {
        if (!SOILS.containsKey(event.getState().getBlock())) return;

        List<Block> requiredSoil = SOILS.get(event.getState().getBlock());
        if (!requiredSoil.contains(event.getLevel().getBlockState(event.getPos().below()).getBlock())) {
            event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);
        }
    }


    protected void reloadConfig() throws JsonParseException {
        SOILS.clear();
        JsonObject config = getConfig();
        if (config == null) return;
        config.keySet().forEach(k -> {
            ResourceLocation key = ResourceLocation.parse(k);
            Optional<Holder.Reference<Block>> optional = BuiltInRegistries.BLOCK.getHolder(key);
            if (optional.isEmpty()) throw new JsonParseException("Invalid block key in IMU Soils config file : " + key);

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
                            Optional<Holder.Reference<Block>> optionalValue = BuiltInRegistries.BLOCK.getHolder(value);
                            if (optionalValue.isEmpty())
                                LogUtils.getLogger().error("Invalid block value for {} in IMU Soils config file : {}", key, value);
                            else blocks.add(optionalValue.get().value());
                        }
                    } else {
                        LogUtils.getLogger().error("A value in the array for {} isn't a string, ignoring it...", key);
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
                        throw new JsonParseException("Tag " + blockTagKey.location() + " for " + key + " in IMU Soils config file is empty or doesn't exist");
                    }
                } else {
                    ResourceLocation value = ResourceLocation.parse(strValue);
                    Optional<Holder.Reference<Block>> optionalValue = BuiltInRegistries.BLOCK.getHolder(value);
                    if (optionalValue.isEmpty())
                        throw new JsonParseException("Invalid block value for " + key + " in IMU Soils config file : " + value);
                    SOILS.put(optional.get().value(), List.of(optionalValue.get().value()));
                }

            } else {
                throw new JsonParseException("Value for " + key + " in IMU Soils config file isn't an array or a string");
            }
        });
    }
}
