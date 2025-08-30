package fr.iglee42.modpackutilities.modules.compressed;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

public class CompressedBlock {

    private final ResourceLocation block;
    private final Map<Integer, Pair<Block, BlockItem>> tierObjects;
    private String singleTexture = "";
    private boolean wasRegister;

    public CompressedBlock(ResourceLocation block) {
        this.block = block;
        this.tierObjects = new HashMap<>();
    }

    public Pair<Block,BlockItem> getPairForTier(int tier){
        return tierObjects.getOrDefault(tier,null);
    }

    public Block getBlockForTier(int tier) {
        if (getPairForTier(tier) == null) return null;
        return getPairForTier(tier).getFirst();
    }
    public BlockItem getItemForTier(int tier) {
        if (getPairForTier(tier) == null) return null;
        return getPairForTier(tier).getSecond();
    }

    public void setSingleTexture(String singleTexture) {
        this.singleTexture = singleTexture;
    }

    public boolean isSingleTexture(){
        return !singleTexture.isBlank();
    }

    public void setBlockForTier(int tier,Block block){
        Pair<Block,BlockItem> pair = tierObjects.getOrDefault(tier,new Pair<>(null,null));
        if (pair.getFirst() == null) {
            pair = new Pair<>(block,pair.getSecond());
            tierObjects.put(tier,pair);
        }
    }

    public void setItemForTier(int tier,BlockItem item){
        Pair<Block,BlockItem> pair = tierObjects.getOrDefault(tier,new Pair<>(null,null));
        if (pair.getSecond() == null) {
            pair = new Pair<>(pair.getFirst(),item);
            tierObjects.put(tier,pair);
        }
    }

    public ResourceLocation getBlock() {
        return block;
    }
}
