package dev.insilicon.cookingX.Utils;

import dev.insilicon.cookingX.CookingX;

public class ConfigUtil {

    //FUCK THE COMPILER I FUCKING HATE IT
    public static String getValueFromConfig(String location) {
        return CookingX.instance.getConfig().getString(location);
    }
}
