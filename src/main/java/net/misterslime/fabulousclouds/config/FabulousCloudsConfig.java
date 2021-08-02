package net.misterslime.fabulousclouds.config;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FabulousCloudsConfig {

    public boolean vibrant_clouds = true;
    public float vibrance_intensity = 1.0f;
    public boolean enable_default_cloud_layer = true;
    public CloudLayer[] cloud_layers = new CloudLayer[] {
            new CloudLayer(64, 1.25f, -1.0f)
    };
    public boolean noise_clouds = true;
    public boolean debug_noise_clouds = false;

    public static FabulousCloudsConfig load() throws IOException {
        Path configFile = FabricLoader.getInstance().getConfigDir().resolve("fabulous-clouds.json");
        Gson gson = new Gson();

        if (!Files.exists(configFile)) {
            save(new FabulousCloudsConfig());
        }

        return gson.fromJson(Files.newBufferedReader(configFile), FabulousCloudsConfig.class);
    }

    public static void save(FabulousCloudsConfig config) throws IOException {
        Path configFile = FabricLoader.getInstance().getConfigDir().resolve("fabulous-clouds.json");
        Gson gson = new Gson();
        JsonWriter writer = gson.newJsonWriter(Files.newBufferedWriter(configFile));

        writer.setIndent("    ");
        gson.toJson(gson.toJsonTree(config, FabulousCloudsConfig.class), writer);
        writer.close();
    }

    public static class CloudLayer {
        public float offset;
        public float scale;
        public float speed;

        public CloudLayer(float cloudOffset, float cloudScale, float cloudSpeed) {
            this.offset = cloudOffset;
            this.scale = cloudScale;
            this.speed = cloudSpeed;
        }
    }
}
