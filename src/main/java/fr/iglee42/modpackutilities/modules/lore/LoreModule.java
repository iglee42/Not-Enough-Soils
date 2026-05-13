package fr.iglee42.modpackutilities.modules.lore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import fr.iglee42.modpackutilities.compat.ftb.lib.FTBLibraryHelper;
import fr.iglee42.modpackutilities.compat.kubejs.KubeJSHelper;
import fr.iglee42.modpackutilities.modules.lore.client.LoreEntityBlockRenderer;
import fr.iglee42.modpackutilities.modules.lore.client.LoreEntityRenderer;
import fr.iglee42.modpackutilities.modules.lore.network.SyncLoreFilesPacket;
import fr.iglee42.modpackutilities.modules.lore.network.SyncLoreProgressPacket;
import fr.iglee42.modpackutilities.modules.lore.network.UnlockLoreEntryPacket;
import fr.iglee42.modpackutilities.modules.lore.progress.LoreProgressManager;
import fr.iglee42.modpackutilities.modules.lore.requirements.ItemRequirement;
import fr.iglee42.modpackutilities.modules.lore.requirements.LoreRequirement;
import fr.iglee42.modpackutilities.modules.lore.requirements.RequirementType;
import fr.iglee42.modpackutilities.compat.ftb.quests.FTBQuestsHelper;
import fr.iglee42.modpackutilities.modules.lore.rewards.ItemReward;
import fr.iglee42.modpackutilities.modules.lore.rewards.LoreReward;
import fr.iglee42.modpackutilities.modules.lore.rewards.RewardType;
import fr.iglee42.modpackutilities.modules.lore.rewards.XPReward;
import fr.iglee42.modpackutilities.utils.Module;
import fr.iglee42.modpackutilities.utils.ModuleLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class LoreModule extends Module {

    public static Registry<RequirementType<? extends LoreRequirement>> LORE_REQUIREMENTS;
    public static Registry<RewardType<? extends LoreReward>> LORE_REWARDS;

    public static RequirementType<ItemRequirement> ITEM_REQUIREMENT;
    public static RewardType<ItemReward> ITEM_REWARD;
    public static RewardType<XPReward> XP_REWARD;

    private final Map<ResourceLocation,LoreFile> files;

    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }


    public LoreModule(ModuleLoader.Modules type, String name) {
        super(type,name, false);
        this.files = new HashMap<>();
    }

    private void registerPackets(RegisterPayloadHandlersEvent event){
        PayloadRegistrar registrar = event.registrar(getName());
        registrar.playToClient(SyncLoreFilesPacket.TYPE,SyncLoreFilesPacket.STREAM_CODEC,(payload,ctx)->SyncLoreFilesPacket.handle(ctx,payload));
        registrar.playToClient(SyncLoreProgressPacket.TYPE,SyncLoreProgressPacket.STREAM_CODEC,(payload,ctx)->SyncLoreProgressPacket.handle(ctx,payload));
        registrar.playToServer(UnlockLoreEntryPacket.TYPE,UnlockLoreEntryPacket.STREAM_CODEC,(payload,ctx)->UnlockLoreEntryPacket.handle(ctx,payload));
    }

    private void registerRender(EntityRenderersEvent.RegisterRenderers event){
        event.registerEntityRenderer(LoreRegistries.ENTITY.get(), LoreEntityRenderer::new);
        event.registerBlockEntityRenderer(LoreRegistries.BE_TYPE.get(), LoreEntityBlockRenderer::new);
    }

    @Override
    public void init(IEventBus modEventBus, IEventBus forgeEventBus) throws Exception {
        super.init(modEventBus, forgeEventBus);
        modEventBus.addListener(this::registerRegistries);
        modEventBus.addListener(this::register);
        if (FMLEnvironment.dist.isClient()) modEventBus.addListener(this::registerRender);
        LoreRegistries.register(modEventBus);
        forgeEventBus.addListener(this::registerReloadListener);
        forgeEventBus.addListener(this::playerLogin);
        if (ModList.get().isLoaded("ftblibrary")) FTBLibraryHelper.registerLore(modEventBus);
        if (ModList.get().isLoaded("ftbquests")) FTBQuestsHelper.registerLore(modEventBus);
        if (ModList.get().isLoaded("kubejs")) KubeJSHelper.registerLore(modEventBus);
        modEventBus.addListener(this::registerPackets);
    }

    public Map<ResourceLocation, LoreFile> getFiles() {
        return files;
    }

    private void registerRegistries(NewRegistryEvent event){
        LORE_REQUIREMENTS = event.create(new RegistryBuilder<RequirementType<? extends LoreRequirement>>(LoreKeys.LORE_REQUIREMENT_KEY));
        LORE_REWARDS = event.create(new RegistryBuilder<RewardType<? extends LoreReward>>(LoreKeys.LORE_REWARD_KEY));
    }

    private void register(RegisterEvent event){
        if (event.getRegistryKey().equals(LoreKeys.LORE_REQUIREMENT_KEY)){
                ITEM_REQUIREMENT = new RequirementType<>(ItemRequirement.CODEC,ItemRequirement.STREAM_CODEC);
                event.register(LoreKeys.LORE_REQUIREMENT_KEY,ResourceLocation.fromNamespaceAndPath(getName(),"item"),()->ITEM_REQUIREMENT);
        }
        if (event.getRegistryKey().equals(LoreKeys.LORE_REWARD_KEY)){
            ITEM_REWARD = new RewardType<>(ItemReward.CODEC,ItemReward.STREAM_CODEC);

            XP_REWARD = new RewardType<>(XPReward.CODEC,XPReward.STREAM_CODEC);
            event.register(LoreKeys.LORE_REWARD_KEY,ResourceLocation.fromNamespaceAndPath(getName(),"item"),()->ITEM_REWARD);
            event.register(LoreKeys.LORE_REWARD_KEY,ResourceLocation.fromNamespaceAndPath(getName(),"xp"),()->XP_REWARD);
        }
    }

    private void registerReloadListener(AddReloadListenerEvent event){
        event.addListener(new LoreFileReader(event.getRegistryAccess()));
    }

    private void playerLogin(PlayerEvent.PlayerLoggedInEvent event){
        if (event.getEntity() instanceof ServerPlayer sp){
            PacketDistributor.sendToPlayer(sp, new SyncLoreFilesPacket(files.values().stream().toList()));
            PacketDistributor.sendToPlayer(sp, new SyncLoreProgressPacket(LoreProgressManager.get().getProgress(sp)));
        }
    }


    class LoreFileReader extends SimpleJsonResourceReloadListener{
        private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
        private final RegistryAccess registryAccess;

        public LoreFileReader(RegistryAccess registryAccess) {
            super(GSON, "lore_files");

            this.registryAccess = registryAccess;
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> elements, ResourceManager p_10794_, ProfilerFiller p_10795_) {
            files.clear();

            elements.forEach((id,json)-> LoreFile.fromJson(json,registryAccess).ifPresent(file->files.put(file.id(),file)));

            info("Loaded {} lore files",files.size());

            if (ServerLifecycleHooks.getCurrentServer() != null){
                PacketDistributor.sendToAllPlayers(new SyncLoreFilesPacket(files.values().stream().toList()));
            }
        }
    }


}
