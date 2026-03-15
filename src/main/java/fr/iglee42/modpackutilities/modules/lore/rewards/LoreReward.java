package fr.iglee42.modpackutilities.modules.lore.rewards;

import com.mojang.serialization.Codec;
import fr.iglee42.modpackutilities.modules.lore.LoreModule;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.BiConsumer;

public interface LoreReward {


    Codec<LoreReward> CODEC = LoreModule.LORE_REWARDS.get().getCodec().dispatch(
            LoreReward::getType,
            type->type.codec().codec()
    );

    void execute(Player player);

    RewardType<? extends LoreReward> getType();

    static List<LoreReward> readList(FriendlyByteBuf buffer){
        return buffer.readList(i->{
            var type = buffer.readRegistryIdUnsafe(LoreModule.LORE_REWARDS.get());
            return type.networkReader().apply(buffer);
        });
    }

    static <R extends LoreReward> void writeList(FriendlyByteBuf buffer, List<R> list){
        buffer.writeCollection(list,(buf,f)->{
            buf.writeRegistryIdUnsafe(LoreModule.LORE_REWARDS.get(),f.getType());
            BiConsumer<R, FriendlyByteBuf> consumer = (BiConsumer<R, FriendlyByteBuf>) f.getType().networkWriter();
            consumer.accept(f,buf);
        });
    }

    Component getTitle();
}
