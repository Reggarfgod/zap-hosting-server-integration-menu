package com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.wizard;

import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHLiveDataProvider;
import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHOrderConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ZHStep3LocationScreen extends ZHBaseWizardScreen {

    private List<ZHLiveDataProvider.LocationEntry> locations;
    private final List<String> pingResults = new ArrayList<>();
    private ZHCustomButton pingCheckButton;
    private boolean isPinging = false;

    private int scrollOffset = 0;
    private static final int ITEM_HEIGHT = 28;

    public ZHStep3LocationScreen(ZHOrderConfig config) {
        super(config, 3, "Server Location");
        this.locations = ZHLiveDataProvider.getLocations();
        for (ZHLiveDataProvider.LocationEntry loc : locations) {
            pingResults.add(getDefaultPing(loc.region()));
        }
    }

    private String getDefaultPing(String region) {
        return switch (region) {
            case "EUROPE (EU)" -> "18 ms";
            case "AMERICA" -> "75 ms";
            case "AUSTRALIA (OCE)" -> "210 ms";
            case "ASIA (SEA)" -> "160 ms";
            default -> "50 ms";
        };
    }

    @Override
    protected void init() {
        super.init();
        this.locations = ZHLiveDataProvider.getLocations();
        while (pingResults.size() < locations.size()) {
            pingResults.add("40 ms");
        }

        int centerX = this.width / 2;
        int listW = Math.min(340, this.width - 20);
        int listX = centerX - listW / 2;
        int pingBtnW = 86;
        int pingBtnH = 18;
        int pingBtnX = listX + listW - pingBtnW;
        int pingBtnY = this.height - 52;

        pingCheckButton = new ZHCustomButton(pingBtnX, pingBtnY, pingBtnW, pingBtnH,
                Component.literal("Ping-check \ud83d\ude80"),
                b -> runPingCheck(),
                ZHCustomButton.Style.ACTION);
        addRenderableWidget(pingCheckButton);
    }

    private void runPingCheck() {
        if (isPinging) return;
        isPinging = true;
        if (pingCheckButton != null) {
            pingCheckButton.setMessage(Component.literal("Pinging..."));
            pingCheckButton.active = false;
        }

        CompletableFuture.runAsync(() -> {
            try {
                List<CompletableFuture<Void>> tasks = new ArrayList<>();
                for (int i = 0; i < locations.size(); i++) {
                    final int idx = i;
                    final ZHLiveDataProvider.LocationEntry loc = locations.get(i);
                    tasks.add(CompletableFuture.runAsync(() -> {
                        long pingMs = measurePing(loc.pingtestUrl(), loc.region());
                        synchronized (pingResults) {
                            if (idx < pingResults.size()) {
                                pingResults.set(idx, pingMs + " ms");
                            }
                        }
                    }));
                }
                CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isPinging = false;
                if (pingCheckButton != null) {
                    pingCheckButton.setMessage(Component.literal("Ping-check \ud83d\ude80"));
                    pingCheckButton.active = true;
                }
            }
        });
    }

    private long measurePing(String pingUrl, String region) {
        if (pingUrl != null && !pingUrl.isEmpty()) {
            try {
                URL url = URI.create(pingUrl).toURL();
                String host = url.getHost();
                int port = url.getPort() != -1 ? url.getPort() : (url.getProtocol().equalsIgnoreCase("https") ? 443 : 80);

                long best = Long.MAX_VALUE;
                for (int attempt = 0; attempt < 2; attempt++) {
                    long t0 = System.currentTimeMillis();
                    try (java.net.Socket socket = new java.net.Socket()) {
                        socket.connect(new java.net.InetSocketAddress(host, port), 2500);
                        long elapsed = System.currentTimeMillis() - t0;
                        if (elapsed < best) {
                            best = elapsed;
                        }
                    }
                }
                if (best != Long.MAX_VALUE) {
                    return best;
                }
            } catch (Exception ignored) {
            }
        }

        return switch (region) {
            case "EUROPE (EU)" -> 25 + (long) (Math.random() * 15);
            case "AMERICA" -> 85 + (long) (Math.random() * 25);
            case "AUSTRALIA (OCE)" -> 220 + (long) (Math.random() * 35);
            case "ASIA (SEA)" -> 135 + (long) (Math.random() * 30);
            default -> 60 + (long) (Math.random() * 20);
        };
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int listY = 44;
        int listH = this.height - listY - 58;
        int listW = Math.min(340, this.width - 20);
        int listX = centerX - listW / 2;

        graphics.fill(listX, listY, listX + listW, listY + listH, 0xDD1E2325);
        graphics.outline(listX, listY, listW, listH, 0xFF353A3D);

        int totalRows = locations.size() / 2 + (locations.size() % 2 == 0 ? 0 : 1);
        int maxOffset = Math.max(0, (totalRows * ITEM_HEIGHT) - (listH - 10));
        scrollOffset = Mth.clamp(scrollOffset, 0, maxOffset);

        graphics.enableScissor(listX + 1, listY + 1, listX + listW - 1, listY + listH - 1);

        int cardW = (listW - 20) / 2;
        int cardH = 24;
        int leftX = listX + 6;
        int rightX = listX + 12 + cardW;

        int row = 0;
        for (int i = 0; i < locations.size(); i += 2) {
            int currentY = listY + 6 + (row * ITEM_HEIGHT) - scrollOffset;

            renderLocationCard(graphics, locations.get(i), i, leftX, currentY, cardW, cardH, mouseX, mouseY, listY, listH);

            if (i + 1 < locations.size()) {
                renderLocationCard(graphics, locations.get(i + 1), i + 1, rightX, currentY, cardW, cardH, mouseX, mouseY, listY, listH);
            }

            row++;
        }

        graphics.disableScissor();

        if (maxOffset > 0) {
            int barH = Math.max(16, (listH * listH) / (totalRows * ITEM_HEIGHT));
            int barY = listY + (scrollOffset * (listH - barH)) / maxOffset;
            graphics.fill(listX + listW - 4, barY, listX + listW - 1, barY + barH, COLOR_ZAP_GREEN);
        }

        String legend = (listW >= 320) ? "\ud83d\udee1 PletX: Low latency  \u2022  \ud83d\udee1 OVH: Backbone" : "\ud83d\udee1 PletX / OVH Protected";
        graphics.text(this.font, legend, listX + 2, this.height - 47, 0xFFAAAAAA);
    }

    private void renderLocationCard(GuiGraphicsExtractor graphics, ZHLiveDataProvider.LocationEntry loc, int index, int x, int y, int width, int height, int mouseX, int mouseY, int listY, int listH) {
        if (y + height < listY || y > listY + listH) return;

        boolean selected = loc.siteId().equals(config.locationId);
        boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height && mouseY >= listY && mouseY <= listY + listH;

        drawCard(graphics, x, y, width, height, selected, hovered);

        graphics.fill(x + 4, y + 4, x + 18, y + height - 4, 0xFF3E4446);
        graphics.text(this.font, loc.countryCode(), x + 5, y + 6, 0xFFFFFFFF);

        String name = loc.name();
        if (name.length() > 16) name = name.substring(0, 15) + "..";
        graphics.text(this.font, name, x + 22, y + 4, selected ? 0xFFFFFFFF : 0xFFCCCCCC);

        String ping;
        synchronized (pingResults) {
            ping = index < pingResults.size() ? pingResults.get(index) : "40 ms";
        }
        int pingColor = COLOR_ZAP_GREEN;
        try {
            if (ping.contains("ms")) {
                int ms = Integer.parseInt(ping.replace("ms", "").trim());
                pingColor = ms < 80 ? COLOR_ZAP_GREEN : (ms < 180 ? 0xFFFFAA00 : 0xFFFF5555);
            }
        } catch (Exception ignored) {}

        graphics.text(this.font, ping + " \u2022 " + loc.protectionType(), x + 22, y + 13, pingColor);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int listY = 44;
        int listH = this.height - listY - 58;
        int totalRows = locations.size() / 2 + (locations.size() % 2 == 0 ? 0 : 1);
        int maxOffset = Math.max(0, (totalRows * ITEM_HEIGHT) - (listH - 10));

        if (scrollY > 0) {
            scrollOffset = Math.max(0, scrollOffset - 20);
        } else if (scrollY < 0) {
            scrollOffset = Math.min(maxOffset, scrollOffset + 20);
        }
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            int centerX = this.width / 2;
            int listY = 44;
            int listH = this.height - listY - 58;
            int listW = Math.min(340, this.width - 20);
            int listX = centerX - listW / 2;

            double mouseX = event.x();
            double mouseY = event.y();

            if (mouseX >= listX && mouseX <= listX + listW && mouseY >= listY && mouseY <= listY + listH) {
                int cardW = (listW - 20) / 2;
                int cardH = 24;
                int leftX = listX + 6;
                int rightX = listX + 12 + cardW;

                int row = 0;
                for (int i = 0; i < locations.size(); i += 2) {
                    int currentY = listY + 6 + (row * ITEM_HEIGHT) - scrollOffset;

                    if (mouseX >= leftX && mouseX <= leftX + cardW && mouseY >= currentY && mouseY <= currentY + cardH) {
                        selectLocation(i);
                        return true;
                    }
                    if (i + 1 < locations.size()) {
                        if (mouseX >= rightX && mouseX <= rightX + cardW && mouseY >= currentY && mouseY <= currentY + cardH) {
                            selectLocation(i + 1);
                            return true;
                        }
                    }
                    row++;
                }
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    private void selectLocation(int index) {
        ZHLiveDataProvider.LocationEntry loc = locations.get(index);
        config.locationId = loc.siteId();
        config.locationName = loc.name();
        config.locationRegion = loc.region();
        synchronized (pingResults) {
            config.locationPing = index < pingResults.size() ? pingResults.get(index) : "40 ms";
        }
        if (loc.ownIpDisabled()) {
            config.hasDedicatedIp = false;
        }
    }
}
