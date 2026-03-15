package fr.iglee42.modpackutilities.modules.lore;

import fr.iglee42.modpackutilities.IgleeModpackUtilities;
import fr.iglee42.modpackutilities.modules.lore.block.LoreEntityBlock;
import fr.iglee42.modpackutilities.modules.lore.block.entity.LoreEntityBlockEntity;
import fr.iglee42.modpackutilities.modules.lore.entity.LoreEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.IModBusEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class LoreRegistries {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, "lore");
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, "lore");
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "lore");
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, "lore");

    public static final RegistryObject<LoreEntityBlock> LORE_BLOCK = BLOCKS.register("lore_block", () -> new LoreEntityBlock(Block.Properties.copy(Blocks.NETHERITE_BLOCK)));
    public static final RegistryObject<BlockItem> LORE_BLOCK_ITEM = ITEMS.register("lore_block", () -> new BlockItem(LORE_BLOCK.get(),new Item.Properties()));
    public static final RegistryObject<BlockEntityType<LoreEntityBlockEntity>> BE_TYPE = BLOCK_ENTITIES.register("lore_entity_block_entity", () -> BlockEntityType.Builder.of(LoreEntityBlockEntity::new, LORE_BLOCK.get()).build(null));
    public static final RegistryObject<EntityType<LoreEntity>> ENTITY = ENTITIES.register("lore_entity", () -> EntityType.Builder.of(LoreEntity::new, MobCategory.MISC).sized(0.75f, 1.75f).build("lore_entity"));

    public static void register(IEventBus bus){
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        ENTITIES.register(bus);
    }
}
