package redstonedisorder.mendingtracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** File persistence is independent of Minecraft so failures can be tested without a client. */
final class ConfigStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LoggerFactory.getLogger("mending-tracker");
    private final Path path;
    private boolean malformed;

    ConfigStore(Path path) { this.path = path.toAbsolutePath(); }

    ConfigOptions load() {
        malformed = false;
        if (!Files.exists(path)) return new ConfigOptions();
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            ConfigOptions options = GSON.fromJson(reader, ConfigOptions.class);
            if (options == null) throw new JsonParseException("Empty or null configuration");
            return options.normalized();
        } catch (JsonParseException | IllegalStateException e) {
            malformed = true;
            LOGGER.warn("Invalid Mending Tracker configuration at {}; using defaults", path, e);
        } catch (IOException e) {
            LOGGER.warn("Cannot read Mending Tracker configuration at {}; using defaults", path, e);
        }
        return new ConfigOptions();
    }

    boolean save(ConfigOptions options) {
        Path temporary = null;
        try {
            Files.createDirectories(path.getParent());
            temporary = Files.createTempFile(path.getParent(), "mending-tracker-", ".tmp");
            Files.writeString(temporary, GSON.toJson(options.normalized()) + "\n", StandardCharsets.UTF_8);
            if (malformed && Files.exists(path)) {
                Path backup = path.resolveSibling(path.getFileName() + ".invalid-" + System.currentTimeMillis() + ".bak");
                Files.copy(path, backup);
            }
            try {
                Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
            malformed = false;
            return true;
        } catch (IOException e) {
            LOGGER.warn("Cannot save Mending Tracker configuration at {}", path, e);
            return false;
        } finally {
            if (temporary != null) {
                try { Files.deleteIfExists(temporary); }
                catch (IOException e) { LOGGER.warn("Cannot remove temporary configuration {}", temporary, e); }
            }
        }
    }
}