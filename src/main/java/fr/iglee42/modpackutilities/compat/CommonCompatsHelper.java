package fr.iglee42.modpackutilities.compat;

import fr.iglee42.modpackutilities.modules.lore.LoreKeys;
import fr.iglee42.modpackutilities.modules.lore.requirements.RequirementType;
import fr.iglee42.modpackutilities.modules.lore.requirements.StageRequirement;
import fr.iglee42.modpackutilities.modules.lore.rewards.RewardType;
import fr.iglee42.modpackutilities.modules.lore.rewards.StageReward;
import fr.iglee42.modpackutilities.utils.ModuleLoader;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CommonCompatsHelper {


    public static final DeferredRegister<RequirementType<?>> STAGE_REGISTER = DeferredRegister.createOptional(LoreKeys.LORE_REQUIREMENT_KEY, ModuleLoader.Modules.LORE.name().toLowerCase());
    public static final DeferredRegister<RewardType<?>> STAGE_REWARD_REGISTER = DeferredRegister.createOptional(LoreKeys.LORE_REWARD_KEY, ModuleLoader.Modules.LORE.name().toLowerCase());
    public static final RegistryObject<RequirementType<StageRequirement>> STAGE = STAGE_REGISTER.register("has_stage",()->new RequirementType<>(
            StageRequirement.CODEC,
            buffer-> new StageRequirement(buffer.readUtf()),
            (req,buf)-> buf.writeUtf(req.stageId())
    ));

    public static final RegistryObject<RewardType<StageReward>> STAGE_REWARD = STAGE_REWARD_REGISTER.register("stage",()->new RewardType<>(
            StageReward.CODEC,
            buffer-> new StageReward(buffer.readUtf()),
            (req,buf)-> buf.writeUtf(req.stageId())
    ));


}
