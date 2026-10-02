package com.reggarf.mods.zap_hosting_server_integration_menu.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.reggarf.mods.zap_hosting_server_integration_menu.ZapHosting;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ZHConfig {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public Common common = new Common();

    public static final class Common {
        // Affiliate & Promo
        public boolean enableOverlay = true;
        public String link = "https://zap-hosting.com/createletscreate";
        public String partnerId = "zap1204486";
        public String code = "REGGARF-1047";
        public int discountPercent = 20;

        // Public Multiplayer Server
        public boolean enablePublicServer = false;
        public String publicServerName = "ZAP-Hosting Official Server";
        public String publicServerIp = "play.zap-hosting.com";
        public String publicServerMotd = "Official Public Server hosted by ZAP-Hosting";
        public boolean enableCustomJoinScreen = false;

        public Common() {}

        public Common copy() {
            Common c = new Common();
            c.enableOverlay = this.enableOverlay;
            c.link = this.link;
            c.partnerId = this.partnerId;
            c.code = this.code;
            c.discountPercent = this.discountPercent;
            c.enablePublicServer = this.enablePublicServer;
            c.publicServerName = this.publicServerName;
            c.publicServerIp = this.publicServerIp;
            c.publicServerMotd = this.publicServerMotd;
            c.enableCustomJoinScreen = this.enableCustomJoinScreen;
            return c;
        }
    }

    public static Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get().resolve(ZapHosting.MOD_ID + ".json");
    }

    public static ZHConfig load() {
        Path path = getConfigPath();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                ZHConfig cfg = GSON.fromJson(reader, ZHConfig.class);
                if (cfg != null) {
                    if (cfg.common == null) {
                        cfg.common = new Common();
                    }
                    return cfg;
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load ZAP-Hosting config from {}: {}", path, e.getMessage());
            }
        }
        ZHConfig def = new ZHConfig();
        def.save();
        return def;
    }

    public void save() {
        Path path = getConfigPath();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save ZAP-Hosting config to {}: {}", path, e.getMessage());
        }
    }
}
