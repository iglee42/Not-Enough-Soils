package fr.iglee42.modpackutilities.mixins.compressed;

import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = AnimationMetadataSection.class,remap = false)
public interface AnimationMetadataSectionAccessor {

    @Accessor("frameWidth")
    public int getFrameWidth();

    @Accessor("frameHeight")
    public int getFrameHeight();

    @Accessor("frames")
    public List<AnimationFrame> getFrames();

}
