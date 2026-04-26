package fr.iglee42.modpackutilities.modules.lore.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.iglee42.modpackutilities.compat.CommonCompatsHelper;
import fr.iglee42.modpackutilities.compat.ftb.lib.FTBLibraryHelper;
import fr.iglee42.modpackutilities.compat.kubejs.KubeJSHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;

public record StageRequirement(String stageId) implements LoreRequirement {
    public static final MapCodec<StageRequirement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("stage").forGetter(r -> r.stageId)
            ).apply(instance, StageRequirement::new)
    );

    @Override
    public boolean test(Player player) {
        if (stageId.trim().isEmpty()) return false;
        if (ModList.get().isLoaded("kubejs"))
            if (KubeJSHelper.doesPlayerHasStage(player,stageId()))
                return true;
        if (ModList.get().isLoaded("ftblibrary")){
            if (FTBLibraryHelper.hasStage(player, stageId()))
                return true;
        }
        return false;
    }

    @Override
    public RequirementType<? extends LoreRequirement> getType() {
        return CommonCompatsHelper.STAGE.get();
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("lore.requirements.stage", stageId());
    }
}
