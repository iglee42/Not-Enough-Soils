package fr.iglee42.modpackutilities.mixins.soils.mscustomization;

import com.blakebr0.mysticalagriculture.api.crop.Crop;
import com.blakebr0.mysticalcustomization.modify.CropModifier;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import fr.iglee42.modpackutilities.modules.soils.CropWithSoils;
import fr.iglee42.modpackutilities.utils.RequiresMods;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiresMods({"mysticalcustomization"})
@Mixin(value = CropModifier.class,remap = false)
public class CropModifierMixin {

    @Inject(method = "modify",at=@At(value = "HEAD"),locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void snes$readCustomSoils(Crop crop, JsonObject json, CallbackInfo ci){

        CropWithSoils castedCrop = (CropWithSoils) crop;

        if (json.has("soils")) {
            if (json.get("soils").isJsonArray()) {
                ArrayList<Block> blocks = new ArrayList<>();
                for (JsonElement j : json.getAsJsonArray("soils")) {
                    if (j.isJsonPrimitive() && j.getAsJsonPrimitive().isString()) {
                        String strValue = j.getAsJsonPrimitive().getAsString();
                        if (strValue.startsWith("#")){
                            TagKey<Block> blockTagKey = TagKey.create(Registries.BLOCK,ResourceLocation.parse(strValue.substring(1)));
                            List<Block> tagBlocks = new ArrayList<>();
                            BuiltInRegistries.BLOCK.getTagOrEmpty(blockTagKey).forEach(h->tagBlocks.add(h.value()));
                            if (!tagBlocks.isEmpty()){
                                blocks.addAll(List.copyOf(tagBlocks));
                            }
                        } else {
                            ResourceLocation value = ResourceLocation.parse(j.getAsJsonPrimitive().getAsString());
                            Optional<Holder.Reference<Block>> optionalValue = BuiltInRegistries.BLOCK.getHolder(value);
                            if (optionalValue.isEmpty())
                                throw new JsonParseException("Invalid soil value : " + value);
                            else blocks.add(optionalValue.get().value());
                        }
                    }
                }
                castedCrop.snes$setCustomSoils(blocks);
            } else if (json.get("soils").isJsonPrimitive() && json.get("soils").getAsJsonPrimitive().isString()) {
                String strValue = json.get("soils").getAsJsonPrimitive().getAsString();
                if (strValue.startsWith("#")){
                    TagKey<Block> blockTagKey = TagKey.create(Registries.BLOCK,ResourceLocation.parse(strValue.substring(1)));
                    List<Block> blocks = new ArrayList<>();
                    BuiltInRegistries.BLOCK.getTagOrEmpty(blockTagKey).forEach(h->blocks.add(h.value()));
                    if (!blocks.isEmpty()){
                        castedCrop.snes$setCustomSoils( List.copyOf(blocks));
                    } else {
                        throw new JsonParseException("Tag "+blockTagKey.location()+" is empty or doesn't exist");
                    }
                } else {
                    ResourceLocation value = ResourceLocation.parse(strValue);
                    Optional<Holder.Reference<Block>> optionalValue = BuiltInRegistries.BLOCK.getHolder(value);
                    if (optionalValue.isEmpty())
                        throw new JsonParseException("Invalid soil value : " + value);
                    castedCrop.snes$setCustomSoils(List.of(optionalValue.get().value()));
                }
            } else {
                throw new JsonParseException("\"soils\" isn't an array or a string");
            }
        }
    }
}
