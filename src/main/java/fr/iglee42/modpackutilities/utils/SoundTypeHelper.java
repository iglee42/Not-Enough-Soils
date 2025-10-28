package fr.iglee42.modpackutilities.utils;

import com.mojang.logging.LogUtils;
import fr.iglee42.modpackutilities.IgleeModpackUtilities;
import net.minecraft.world.level.block.SoundType;

import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

//This class is inspired by the SoundTypeWrapper class from KubeJS
public class SoundTypeHelper {

    public static final SoundTypeHelper INSTANCE = new SoundTypeHelper();

    private Map<String, SoundType> types;

    public Map<String, SoundType> getTypes() {
        if (types == null){
            types = new LinkedHashMap<>();
            types.put("empty",SoundType.EMPTY);
            try {
                for (var field : SoundType.class.getFields()){
                    if (field.getType() == SoundType.class && Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())){
                        try {
                            types.put(field.getName().toLowerCase(Locale.ROOT), (SoundType) field.get(null));
                        } catch (Exception ex){
                            LogUtils.getLogger().error("Failed to access field {} to add it to sound types map", field.getName(),ex);
                        }
                    }
                }
            } catch (Exception ex){
                LogUtils.getLogger().error("Failed to populate sound types map",ex);
            }
        }
        return types;
    }
}
