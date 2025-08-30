package fr.iglee42.modpackutilities.modules.compressed;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import fr.iglee42.modpackutilities.utils.Module;
import net.neoforged.bus.api.IEventBus;

public class CompressedModule extends Module {
    public CompressedModule() {
        super("compressed", true);
    }

    @Override
    public void init(IEventBus modEventBus, IEventBus forgeEventBus) throws Exception {
        super.init(modEventBus, forgeEventBus);
        JsonObject config = getConfig();
        if (!config.has("maxCompressedTiers")){
            throw new JsonParseException("Missing maxCompressedTiers key in the compressed json");
        }
    }

    @Override
    protected JsonObject getDefaultConfig() {
        JsonObject obj = new JsonObject();
        obj.addProperty("maxCompressedTiers",9);
        return obj;
    }
}
