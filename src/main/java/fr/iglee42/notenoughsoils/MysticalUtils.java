package fr.iglee42.notenoughsoils;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;

public class MysticalUtils {
    public static void reloadCropsSoils() {
        MysticalAgricultureAPI.getCropRegistry().getCrops().forEach(c->{
            if (c instanceof CropWithSoils casted){
                if (casted.snes$getCustomSoils() != null && !casted.snes$getCustomSoils().isEmpty())NotEnoughSoils.SOILS.put(c.getCropBlock(),casted.snes$getCustomSoils());
            }
        });
    }
}
