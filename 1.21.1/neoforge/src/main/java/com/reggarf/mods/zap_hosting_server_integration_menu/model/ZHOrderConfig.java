package com.reggarf.mods.zap_hosting_server_integration_menu.model;

import com.reggarf.mods.zap_hosting_server_integration_menu.ZapHosting;

public class ZHOrderConfig {

    // Step 1: Launcher
    public String launcherKey = "curse-twitch";
    public String launcherName = "Curse / Twitch";

    // Step 2: Game / Modpack (Game Servers)
    public String pgid = "220";
    public String gameName = "Curse / Twitch: Space Astronomy";
    public int gameId = 285;

    // Step 3: Location
    public String locationId = "2"; // Default Eygelshoven FFM
    public String locationName = "FFM / Eygelshoven, GER";
    public String locationRegion = "EUROPE (EU)";
    public String locationPing = "18 ms";

    // Step 4: Slots
    public int slots = 4;

    // Step 5: Memory Boost
    public int ramBoostGB = 0;
    public String ramOptionId = "467";

    // Step 6: Additional Disk Space
    public int diskSpaceGB = 0;
    public String diskOptionId = "646";

    // Step 7: CPU & Host Server
    public boolean isPremiumCpu = false;

    // Step 8: Own IPv4 Address
    public boolean hasDedicatedIp = false;

    // Step 9: DDoS Overview
    public boolean hasDdosOverview = false;

    // Step 10: Billing Interval
    public int billingIntervalDays = 30;
    public String billingOptionId = "12";

    // --- Specialized VPS Settings ---
    public String vpsOs = "Ubuntu 24.04 LTS";
    public String vpsOsId = "ubuntu-2404";
    public int vpsCpuCores = 4;
    public String vpsCpuOptionId = "506";
    public int vpsRamGB = 4;
    public String vpsRamOptionId = "473";
    public int vpsDiskGB = 25;
    public String vpsDiskOptionId = "501";
    public int vpsIps = 1;
    public String vpsIpOptionId = "39";
    public int vpsBandwidthGbps = 1;
    public String vpsBandwidthOptionId = "436";
    public String vpsBillingOptionId = "614";

    // --- Specialized Dedicated Server Settings ---
    public String dediModelName = "Intel Core i9-9900K (8C/16T, 64GB, 2x 1TB NVMe)";
    public String dediModelId = "i9-9900k";
    public double dediBasePrice = 67.15;
    public String dediOs = "Debian 12 64-bit";
    public int dediIps = 1;
    public String dediIpOptionId = "714";
    public String dediBillingOptionId = "12";

    public int getBaseRamGB() {
        if (slots <= 8) return 2;
        if (slots <= 16) return 3;
        if (slots <= 32) return 4;
        if (slots <= 64) return 6;
        if (slots <= 100) return 8;
        return 16;
    }

    public int getTotalRamGB() {
        return getBaseRamGB() + ramBoostGB;
    }

    public double getSlotPriceMonthly() {
        return Math.max(3.15, 3.15 + (slots - 4) * 0.42);
    }

    public double getRamPriceMonthly() {
        return ramBoostGB * 2.74;
    }

    public double getDiskPriceMonthly() {
        return (diskSpaceGB / 25.0) * 2.17;
    }

    public double getCpuPriceMonthly() {
        return isPremiumCpu ? 4.50 : 0.00;
    }

    public double getIpPriceMonthly() {
        return hasDedicatedIp ? 3.42 : 0.00;
    }

    public double getDdosPriceMonthly() {
        return hasDdosOverview ? 1.16 : 0.00;
    }

    public double getVpsMonthlyPrice() {
        double cpu = switch (vpsCpuCores) {
            case 6 -> 4.45;
            case 8 -> 5.59;
            case 10 -> 7.87;
            case 12 -> 10.15;
            case 16 -> 16.99;
            case 32 -> 28.39;
            case 64 -> 45.49;
            default -> 2.05;
        };
        double ram = switch (vpsRamGB) {
            case 8 -> 9.01;
            case 16 -> 16.99;
            case 32 -> 26.11;
            case 48 -> 34.09;
            case 64 -> 42.07;
            case 96 -> 56.89;
            case 128 -> 68.29;
            default -> 6.73;
        };
        double disk = switch (vpsDiskGB) {
            case 50 -> 2.17;
            case 100 -> 3.31;
            case 200 -> 5.59;
            case 500 -> 12.43;
            case 1000 -> 23.83;
            default -> 0.00;
        };
        double ip = (vpsIps > 1) ? (vpsIps - 1) * 3.42 : 0.00;
        return cpu + ram + disk + ip;
    }

    public double getDediMonthlyPrice() {
        double ip = (dediIps > 1) ? (dediIps - 1) * 2.28 : 0.00;
        return dediBasePrice + ip;
    }

    public double getTotalMonthlyPrice() {
        if ("vps".equals(launcherKey)) {
            return getVpsMonthlyPrice();
        } else if ("dedicated-server".equals(launcherKey)) {
            return getDediMonthlyPrice();
        }
        return getSlotPriceMonthly()
                + getRamPriceMonthly()
                + getDiskPriceMonthly()
                + getCpuPriceMonthly()
                + getIpPriceMonthly()
                + getDdosPriceMonthly();
    }

    public int getDiscountPercent() {
        return switch (billingIntervalDays) {
            case 90 -> 10;
            case 180 -> 20;
            case 360, 365 -> 30;
            case 720 -> 40;
            case 1800 -> 50;
            default -> 0;
        };
    }

    public String getVoucherCode() {
        if (ZapHosting.CONFIG != null && ZapHosting.CONFIG.common != null) {
            String c = ZapHosting.CONFIG.common.code;
            if (c != null && !c.trim().isEmpty()) {
                return c.trim();
            }
        }
        return "REGGARF-1047";
    }

    public int getVoucherDiscountPercent() {
        String code = getVoucherCode();
        if (!code.isEmpty()) {
            if (ZapHosting.CONFIG != null && ZapHosting.CONFIG.common != null) {
                return ZapHosting.CONFIG.common.discountPercent;
            }
            return 20;
        }
        return 0;
    }

    public double getSubtotalDue() {
        double monthly = getTotalMonthlyPrice();
        double months = billingIntervalDays / 30.0;
        double discountMultiplier = 1.0 - (getDiscountPercent() / 100.0);
        return monthly * months * discountMultiplier;
    }

    public double getOriginalDueToday() {
        return getSubtotalDue();
    }

    public double getVoucherSavings() {
        int vDiscount = getVoucherDiscountPercent();
        if (vDiscount <= 0) return 0.0;
        return getSubtotalDue() * (vDiscount / 100.0);
    }

    public double getDueToday() {
        return Math.max(0.0, getSubtotalDue() - getVoucherSavings());
    }
}
