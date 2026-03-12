package fr.iglee42.modpackutilities.modules.lore;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.iglee42.modpackutilities.IgleeModpackUtilities;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public record LoreFile(ResourceLocation id, String name, Set<LoreEntry> entries, String finished) {

    public static final Codec<LoreFile> CODEC = RecordCodecBuilder.create(instance->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(o -> o.id),
                    Codec.STRING.fieldOf("name").forGetter(o -> o.name),
                    Codec.list(LoreEntry.CODEC).xmap(LinkedHashSet::new, ArrayList::new).fieldOf("entries").forGetter(o -> (LinkedHashSet<LoreEntry>) o.entries),
                    Codec.STRING.fieldOf("finished").forGetter(o -> o.finished)
            ).apply(instance,LoreFile::new));

    public static LoreFile read(FriendlyByteBuf f){
        ResourceLocation id = f.readResourceLocation();
        String name = f.readUtf();
        LinkedHashSet<LoreEntry> entries = f.readCollection(LinkedHashSet::new,LoreEntry::read);
        String finished = f.readUtf();
        return new LoreFile(id,name, entries, finished);
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
        buf.writeUtf(name);
        buf.writeCollection(entries, (b,e)->e.write(b));
        buf.writeUtf(finished);
    }

    public boolean entryExists(ResourceLocation entryId) {
        return entries.stream().anyMatch(entry -> entry.id().equals(entryId));
    }

    public Optional<LoreEntry> getEntry(ResourceLocation entryId){
        return entries.stream().filter(entry -> entry.id().equals(entryId)).findFirst();
    }


}
