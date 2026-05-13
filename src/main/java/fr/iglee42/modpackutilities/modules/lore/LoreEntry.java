package fr.iglee42.modpackutilities.modules.lore;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.iglee42.modpackutilities.IgleeModpackUtilities;
import fr.iglee42.modpackutilities.modules.lore.progress.IProgressHandler;
import fr.iglee42.modpackutilities.modules.lore.progress.LoreProgressManager;
import fr.iglee42.modpackutilities.modules.lore.requirements.LoreRequirement;
import fr.iglee42.modpackutilities.modules.lore.rewards.LoreReward;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record  LoreEntry(ResourceLocation id, List<String> lines, List<LoreRequirement> requirements,
                        Optional<ResourceLocation> voiceLocation, List<LoreReward> rewards, boolean hiddenRewards) {

    public static final Codec<LoreEntry> CODEC = RecordCodecBuilder.create(instance->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(o -> o.id),
                    Codec.STRING.listOf().fieldOf("lines").forGetter(o -> o.lines),
                    LoreRequirement.CODEC.listOf().optionalFieldOf("requirements",new ArrayList<>()).forGetter(o -> o.requirements),
                    ResourceLocation.CODEC.optionalFieldOf("voice_location").forGetter(o -> o.voiceLocation),
                    LoreReward.CODEC.listOf().optionalFieldOf("rewards",new ArrayList<>()).forGetter(o -> o.rewards),
                    Codec.BOOL.optionalFieldOf("hidden_rewards",false).forGetter(o -> o.hiddenRewards)
                    ).apply(instance,LoreEntry::new));

    public static LoreEntry read(RegistryFriendlyByteBuf buf){
        var id = buf.readResourceLocation();
        var lines = buf.readUtf().lines().toList();
        var requirements = LoreRequirement.readList(buf);
        var voiceLocation = buf.readOptional(FriendlyByteBuf::readResourceLocation);
        var rewards = LoreReward.readList(buf);
        var hiddenRewards = buf.readBoolean();
        return new LoreEntry(id,lines,requirements,voiceLocation,rewards,hiddenRewards);
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
        return previousEntry.get().isUnlocked(player);
    }

    public boolean isUnlocked(Player player) {
        Optional<LoreFile> file = getLoreFile();
        if (file.isEmpty()) return false;
        if (getPreviousEntry().isEmpty()) return true;
        return getProgressHandler(player.level().isClientSide).getProgress(player).hasUnlockedEntry(file.get().id(), id());
    }

    public boolean canFulfillRequirements(Player player) {
        return requirements.stream().allMatch(req -> req.test(player));
    }

    private static IProgressHandler getProgressHandler(boolean client) {
        return client ? ClientLoreModule.getInstance() : LoreProgressManager.get();
    }

    public boolean showRewards(){
        return !rewards.isEmpty() && !hiddenRewards;
    }

    public void write(RegistryFriendlyByteBuf buf){
        buf.writeResourceLocation(id);
        buf.writeUtf(String.join("\n", lines));
        LoreRequirement.writeList(buf, requirements);
        buf.writeOptional(voiceLocation,FriendlyByteBuf::writeResourceLocation);
        LoreReward.writeList(buf, rewards);
        buf.writeBoolean(hiddenRewards);
    }
}
