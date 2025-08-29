package fr.iglee42.modpackutilities.resourcepack;

import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.RepositorySource;

import java.nio.file.Path;
import java.util.function.Consumer;

import static net.minecraft.server.packs.repository.BuiltInPackSource.fixedResources;

public class IMUPackFinder implements RepositorySource {


	private final CustomPackType type;

	public IMUPackFinder(CustomPackType type) {

		this.type = type;
	}

	@Override
	public void loadPacks(Consumer<Pack> consumer) {
		Path rootPath = PathConstant.ROOT_PATH;
		Pack pack = Pack.readMetaAndCreate(InMemoryPack.getPackInfo(type.getVanillaType()),fixedResources(new InMemoryPack(type.getVanillaType(),rootPath)),type.getVanillaType(),new PackSelectionConfig(true, Pack.Position.TOP,true));
		if (pack != null) {
			consumer.accept(pack);
		}
	}
}