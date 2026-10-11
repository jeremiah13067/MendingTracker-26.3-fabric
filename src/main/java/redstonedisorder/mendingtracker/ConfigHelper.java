package redstonedisorder.mendingtracker;

import net.fabricmc.loader.api.FabricLoader;

public final class ConfigHelper {
    private static final ConfigStore STORE = new ConfigStore(
            FabricLoader.getInstance().getConfigDir().resolve("mending-tracker.json"));
    public static ConfigOptions configOptions = new ConfigOptions();

    private ConfigHelper() {}

    public static void getConfigOptions() { configOptions = STORE.load(); }

    public static boolean saveConfig(ConfigOptions options) {
        if (!STORE.save(options)) return false;
        configOptions = options.normalized();
        return true;
    }
}