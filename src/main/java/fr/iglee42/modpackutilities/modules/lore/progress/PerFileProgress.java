package fr.iglee42.modpackutilities.modules.lore.progress;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.function.Function;

public class PerFileProgress {

    private static final Codec<PerFileProgress> RAW_CODEC = RecordCodecBuilder.create(instance->
            instance.group(
                    ResourceLocation.CODEC.listOf().fieldOf("completedEntries").forGetter(p->p.completedEntries)
            ).apply(instance, PerFileProgress::new)
    );

    public static final Codec<PerFileProgress> CODEC = RAW_CODEC.xmap(in->new PerFileProgress(new ArrayList<>(in.completedEntries)), Function.identity());

    public static PerFileProgress read(FriendlyByteBuf buf){
        var entries = buf.readList(FriendlyByteBuf::readResourceLocation);
        return new PerFileProgress(entries);
    }

    private List<ResourceLocation> completedEntries;


    private PerFileProgress(List<ResourceLocation> completedEntries) {
        this.completedEntries = completedEntries;
    }


    public static PerFileProgress empty() {
        return new PerFileProgress(new ArrayList<>());
    }

    public boolean hasCompletedEntry(ResourceLocation entryId){
        return completedEntries.contains(entryId);
    }

    public boolean completeEntry(ResourceLocation entryId) {
        if (hasCompletedEntry(entryId)) {
            return false;
        }
        completedEntries.add(entryId);
        return true;
    }

    public void write(FriendlyByteBuf buf){
        buf.writeCollection(completedEntries, FriendlyByteBuf::writeResourceLocation);
    }

}
