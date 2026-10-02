package com.reggarf.mods.zap_hosting_server_integration_menu.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.reggarf.mods.zap_hosting_server_integration_menu.ZapHosting;
import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHOrderConfig;
import net.minecraft.Util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZHOrderLinkGenerator {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36";

    public static void generateAndOpenOrder(ZHOrderConfig config, Consumer<Boolean> onComplete) {
        CompletableFuture.supplyAsync(() -> {
            try {
                CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
                CookieHandler.setDefault(cookieManager);

                boolean isVps = "vps".equals(config.launcherKey);
                boolean isDedicated = "dedicated-server".equals(config.launcherKey);

                String productUrl;
                if (isVps) {
                    productUrl = "https://legacy.zap-hosting.com/en/shop/product/vps/";
                } else if (isDedicated) {
                    productUrl = "https://legacy.zap-hosting.com/en/shop/product/dedicated-server/";
                } else {
                    String launcher = config.launcherKey != null ? config.launcherKey : "curse-twitch";
                    productUrl = "https://legacy.zap-hosting.com/en/shop/product/cloud-gameserver/" + launcher + "/";
                }

                // 1. Visit product page and extract all hidden form values (csrf token, paymentOptions, etc.)
                HttpURLConnection initConn = (HttpURLConnection) URI.create(productUrl).toURL().openConnection();
                initConn.setRequestMethod("GET");
                initConn.setRequestProperty("User-Agent", USER_AGENT);
                initConn.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
                initConn.setConnectTimeout(8000);
                initConn.setReadTimeout(8000);

                StringBuilder htmlBuilder = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(initConn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        htmlBuilder.append(line).append("\n");
                    }
                }
                initConn.disconnect();
                String html = htmlBuilder.toString();

                // Extract form inputs
                Map<String, String> formData = new HashMap<>();
                Pattern formPattern = Pattern.compile("<form[^>]+id=[\"']productForm[\"'][^>]*>(.*?)</form>", Pattern.DOTALL);
                Matcher formMatcher = formPattern.matcher(html);
                String formHtml = formMatcher.find() ? formMatcher.group(1) : html;

                Pattern inputPattern = Pattern.compile("<input[^>]+name=[\"']([^\"']+)[\"'][^>]*value=[\"']([^\"']*)[\"']");
                Matcher inputMatcher = inputPattern.matcher(formHtml);
                while (inputMatcher.find()) {
                    formData.put(inputMatcher.group(1), inputMatcher.group(2));
                }

                // Apply location
                String selectedLocation = config.locationId != null ? config.locationId : "2";
                formData.put("field[208]", selectedLocation);
                formData.put("field[site]", selectedLocation);
                formData.put("site", selectedLocation);
                formData.put("site_id", selectedLocation);

                if (isVps) {
                    // VPS specific configuration
                    formData.put("product_id", "50");

                    // Operating System template
                    if (config.vpsOsId != null && !config.vpsOsId.isEmpty()) {
                        formData.put("field[215]", config.vpsOsId);
                        formData.put("sel_field[215]", config.vpsOsId);
                    }

                    // CPU Cores
                    formData.put("field[180]", config.vpsCpuOptionId != null ? config.vpsCpuOptionId : "506");
                    formData.put("sel_field[180]", config.vpsCpuOptionId != null ? config.vpsCpuOptionId : "506");

                    // RAM Memory
                    formData.put("field[171]", config.vpsRamOptionId != null ? config.vpsRamOptionId : "473");
                    formData.put("sel_field[171]", config.vpsRamOptionId != null ? config.vpsRamOptionId : "473");

                    // NVMe SSD Storage
                    formData.put("field[179]", config.vpsDiskOptionId != null ? config.vpsDiskOptionId : "501");
                    formData.put("sel_field[179]", config.vpsDiskOptionId != null ? config.vpsDiskOptionId : "501");

                    // Dedicated IPv4
                    formData.put("field[13]", config.vpsIpOptionId != null ? config.vpsIpOptionId : "39");
                    formData.put("sel_field[13]", config.vpsIpOptionId != null ? config.vpsIpOptionId : "39");

                    // Bandwidth
                    formData.put("field[146]", config.vpsBandwidthOptionId != null ? config.vpsBandwidthOptionId : "436");
                    formData.put("sel_field[146]", config.vpsBandwidthOptionId != null ? config.vpsBandwidthOptionId : "436");

                    // Billing Interval
                    formData.put("field[229]", config.vpsBillingOptionId != null ? config.vpsBillingOptionId : "614");
                    formData.put("sel_field[229]", config.vpsBillingOptionId != null ? config.vpsBillingOptionId : "614");
                } else if (isDedicated) {
                    // Dedicated Server specific configuration
                    formData.put("product_id", "61");
                    formData.put("field[281]", config.dediIpOptionId != null ? config.dediIpOptionId : "714");
                    formData.put("sel_field[281]", config.dediIpOptionId != null ? config.dediIpOptionId : "714");

                    formData.put("field[6]", config.dediBillingOptionId != null ? config.dediBillingOptionId : "12");
                    formData.put("sel_field[6]", config.dediBillingOptionId != null ? config.dediBillingOptionId : "12");
                } else {
                    // Standard Cloud Gameserver configuration
                    formData.put("field[slot]", String.valueOf(config.slots));
                    formData.put("field[203]", config.isPremiumCpu ? "590" : "588");
                    formData.put("field[147]", config.hasDedicatedIp ? "439" : "438");
                    formData.put("field[250]", config.hasDdosOverview ? "637" : "636");

                    String ramId = config.ramOptionId != null ? config.ramOptionId : "467";
                    formData.put("field[4]", ramId);
                    formData.put("sel_field[4]", ramId);

                    String diskId = config.diskOptionId != null ? config.diskOptionId : "646";
                    formData.put("field[255]", diskId);
                    formData.put("sel_field[255]", diskId);

                    String billId = config.billingOptionId != null ? config.billingOptionId : "12";
                    formData.put("field[6]", billId);
                    formData.put("sel_field[6]", billId);

                    if (config.pgid != null && !config.pgid.isEmpty()) {
                        formData.put("field[game-selection]", config.pgid);
                        formData.put("field[game_id]", config.pgid);
                        formData.put("game_id", config.pgid);
                    }
                }

                // Auto-apply voucher / promo code
                String voucherCode = config.getVoucherCode();
                if (voucherCode != null && !voucherCode.isEmpty()) {
                    formData.put("voucher", voucherCode);
                    formData.put("voucher_code", voucherCode);
                    formData.put("field[voucher]", voucherCode);
                    formData.put("coupon", voucherCode);
                }

                // 2. Submit configured options to json_computeProductPrice.php
                String computeUrl = "https://legacy.zap-hosting.com/interface/shop/product/_ajax/json_computeProductPrice.php";
                HttpURLConnection compConn = (HttpURLConnection) URI.create(computeUrl).toURL().openConnection();
                compConn.setRequestMethod("POST");
                compConn.setDoOutput(true);
                compConn.setRequestProperty("User-Agent", USER_AGENT);
                compConn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
                compConn.setRequestProperty("Referer", productUrl);
                compConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                compConn.setConnectTimeout(8000);
                compConn.setReadTimeout(8000);

                StringBuilder postBody = new StringBuilder();
                for (Map.Entry<String, String> entry : formData.entrySet()) {
                    appendParam(postBody, entry.getKey(), entry.getValue());
                }

                try (OutputStream os = compConn.getOutputStream()) {
                    os.write(postBody.toString().getBytes(StandardCharsets.UTF_8));
                }
                compConn.getResponseCode();
                compConn.disconnect();

                // 3. Fetch prefilled share link from json_getShareLink.php
                String shareUrl = "https://legacy.zap-hosting.com/interface/shop/product/_ajax/json_getShareLink.php";
                HttpURLConnection shareConn = (HttpURLConnection) URI.create(shareUrl).toURL().openConnection();
                shareConn.setRequestMethod("GET");
                shareConn.setRequestProperty("User-Agent", USER_AGENT);
                shareConn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
                shareConn.setRequestProperty("Referer", productUrl);
                shareConn.setConnectTimeout(8000);
                shareConn.setReadTimeout(8000);

                StringBuilder shareResp = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(shareConn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        shareResp.append(line);
                    }
                }
                shareConn.disconnect();

                JsonObject json = JsonParser.parseString(shareResp.toString()).getAsJsonObject();
                if (json.has("data") && json.getAsJsonObject("data").has("link")) {
                    String link = json.getAsJsonObject("data").get("link").getAsString();
                    if (link != null && link.contains("myorder=") && !link.endsWith("myorder=")) {
                        if (voucherCode != null && !voucherCode.isEmpty()) {
                            link += (link.contains("?") ? "&voucher=" : "?voucher=") + URLEncoder.encode(voucherCode, StandardCharsets.UTF_8);
                        }
                        String partnerId = (ZapHosting.CONFIG != null && ZapHosting.CONFIG.common != null && ZapHosting.CONFIG.common.partnerId != null && !ZapHosting.CONFIG.common.partnerId.trim().isEmpty())
                                ? ZapHosting.CONFIG.common.partnerId
                                : "zap1204486";
                        if (partnerId != null && !partnerId.isEmpty()) {
                            link += (link.contains("?") ? "&ref=" : "?ref=") + URLEncoder.encode(partnerId, StandardCharsets.UTF_8);
                        }
                        return link;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Fallback URLs
            String fallback;
            if ("vps".equals(config.launcherKey)) {
                fallback = "https://zap-hosting.com/en/shop/product/vps/";
            } else if ("dedicated-server".equals(config.launcherKey)) {
                fallback = "https://zap-hosting.com/en/shop/product/dedicated-server/";
            } else {
                fallback = "https://zap-hosting.com/en/shop/product/cloud-gameserver/" + config.launcherKey + "/";
                if (config.pgid != null && !config.pgid.isEmpty()) {
                    fallback += "?pgid=" + config.pgid;
                }
            }

            String voucherCode = config.getVoucherCode();
            if (voucherCode != null && !voucherCode.isEmpty()) {
                fallback += (fallback.contains("?") ? "&voucher=" : "?voucher=") + URLEncoder.encode(voucherCode, StandardCharsets.UTF_8);
            }
            String partnerId = (ZapHosting.CONFIG != null && ZapHosting.CONFIG.common != null && ZapHosting.CONFIG.common.partnerId != null && !ZapHosting.CONFIG.common.partnerId.trim().isEmpty())
                    ? ZapHosting.CONFIG.common.partnerId
                    : "zap1204486";
            if (partnerId != null && !partnerId.isEmpty()) {
                fallback += (fallback.contains("?") ? "&ref=" : "?ref=") + URLEncoder.encode(partnerId, StandardCharsets.UTF_8);
            }
            return fallback;
        }).thenAccept(url -> {
            try {
                // Background ping to partner link to record campaign click (takes link from config 1st, fallback to default)
                String partnerLink = (ZapHosting.CONFIG != null && ZapHosting.CONFIG.common != null && ZapHosting.CONFIG.common.link != null && !ZapHosting.CONFIG.common.link.trim().isEmpty())
                        ? ZapHosting.CONFIG.common.link
                        : "https://zap-hosting.com/createletscreate";
                try {
                    HttpURLConnection partnerConn = (HttpURLConnection) URI.create(partnerLink).toURL().openConnection();
                    partnerConn.setRequestMethod("GET");
                    partnerConn.setRequestProperty("User-Agent", USER_AGENT);
                    partnerConn.setConnectTimeout(4000);
                    partnerConn.setReadTimeout(4000);
                    partnerConn.getResponseCode();
                    partnerConn.disconnect();
                } catch (Exception ignored) {}

                // Directly open official ZAP-Hosting prefilled order URL in default browser
                Util.getPlatform().openUri(URI.create(url));
                if (onComplete != null) onComplete.accept(true);
            } catch (Exception e) {
                e.printStackTrace();
                if (onComplete != null) onComplete.accept(false);
            }
        });
    }

    private static void appendParam(StringBuilder sb, String key, String value) {
        if (value == null) return;
        if (!sb.isEmpty()) sb.append("&");
        sb.append(URLEncoder.encode(key, StandardCharsets.UTF_8))
          .append("=")
          .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
    }
}
