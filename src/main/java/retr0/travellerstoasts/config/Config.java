package retr0.travellerstoasts.config;

import retr0.carrotconfig.config.CarrotConfig;

public class Config extends CarrotConfig {
    @Entry
    public static boolean roundedIconBackground = false;

    @Entry(min = 0)
    public static float maxInhabitedTime = 120.0f;

    @Entry(min = 0)
    public static float toastCooldownTime = 30.0f;
}
