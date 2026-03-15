package fr.iglee42.modpackutilities.modules.lore.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import fr.iglee42.modpackutilities.modules.lore.LoreModule;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface LoreRequirement {


    Codec<LoreRequirement> CODEC = LoreModule.LORE_REQUIREMENTS.get().getCodec().dispatch(
            LoreRequirement::getType,
            type->type.codec().codec()
    );

    boolean test(Player player);

    RequirementType<? extends LoreRequirement> getType();

    static List<LoreRequirement> readList(FriendlyByteBuf buffer){
        return buffer.readList(i->{
            var type = buffer.readRegistryIdUnsafe(LoreModule.LORE_REQUIREMENTS.get());
            return type.networkReader().apply(buffer);
        });
    }

    static <R extends LoreRequirement> void writeList(FriendlyByteBuf buffer,List<R> list){
        buffer.writeCollection(list,(buf,f)->{
            buf.writeRegistryIdUnsafe(LoreModule.LORE_REQUIREMENTS.get(),f.getType());
            BiConsumer<R, FriendlyByteBuf> consumer = (BiConsumer<R, FriendlyByteBuf>) f.getType().networkWriter();
            consumer.accept(f,buf);
        });
    }

    Component getTitle();
}
