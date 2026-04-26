package fr.iglee42.modpackutilities.modules.lore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import fr.iglee42.modpackutilities.compat.ftb.lib.FTBLibraryHelper;
import fr.iglee42.modpackutilities.compat.kubejs.KubeJSHelper;
import fr.iglee42.modpackutilities.modules.lore.client.LoreEntityBlockRenderer;
import fr.iglee42.modpackutilities.modules.lore.client.LoreEntityRenderer;
import fr.iglee42.modpackutilities.modules.lore.network.ChangeLoreFilePacket;
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
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class LoreModule extends Module {

    public static Supplier<IForgeRegistry<RequirementType<? extends LoreRequirement>>> LORE_REQUIREMENTS;
    public static Supplier<IForgeRegistry<RewardType<? extends LoreReward>>> LORE_REWARDS;

    public static RequirementType<ItemRequirement> ITEM_REQUIREMENT;
    public static RewardType<ItemReward> ITEM_REWARD;
    public static RewardType<XPReward> XP_REWARD;

    private final Map<ResourceLocation,LoreFile> files;

    public static SimpleChannel NET_INSTANCE;

    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }


    public LoreModule(ModuleLoader.Modules type, String name) {
        super(type,name, false);
        this.files = new HashMap<>();
    }

    private void registerPackets(SimpleChannel net){
        net.messageBuilder(SyncLoreFilesPacket.class,id(),NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncLoreFilesPacket::toBytes)
                .decoder(SyncLoreFilesPacket::new)
                .consumerMainThread(SyncLoreFilesPacket::handle)
                .add();
        net.messageBuilder(SyncLoreProgressPacket.class,id(),NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncLoreProgressPacket::toBytes)
                .decoder(SyncLoreProgressPacket::new)
                .consumerMainThread(SyncLoreProgressPacket::handle)
                .add();
        net.messageBuilder(UnlockLoreEntryPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(UnlockLoreEntryPacket::toBytes)
                .decoder(UnlockLoreEntryPacket::new)
                .consumerMainThread(UnlockLoreEntryPacket::handle)
                .add();
        net.messageBuilder(ChangeLoreFilePacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(ChangeLoreFilePacket::toBytes)
                .decoder(ChangeLoreFilePacket::new)
                .consumerMainThread(ChangeLoreFilePacket::handle)
                .add();
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
        if (FMLEnvironment.dist.isClient()) modEventBus.addListener(this::addToCreativeTab);
        LoreRegistries.register(modEventBus);
        forgeEventBus.addListener(this::registerReloadListener);
        forgeEventBus.addListener(this::playerLogin);
        if (ModList.get().isLoaded("ftblibrary")) FTBLibraryHelper.registerLore(modEventBus);
        if (ModList.get().isLoaded("ftbquests")) FTBQuestsHelper.registerLore(modEventBus);
        if (ModList.get().isLoaded("kubejs")) KubeJSHelper.registerLore(modEventBus);
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(ResourceLocation.fromNamespaceAndPath(getName(), "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        NET_INSTANCE = net;

        registerPackets(net);
    }

    public Map<ResourceLocation, LoreFile> getFiles() {
        return files;
    }

    private void registerRegistries(NewRegistryEvent event){
        LORE_REQUIREMENTS = event.create(RegistryBuilder.<RequirementType<? extends LoreRequirement>>of(LoreKeys.LORE_REQUIREMENT_KEY.location()));
        LORE_REWARDS = event.create(RegistryBuilder.<RewardType<? extends LoreReward>>of(LoreKeys.LORE_REWARD_KEY.location()));
    }

    private void register(RegisterEvent event){
        if (event.getRegistryKey().equals(LoreKeys.LORE_REQUIREMENT_KEY)){
                ITEM_REQUIREMENT = new RequirementType<>(ItemRequirement.CODEC,buf->{
                    var item = buf.readById(BuiltInRegistries.ITEM);
                    var count = buf.readInt();
                    return new ItemRequirement(item,count);
                },(req,buf)->{
                    buf.writeId(BuiltInRegistries.ITEM,req.item());
                    buf.writeInt(req.count());
                });
                event.register(LoreKeys.LORE_REQUIREMENT_KEY,ResourceLocation.fromNamespaceAndPath(getName(),"item"),()->ITEM_REQUIREMENT);
        }
        if (event.getRegistryKey().equals(LoreKeys.LORE_REWARD_KEY)){
            ITEM_REWARD = new RewardType<>(ItemReward.CODEC,buf->{
                var item = buf.readById(BuiltInRegistries.ITEM);
                var count = buf.readInt();
                return new ItemReward(item,count);
            },(req,buf)->{
                buf.writeId(BuiltInRegistries.ITEM,req.item());
                buf.writeInt(req.count());
            });

            XP_REWARD = new RewardType<>(XPReward.CODEC,buf->{
                var amount = buf.readInt();
                var levels = buf.readBoolean();
                return new XPReward(amount,levels);
            },(req,buf)->{
                buf.writeInt(req.amount());
                buf.writeBoolean(req.levels());
            });
            event.register(LoreKeys.LORE_REWARD_KEY,ResourceLocation.fromNamespaceAndPath(getName(),"item"),()->ITEM_REWARD);
            event.register(LoreKeys.LORE_REWARD_KEY,ResourceLocation.fromNamespaceAndPath(getName(),"xp"),()->XP_REWARD);
        }
    }

    private void registerReloadListener(AddReloadListenerEvent event){
        event.addListener(new LoreFileReader(event.getRegistryAccess()));
    }

    private void playerLogin(PlayerEvent.PlayerLoggedInEvent event){
        if (event.getEntity() instanceof ServerPlayer sp){
            NET_INSTANCE.send(PacketDistributor.PLAYER.with(()->sp), new SyncLoreFilesPacket(files.values().stream().toList()));
            NET_INSTANCE.send(PacketDistributor.PLAYER.with(()->sp), new SyncLoreProgressPacket(LoreProgressManager.get().getProgress(sp)));
        }
    }

    private void addToCreativeTab(BuildCreativeModeTabContentsEvent event){
        if (event.getTabKey().equals(CreativeModeTabs.OP_BLOCKS) && event.hasPermissions())
            event.accept(LoreRegistries.LORE_BLOCK_ITEM);
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
                NET_INSTANCE.send(PacketDistributor.ALL.noArg(), new SyncLoreFilesPacket(files.values().stream().toList()));
            }
        }
    }


}
