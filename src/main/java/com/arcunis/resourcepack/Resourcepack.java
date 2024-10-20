package com.arcunis.resourcepack;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Resourcepack implements Listener {

    public final Main plugin;

    public Resourcepack(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String owner = plugin.getConfig().getString("owner");
        String repo = plugin.getConfig().getString("repo");
        String fileName = plugin.getConfig().getString("file");

        try {
            String latestReleaseJson = fetchLatestRelease(owner, repo);
            String downloadUrl = extractDownloadUrl(latestReleaseJson, fileName);
            player.setResourcePack(downloadUrl); // Using deprecated function becouse the one with the hash entry does not work
        } catch (Exception e) {
            throw new RuntimeException("Error fetching resource pack: " + e.getMessage());
        }
    }

    private String fetchLatestRelease(String owner, String repo) throws Exception {
        String apiUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/releases/latest";
        HttpURLConnection connection = (HttpURLConnection) new URI(apiUrl).toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "PaperMCPlugin");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    private String extractDownloadUrl(String json, String fileName) throws Exception {
        // Deserialize JSON to GitHubRelease object
        GitHubRelease release = new Gson().fromJson(json, GitHubRelease.class);

        // Check the assets for the file name
        for (Asset asset : release.getAssets()) {
            if (asset.getName().equalsIgnoreCase(fileName)) {
                return asset.getBrowserDownloadUrl(); // Return the URL
            }
        }

        throw new Exception("File not found in the latest release: " + fileName);
    }

    private static class GitHubRelease {
        @SerializedName("assets")
        private List<Asset> assets;

        public List<Asset> getAssets() {
            return assets;
        }
    }

    private static class Asset {
        @SerializedName("name")
        private String name;

        @SerializedName("browser_download_url")
        private String browserDownloadUrl;

        public String getName() {
            return name;
        }

        public String getBrowserDownloadUrl() {
            return browserDownloadUrl;
        }
    }

}
