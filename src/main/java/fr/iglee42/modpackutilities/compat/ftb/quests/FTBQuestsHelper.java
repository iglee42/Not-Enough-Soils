package fr.iglee42.modpackutilities.compat.ftb.quests;

import fr.iglee42.modpackutilities.modules.lore.LoreKeys;
import fr.iglee42.modpackutilities.modules.lore.requirements.CompletedQuestObjectRequirement;
import fr.iglee42.modpackutilities.modules.lore.requirements.RequirementType;
import fr.iglee42.modpackutilities.modules.lore.rewards.CompleteQuestTaskReward;
import fr.iglee42.modpackutilities.modules.lore.rewards.RewardType;
import fr.iglee42.modpackutilities.utils.ModuleLoader;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class FTBQuestsHelper {
    public static final DeferredRegister<RequirementType<?>> LORE_REQUIREMENTS = DeferredRegister.create(LoreKeys.LORE_REQUIREMENT_KEY, ModuleLoader.Modules.LORE.name().toLowerCase());
    public static final DeferredRegister<RewardType<?>> LORE_REWARD = DeferredRegister.create(LoreKeys.LORE_REWARD_KEY, ModuleLoader.Modules.LORE.name().toLowerCase());

    public static final RegistryObject<RequirementType<CompletedQuestObjectRequirement>> QUEST_OBJECT = LORE_REQUIREMENTS.register("complete_quest_object",()->new RequirementType<>(
            CompletedQuestObjectRequirement.CODEC,
            buffer-> new CompletedQuestObjectRequirement(buffer.readUtf()),
            (req,buf)-> buf.writeUtf(req.objectId())
    ));

    public static final RegistryObject<RewardType<CompleteQuestTaskReward>> TASK = LORE_REWARD.register("complete_task",()->new RewardType<>(
            CompleteQuestTaskReward.CODEC,
            buffer-> new CompleteQuestTaskReward(buffer.readUtf()),
            (req,buf)-> buf.writeUtf(req.taskId())
    ));


    public static void registerLore(IEventBus bus){
        LORE_REQUIREMENTS.register(bus);
        LORE_REWARD.register(bus);
    }
}
