package fr.iglee42.modpackutilities.compat;

import fr.iglee42.modpackutilities.modules.lore.LoreKeys;
import fr.iglee42.modpackutilities.modules.lore.requirements.RequirementType;
import fr.iglee42.modpackutilities.modules.lore.requirements.StageRequirement;
import fr.iglee42.modpackutilities.modules.lore.rewards.RewardType;
import fr.iglee42.modpackutilities.modules.lore.rewards.StageReward;
import fr.iglee42.modpackutilities.utils.ModuleLoader;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CommonCompatsHelper {


    public static final DeferredRegister<RequirementType<?>> STAGE_REGISTER = DeferredRegister.create(LoreKeys.LORE_REQUIREMENT_KEY, ModuleLoader.Modules.LORE.name().toLowerCase());
    public static final DeferredRegister<RewardType<?>> STAGE_REWARD_REGISTER = DeferredRegister.create(LoreKeys.LORE_REWARD_KEY, ModuleLoader.Modules.LORE.name().toLowerCase());
    public static final DeferredHolder<RequirementType<?>,RequirementType<StageRequirement>> STAGE = STAGE_REGISTER.register("has_stage",()->new RequirementType<>(
            StageRequirement.CODEC,
            StageRequirement.STREAM_CODEC
    ));

    public static final DeferredHolder<RewardType<?>,RewardType<StageReward>> STAGE_REWARD = STAGE_REWARD_REGISTER.register("stage",()->new RewardType<>(
            StageReward.CODEC,
            StageReward.STREAM_CODEC
    ));


}
