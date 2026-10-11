package redstonedisorder.mendingtracker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class ConfigStoreTest {
    @TempDir Path directory;

    @Test void missingFileAndMissingFieldsUseOriginalDefaults() throws Exception {
        Path path = directory.resolve("mending-tracker.json");
        ConfigStore store = new ConfigStore(path);
        assertDefaults(store.load());
        Files.writeString(path, "{}");
        assertDefaults(store.load());
    }

    @Test void readsExistingConfigurationAndRoundTripsAllFields() throws Exception {
        Path path = directory.resolve("mending-tracker.json");
        Files.writeString(path, """
                {"xPos":37,"yPos":68,"textAlignment":"Right","overrideMinihud":true}
                """);
        ConfigStore store = new ConfigStore(path);
        ConfigOptions options = store.load();
        assertEquals(37, options.xPos);
        assertEquals(68, options.yPos);
        assertEquals(ConfigOptions.Alignment.Right, options.textAlignment);
        assertTrue(options.overrideMinihud);
        assertTrue(store.save(options));
        assertEquals(ConfigOptions.Alignment.Right, new ConfigStore(path).load().textAlignment);
        assertTrue(new ConfigStore(path).load().overrideMinihud);
        try (var files = Files.list(directory)) {
            assertEquals(1, files.count(), "successful saves must not leak temporary files");
        }
    }

    @Test void clampsCoordinatesAndDefaultsUnknownAlignment() throws Exception {
        Path path = directory.resolve("mending-tracker.json");
        Files.writeString(path, """
                {"xPos":-40,"yPos":400,"textAlignment":"Unknown"}
                """);
        ConfigOptions options = new ConfigStore(path).load();
        assertEquals(0, options.xPos);
        assertEquals(100, options.yPos);
        assertEquals(ConfigOptions.Alignment.Left, options.textAlignment);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "null", "{broken", "[]", "{\"xPos\":\"invalid\"}"})
    void malformedFilesRecoverWithoutLosingTheOriginal(String original) throws Exception {
        Path path = directory.resolve("mending-tracker.json");
        Files.writeString(path, original);
        ConfigStore store = new ConfigStore(path);
        assertDefaults(store.load());
        assertEquals(original, Files.readString(path));
        assertTrue(store.save(new ConfigOptions()));
        try (var files = Files.list(directory)) {
            Path backup = files.filter(file -> file.getFileName().toString().endsWith(".bak")).findFirst().orElseThrow();
            assertEquals(original, Files.readString(backup));
        }
        assertDefaults(new ConfigStore(path).load());
    }

    @Test void createsMissingDirectories() {
        ConfigStore store = new ConfigStore(directory.resolve("nested/config/mending-tracker.json"));
        assertTrue(store.save(new ConfigOptions()));
        assertDefaults(store.load());
    }

    @Test void failedSaveDoesNotReplaceExistingContents() throws Exception {
        Path parentFile = directory.resolve("not-a-directory");
        Files.writeString(parentFile, "preserve me");
        assertFalse(new ConfigStore(parentFile.resolve("config.json")).save(new ConfigOptions()));
        assertEquals("preserve me", Files.readString(parentFile));
    }

    private static void assertDefaults(ConfigOptions options) {
        assertEquals(1, options.xPos);
        assertEquals(2, options.yPos);
        assertEquals(ConfigOptions.Alignment.Left, options.textAlignment);
        assertFalse(options.overrideMinihud);
    }
}