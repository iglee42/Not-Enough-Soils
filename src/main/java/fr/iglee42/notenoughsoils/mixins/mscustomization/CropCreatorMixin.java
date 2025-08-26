package fr.iglee42.notenoughsoils.mixins.mscustomization;

import com.blakebr0.mysticalagriculture.api.crop.Crop;
import com.blakebr0.mysticalagriculture.api.crop.CropRecipes;
import com.blakebr0.mysticalagriculture.api.crop.CropTextures;
import com.blakebr0.mysticalagriculture.api.lib.LazyIngredient;
import com.blakebr0.mysticalcustomization.create.CropCreator;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import fr.iglee42.notenoughsoils.CropWithSoils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(value = CropCreator.class,remap = false)
public class CropCreatorMixin {

    @Inject(method = "create",at= @At(value = "RETURN",ordinal = 0),locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void snes$readCustomSoils(ResourceLocation id, JsonObject json, CallbackInfoReturnable<Crop> cir, JsonObject ingredient, LazyIngredient material, Crop crop){
        if (json.has("soils")) {
            CropWithSoils castedCrop = (CropWithSoils) crop;
            if (json.get("soils").isJsonArray()) {
                ArrayList<Block> blocks = new ArrayList<>();
                for (JsonElement j : json.getAsJsonArray("soils")) {
                    if (j.isJsonPrimitive() && j.getAsJsonPrimitive().isString()) {
                        String strValue = j.getAsJsonPrimitive().getAsString();
                        if (strValue.startsWith("#")){
                            TagKey<Block> blockTagKey = TagKey.create(Registries.BLOCK,ResourceLocation.parse(strValue.substring(1)));
                                List<Block> tagBlocks = ForgeRegistries.BLOCKS.tags() == null || ForgeRegistries.BLOCKS.tags().getTag(blockTagKey).isEmpty() ? new ArrayList<>(): ForgeRegistries.BLOCKS.tags().getTag(blockTagKey).stream().toList();
                            if (!tagBlocks.isEmpty()){
                                blocks.addAll(List.copyOf(tagBlocks));
                            }
                        } else {
                            ResourceLocation value = ResourceLocation.parse(j.getAsJsonPrimitive().getAsString());
                            Optional<Holder<Block>> optionalValue = ForgeRegistries.BLOCKS.getHolder(value);
                            if (optionalValue.isEmpty())
                                throw new JsonParseException("Invalid soil value : " + value);
                            else blocks.add(optionalValue.get().get());
                        }
                    }
                }
                castedCrop.snes$setCustomSoils(blocks);
            } else if (json.get("soils").isJsonPrimitive() && json.get("soils").getAsJsonPrimitive().isString()) {
                String strValue = json.get("soils").getAsJsonPrimitive().getAsString();
                if (strValue.startsWith("#")){
                    TagKey<Block> blockTagKey = TagKey.create(Registries.BLOCK,ResourceLocation.parse(strValue.substring(1)));
                        List<Block> blocks = ForgeRegistries.BLOCKS.tags() == null || ForgeRegistries.BLOCKS.tags().getTag(blockTagKey).isEmpty() ? new ArrayList<>(): ForgeRegistries.BLOCKS.tags().getTag(blockTagKey).stream().toList();
                    if (!blocks.isEmpty()){
                        castedCrop.snes$setCustomSoils( List.copyOf(blocks));
                    } else {
                        throw new JsonParseException("Tag "+blockTagKey.location()+" is empty or doesn't exist");
                    }
                } else {
                    ResourceLocation value = ResourceLocation.parse(strValue);
                    Optional<Holder<Block>> optionalValue = ForgeRegistries.BLOCKS.getHolder(value);
                    if (optionalValue.isEmpty())
                        throw new JsonParseException("Invalid soil value : " + value);
                    castedCrop.snes$setCustomSoils(List.of(optionalValue.get().get()));
                }
            } else {
                throw new JsonParseException("\"soils\" isn't an array or a string");
            }
        }
    }
}
