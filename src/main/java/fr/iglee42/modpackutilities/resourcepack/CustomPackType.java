package fr.iglee42.modpackutilities.resourcepack;

import net.minecraft.server.packs.PackType;

public enum CustomPackType {
    DATA("data", PackType.SERVER_DATA),
    RESOURCE("resource",PackType.CLIENT_RESOURCES)
    ;
    private final String suffix;
    private final PackType vanillaType;

    CustomPackType(String suffix, PackType vanillaType) {

        this.suffix = suffix;
        this.vanillaType = vanillaType;
    }

    public String getSuffix() {
        return suffix;
    }

    public PackType getVanillaType() {
        return vanillaType;
    }
}