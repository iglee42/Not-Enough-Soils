package fr.iglee42.modpackutilities.modules.lore.rewards;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.iglee42.modpackutilities.modules.lore.LoreModule;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record ItemReward(Item item, int count) implements LoreReward {

    public static final MapCodec<ItemReward> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(r -> r.item),
                    Codec.INT.fieldOf("count").forGetter(r -> r.count)
            ).apply(instance, ItemReward::new)
    );


    @Override
    public void execute(Player player) {
        player.getInventory().add(new ItemStack(item(),count()));
    }

    @Override
    public RewardType<? extends LoreReward> getType() {
        return LoreModule.ITEM_REWARD;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.literal(count() + "x " + item.getDescription().getString());
    }
}
