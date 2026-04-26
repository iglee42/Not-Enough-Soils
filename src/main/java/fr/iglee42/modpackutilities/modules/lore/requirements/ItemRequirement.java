package fr.iglee42.modpackutilities.modules.lore.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.iglee42.modpackutilities.modules.lore.LoreModule;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record ItemRequirement(Item item, int count) implements LoreRequirement {

    public static final MapCodec<ItemRequirement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(r -> r.item),
                    Codec.INT.fieldOf("count").forGetter(r -> r.count)
            ).apply(instance, ItemRequirement::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf,ItemRequirement> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.ITEM),ItemRequirement::item,
            ByteBufCodecs.INT,ItemRequirement::count,
            ItemRequirement::new
    );



    @Override
    public boolean test(Player player) {
        return player.getInventory().countItem(item) >= count;
    }

    @Override
    public RequirementType<? extends LoreRequirement> getType() {
        return LoreModule.ITEM_REQUIREMENT;
    }
}
