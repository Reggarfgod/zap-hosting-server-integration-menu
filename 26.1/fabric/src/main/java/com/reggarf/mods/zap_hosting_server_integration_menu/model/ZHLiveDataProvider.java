package com.reggarf.mods.zap_hosting_server_integration_menu.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZHLiveDataProvider {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36";

    public record LauncherEntry(String key, String displayName, String fromPrice, String iconUrl, String defaultProductPath) {}
    public record GameItem(String optionId, String displayName, String pgid, String group) {}
    public record LocationEntry(String siteId, String name, String region, String countryCode, String protectionType, String pingtestUrl, boolean ownIpDisabled) {}
    public record OptionStep(String id, String label, int numericValue, double extraPrice) {
        public OptionStep(String id, String label, int numericValue) {
            this(id, label, numericValue, 0.0);
        }
    }

    // Dynamic Cached Lists (Gameservers)
    private static final List<LauncherEntry> launchers = new ArrayList<>();
    private static final Map<String, List<GameItem>> gamesByLauncher = new ConcurrentHashMap<>();
    private static final Set<String> webFetchedLaunchers = ConcurrentHashMap.newKeySet();
    private static final List<LocationEntry> locations = new ArrayList<>();
    private static final List<OptionStep> slotSteps = new ArrayList<>();
    private static final List<OptionStep> ramSteps = new ArrayList<>();
    private static final List<OptionStep> diskSteps = new ArrayList<>();
    private static final List<OptionStep> billingSteps = new ArrayList<>();

    // Dynamic Cached Lists (VPS from https://legacy.zap-hosting.com/en/shop/product/vps/)
    private static final List<OptionStep> vpsCpuSteps = new ArrayList<>();
    private static final List<OptionStep> vpsRamSteps = new ArrayList<>();
    private static final List<OptionStep> vpsDiskSteps = new ArrayList<>();
    private static final List<OptionStep> vpsIpSteps = new ArrayList<>();
    private static final List<OptionStep> vpsBillingSteps = new ArrayList<>();

    private static boolean isInitialized = false;
    private static CompletableFuture<Void> initFuture = null;
    public static boolean isFullyPreloaded = false;

    static {
        loadDefaults();
    }

    public static synchronized CompletableFuture<Void> ensureLoaded() {
        if (initFuture != null) {
            return initFuture;
        }

        initFuture = CompletableFuture.runAsync(() -> {
            try {
                loadFromWeb();
                loadVpsFromWeb();
                isInitialized = true;
            } catch (Exception e) {
                e.printStackTrace();
                isInitialized = true;
            }
        });
        return initFuture;
    }

    public static CompletableFuture<Void> preloadAll(Consumer<String> statusUpdate, Consumer<Float> progressConsumer) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (statusUpdate != null) statusUpdate.accept("Connecting to ZAP-Hosting...");
                if (progressConsumer != null) progressConsumer.accept(0.15f);

                // 1. Fetch web config (locations, launchers, steps, and VPS config)
                ensureLoaded().join();
                if (progressConsumer != null) progressConsumer.accept(0.40f);

                // 2. Fetch game catalogues for all modpack launchers in parallel
                if (statusUpdate != null) statusUpdate.accept("Caching games and modpacks...");
                List<String> toFetch = List.of("curse-twitch", "minecraft", "feed-the-beast", "at-launcher", "technic-launcher", "voids-wrath-launcher", "minecraft-adventure");

                AtomicInteger count = new AtomicInteger(0);
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                for (String key : toFetch) {
                    futures.add(CompletableFuture.runAsync(() -> {
                        fetchGamesForLauncher(key);
                        int done = count.incrementAndGet();
                        if (progressConsumer != null) {
                            progressConsumer.accept(0.40f + 0.55f * ((float) done / toFetch.size()));
                        }
                    }));
                }

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

                if (statusUpdate != null) statusUpdate.accept("Ready!");
                if (progressConsumer != null) progressConsumer.accept(1.0f);
                isFullyPreloaded = true;
            } catch (Exception e) {
                e.printStackTrace();
                isFullyPreloaded = true;
                if (progressConsumer != null) progressConsumer.accept(1.0f);
            }
        });
    }

    public static List<LauncherEntry> getLaunchers() {
        if (!isInitialized && initFuture == null) ensureLoaded();
        return new ArrayList<>(launchers);
    }

    public static List<GameItem> getGames(String launcherKey) {
        List<GameItem> list = gamesByLauncher.get(launcherKey);
        if (list == null || list.isEmpty()) {
            CompletableFuture.runAsync(() -> fetchGamesForLauncher(launcherKey));
            list = gamesByLauncher.getOrDefault(launcherKey, List.of(new GameItem("default", "Standard " + launcherKey, "1", launcherKey)));
        }
        return new ArrayList<>(list);
    }

    public static List<LocationEntry> getLocations() {
        if (!isInitialized && initFuture == null) ensureLoaded();
        return new ArrayList<>(locations);
    }

    public static List<OptionStep> getSlotSteps() {
        if (!isInitialized && initFuture == null) ensureLoaded();
        return new ArrayList<>(slotSteps);
    }

    public static List<OptionStep> getRamSteps() {
        if (!isInitialized && initFuture == null) ensureLoaded();
        return new ArrayList<>(ramSteps);
    }

    public static List<OptionStep> getDiskSteps() {
        if (!isInitialized && initFuture == null) ensureLoaded();
        return new ArrayList<>(diskSteps);
    }

    public static List<OptionStep> getBillingSteps() {
        if (!isInitialized && initFuture == null) ensureLoaded();
        return new ArrayList<>(billingSteps);
    }

    public static List<OptionStep> getVpsCpuSteps() {
        if (!isInitialized && initFuture == null) ensureLoaded();
        return new ArrayList<>(vpsCpuSteps);
    }

    public static List<OptionStep> getVpsRamSteps() {
        if (!isInitialized && initFuture == null) ensureLoaded();
        return new ArrayList<>(vpsRamSteps);
    }

    public static List<OptionStep> getVpsDiskSteps() {
        if (!isInitialized && initFuture == null) ensureLoaded();
        return new ArrayList<>(vpsDiskSteps);
    }

    public static List<OptionStep> getVpsIpSteps() {
        if (!isInitialized && initFuture == null) ensureLoaded();
        return new ArrayList<>(vpsIpSteps);
    }

    public static List<OptionStep> getVpsBillingSteps() {
        if (!isInitialized && initFuture == null) ensureLoaded();
        return new ArrayList<>(vpsBillingSteps);
    }

    private static void loadFromWeb() {
        try {
            CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
            CookieHandler.setDefault(cookieManager);

            String url = "https://legacy.zap-hosting.com/en/shop/product/cloud-gameserver/curse-twitch/";
            HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(6000);

            StringBuilder htmlBuilder = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    htmlBuilder.append(line).append("\n");
                }
            }
            conn.disconnect();
            String html = htmlBuilder.toString();

            // 1. Parse Locations from :site-categories='[...]'
            parseLocations(html);

            // 2. Parse Launcher entries
            parseLaunchers(html);

            // 3. Parse Curse/Twitch Games
            parseGamesFromSelect(html, "curse-twitch");

            // 4. Parse Slots, RAM, Disk, Billing Steps
            parseOptions(html);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadVpsFromWeb() {
        try {
            String url = "https://legacy.zap-hosting.com/en/shop/product/vps/";
            HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(6000);

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
            conn.disconnect();
            String html = sb.toString();

            parseVpsOptions(html);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void parseVpsOptions(String html) {
        try {
            // 1. CPU Cores: sel_field[180]
            Pattern cpuPat = Pattern.compile("<select[^>]+name=['\"]sel_field\\[180\\]['\"][^>]*>(.*?)</select>", Pattern.DOTALL);
            Matcher cpuMat = cpuPat.matcher(html);
            if (cpuMat.find()) {
                Pattern optPat = Pattern.compile("<option[^>]+value=['\"]([^'\"]*)['\"][^>]*data-base-text=['\"]([^'\"]*)['\"][^>]*>", Pattern.DOTALL);
                Matcher optMat = optPat.matcher(cpuMat.group(1));
                List<OptionStep> list = new ArrayList<>();
                while (optMat.find()) {
                    String id = optMat.group(1);
                    String raw = optMat.group(2).replaceAll("<[^>]+>", "").trim();
                    int cores = 4;
                    Matcher m = Pattern.compile("(\\d+)\\s*Cores?").matcher(raw);
                    if (m.find()) cores = Integer.parseInt(m.group(1));
                    double price = 0.0;
                    Matcher pm = Pattern.compile("\\+\\s*\\$?([0-9.]+)").matcher(raw);
                    if (pm.find()) price = Double.parseDouble(pm.group(1));
                    list.add(new OptionStep(id, cores + " Cores", cores, price));
                }
                if (!list.isEmpty()) {
                    synchronized (vpsCpuSteps) {
                        vpsCpuSteps.clear();
                        vpsCpuSteps.addAll(list);
                    }
                }
            }

            // 2. RAM: sel_field[171]
            Pattern ramPat = Pattern.compile("<select[^>]+name=['\"]sel_field\\[171\\]['\"][^>]*>(.*?)</select>", Pattern.DOTALL);
            Matcher ramMat = ramPat.matcher(html);
            if (ramMat.find()) {
                Pattern optPat = Pattern.compile("<option[^>]+value=['\"]([^'\"]*)['\"][^>]*data-base-text=['\"]([^'\"]*)['\"][^>]*>", Pattern.DOTALL);
                Matcher optMat = optPat.matcher(ramMat.group(1));
                List<OptionStep> list = new ArrayList<>();
                while (optMat.find()) {
                    String id = optMat.group(1);
                    String raw = optMat.group(2).replaceAll("<[^>]+>", "").trim();
                    int gb = 4;
                    Matcher m = Pattern.compile("(\\d+)\\s*GB").matcher(raw);
                    if (m.find()) gb = Integer.parseInt(m.group(1));
                    double price = 0.0;
                    Matcher pm = Pattern.compile("\\+\\s*\\$?([0-9.]+)").matcher(raw);
                    if (pm.find()) price = Double.parseDouble(pm.group(1));
                    list.add(new OptionStep(id, gb + " GB", gb, price));
                }
                if (!list.isEmpty()) {
                    synchronized (vpsRamSteps) {
                        vpsRamSteps.clear();
                        vpsRamSteps.addAll(list);
                    }
                }
            }

            // 3. Storage (NVMe SSD): sel_field[179]
            Pattern diskPat = Pattern.compile("<select[^>]+name=['\"]sel_field\\[179\\]['\"][^>]*>(.*?)</select>", Pattern.DOTALL);
            Matcher diskMat = diskPat.matcher(html);
            if (diskMat.find()) {
                Pattern optPat = Pattern.compile("<option[^>]+value=['\"]([^'\"]*)['\"][^>]*data-base-text=['\"]([^'\"]*)['\"][^>]*>", Pattern.DOTALL);
                Matcher optMat = optPat.matcher(diskMat.group(1));
                List<OptionStep> list = new ArrayList<>();
                while (optMat.find()) {
                    String id = optMat.group(1);
                    String raw = optMat.group(2).replaceAll("<[^>]+>", "").trim();
                    int gb = 25;
                    Matcher m = Pattern.compile("(\\d+)\\s*GB").matcher(raw);
                    if (m.find()) gb = Integer.parseInt(m.group(1));
                    double price = 0.0;
                    Matcher pm = Pattern.compile("\\+\\s*\\$?([0-9.]+)").matcher(raw);
                    if (pm.find()) price = Double.parseDouble(pm.group(1));
                    list.add(new OptionStep(id, gb + " GB", gb, price));
                }
                if (!list.isEmpty()) {
                    synchronized (vpsDiskSteps) {
                        vpsDiskSteps.clear();
                        vpsDiskSteps.addAll(list);
                    }
                }
            }

            // 4. Dedicated IPv4 Addresses: sel_field[13]
            Pattern ipPat = Pattern.compile("<select[^>]+name=['\"]sel_field\\[13\\]['\"][^>]*>(.*?)</select>", Pattern.DOTALL);
            Matcher ipMat = ipPat.matcher(html);
            if (ipMat.find()) {
                Pattern optPat = Pattern.compile("<option[^>]+value=['\"]([^'\"]*)['\"][^>]*data-base-text=['\"]([^'\"]*)['\"][^>]*>", Pattern.DOTALL);
                Matcher optMat = optPat.matcher(ipMat.group(1));
                List<OptionStep> list = new ArrayList<>();
                while (optMat.find()) {
                    String id = optMat.group(1);
                    String raw = optMat.group(2).replaceAll("<[^>]+>", "").trim();
                    int ips = 1;
                    Matcher m = Pattern.compile("(\\d+)\\s*IP").matcher(raw);
                    if (m.find()) ips = Integer.parseInt(m.group(1));
                    double price = 0.0;
                    Matcher pm = Pattern.compile("\\+\\s*\\$?([0-9.]+)").matcher(raw);
                    if (pm.find()) price = Double.parseDouble(pm.group(1));
                    String label = ips == 1 ? "1 IP (Included)" : ips + " IPs (+$" + String.format("%.2f", price) + ")";
                    list.add(new OptionStep(id, label, ips, price));
                }
                if (!list.isEmpty()) {
                    synchronized (vpsIpSteps) {
                        vpsIpSteps.clear();
                        vpsIpSteps.addAll(list);
                    }
                }
            }

            // 5. Billing Interval: sel_field[229]
            Pattern billPat = Pattern.compile("<select[^>]+name=['\"]sel_field\\[229\\]['\"][^>]*>(.*?)</select>", Pattern.DOTALL);
            Matcher billMat = billPat.matcher(html);
            if (billMat.find()) {
                Pattern optPat = Pattern.compile("<option[^>]+value=['\"]([^'\"]*)['\"][^>]*>(.*?)</option>", Pattern.DOTALL);
                Matcher optMat = optPat.matcher(billMat.group(1));
                List<OptionStep> list = new ArrayList<>();
                while (optMat.find()) {
                    String id = optMat.group(1);
                    String raw = optMat.group(2).replaceAll("<[^>]+>", "").trim();
                    int days = raw.contains("1800") ? 1800 : raw.contains("720") ? 720 : raw.contains("360") ? 360 : raw.contains("180") ? 180 : raw.contains("90") ? 90 : 30;
                    list.add(new OptionStep(id, raw, days));
                }
                if (!list.isEmpty()) {
                    synchronized (vpsBillingSteps) {
                        vpsBillingSteps.clear();
                        vpsBillingSteps.addAll(list);
                    }
                }
            }

            // 6. Operating System Templates: data-template-solus-id
            Pattern osPat = Pattern.compile("<li[^>]+class=['\"][^'\"]*configuration-module-highlight-box-list-entry[^'\"]*['\"][^>]+data-template-solus-id=['\"]([^'\"]+)['\"][^>]*>(.*?)</li>", Pattern.DOTALL);
            Matcher osMat = osPat.matcher(html);
            List<GameItem> osItems = new ArrayList<>();
            while (osMat.find()) {
                String solusId = osMat.group(1).trim();
                String raw = osMat.group(2).replaceAll("<[^>]+>", "").trim();
                String name = raw.replaceAll("\\s+", " ");
                if (!solusId.isEmpty() && !name.isEmpty()) {
                    osItems.add(new GameItem(solusId, name, solusId, "vps"));
                }
            }
            if (!osItems.isEmpty()) {
                gamesByLauncher.put("vps", osItems);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void fetchGamesForLauncher(String launcherKey) {
        if (webFetchedLaunchers.contains(launcherKey)) {
            return;
        }
        if ("vps".equals(launcherKey)) {
            loadVpsFromWeb();
            webFetchedLaunchers.add(launcherKey);
            return;
        }
        if ("dedicated-server".equals(launcherKey)) {
            webFetchedLaunchers.add(launcherKey);
            return;
        }

        try {
            String url = "https://legacy.zap-hosting.com/en/shop/product/cloud-gameserver/" + launcherKey + "/";
            HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
            conn.disconnect();
            parseGamesFromSelect(sb.toString(), launcherKey);
            webFetchedLaunchers.add(launcherKey);
        } catch (Exception e) {
            webFetchedLaunchers.add(launcherKey);
        }
    }

    private static void parseLocations(String html) {
        Pattern pattern = Pattern.compile(":site-categories=['\"](\\[.*?\\])['\"]", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            try {
                JsonArray categories = JsonParser.parseString(matcher.group(1)).getAsJsonArray();
                List<LocationEntry> parsedLocs = new ArrayList<>();
                for (JsonElement catEl : categories) {
                    JsonObject cat = catEl.getAsJsonObject();
                    String regionName = cat.get("display_name").getAsString().toUpperCase();
                    if (cat.has("sites")) {
                        JsonArray sites = cat.getAsJsonArray("sites");
                        for (JsonElement siteEl : sites) {
                            JsonObject s = siteEl.getAsJsonObject();
                            String siteId = String.valueOf(s.get("site_id").getAsInt());
                            String name = s.get("site_displayname").getAsString();
                            String country = siteId.equals("2") ? "DE" : siteId.equals("7") ? "UK" : siteId.equals("5") ? "AU" : siteId.equals("4") ? "SG" : "US";
                            String prot = (siteId.equals("7") || siteId.equals("5") || siteId.equals("4") || siteId.equals("3")) ? "OVH" : "PletX";
                            String pingUrl = s.has("site_pingtest_url") && !s.get("site_pingtest_url").isJsonNull() ? s.get("site_pingtest_url").getAsString() : "";
                            boolean ownIpDisabled = s.has("site_own_ip_disabled") && s.get("site_own_ip_disabled").getAsBoolean();
                            parsedLocs.add(new LocationEntry(siteId, name, regionName, country, prot, pingUrl, ownIpDisabled));
                        }
                    }
                }
                if (!parsedLocs.isEmpty()) {
                    synchronized (locations) {
                        locations.clear();
                        locations.addAll(parsedLocs);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void parseGamesFromSelect(String html, String launcherKey) {
        Pattern selPattern = Pattern.compile("<select[^>]+name=['\"]game-selection['\"][^>]*>(.*?)</select>", Pattern.DOTALL);
        Matcher selMatcher = selPattern.matcher(html);
        if (selMatcher.find()) {
            String content = selMatcher.group(1);
            Pattern optPattern = Pattern.compile("<option[^>]+value=['\"]([^'\"]*)['\"][^>]*>(.*?)</option>", Pattern.DOTALL);
            Matcher optMatcher = optPattern.matcher(content);
            List<GameItem> items = new ArrayList<>();
            while (optMatcher.find()) {
                String val = optMatcher.group(1).trim();
                String rawText = optMatcher.group(2).replaceAll("<[^>]+>", "").trim();
                String name = rawText.replaceAll("\\s+", " ");
                if (!val.isEmpty()) {
                    items.add(new GameItem(val, name, val, launcherKey));
                }
            }
            if (!items.isEmpty()) {
                gamesByLauncher.put(launcherKey, items);
            }
        }
    }

    private static void parseLaunchers(String html) {
        List<LauncherEntry> list = new ArrayList<>();
        list.add(new LauncherEntry("curse-twitch", "Curse / Twitch", "from $3.15", "", "curse-twitch"));
        list.add(new LauncherEntry("vps", "VPS", "from $8.78", "", "vps"));
        list.add(new LauncherEntry("dedicated-server", "Dedicated Server", "from $67.15", "", "dedicated-server"));
        list.add(new LauncherEntry("feed-the-beast", "Feed the Beast", "from $3.15", "", "feed-the-beast"));
        list.add(new LauncherEntry("minecraft", "Minecraft", "from $3.15", "", "minecraft"));
        list.add(new LauncherEntry("at-launcher", "AT Launcher", "from $3.15", "", "at-launcher"));
        list.add(new LauncherEntry("technic-launcher", "Technic Launcher", "from $3.15", "", "technic-launcher"));
        list.add(new LauncherEntry("voids-wrath-launcher", "Voids Wrath Launcher", "from $3.15", "", "voids-wrath-launcher"));
        list.add(new LauncherEntry("minecraft-adventure", "Minecraft Adventure", "from $3.15", "", "minecraft-adventure"));

        synchronized (launchers) {
            launchers.clear();
            launchers.addAll(list);
        }
    }

    private static void parseOptions(String html) {
        // Slot values
        Pattern slotPat = Pattern.compile("var\\s+values\\s*=\\s*\\[([0-9,\\s]+)\\];");
        Matcher slotMat = slotPat.matcher(html);
        if (slotMat.find()) {
            String[] nums = slotMat.group(1).split(",");
            List<OptionStep> parsedSlots = new ArrayList<>();
            for (String n : nums) {
                int val = Integer.parseInt(n.trim());
                parsedSlots.add(new OptionStep(String.valueOf(val), val + " Slots", val));
            }
            if (!parsedSlots.isEmpty()) {
                synchronized (slotSteps) {
                    slotSteps.clear();
                    slotSteps.addAll(parsedSlots);
                }
            }
        }

        // RAM options: sel_field[4]
        Pattern ramPat = Pattern.compile("<select[^>]+name=['\"]sel_field\\[4\\]['\"][^>]*>(.*?)</select>", Pattern.DOTALL);
        Matcher ramMat = ramPat.matcher(html);
        if (ramMat.find()) {
            Pattern optPat = Pattern.compile("<option[^>]+value=['\"]([^'\"]*)['\"][^>]*>(.*?)</option>", Pattern.DOTALL);
            Matcher optMat = optPat.matcher(ramMat.group(1));
            List<OptionStep> parsedRam = new ArrayList<>();
            while (optMat.find()) {
                String id = optMat.group(1);
                String rawText = optMat.group(2).replaceAll("<[^>]+>", "").trim();
                String label = rawText.replaceAll("\\s+", " ");
                int num = 0;
                Matcher nm = Pattern.compile("(\\d+)\\s*GB").matcher(label);
                if (nm.find()) {
                    num = Integer.parseInt(nm.group(1));
                }
                parsedRam.add(new OptionStep(id, num + " GB RAM", num));
            }
            if (parsedRam.size() > 1) {
                synchronized (ramSteps) {
                    ramSteps.clear();
                    ramSteps.addAll(parsedRam);
                }
            }
        }

        // Disk options: sel_field[255]
        Pattern diskPat = Pattern.compile("<select[^>]+name=['\"]sel_field\\[255\\]['\"][^>]*>(.*?)</select>", Pattern.DOTALL);
        Matcher diskMat = diskPat.matcher(html);
        if (diskMat.find()) {
            Pattern optPat = Pattern.compile("<option[^>]+value=['\"]([^'\"]*)['\"][^>]*>(.*?)</option>", Pattern.DOTALL);
            Matcher optMat = optPat.matcher(diskMat.group(1));
            List<OptionStep> parsedDisk = new ArrayList<>();
            while (optMat.find()) {
                String id = optMat.group(1);
                String rawText = optMat.group(2).replaceAll("<[^>]+>", "").trim();
                String label = rawText.replaceAll("\\s+", " ");
                int num = 0;
                Matcher nm = Pattern.compile("(\\d+)\\s*(?:GB|gigabyte)", Pattern.CASE_INSENSITIVE).matcher(label);
                if (nm.find()) {
                    num = Integer.parseInt(nm.group(1));
                }
                parsedDisk.add(new OptionStep(id, num + " Gigabyte", num));
            }
            if (parsedDisk.size() > 1) {
                synchronized (diskSteps) {
                    diskSteps.clear();
                    diskSteps.addAll(parsedDisk);
                }
            }
        }

        // Billing options: sel_field[6]
        Pattern billPat = Pattern.compile("<select[^>]+name=['\"]sel_field\\[6\\]['\"][^>]*>(.*?)</select>", Pattern.DOTALL);
        Matcher billMat = billPat.matcher(html);
        if (billMat.find()) {
            Pattern optPat = Pattern.compile("<option[^>]+value=['\"]([^'\"]*)['\"][^>]*>(.*?)</option>", Pattern.DOTALL);
            Matcher optMat = optPat.matcher(billMat.group(1));
            List<OptionStep> parsedBill = new ArrayList<>();
            while (optMat.find()) {
                String id = optMat.group(1);
                String rawText = optMat.group(2).replaceAll("<[^>]+>", "").trim();
                String label = rawText.replaceAll("\\s+", " ");
                int days = label.contains("1800") ? 1800 : label.contains("720") ? 720 : label.contains("360") ? 360 : label.contains("180") ? 180 : label.contains("90") ? 90 : 30;
                parsedBill.add(new OptionStep(id, label, days));
            }
            if (parsedBill.size() > 1) {
                synchronized (billingSteps) {
                    billingSteps.clear();
                    billingSteps.addAll(parsedBill);
                }
            }
        }
    }

    private static void loadDefaults() {
        if (launchers.isEmpty()) {
            parseLaunchers("");
        }

        if (gamesByLauncher.isEmpty()) {
            gamesByLauncher.put("curse-twitch", List.of(
                    new GameItem("1", "All the Mods 9", "1", "curse-twitch"),
                    new GameItem("2", "Better MC [FORGE] - BMC4", "2", "curse-twitch"),
                    new GameItem("3", "RLCraft", "3", "curse-twitch"),
                    new GameItem("4", "Prominence II RPG", "4", "curse-twitch"),
                    new GameItem("5", "DawnCraft - An Adventure RPG", "5", "curse-twitch"),
                    new GameItem("6", "Pixelmon Modpack", "6", "curse-twitch"),
                    new GameItem("7", "Vault Hunters 3rd Edition", "7", "curse-twitch"),
                    new GameItem("8", "Create: Astral", "8", "curse-twitch")
            ));
            gamesByLauncher.put("minecraft", List.of(
                    new GameItem("10", "Vanilla Minecraft (Latest)", "10", "minecraft"),
                    new GameItem("11", "PaperMC / Spigot (High Performance)", "11", "minecraft"),
                    new GameItem("12", "NeoForge Modded Server", "12", "minecraft"),
                    new GameItem("13", "Forge Modded Server", "13", "minecraft"),
                    new GameItem("14", "Fabric Modded Server", "14", "minecraft"),
                    new GameItem("15", "Purpur Server", "15", "minecraft"),
                    new GameItem("16", "BungeeCord Proxy Server", "16", "minecraft")
            ));
            gamesByLauncher.put("vps", List.of(
                    new GameItem("777700065", "Ubuntu 24.04 EN - 64bit", "777700065", "vps"),
                    new GameItem("777700051", "Debian 12 EN - 64bit", "777700051", "vps"),
                    new GameItem("777700053", "Debian 11 EN - 64bit", "777700053", "vps"),
                    new GameItem("777700002", "WindowsServer 2022 - EN - EVAL", "777700002", "vps"),
                    new GameItem("777700006", "WindowsServer 2025 - EN - EVAL", "777700006", "vps"),
                    new GameItem("777700061", "Ubuntu 20.04 EN - 64bit", "777700061", "vps"),
                    new GameItem("777700063", "Ubuntu 22.04 EN - 64bit", "777700063", "vps")
            ));
            gamesByLauncher.put("dedicated-server", List.of(
                    new GameItem("i9-9900k", "Intel Core i9-9900K (8C/16T @ 5.0 GHz | 64GB | 2x 1TB NVMe) - $67.15/mo", "1", "dedicated-server"),
                    new GameItem("ryzen-3700x", "AMD Ryzen 7 3700X (8C/16T @ 4.4 GHz | 64GB | 2x 1TB NVMe) - $79.00/mo", "2", "dedicated-server"),
                    new GameItem("xeon-e2276g", "Intel Xeon E-2276G (6C/12T @ 4.9 GHz | 32GB | 2x 500GB NVMe) - $89.00/mo", "3", "dedicated-server"),
                    new GameItem("ryzen-5950x", "AMD Ryzen 9 5950X (16C/32T @ 4.9 GHz | 128GB ECC | 2x 2TB NVMe) - $119.00/mo", "4", "dedicated-server"),
                    new GameItem("xeon-silver", "Dual Intel Xeon Silver 4210R (20C/40T @ 3.4 GHz | 128GB | 2x 2TB SSD) - $149.00/mo", "5", "dedicated-server"),
                    new GameItem("epyc-7502p", "AMD EPYC 7502P (32C/64T @ 3.35 GHz | 128GB ECC | 2x 2TB NVMe) - $189.00/mo", "6", "dedicated-server"),
                    new GameItem("epyc-7702", "AMD EPYC 7702 (64C/128T @ 3.35 GHz | 256GB ECC | 2x 4TB NVMe) - $249.00/mo", "7", "dedicated-server"),
                    new GameItem("xeon-gold", "Dual Intel Xeon Gold 6230 (40C/80T @ 3.9 GHz | 256GB ECC | 2x 4TB SSD) - $299.00/mo", "8", "dedicated-server"),
                    new GameItem("epyc-7742", "Dual AMD EPYC 7742 (128C/256T @ 3.4 GHz | 512GB ECC | 4x 4TB NVMe) - $499.00/mo", "9", "dedicated-server")
            ));
            gamesByLauncher.put("feed-the-beast", List.of(
                    new GameItem("ftb-1", "FTB Skies", "1", "feed-the-beast"),
                    new GameItem("ftb-2", "FTB Presents Direwolf20 1.20", "2", "feed-the-beast"),
                    new GameItem("ftb-3", "FTB Genesis", "3", "feed-the-beast"),
                    new GameItem("ftb-4", "FTB Stoneblock 3", "4", "feed-the-beast"),
                    new GameItem("ftb-5", "FTB OceanBlock", "5", "feed-the-beast"),
                    new GameItem("ftb-6", "FTB Revelation", "6", "feed-the-beast")
            ));
            gamesByLauncher.put("at-launcher", List.of(
                    new GameItem("at-1", "SevTech: Ages", "1", "at-launcher"),
                    new GameItem("at-2", "Crucial 2", "2", "at-launcher"),
                    new GameItem("at-3", "The Lost Era Modpack", "3", "at-launcher"),
                    new GameItem("at-4", "Caelum", "4", "at-launcher")
            ));
            gamesByLauncher.put("technic-launcher", List.of(
                    new GameItem("tech-1", "The 1.12.2 Pack", "1", "technic-launcher"),
                    new GameItem("tech-2", "Tekkit 2", "2", "technic-launcher"),
                    new GameItem("tech-3", "Hexxit II", "3", "technic-launcher"),
                    new GameItem("tech-4", "Attack of the B-Team", "4", "technic-launcher"),
                    new GameItem("tech-5", "Blightfall", "5", "technic-launcher")
            ));
            gamesByLauncher.put("voids-wrath-launcher", List.of(
                    new GameItem("void-1", "Crazy Craft 4.0", "1", "voids-wrath-launcher"),
                    new GameItem("void-2", "Jurassic Craft", "2", "voids-wrath-launcher"),
                    new GameItem("void-3", "The DreamCraft", "3", "voids-wrath-launcher"),
                    new GameItem("void-4", "PokePack", "4", "voids-wrath-launcher")
            ));
            gamesByLauncher.put("minecraft-adventure", List.of(
                    new GameItem("adv-1", "Medieval MC [FABRIC/FORGE]", "1", "minecraft-adventure"),
                    new GameItem("adv-2", "Roguelike Adventures and Dungeons 2 (RAD 2)", "2", "minecraft-adventure"),
                    new GameItem("adv-3", "Dungeons, Dragons and Space Shuttles", "3", "minecraft-adventure"),
                    new GameItem("adv-4", "Rebirth of the Night", "4", "minecraft-adventure")
            ));
        }

        if (locations.isEmpty()) {
            locations.addAll(List.of(
                    new LocationEntry("2", "FFM / Eygelshoven, GER", "EUROPE (EU)", "DE", "PletX", "https://ping-frankfurt.zap-hosting.com/", false),
                    new LocationEntry("7", "London, UK", "EUROPE (EU)", "UK", "OVH", "https://ping-london.zap-hosting.com/", true),
                    new LocationEntry("12", "Los Angeles, USA (west)", "AMERICA", "US", "PletX", "https://ping-losangeles.zap-hosting.com/", false),
                    new LocationEntry("13", "Ashburn, USA (East)", "AMERICA", "US", "PletX", "https://ping-ashburn.zap-hosting.com/", false),
                    new LocationEntry("3", "Montreal, Canada", "AMERICA", "CA", "OVH", "https://ping-montreal.zap-hosting.com/", true),
                    new LocationEntry("1", "Dallas, USA (central)", "AMERICA", "US", "PletX", "https://ping-dallas.zap-hosting.com/", false),
                    new LocationEntry("5", "Sydney, Australia", "AUSTRALIA (OCE)", "AU", "OVH", "https://ping-sydney.zap-hosting.com/", true),
                    new LocationEntry("4", "Singapore, Asia", "ASIA (SEA)", "SG", "OVH", "https://ping-singapore.zap-hosting.com/", true)
            ));
        }

        if (slotSteps.isEmpty()) {
            int[] vals = {4,6,8,10,12,14,16,18,20,22,24,26,28,30,32,36,40,44,48,52,56,60,64,68,72,76,80,90,100,120,140,160,180,200,300,400,500};
            for (int v : vals) slotSteps.add(new OptionStep(String.valueOf(v), v + " Slots", v));
        }

        if (ramSteps.isEmpty()) {
            ramSteps.add(new OptionStep("467", "0 GB RAM", 0));
            ramSteps.add(new OptionStep("7", "1 GB RAM", 1));
            ramSteps.add(new OptionStep("8", "2 GB RAM", 2));
            ramSteps.add(new OptionStep("9", "3 GB RAM", 3));
            ramSteps.add(new OptionStep("10", "4 GB RAM", 4));
            ramSteps.add(new OptionStep("560", "5 GB RAM", 5));
            ramSteps.add(new OptionStep("561", "6 GB RAM", 6));
            ramSteps.add(new OptionStep("562", "7 GB RAM", 7));
            ramSteps.add(new OptionStep("563", "8 GB RAM", 8));
            ramSteps.add(new OptionStep("564", "9 GB RAM", 9));
            ramSteps.add(new OptionStep("565", "10 GB RAM", 10));
            ramSteps.add(new OptionStep("566", "11 GB RAM", 11));
            ramSteps.add(new OptionStep("567", "12 GB RAM", 12));
            ramSteps.add(new OptionStep("568", "13 GB RAM", 13));
            ramSteps.add(new OptionStep("569", "14 GB RAM", 14));
            ramSteps.add(new OptionStep("570", "15 GB RAM", 15));
            ramSteps.add(new OptionStep("571", "16 GB RAM", 16));
            ramSteps.add(new OptionStep("735", "18 GB RAM", 18));
            ramSteps.add(new OptionStep("736", "20 GB RAM", 20));
            ramSteps.add(new OptionStep("737", "22 GB RAM", 22));
            ramSteps.add(new OptionStep("738", "24 GB RAM", 24));
            ramSteps.add(new OptionStep("739", "26 GB RAM", 26));
            ramSteps.add(new OptionStep("740", "28 GB RAM", 28));
            ramSteps.add(new OptionStep("741", "30 GB RAM", 30));
            ramSteps.add(new OptionStep("742", "32 GB RAM", 32));
        }

        if (diskSteps.isEmpty()) {
            diskSteps.add(new OptionStep("646", "0 Gigabyte", 0));
            diskSteps.add(new OptionStep("647", "25 Gigabyte", 25));
            diskSteps.add(new OptionStep("648", "50 Gigabyte", 50));
            diskSteps.add(new OptionStep("649", "75 Gigabyte", 75));
            diskSteps.add(new OptionStep("650", "100 Gigabyte", 100));
            diskSteps.add(new OptionStep("651", "150 Gigabyte", 150));
            diskSteps.add(new OptionStep("841", "200 Gigabyte", 200));
        }

        if (billingSteps.isEmpty()) {
            billingSteps.add(new OptionStep("12", "1 month / 30 days", 30));
            billingSteps.add(new OptionStep("13", "3 months / 90 days (-10%)", 90));
            billingSteps.add(new OptionStep("14", "6 months / 180 days (- 20%)", 180));
            billingSteps.add(new OptionStep("38", "1 year / 360 days (-30%)", 360));
            billingSteps.add(new OptionStep("607", "2 years / 720 days (- 40%)", 720));
            billingSteps.add(new OptionStep("608", "5 years / 1800 days (- 50%)", 1800));
        }

        // VPS Defaults
        if (vpsCpuSteps.isEmpty()) {
            vpsCpuSteps.add(new OptionStep("506", "4 Cores", 4, 2.05));
            vpsCpuSteps.add(new OptionStep("507", "6 Cores", 6, 4.45));
            vpsCpuSteps.add(new OptionStep("508", "8 Cores", 8, 5.59));
            vpsCpuSteps.add(new OptionStep("583", "10 Cores", 10, 7.87));
            vpsCpuSteps.add(new OptionStep("584", "12 Cores", 12, 10.15));
            vpsCpuSteps.add(new OptionStep("585", "14 Cores", 14, 14.71));
            vpsCpuSteps.add(new OptionStep("586", "16 Cores", 16, 16.99));
            vpsCpuSteps.add(new OptionStep("869", "32 Cores", 32, 28.39));
            vpsCpuSteps.add(new OptionStep("870", "64 Cores", 64, 45.49));
        }

        if (vpsRamSteps.isEmpty()) {
            vpsRamSteps.add(new OptionStep("473", "4 GB", 4, 6.73));
            vpsRamSteps.add(new OptionStep("474", "8 GB", 8, 9.01));
            vpsRamSteps.add(new OptionStep("476", "16 GB", 16, 16.99));
            vpsRamSteps.add(new OptionStep("475", "32 GB", 32, 26.11));
            vpsRamSteps.add(new OptionStep("871", "48 GB", 48, 34.09));
            vpsRamSteps.add(new OptionStep("872", "64 GB", 64, 42.07));
            vpsRamSteps.add(new OptionStep("875", "96 GB", 96, 56.89));
            vpsRamSteps.add(new OptionStep("880", "128 GB", 128, 68.29));
        }

        if (vpsDiskSteps.isEmpty()) {
            vpsDiskSteps.add(new OptionStep("501", "25 GB", 25, 0.00));
            vpsDiskSteps.add(new OptionStep("502", "50 GB", 50, 2.17));
            vpsDiskSteps.add(new OptionStep("503", "100 GB", 100, 3.31));
            vpsDiskSteps.add(new OptionStep("504", "200 GB", 200, 5.59));
            vpsDiskSteps.add(new OptionStep("876", "300 GB", 300, 7.87));
            vpsDiskSteps.add(new OptionStep("877", "400 GB", 400, 10.15));
            vpsDiskSteps.add(new OptionStep("878", "500 GB", 500, 12.43));
            vpsDiskSteps.add(new OptionStep("879", "600 GB", 600, 14.71));
            vpsDiskSteps.add(new OptionStep("910", "1000 GB", 1000, 23.83));
            vpsDiskSteps.add(new OptionStep("912", "2000 GB", 2000, 46.63));
        }

        if (vpsIpSteps.isEmpty()) {
            vpsIpSteps.add(new OptionStep("39", "1 IP (Included)", 1, 0.00));
            vpsIpSteps.add(new OptionStep("40", "2 IPs (+$6.84)", 2, 6.84));
            vpsIpSteps.add(new OptionStep("41", "3 IPs (+$10.26)", 3, 10.26));
            vpsIpSteps.add(new OptionStep("42", "4 IPs (+$13.68)", 4, 13.68));
            vpsIpSteps.add(new OptionStep("43", "5 IPs (+$17.10)", 5, 17.10));
            vpsIpSteps.add(new OptionStep("499", "10 IPs (+$34.20)", 10, 34.20));
        }

        if (vpsBillingSteps.isEmpty()) {
            vpsBillingSteps.add(new OptionStep("614", "1 month / 30 days", 30));
            vpsBillingSteps.add(new OptionStep("615", "3 months / 90 days (-10%)", 90));
            vpsBillingSteps.add(new OptionStep("616", "6 months / 180 days (- 20%)", 180));
            vpsBillingSteps.add(new OptionStep("617", "1 year / 360 days (-30%)", 360));
            vpsBillingSteps.add(new OptionStep("618", "2 years / 720 days (- 40%)", 720));
            vpsBillingSteps.add(new OptionStep("619", "5 years / 1800 days (- 50%)", 1800));
        }
    }
}
