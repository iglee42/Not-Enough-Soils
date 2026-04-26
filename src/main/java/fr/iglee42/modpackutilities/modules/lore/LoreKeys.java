package fr.iglee42.modpackutilities.modules.lore;

import fr.iglee42.modpackutilities.modules.lore.requirements.LoreRequirement;
import fr.iglee42.modpackutilities.modules.lore.requirements.RequirementType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class LoreKeys {
    // Keys
    public static final ResourceKey<Registry<RequirementType<? extends LoreRequirement>>> LORE_REQUIREMENT_KEY
            = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("lore", "lore_requirements"));
}
