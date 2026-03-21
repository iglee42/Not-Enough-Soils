package fr.iglee42.modpackutilities.modules.compressed;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.FallingBlock;

public class SimpleCompressedFallingBlock extends FallingBlock {
    public SimpleCompressedFallingBlock(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<? extends FallingBlock> codec() {
        return simpleCodec(SimpleCompressedFallingBlock::new);
    }
}
