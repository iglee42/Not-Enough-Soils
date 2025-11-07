package fr.iglee42.modpackutilities.resourcepack;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;

import java.nio.file.Path;
import java.util.function.Consumer;

public class IMUPackFinder implements RepositorySource {


	private final CustomPackType type;

	public IMUPackFinder(CustomPackType type) {

		this.type = type;
	}

	@Override
	public void loadPacks(Consumer<Pack> consumer) {
		Path rootPath = PathConstant.ROOT_PATH;
		Pack pack = Pack.readMetaAndCreate("imu_"+type.getSuffix(), Component.literal("IMU Builtin Pack"),true,t->new InMemoryPack(type.getVanillaType(),rootPath),type.getVanillaType(), Pack.Position.TOP, PackSource.BUILT_IN);
		if (pack != null) {
			consumer.accept(pack);
		}
	}
}