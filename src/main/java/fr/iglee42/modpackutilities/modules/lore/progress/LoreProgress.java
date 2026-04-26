package fr.iglee42.modpackutilities.modules.lore.progress;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public record LoreProgress(Map<ResourceLocation, PerFileProgress> progress) {

    private static final Codec<Map<ResourceLocation,PerFileProgress>> PROGRESS_CODEC = Codec.unboundedMap(ResourceLocation.CODEC, PerFileProgress.CODEC).xmap(HashMap::new,Map::copyOf);

    private static final Codec<LoreProgress> RAW_CODEC = RecordCodecBuilder.create(instance->
            instance.group(
                    PROGRESS_CODEC.fieldOf("progress").forGetter(LoreProgress::progress)
            ).apply(instance, LoreProgress::new)
    );

    public static final Codec<LoreProgress> CODEC = RAW_CODEC.xmap(in->new LoreProgress(new HashMap<>(in.progress)), Function.identity());

    public static final StreamCodec<RegistryFriendlyByteBuf,LoreProgress> STREAM_CODEC = StreamCodec.of(
            (buf,progress)->progress.write(buf),
            LoreProgress::read
    );

    public static LoreProgress empty() {
        return new LoreProgress(new HashMap<>());
    }

    public static LoreProgress read(FriendlyByteBuf buf){
        var progress = buf.readMap(FriendlyByteBuf::readResourceLocation, PerFileProgress::read);
        return new LoreProgress(progress);
    }

    @Override
    public Map<ResourceLocation, PerFileProgress> progress() {
        return Collections.unmodifiableMap(progress);
    }

    public boolean hasUnlockedEntry(ResourceLocation fileId, ResourceLocation entryId){
        return progress.containsKey(fileId) && progress.get(fileId).hasCompletedEntry(entryId);
    }

    public boolean unlockEntry(ResourceLocation fileId, ResourceLocation entryId) {
        return progress.computeIfAbsent(fileId, $ -> PerFileProgress.empty()).completeEntry(entryId);
    }

    public void write(FriendlyByteBuf buf){
        buf.writeMap(progress, FriendlyByteBuf::writeResourceLocation, (b,p)->p.write(b));
    }
}
