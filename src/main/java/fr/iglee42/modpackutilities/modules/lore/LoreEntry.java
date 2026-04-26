package fr.iglee42.modpackutilities.modules.lore;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.iglee42.modpackutilities.IgleeModpackUtilities;
import fr.iglee42.modpackutilities.modules.lore.progress.IProgressHandler;
import fr.iglee42.modpackutilities.modules.lore.progress.LoreProgressManager;
import fr.iglee42.modpackutilities.modules.lore.requirements.LoreRequirement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Optional;

public record  LoreEntry(ResourceLocation id, List<String> lines, List<LoreRequirement> requirements,
                        ResourceLocation voiceLocation) {

    public static final Codec<LoreEntry> CODEC = RecordCodecBuilder.create(instance->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(o -> o.id),
                    Codec.STRING.listOf().fieldOf("lines").forGetter(o -> o.lines),
                    LoreRequirement.CODEC.listOf().fieldOf("requirements").forGetter(o -> o.requirements),
                    ResourceLocation.CODEC.fieldOf("voice_location").forGetter(o -> o.voiceLocation)
            ).apply(instance,LoreEntry::new));

    public static LoreEntry read(FriendlyByteBuf buf){
        var id = buf.readResourceLocation();
        var lines = buf.readUtf().lines().toList();
        var requirements = LoreRequirement.readList(buf);
        var voiceLocation = buf.readResourceLocation();
        return new LoreEntry(id,lines,requirements,voiceLocation);
    }

    public Optional<LoreFile> getLoreFile() {
        if (IgleeModpackUtilities.getModule(LoreModule.class) == null)
            return Optional.empty();
        return IgleeModpackUtilities.getModule(LoreModule.class).getFiles().values().stream().filter(file -> file.entries().contains(this)).findFirst();
    }

    public Optional<LoreEntry> getPreviousEntry() {
        Optional<LoreFile> loreFile = getLoreFile();
        if (loreFile.isEmpty())
            return Optional.empty();
        LoreFile file = loreFile.get();
        LoreEntry previous = null;
        for (LoreEntry entry : file.entries()) {
            if (entry.equals(this))
                return Optional.ofNullable(previous);
            previous = entry;
        }
        return Optional.empty();
    }

    public boolean isPreviousEntryUnlocked(Player player) {
        Optional<LoreFile> file = getLoreFile();
        if (file.isEmpty()) return false;
        Optional<LoreEntry> previousEntry = getPreviousEntry();
        if (previousEntry.isEmpty())
            return true;
        getProgressHandler(player.level().isClientSide).getProgress(player).hasUnlockedEntry(file.get().id(), previousEntry.get().id());
        return true;
    }

    public boolean isUnlocked(Player player) {
        Optional<LoreFile> file = getLoreFile();
        if (file.isEmpty()) return false;
        if (getPreviousEntry().isEmpty()) return true;
        getProgressHandler(player.level().isClientSide).getProgress(player).hasUnlockedEntry(file.get().id(), id());
        return true;
    }

    public boolean canFulfillRequirements(Player player) {
        return requirements.stream().allMatch(req -> req.test(player));
    }

    private static IProgressHandler getProgressHandler(boolean client) {
        return client ? ClientLoreModule.getInstance() : LoreProgressManager.get();
    }

    public void write(FriendlyByteBuf buf){
        buf.writeResourceLocation(id);
        buf.writeUtf(String.join("\n", lines));
        LoreRequirement.writeList(buf, requirements);
        buf.writeResourceLocation(voiceLocation);
    }
}
