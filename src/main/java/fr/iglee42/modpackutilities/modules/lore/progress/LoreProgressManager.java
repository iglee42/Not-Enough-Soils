package fr.iglee42.modpackutilities.modules.lore.progress;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.iglee42.modpackutilities.IgleeModpackUtilities;
import fr.iglee42.modpackutilities.modules.lore.LoreEntry;
import fr.iglee42.modpackutilities.modules.lore.LoreFile;
import fr.iglee42.modpackutilities.modules.lore.LoreModule;
import fr.iglee42.modpackutilities.modules.lore.network.SyncLoreProgressPacket;
import fr.iglee42.modpackutilities.utils.FTBTeamsHelper;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class LoreProgressManager extends SavedData implements IProgressHandler {

    private static final Codec<Map<UUID,LoreProgress>> PROGRESS_CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, LoreProgress.CODEC).xmap(HashMap::new, Map::copyOf);

    public static final Codec<LoreProgressManager> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            PROGRESS_CODEC.fieldOf("progress").forGetter(m -> m.progresses)
    ).apply(builder, LoreProgressManager::new));

    private final Map<UUID , LoreProgress> progresses;

    private LoreProgressManager(Map<UUID, LoreProgress> progresses) {
        this.progresses = progresses;
    }

    private LoreProgressManager() {
        this(new HashMap<>());
    }

    public static LoreProgressManager get(){
        return get(Objects.requireNonNull(ServerLifecycleHooks.getCurrentServer()));
    }

    public static LoreProgressManager get(MinecraftServer server){
        DimensionDataStorage storage = Objects.requireNonNull(server.overworld()).getDataStorage();

        return storage.computeIfAbsent(new Factory<>(LoreProgressManager::new,LoreProgressManager::load), "lore_progress");
    }
    public static LoreProgressManager load(CompoundTag tag, HolderLookup.Provider registries) {
        return CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.getCompound("progress"))
                .resultOrPartial(err -> IgleeModpackUtilities.getModule(LoreModule.class).error("Failed to deserialize lore progress data: {}", err))
                .orElse(new LoreProgressManager());
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag compoundTag, HolderLookup.Provider registries) {
        return Util.make(new CompoundTag(),tag->{
            var out = CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE),this)
                    .resultOrPartial(err -> IgleeModpackUtilities.getModule(LoreModule.class).error("Failed to serialize lore progress data: {}", err))
                    .orElse(new CompoundTag());
            tag.put("progress", out);
        });
    }

    public boolean tryUnlockEntry(Player player, LoreFile file, ResourceLocation entryId){
        return change(player,progress->{
            Optional<LoreEntry> entryOpt = file.getEntry(entryId);
            if (entryOpt.isEmpty()) return false;
            LoreEntry entry = entryOpt.get();
            if (!entry.isPreviousEntryUnlocked(player)) return false;
            if (!entry.canFulfillRequirements(player)) return false;
            return progress.unlockEntry(file.id(),entryId);
        });
    }

    public boolean change(Player player, Function<LoreProgress,Boolean> function){
        LoreProgress progress = getProgress(player);
        if (function.apply(progress)){
            setDirty();
            PacketDistributor.sendToPlayer((ServerPlayer) player, new SyncLoreProgressPacket(progress));
            if (ModList.get().isLoaded("ftbteams")){
                FTBTeamsHelper.syncProgressToPlayerTeam(player,progress);
            }
            return true;
        }
        return false;
    }

    private static UUID getPlayerUUID(Player player){
        if (ModList.get().isLoaded("ftbteams")){
            return FTBTeamsHelper.getTeamUUID(player).orElse(player.getUUID());
        }
        return player.getUUID();
    }

    @Override
    public LoreProgress getProgress(Player player) {
        return progresses.computeIfAbsent(getPlayerUUID(player),$->createNew());
    }

    private LoreProgress createNew(){
        setDirty();
        return LoreProgress.empty();
    }
}
