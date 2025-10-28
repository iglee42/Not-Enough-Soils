package fr.iglee42.modpackutilities.modules.soils;

import net.minecraft.world.level.block.Block;

import java.util.List;

public interface CropWithSoils {

    List<Block> snes$getCustomSoils();
    void snes$setCustomSoils(List<Block> soils);
}
