package fr.iglee42.modpackutilities.utils;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;

public class TextureLayerApplier {



    public static void applyLayer(Function<ResourceLocation, Resource> spriteGetter, ResourceLocation baseTexture, ResourceLocation layer, Path outputPath) throws Exception {
            Minecraft mc = Minecraft.getInstance();

            Resource baseSprite = spriteGetter.apply(baseTexture);
            if (baseSprite == null)
                throw new IllegalArgumentException("Texture " + baseTexture + " isn't an existing texture");
            Resource layerSprite = spriteGetter.apply(layer);
            if (layerSprite == null)
                throw new IllegalArgumentException("Texture " + layer + " isn't an existing texture");
            try (NativeImage baseImage = NativeImage.read(baseSprite.open())) {
                try (NativeImage layerImage = NativeImage.read(layerSprite.open())) {
                    for (int x = 0; x < baseImage.getWidth(); x++) {
                        for (int y = 0; y < baseImage.getHeight(); y++) {
                            try {
                                int baseColor = baseImage.getPixelRGBA(x, y);
                                int layerColor = layerImage.getPixelRGBA(x, y);

                                int a = (layerColor >> 24) & 0xFF;
                                if (a > 0) {
                                    baseImage.setPixelRGBA(x, y, blend(baseColor, layerColor));
                                }
                            } catch (IllegalStateException ignored) {
                            }
                        }
                    }

                    outputPath.toFile().getParentFile().mkdirs();
                    baseImage.writeToFile(outputPath);
                }
            }

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
