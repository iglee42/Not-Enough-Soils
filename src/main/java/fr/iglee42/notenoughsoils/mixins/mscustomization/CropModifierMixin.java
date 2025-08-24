package fr.iglee42.notenoughsoils.mixins.mscustomization;

import com.blakebr0.mysticalagriculture.api.crop.Crop;
import com.blakebr0.mysticalcustomization.modify.CropModifier;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import fr.iglee42.notenoughsoils.CropWithSoils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
                        ResourceLocation value = ResourceLocation.parse(j.getAsJsonPrimitive().getAsString());
                        Optional<Holder.Reference<Block>> optionalValue = BuiltInRegistries.BLOCK.getHolder(value);
                        if (optionalValue.isEmpty())
                            throw new JsonParseException("Invalid soil value : " + value);
                        else blocks.add(optionalValue.get().value());
                    }
                }
                castedCrop.snes$setCustomSoils(blocks);
            } else if (json.get("soils").isJsonPrimitive() && json.get("soils").getAsJsonPrimitive().isString()) {
                ResourceLocation value = ResourceLocation.parse(json.get("soils").getAsJsonPrimitive().getAsString());
                Optional<Holder.Reference<Block>> optionalValue = BuiltInRegistries.BLOCK.getHolder(value);
                if (optionalValue.isEmpty())
                    throw new JsonParseException("Invalid soil value : " + value);
                castedCrop.snes$setCustomSoils(List.of(optionalValue.get().value()));
            } else {
                throw new JsonParseException("\"soils\" isn't an array or a string");
            }
        }
    }
}
