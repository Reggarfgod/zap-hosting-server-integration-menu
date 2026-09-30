# ZAP-Hosting In-Game Server Integration Mod

**Prepared for:** Marvin & the ZAP-Hosting Team  
**Author / Developer:** Reggarf  
**Target Platform:** Minecraft NeoForge & Forge (1.20.x / 1.21.x)  
**Repository:** [Reggarfgod/zap-hosting-server-integration-menu](https://github.com/Reggarfgod/zap-hosting-server-integration-menu)

---

## 1. Executive Summary

The **ZAP-Hosting Server Integration Mod** is an in-game integration designed to seamlessly bridge Minecraft players and modpack users directly to ZAP-Hosting services. 

Instead of forcing players to leave their client, open external browsers, manually search through complex server configurators, or lose their partner voucher codes, this mod embeds a **native, 10-step server configuration wizard**, **live ping test tool**, **multiplayer menu banners**, and **first-time player welcome overlay** directly inside the Minecraft user interface.

Every server order configured inside Minecraft automatically carries the creator's partner/affiliate voucher code (e.g. `REGGARF-1047`) and directly fills the official ZAP-Hosting checkout web cart with 100% accurate pre-configured parameters.

---

## 2. Visual Walkthrough & Core Features

### 🎁 1. In-Game First-Time Welcome Popup
When players boot a modpack or join a server for the first time, a polished welcome dialog appears with instant partner branding, one-click voucher copying, direct access to the in-game configurator, and a "Don't show this again" option.

![In-Game Welcome Popup](docs/images/00_welcome_popup.png)

---

### 🌐 2. Multiplayer Menu Banner Integration
A clean, non-intrusive banner entry integrated directly into Minecraft's multiplayer server selection screen, inviting players who need a server to launch the wizard with a single click.

![Multiplayer Menu Banner](docs/images/11_multiplayer_banner.png)

---

### 🛠️ 3. 10-Step Native In-Game Server Configurator

#### Step 1: Platform & Launcher Selection
Select between popular launcher formats including CurseForge/Twitch, FTB, Vanilla Minecraft, ATLauncher, Technic, VPS, and Dedicated Servers with live starting prices.
![Step 1 - Platform Selection](docs/images/01_step1_platform.png)

#### Step 2: Preinstalled Game & Modpack
Search and filter through live-indexed modpacks and Minecraft versions.
![Step 2 - Modpack Selection](docs/images/02_step2_modpack.png)

#### Step 3: Location Selection with Real-Time Ping Checker
Conducts real-time ICMP/TCP ping checks against actual ZAP-Hosting data centers worldwide (Frankfurt, London, Los Angeles, Ashburn, Montreal, Dallas, Sydney, Singapore) showing latency in milliseconds and protection backbones (PletX vs. OVH).
![Step 3 - Location and Ping Check](docs/images/03_step3_location_ping.png)

#### Step 4: Game Server Slots
Intuitive slider allowing players to select their desired player slots with bundled RAM capacity clearly noted.
![Step 4 - Slots Slider](docs/images/04_step4_slots.png)

#### Step 5: RAM Boost
Dynamic memory upgrade slider showing base RAM + boost RAM calculations in real time.
![Step 5 - RAM Boost Slider](docs/images/05_step5_ram_boost.png)

#### Step 6: Additional NVMe Disk Space
Slider for high-speed NVMe SSD storage adjustments.
![Step 6 - Disk Space](docs/images/06_step6_disk_space.png)

#### Step 7: CPU & Host Server Tier
Selection between Standard SSD and Premium Gaming CPU (M.2 SSD, 3.4–4.4 GHz) with clear performance recommendations.
![Step 7 - CPU & Host Tier](docs/images/07_step7_cpu_tier.png)

#### Step 8: Dedicated IPv4 Address
Option to toggle a dedicated IP address (standard `:25565` port) for custom domains.
![Step 8 - Dedicated IPv4](docs/images/08_step8_ipv4.png)

#### Step 9: DDoS Manager Overview
Option to activate live DDoS attack logs and dashboard graph analytics.
![Step 9 - DDoS Manager](docs/images/09_step9_ddos.png)

#### Step 10: Billing Interval, Pre-Payment Discounts & Checkout
Comprehensive cost summary displaying billing cycle, monthly breakdown, discount tiers, and partner voucher code application before checkout.
![Step 10 - Billing & Summary](docs/images/10_step10_checkout.png)

---

### 🛒 4. Automated Web Cart Transfer
Upon clicking **"Order Server Now"**, the mod opens the browser directly to ZAP-Hosting's official order page with **all configuration options pre-selected** and the **partner voucher code automatically applied**.

![ZAP-Hosting Cart with Auto-Applied Coupon](docs/images/12_website_cart_applied.png)

---

### ⚙️ 5. In-Game Configuration GUI (Creator & Partner Friendly)

The mod includes an in-game graphical settings screen accessible via the Mod Menu or NeoForge config list, allowing server owners and modpack authors to customize tracking and branding without manual JSON editing:

#### Tab 1: Affiliate & Promo Settings
Modpack developers and partners can effortlessly customize their promotion without recompiling:
- **In-Game Welcome Popup:** Toggle on/off or click **"Preview"** to test the popup anytime.
- **Partner Campaign Link:** URL target pinged to credit partner referral campaigns.
- **Partner ID (Affiliate Ref):** Automatically appends the creator's affiliate parameter (`?ref=...`) to all cart links.
- **Promo Voucher Code:** The custom discount coupon automatically applied at checkout (e.g. `REGGARF-1047`).
- **Voucher Discount %:** Interactive slider configuring the displayed promotional discount percentage.

![Config Screen - Affiliate & Promo](docs/images/13_config_affiliate_promo.png)

#### Tab 2: Public Multiplayer Server Integration
Modpack developers or network operators can feature an official multiplayer server directly in the Minecraft server list:
- **Enable Public Server:** Pin a permanent public/community server entry at the top of the multiplayer screen.
- **Server Display Name:** Custom title of the pinned entry (e.g. `"ZAP-Hosting Official Server"`).
- **Server IP / Domain:** The target Minecraft server host address (e.g. `play.zap-hosting.com`).
- **Server MOTD / Description:** Subtitle shown directly below the server title.
- **Custom Connect Screen:** Enables a branded, themed ZAP-Hosting loading/connecting screen while joining.

![Config Screen - Public Multiplayer Server](docs/images/14_config_public_server.png)

---

## 3. Technical Architecture & Dedicated Server Safety

- **Dist-Clean Architecture:** Strictly isolates client GUI and rendering classes (`ZHClientSetup`, `ZHClientTicker`, screens) from common code. Dedicated servers run headless with zero classloading errors.
- **Optional Payload Networking:** Uses NeoForge's 1.21 `CustomPacketPayload` (`ZHWelcomePopupPayload`) registered as `.optional()` so vanilla clients or servers without the mod never experience connection errors or handshake failures.
- **Persistent Player Data:** Server-side tracking via player persistent NBT (`hasJoinedBefore`) ensures the welcome popup only triggers once per player per world or server.
- **Background Live Data Provider (`ZHLiveDataProvider`):** Asynchronously fetches and parses real-time pricing and options with offline fallback defaults to guarantee 0 UI latency.

---

## 4. Value Proposition for ZAP-Hosting

1. **Eliminate Funnel Drop-off:** Configured within Minecraft's native aesthetic, drastically increasing completion and checkout rates compared to external web forms.
2. **Turnkey Solution for Modpack Creators:** Modpack developers can include this mod out-of-the-box, providing a high-converting affiliate channel for their communities.
3. **Official Partnership Opportunities:**
   - **Direct Partner API Integration:** Hook into an authenticated ZAP-Hosting JSON catalog endpoint for dynamic pricing updates and new product listings.
   - **In-Game Server Management:** Enable players to link their ZAP-Hosting account to view server metrics and execute Start/Stop/Restart commands right from Minecraft.

---

*For inquiries, live demos, or code review, please reach out via GitHub or Discord.*
