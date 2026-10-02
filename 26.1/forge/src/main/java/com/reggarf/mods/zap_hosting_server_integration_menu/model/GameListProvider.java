package com.reggarf.mods.zap_hosting_server_integration_menu.model;

import java.util.*;

public class GameListProvider {

    public record GameEntry(String name, String pgid, int gameId) {}

    private static final Map<String, List<GameEntry>> GAMES_BY_LAUNCHER = new LinkedHashMap<>();

    static {
        // 1. Curse / Twitch
        GAMES_BY_LAUNCHER.put("curse-twitch", List.of(
                new GameEntry("Curse / Twitch: Space Astronomy", "curse-twitch-space-astronomy", 32),
                new GameEntry("Curse / Twitch: Forever Stranded", "curse-twitch-forever-stranded", 32),
                new GameEntry("Curse / Twitch: All The Mods Expert", "curse-twitch-all-the-mods-expert", 32),
                new GameEntry("Curse / Twitch: Invasion", "curse-twitch-invasion", 32),
                new GameEntry("Curse / Twitch: Project Ozone 2: Reloaded", "curse-twitch-project-ozone-2-reloaded", 32),
                new GameEntry("Curse / Twitch: Farming Valley", "curse-twitch-farming-valley", 32),
                new GameEntry("Curse / Twitch: All the Mods 9", "curse-twitch-all-the-mods-9", 32),
                new GameEntry("Curse / Twitch: All the Mods 10", "curse-twitch-all-the-mods-10", 32),
                new GameEntry("Curse / Twitch: Better MC [Forge] BMC4", "curse-twitch-better-mc-forge-bmc4", 32),
                new GameEntry("Curse / Twitch: Better MC [NeoForge] BMC5", "curse-twitch-better-mc-neoforge-bmc5", 32),
                new GameEntry("Curse / Twitch: Medieval MC [Forge] MMC4", "curse-twitch-medieval-mc-forge-mmc4", 32),
                new GameEntry("Curse / Twitch: Prominence II RPG", "curse-twitch-prominence-ii-fabric", 32),
                new GameEntry("Curse / Twitch: Create: Astral", "curse-twitch-create-astral", 32),
                new GameEntry("Curse / Twitch: Create: Perfect World 2", "curse-twitch-create-perfect-world-2", 32),
                new GameEntry("Curse / Twitch: Vault Hunters 3rd Edition", "curse-twitch-vault-hunters-3rd-edition", 32),
                new GameEntry("Curse / Twitch: DawnCraft", "curse-twitch-dawncraft", 32),
                new GameEntry("Curse / Twitch: Roguelike Adventures & Dungeons 2", "curse-twitch-roguelike-adventures-and-dungeons-2", 32),
                new GameEntry("Curse / Twitch: Cobblemon Official", "curse-twitch-cobblemon-official-forge", 32),
                new GameEntry("Curse / Twitch: SevTech: Ages", "curse-twitch-sevtech-ages", 32),
                new GameEntry("Curse / Twitch: SkyFactory 4", "curse-twitch-skyfactory-4", 32),
                new GameEntry("Curse / Twitch: RLCraft", "curse-twitch-rlcraft", 32)
        ));

        // 2. VPS
        GAMES_BY_LAUNCHER.put("vps", List.of(
                new GameEntry("Ubuntu 24.04 LTS (Noble Numbat)", "vps-ubuntu-2404", 100),
                new GameEntry("Ubuntu 22.04 LTS (Jammy Jellyfish)", "vps-ubuntu-2204", 100),
                new GameEntry("Debian 12 (Bookworm)", "vps-debian-12", 100),
                new GameEntry("Debian 11 (Bullseye)", "vps-debian-11", 100),
                new GameEntry("Windows Server 2022 Standard", "vps-win-2022", 100),
                new GameEntry("Windows Server 2019 Standard", "vps-win-2019", 100),
                new GameEntry("CentOS Stream 9", "vps-centos-9", 100),
                new GameEntry("AlmaLinux 9", "vps-almalinux-9", 100)
        ));

        // 3. Dedicated Server
        GAMES_BY_LAUNCHER.put("dedicated-server", List.of(
                new GameEntry("AMD Ryzen 9 7950X - 16 Cores / 32 Threads - 128 GB DDR5", "dedi-ryzen-7950x", 200),
                new GameEntry("AMD Ryzen 9 5950X - 16 Cores / 32 Threads - 64 GB DDR4", "dedi-ryzen-5950x", 200),
                new GameEntry("Intel Core i9-13900K - 24 Cores / 32 Threads - 128 GB DDR5", "dedi-i9-13900k", 200),
                new GameEntry("AMD EPYC 7702 - 64 Cores / 128 Threads - 256 GB RAM", "dedi-epyc-7702", 200)
        ));

        // 4. Feed the Beast
        GAMES_BY_LAUNCHER.put("feed-the-beast", List.of(
                new GameEntry("FTB Skies 2", "feed-the-beast-skies-2", 32),
                new GameEntry("FTB Stoneblock 3", "feed-the-beast-ftb-stoneblock-3", 32),
                new GameEntry("FTB Stoneblock 2", "feed-the-beast-ftb-stoneblock-2", 32),
                new GameEntry("FTB Oceanblock", "feed-the-beast-ftb-oceanblock", 32),
                new GameEntry("FTB Presents Direwolf20 1.20", "feed-the-beast-ftb-presents-direwolf20-120", 32),
                new GameEntry("FTB Presents Direwolf20 1.19", "feed-the-beast-ftb-presents-direwolf20-119", 32),
                new GameEntry("FTB Presents Direwolf20 1.12", "feed-the-beast-ftb-presents-direwolf20-112", 32),
                new GameEntry("FTB Skies Expert", "feed-the-beast-ftb-skies-expert", 32),
                new GameEntry("FTB Revelation", "feed-the-beast-revelation", 32),
                new GameEntry("FTB Infinity Evolved", "feed-the-beast-infinity-evolved", 32),
                new GameEntry("FTB Beyond", "feed-the-beast-ftb-beyond", 32),
                new GameEntry("FTB University", "feed-the-beast-ftb-university", 32),
                new GameEntry("FTB Academy", "feed-the-beast-ftb-academy-116", 32),
                new GameEntry("FTB Arcanum Institute", "feed-the-beast-ftb-arcanum-institute", 32),
                new GameEntry("FTB Genesis", "feed-the-beast-genesis", 32),
                new GameEntry("FTB Builders Paradise 2", "feed-the-beast-ftb-builders-paradise-2", 32)
        ));

        // 5. Minecraft
        GAMES_BY_LAUNCHER.put("minecraft", List.of(
                new GameEntry("Minecraft: Paper Spigot (High Performance)", "minecraft-paper-spigot", 435),
                new GameEntry("Minecraft: Purpur", "minecraft-purpur", 435),
                new GameEntry("Minecraft: NeoForge", "minecraft-neoforge", 106),
                new GameEntry("Minecraft: Fabric", "minecraft-fabric", 593),
                new GameEntry("Minecraft: Forge", "minecraft-forge", 106),
                new GameEntry("Minecraft: Vanilla", "minecraft-vanilla", 3),
                new GameEntry("Minecraft: Spigot", "minecraft-spigot", 31),
                new GameEntry("Minecraft: Bukkit", "minecraft-bukkit", 5),
                new GameEntry("Minecraft: Bedrock Edition", "minecraft-bedrock", 392),
                new GameEntry("Minecraft: BungeeCord Proxy Server", "minecraft-bungeecord", 115),
                new GameEntry("Minecraft: Waterfall Proxy", "minecraft-waterfall", 555),
                new GameEntry("Minecraft: SpongeForge", "minecraft-spongeforge", 436),
                new GameEntry("Minecraft: SpongeVanilla", "minecraft-spongevanilla", 437)
        ));

        // 6. AT Launcher
        GAMES_BY_LAUNCHER.put("at-launcher", List.of(
                new GameEntry("AT Launcher: SevTech: Ages", "at-launcher-sevtech-ages", 32),
                new GameEntry("AT Launcher: SkyFactory 4", "at-launcher-skyfactory-4", 32),
                new GameEntry("AT Launcher: Crucial 2", "at-launcher-crucial-2", 32),
                new GameEntry("AT Launcher: Farming Valley", "at-launcher-farming-valley", 32),
                new GameEntry("AT Launcher: The Pixelmon Modpack", "at-launcher-pixelmon", 32),
                new GameEntry("AT Launcher: Caelum 2", "at-launcher-caelum-2", 32),
                new GameEntry("AT Launcher: MoonQuest", "at-launcher-moonquest", 32)
        ));

        // 7. Technic Launcher
        GAMES_BY_LAUNCHER.put("technic-launcher", List.of(
                new GameEntry("Technic: Tekkit 2", "technic-launcher-tekkit-2", 32),
                new GameEntry("Technic: Tekkit Classic", "technic-launcher-tekkit-classic", 32),
                new GameEntry("Technic: Hexxit II", "technic-launcher-hexxit-ii", 32),
                new GameEntry("Technic: Hexxit", "technic-launcher-hexxit", 32),
                new GameEntry("Technic: Attack of the B-Team", "technic-launcher-attack-of-the-b-team", 32),
                new GameEntry("Technic: Voltz", "technic-launcher-voltz", 32),
                new GameEntry("Technic: The 1.7.10 Pack", "technic-launcher-the-1710-pack", 32),
                new GameEntry("Technic: The 1.12.2 Pack", "technic-launcher-the-1122-pack", 32),
                new GameEntry("Technic: Crafting Dead: Aftermath", "technic-launcher-crafting-dead-aftermath", 32),
                new GameEntry("Technic: GT New Horizons", "technic-launcher-gt-new-horizons", 32),
                new GameEntry("Technic: Pixelmon Generations", "technic-launcher-pixelmon-generations-official", 32)
        ));

        // 8. Voids Wrath Launcher
        GAMES_BY_LAUNCHER.put("voids-wrath-launcher", List.of(
                new GameEntry("Voids Wrath: Crazy Craft 4.0", "voids-wrath-launcher-crazy-craft-40", 32),
                new GameEntry("Voids Wrath: Crazy Craft Legacy", "voids-wrath-launcher-crazy-craft-legacy", 32),
                new GameEntry("Voids Wrath: Dream Craft 2", "voids-wrath-launcher-dreamcraft-2", 32),
                new GameEntry("Voids Wrath: Poke Pack", "voids-wrath-launcher-poke-pack", 32),
                new GameEntry("Voids Wrath: Scramble Craft", "voids-wrath-launcher-scramble-craft", 32),
                new GameEntry("Voids Wrath: Void's Wrath", "voids-wrath-launcher-voids-wrath", 32)
        ));

        // 9. Minecraft Adventure
        GAMES_BY_LAUNCHER.put("minecraft-adventure", List.of(
                new GameEntry("Adventure: Random Item Skyblock", "minecraft-adventure-random-item-skyblock", 32),
                new GameEntry("Adventure: One Block Survival", "minecraft-adventure-one-block", 32),
                new GameEntry("Adventure: Custom Maps & Minigames", "minecraft-adventure-custom-maps", 32),
                new GameEntry("Adventure: Prison & Skyblock Server", "minecraft-adventure-prison-skyblock", 32)
        ));
    }

    public static List<GameEntry> getGamesForLauncher(String launcherKey) {
        return GAMES_BY_LAUNCHER.getOrDefault(launcherKey, GAMES_BY_LAUNCHER.get("curse-twitch"));
    }
}
