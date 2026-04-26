package fr.iglee42.modpackutilities.modules.lore;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.iglee42.modpackutilities.IgleeModpackUtilities;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public record LoreFile(ResourceLocation id, Set<LoreEntry> entries) {

    public static final Codec<LoreFile> CODEC = RecordCodecBuilder.create(instance->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(o -> o.id),
                    Codec.list(LoreEntry.CODEC).xmap(LinkedHashSet::new, ArrayList::new).fieldOf("entries").forGetter(o -> (LinkedHashSet<LoreEntry>) o.entries)
            ).apply(instance,LoreFile::new));

    public static final StreamCodec<RegistryFriendlyByteBuf,LoreFile> STREAM_CODEC = StreamCodec.of(
            (buf,file)->file.write(buf),
            LoreFile::read
    );

    public static LoreFile read(FriendlyByteBuf f){
        ResourceLocation id = f.readResourceLocation();
        LinkedHashSet<LoreEntry> entries = f.readCollection(LinkedHashSet::new,LoreEntry::read);
        return new LoreFile(id, entries);
    }

    public static Optional<LoreFile> getLoreFile(ResourceLocation id) {
        if (ServerLifecycleHooks.getCurrentServer() == null){
            if (ClientLoreModule.getInstance().getLoreFiles() == null)
                return Optional.empty();
            return ClientLoreModule.getInstance().getLoreFiles().values().stream().filter(file -> file.id().equals(id)).findFirst();
        }
        if (IgleeModpackUtilities.getModule(LoreModule.class) == null)
            return Optional.empty();
        return IgleeModpackUtilities.getModule(LoreModule.class).getFiles().values().stream().filter(file -> file.id().equals(id)).findFirst();
    }


    public static Optional<LoreFile> fromJson(JsonElement json, RegistryAccess registryAccess) {
        return CODEC.decode(RegistryOps.create(JsonOps.INSTANCE,registryAccess), json)
                .resultOrPartial(error -> IgleeModpackUtilities.getModule(LoreModule.class).error("JSON parse failure: {}", error))
                .map(Pair::getFirst);
    }

    public void write(FriendlyByteBuf buf){
        buf.writeResourceLocation(id);
        buf.writeCollection(entries, (b,e)->e.write(b));
    }

    public boolean entryExists(ResourceLocation entryId) {
        return entries.stream().anyMatch(entry -> entry.id().equals(entryId));
    }

    public Optional<LoreEntry> getEntry(ResourceLocation entryId){
        return entries.stream().filter(entry -> entry.id().equals(entryId)).findFirst();
    }


}
