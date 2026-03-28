package fr.iglee42.modpackutilities.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import fr.iglee42.modpackutilities.mixins.compressed.AnimationMetadataSectionAccessor;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceMetadata;

import java.io.FileWriter;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;

public class TextureLayerApplier {

    public static void applyLayer(Function<ResourceLocation, Resource> spriteGetter,
                                  ResourceLocation baseTexture,
                                  ResourceLocation layer,
                                  Path outputPath) throws Exception {

        Resource baseSprite = spriteGetter.apply(baseTexture);
        if (baseSprite == null)
            throw new IllegalArgumentException("Texture " + baseTexture + " isn't an existing texture");

        Resource layerSprite = spriteGetter.apply(layer);
        if (layerSprite == null)
            throw new IllegalArgumentException("Texture " + layer + " isn't an existing texture");

        try (NativeImage baseImage = NativeImage.read(baseSprite.open());
             NativeImage layerImage = NativeImage.read(layerSprite.open())) {

            int baseWidth = baseImage.getWidth();
            int baseHeight = baseImage.getHeight();

            int layerWidth = layerImage.getWidth();
            int layerHeight = layerImage.getHeight();

            int frameCount = baseHeight / baseWidth;

            for (int frame = 0; frame < frameCount; frame++) {

                int frameYOffset = frame * baseWidth;

                for (int x = 0; x < baseWidth; x++) {
                    for (int y = 0; y < baseWidth; y++) {

                        int baseY = frameYOffset + y;

                        int baseColor = baseImage.getPixelRGBA(x, baseY);

                        int lx = x % layerWidth;
                        int ly = y % layerHeight;

                        int layerColor = layerImage.getPixelRGBA(lx, ly);

                        int a = (layerColor >> 24) & 0xFF;
                        if (a > 0) {
                            baseImage.setPixelRGBA(x, baseY, blend(baseColor, layerColor));
                        }
                    }
                }
            }

            outputPath.toFile().getParentFile().mkdirs();
            baseImage.writeToFile(outputPath);

            ResourceMetadata metadata = baseSprite.metadata();
            Optional<AnimationMetadataSection> animation = metadata.getSection(AnimationMetadataSection.SERIALIZER);

            if (animation.isPresent()) {
                Path metaPath = outputPath.resolveSibling(outputPath.getFileName().toString() + ".mcmeta");

                try (FileWriter writer = new FileWriter(metaPath.toFile())) {
                    writer.write(new Gson().toJson(animationToJson(animation.get())));
                } catch (Exception ignored) {}
            }
        }
    }

    public static JsonObject animationToJson(AnimationMetadataSection meta) {
        JsonObject root = new JsonObject();
        JsonObject json = new JsonObject();

        // frametime
        if (meta.getDefaultFrameTime() != 1) {
            json.addProperty("frametime", meta.getDefaultFrameTime());
        }

        // width
        if (((AnimationMetadataSectionAccessor)meta).getFrameWidth() != -1) {
            json.addProperty("width", ((AnimationMetadataSectionAccessor)meta).getFrameWidth());
        }

        // height
        if (((AnimationMetadataSectionAccessor)meta).getFrameHeight() != -1) {
            json.addProperty("height", ((AnimationMetadataSectionAccessor)meta).getFrameHeight());
        }

        // interpolate
        if (meta.isInterpolatedFrames()) {
            json.addProperty("interpolate", true);
        }

        // frames
        if (!((AnimationMetadataSectionAccessor)meta).getFrames().isEmpty()) {
            JsonArray frames = new JsonArray();

            for (AnimationFrame frame : ((AnimationMetadataSectionAccessor)meta).getFrames()) {
                if (frame.getTime(-1) == -1) {
                    frames.add(frame.getIndex());
                } else {
                    JsonObject frameObj = new JsonObject();
                    frameObj.addProperty("index", frame.getIndex());
                    frameObj.addProperty("time", frame.getTime(-1));
                    frames.add(frameObj);
                }
            }

            json.add("frames", frames);
        }

        root.add("animation", json);
        return root;
    }

    private static int blend(int base, int overlay) {
        int aO = (overlay >> 24) & 0xFF;
        int rO = (overlay >> 16) & 0xFF;
        int gO = (overlay >> 8) & 0xFF;
        int bO = overlay & 0xFF;

        int aB = (base >> 24) & 0xFF;
        int rB = (base >> 16) & 0xFF;
        int gB = (base >> 8) & 0xFF;
        int bB = base & 0xFF;

        float alpha = aO / 255.0f;
        int r = (int) (rO * alpha + rB * (1 - alpha));
        int g = (int) (gO * alpha + gB * (1 - alpha));
        int b = (int) (bO * alpha + bB * (1 - alpha));
        int a = Math.max(aB, aO);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
