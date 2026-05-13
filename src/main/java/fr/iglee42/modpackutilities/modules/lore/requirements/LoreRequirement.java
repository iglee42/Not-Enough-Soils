package fr.iglee42.modpackutilities.modules.lore.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import fr.iglee42.modpackutilities.modules.lore.LoreKeys;
import fr.iglee42.modpackutilities.modules.lore.LoreModule;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface LoreRequirement {


    Codec<LoreRequirement> CODEC = LoreModule.LORE_REQUIREMENTS.byNameCodec().dispatch(
            LoreRequirement::getType,
            RequirementType::codec
    );

    StreamCodec<RegistryFriendlyByteBuf,LoreRequirement> STREAM_CODEC = ByteBufCodecs.registry(LoreKeys.LORE_REQUIREMENT_KEY)
            .dispatch(
                    LoreRequirement::getType,
                    RequirementType::streamCodec
            );

    boolean test(Player player);

    RequirementType<? extends LoreRequirement> getType();

    static List<LoreRequirement> readList(RegistryFriendlyByteBuf buffer){
        return STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer);
    }

    static <R extends LoreRequirement> void writeList(RegistryFriendlyByteBuf buffer,List<R> list){
        STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, (List<LoreRequirement>) list);
    }

    Component getTitle();
}
