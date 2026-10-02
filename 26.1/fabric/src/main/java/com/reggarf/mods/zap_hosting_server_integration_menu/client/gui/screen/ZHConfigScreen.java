package com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen;

import com.reggarf.mods.zap_hosting_server_integration_menu.ZapHosting;
import com.reggarf.mods.zap_hosting_server_integration_menu.config.ZHConfig;
import com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.wizard.ZHBaseWizardScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ZHConfigScreen extends Screen {

    private final Screen parent;
    private int activeTab = 0; // 0: Affiliate & Promo, 1: Public Server

    // Temporary working values
    private boolean tempEnableOverlay;
    private String tempLink;
    private String tempPartnerId;
    private String tempCode;
    private int tempDiscountPercent;

    private boolean tempEnablePublicServer;
    private String tempPublicServerName;
    private String tempPublicServerIp;
    private String tempPublicServerMotd;
    private boolean tempEnableCustomJoinScreen;

    // Tab buttons
    private ZHBaseWizardScreen.ZHCustomButton tabAffiliateBtn;
    private ZHBaseWizardScreen.ZHCustomButton tabPublicServerBtn;

    // Tab 0 Widgets (Affiliate & Promo)
    private ZHToggleButton overlayToggle;
    private ZHBaseWizardScreen.ZHCustomButton previewPopupBtn;
    private EditBox linkBox;
    private EditBox partnerIdBox;
    private EditBox codeBox;
    private ZHDiscountSlider discountSlider;

    // Tab 1 Widgets (Public Server)
    private ZHToggleButton publicServerToggle;
    private EditBox serverNameBox;
    private EditBox serverIpBox;
    private EditBox serverMotdBox;
    private ZHToggleButton customJoinToggle;

    // Footer buttons (Always anchored at screen bottom)
    private ZHBaseWizardScreen.ZHCustomButton resetBtn;
    private ZHBaseWizardScreen.ZHCustomButton cancelBtn;
    private ZHBaseWizardScreen.ZHCustomButton saveBtn;

    // Layout coordinates
    private int cardX;
    private int cardY;
    private int cardW;
    private int cardH;
    private int rowCount = 5;
    private int rowH = 36;
    private int contentStartY;
    private int controlW;
    private int controlH = 20;
    private int controlX;
    private int labelX;

    // List of registered edit box containers for custom styling
    private static record BoxContainer(EditBox box, int containerX, int containerY, int containerW, int containerH) {}
    private final List<BoxContainer> editBoxContainers = new ArrayList<>();

    public ZHConfigScreen(Screen parent) {
        super(Component.translatableWithFallback("zap_hosting.config.title", "ZAP-Hosting Configuration"));
        this.parent = parent;

        ZHConfig.Common cfg = (ZapHosting.CONFIG != null && ZapHosting.CONFIG.common != null)
                ? ZapHosting.CONFIG.common
                : new ZHConfig.Common();

        this.tempEnableOverlay = cfg.enableOverlay;
        this.tempLink = cfg.link != null ? cfg.link : "https://zap-hosting.com/createletscreate";
        this.tempPartnerId = cfg.partnerId != null ? cfg.partnerId : "zap1204486";
        this.tempCode = cfg.code != null ? cfg.code : "REGGARF-1047";
        this.tempDiscountPercent = cfg.discountPercent;

        this.tempEnablePublicServer = cfg.enablePublicServer;
        this.tempPublicServerName = cfg.publicServerName != null ? cfg.publicServerName : "ZAP-Hosting Official Server";
        this.tempPublicServerIp = cfg.publicServerIp != null ? cfg.publicServerIp : "play.zap-hosting.com";
        this.tempPublicServerMotd = cfg.publicServerMotd != null ? cfg.publicServerMotd : "Official Public Server hosted by ZAP-Hosting";
        this.tempEnableCustomJoinScreen = cfg.enableCustomJoinScreen;
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();
        this.editBoxContainers.clear();

        int centerX = this.width / 2;

        // 1. Top Navigation Tabs
        int tabW = 160;
        int tabH = 22;
        int tabGap = 8;
        int tabsTotalW = tabW * 2 + tabGap;
        int tabsStartX = centerX - tabsTotalW / 2;
        int tabY = 40;

        tabAffiliateBtn = new ZHBaseWizardScreen.ZHCustomButton(
                tabsStartX, tabY, tabW, tabH,
                Component.translatableWithFallback("zap_hosting.config.tab.affiliate", "\uD83D\uDCB0 Affiliate & Promo"),
                b -> switchTab(0),
                activeTab == 0 ? ZHBaseWizardScreen.ZHCustomButton.Style.PRIMARY : ZHBaseWizardScreen.ZHCustomButton.Style.SECONDARY
        );
        addRenderableWidget(tabAffiliateBtn);

        tabPublicServerBtn = new ZHBaseWizardScreen.ZHCustomButton(
                tabsStartX + tabW + tabGap, tabY, tabW, tabH,
                Component.translatableWithFallback("zap_hosting.config.tab.public_server", "\uD83C\uDF10 Public Multiplayer Server"),
                b -> switchTab(1),
                activeTab == 1 ? ZHBaseWizardScreen.ZHCustomButton.Style.PRIMARY : ZHBaseWizardScreen.ZHCustomButton.Style.SECONDARY
        );
        addRenderableWidget(tabPublicServerBtn);

        // 2. Footer Action Buttons (ALWAYS anchored to the very bottom of the screen)
        int footerBtnW = 115;
        int footerBtnH = 20;
        int footerSpacing = 12;
        int footerBtnY = this.height - 28;

        resetBtn = new ZHBaseWizardScreen.ZHCustomButton(
                centerX - footerBtnW - footerSpacing - footerBtnW / 2, footerBtnY,
                footerBtnW, footerBtnH,
                Component.translatableWithFallback("zap_hosting.config.reset", "\u21BA Reset Defaults"),
                b -> resetToDefaults(),
                ZHBaseWizardScreen.ZHCustomButton.Style.SECONDARY
        );
        addRenderableWidget(resetBtn);

        cancelBtn = new ZHBaseWizardScreen.ZHCustomButton(
                centerX - footerBtnW / 2, footerBtnY,
                footerBtnW, footerBtnH,
                Component.translatableWithFallback("gui.cancel", "Cancel"),
                b -> onClose(),
                ZHBaseWizardScreen.ZHCustomButton.Style.SECONDARY
        );
        addRenderableWidget(cancelBtn);

        saveBtn = new ZHBaseWizardScreen.ZHCustomButton(
                centerX + footerBtnW / 2 + footerSpacing, footerBtnY,
                footerBtnW, footerBtnH,
                Component.translatableWithFallback("zap_hosting.config.save", "\u2713 Save & Exit"),
                b -> saveAndClose(),
                ZHBaseWizardScreen.ZHCustomButton.Style.PRIMARY
        );
        addRenderableWidget(saveBtn);

        // 3. Central Card Container Calculations
        cardW = Math.min(520, this.width - 24);
        cardX = centerX - cardW / 2;
        cardY = tabY + tabH + 8;

        int availableCardH = (footerBtnY - 10) - cardY;
        rowH = Math.max(26, Math.min(40, (availableCardH - 16) / rowCount));
        cardH = rowCount * rowH + 16;
        contentStartY = cardY + 8 + (availableCardH - cardH) / 2;
        cardY = contentStartY - 8;

        controlW = Math.min(230, cardW / 2 - 16);
        controlH = Math.min(20, rowH - 6);
        controlX = cardX + cardW - controlW - 16;
        labelX = cardX + 16;

        // --- Build Tab 0 Widgets (Affiliate & Promo) ---
        // Row 1: Overlay Toggle & Preview Button
        int r0Y = contentStartY + (rowH - controlH) / 2;
        int previewBtnW = 68;
        int toggleW = controlW - previewBtnW - 6;
        overlayToggle = new ZHToggleButton(controlX, r0Y, toggleW, controlH, tempEnableOverlay, v -> tempEnableOverlay = v);
        addRenderableWidget(overlayToggle);

        previewPopupBtn = new ZHBaseWizardScreen.ZHCustomButton(
                controlX + toggleW + 6, r0Y, previewBtnW, controlH,
                Component.translatableWithFallback("zap_hosting.config.preview_popup", "\uD83D\uDC41 Preview"),
                b -> {
                    // Update draft values so the preview reflects currently entered details
                    if (ZapHosting.CONFIG != null && ZapHosting.CONFIG.common != null) {
                        ZapHosting.CONFIG.common.link = tempLink;
                        ZapHosting.CONFIG.common.partnerId = tempPartnerId;
                        ZapHosting.CONFIG.common.code = tempCode;
                        ZapHosting.CONFIG.common.discountPercent = tempDiscountPercent;
                    }
                    Minecraft.getInstance().setScreen(new ZHWelcomePopupScreen(this));
                },
                ZHBaseWizardScreen.ZHCustomButton.Style.ACTION
        );
        addRenderableWidget(previewPopupBtn);

        // Row 2: Partner Campaign Link
        int r1Y = contentStartY + rowH + (rowH - controlH) / 2;
        linkBox = createEditBox(controlX, r1Y, controlW, controlH, tempLink, val -> tempLink = val);
        addRenderableWidget(linkBox);

        // Row 3: Partner ID
        int r2Y = contentStartY + rowH * 2 + (rowH - controlH) / 2;
        partnerIdBox = createEditBox(controlX, r2Y, controlW, controlH, tempPartnerId, val -> tempPartnerId = val);
        addRenderableWidget(partnerIdBox);

        // Row 4: Voucher Code
        int r3Y = contentStartY + rowH * 3 + (rowH - controlH) / 2;
        codeBox = createEditBox(controlX, r3Y, controlW, controlH, tempCode, val -> tempCode = val);
        addRenderableWidget(codeBox);

        // Row 5: Discount %
        int r4Y = contentStartY + rowH * 4 + (rowH - controlH) / 2;
        discountSlider = new ZHDiscountSlider(controlX, r4Y, controlW, controlH, tempDiscountPercent, val -> tempDiscountPercent = val);
        addRenderableWidget(discountSlider);

        // --- Build Tab 1 Widgets (Public Server) ---
        // Row 1: Public Server Enable
        publicServerToggle = new ZHToggleButton(controlX, r0Y, controlW, controlH, tempEnablePublicServer, v -> tempEnablePublicServer = v);
        addRenderableWidget(publicServerToggle);

        // Row 2: Public Server Name
        serverNameBox = createEditBox(controlX, r1Y, controlW, controlH, tempPublicServerName, val -> tempPublicServerName = val);
        addRenderableWidget(serverNameBox);

        // Row 3: Public Server IP
        serverIpBox = createEditBox(controlX, r2Y, controlW, controlH, tempPublicServerIp, val -> tempPublicServerIp = val);
        addRenderableWidget(serverIpBox);

        // Row 4: Public Server MOTD
        serverMotdBox = createEditBox(controlX, r3Y, controlW, controlH, tempPublicServerMotd, val -> tempPublicServerMotd = val);
        addRenderableWidget(serverMotdBox);

        // Row 5: Custom Join Screen Toggle
        customJoinToggle = new ZHToggleButton(controlX, r4Y, controlW, controlH, tempEnableCustomJoinScreen, v -> tempEnableCustomJoinScreen = v);
        addRenderableWidget(customJoinToggle);

        updateTabVisibility();
    }

    private EditBox createEditBox(int x, int y, int width, int height, String initialValue, Consumer<String> responder) {
        int innerPaddingX = 6;
        int innerPaddingY = (height - 9) / 2;
        EditBox box = new EditBox(this.font, x + innerPaddingX, y + innerPaddingY, width - innerPaddingX * 2, 9, Component.empty());
        box.setMaxLength(256);
        box.setValue(initialValue != null ? initialValue : "");
        box.moveCursorToStart(false);
        box.setBordered(false);
        box.setTextColor(0xFFFFFFFF);
        box.setTextColorUneditable(0xFF777777);
        box.setResponder(responder);
        editBoxContainers.add(new BoxContainer(box, x, y, width, height));
        return box;
    }

    private void switchTab(int tab) {
        if (this.activeTab == tab) return;
        this.activeTab = tab;
        rebuildWidgets();
    }

    private void updateTabVisibility() {
        boolean showTab0 = activeTab == 0;
        boolean showTab1 = activeTab == 1;

        overlayToggle.visible = showTab0;
        overlayToggle.active = showTab0;
        previewPopupBtn.visible = showTab0;
        previewPopupBtn.active = showTab0;
        linkBox.visible = showTab0;
        linkBox.setEditable(showTab0);
        partnerIdBox.visible = showTab0;
        partnerIdBox.setEditable(showTab0);
        codeBox.visible = showTab0;
        codeBox.setEditable(showTab0);
        discountSlider.visible = showTab0;
        discountSlider.active = showTab0;

        publicServerToggle.visible = showTab1;
        publicServerToggle.active = showTab1;
        serverNameBox.visible = showTab1;
        serverNameBox.setEditable(showTab1);
        serverIpBox.visible = showTab1;
        serverIpBox.setEditable(showTab1);
        serverMotdBox.visible = showTab1;
        serverMotdBox.setEditable(showTab1);
        customJoinToggle.visible = showTab1;
        customJoinToggle.active = showTab1;
    }

    private void resetToDefaults() {
        ZHConfig.Common def = new ZHConfig.Common();

        if (activeTab == 0) {
            tempEnableOverlay = def.enableOverlay;
            tempLink = def.link;
            tempPartnerId = def.partnerId;
            tempCode = def.code;
            tempDiscountPercent = def.discountPercent;

            overlayToggle.setValue(tempEnableOverlay);
            linkBox.setValue(tempLink);
            linkBox.moveCursorToStart(false);
            partnerIdBox.setValue(tempPartnerId);
            partnerIdBox.moveCursorToStart(false);
            codeBox.setValue(tempCode);
            codeBox.moveCursorToStart(false);
            discountSlider.setPercent(tempDiscountPercent);

        } else {
            tempEnablePublicServer = def.enablePublicServer;
            tempPublicServerName = def.publicServerName;
            tempPublicServerIp = def.publicServerIp;
            tempPublicServerMotd = def.publicServerMotd;
            tempEnableCustomJoinScreen = def.enableCustomJoinScreen;

            publicServerToggle.setValue(tempEnablePublicServer);
            serverNameBox.setValue(tempPublicServerName);
            serverNameBox.moveCursorToStart(false);
            serverIpBox.setValue(tempPublicServerIp);
            serverIpBox.moveCursorToStart(false);
            serverMotdBox.setValue(tempPublicServerMotd);
            serverMotdBox.moveCursorToStart(false);
            customJoinToggle.setValue(tempEnableCustomJoinScreen);
        }
    }

    private void saveAndClose() {
        if (ZapHosting.CONFIG == null) {
            ZapHosting.CONFIG = new ZHConfig();
        }
        if (ZapHosting.CONFIG.common == null) {
            ZapHosting.CONFIG.common = new ZHConfig.Common();
        }

        ZHConfig.Common cfg = ZapHosting.CONFIG.common;
        cfg.enableOverlay = tempEnableOverlay;
        cfg.link = (tempLink != null && !tempLink.trim().isEmpty()) ? tempLink.trim() : "https://zap-hosting.com/createletscreate";
        cfg.partnerId = (tempPartnerId != null && !tempPartnerId.trim().isEmpty()) ? tempPartnerId.trim() : "zap1204486";
        cfg.code = (tempCode != null && !tempCode.trim().isEmpty()) ? tempCode.trim() : "REGGARF-1047";
        cfg.discountPercent = tempDiscountPercent;

        cfg.enablePublicServer = tempEnablePublicServer;
        cfg.publicServerName = (tempPublicServerName != null && !tempPublicServerName.trim().isEmpty()) ? tempPublicServerName.trim() : "ZAP-Hosting Official Server";
        cfg.publicServerIp = (tempPublicServerIp != null && !tempPublicServerIp.trim().isEmpty()) ? tempPublicServerIp.trim() : "play.zap-hosting.com";
        cfg.publicServerMotd = (tempPublicServerMotd != null && !tempPublicServerMotd.trim().isEmpty()) ? tempPublicServerMotd.trim() : "Official Public Server hosted by ZAP-Hosting";
        cfg.enableCustomJoinScreen = tempEnableCustomJoinScreen;

        ZapHosting.CONFIG.save();
        onClose();
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(this.parent);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(0, 0, this.width, this.height, 0xFF141617, 0xFF0B0C0D);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {

        int centerX = this.width / 2;

        // Header Title & Subtitle
        graphics.centeredText(this.font,
                Component.translatableWithFallback("zap_hosting.config.title", "ZAP-Hosting Configuration").withStyle(ChatFormatting.BOLD),
                centerX, 10, ZHBaseWizardScreen.COLOR_ZAP_GREEN);
        graphics.centeredText(this.font,
                Component.translatableWithFallback("zap_hosting.config.subtitle", "Customize affiliate links, voucher promo codes, and public server list integration"),
                centerX, 23, 0xFFAAAAAA);

        // Header bottom accent line
        graphics.fill(centerX - 180, 34, centerX + 180, 35, ZHBaseWizardScreen.COLOR_ZAP_GREEN);

        // Central Container Card Panel
        ZHBaseWizardScreen.drawCard(graphics, cardX, cardY, cardW, cardH, false, false);

        // Card Header Accent Stripe
        graphics.fill(cardX + 1, cardY + 1, cardX + cardW - 1, cardY + 3, ZHBaseWizardScreen.COLOR_ZAP_GREEN);

        // Draw Row Separators and Labels
        for (int i = 0; i < rowCount; i++) {
            int currentY = contentStartY + i * rowH;

            if (i > 0) {
                graphics.fill(cardX + 12, currentY - 2, cardX + cardW - 12, currentY - 1, 0xFF2A2D2F);
            }

            Component title;
            Component desc;

            if (activeTab == 0) {
                switch (i) {
                    case 0 -> {
                        title = Component.translatableWithFallback("zap_hosting.config.overlay.title", "In-Game Welcome Popup");
                        desc = Component.translatableWithFallback("zap_hosting.config.overlay.desc", "Show promo popup GUI on 1st time world join");
                    }
                    case 1 -> {
                        title = Component.translatableWithFallback("zap_hosting.config.link.title", "Partner Campaign Link");
                        desc = Component.translatableWithFallback("zap_hosting.config.link.desc", "URL pinged to credit partner order clicks");
                    }
                    case 2 -> {
                        title = Component.translatableWithFallback("zap_hosting.config.partner_id.title", "Partner ID (Affiliate Ref)");
                        desc = Component.translatableWithFallback("zap_hosting.config.partner_id.desc", "Appended to prefilled orders as ?ref=...");
                    }
                    case 3 -> {
                        title = Component.translatableWithFallback("zap_hosting.config.code.title", "Promo Voucher Code");
                        desc = Component.translatableWithFallback("zap_hosting.config.code.desc", "Auto-applied promo code at checkout");
                    }
                    default -> {
                        title = Component.translatableWithFallback("zap_hosting.config.discount.title", "Voucher Discount %");
                        desc = Component.translatableWithFallback("zap_hosting.config.discount.desc", "Discount displayed in server configuration");
                    }
                }
            } else {
                switch (i) {
                    case 0 -> {
                        title = Component.translatableWithFallback("zap_hosting.config.public_server.title", "Enable Public Server");
                        desc = Component.translatableWithFallback("zap_hosting.config.public_server.desc", "Pin permanent server to multiplayer list");
                    }
                    case 1 -> {
                        title = Component.translatableWithFallback("zap_hosting.config.server_name.title", "Server Display Name");
                        desc = Component.translatableWithFallback("zap_hosting.config.server_name.desc", "Top title of the multiplayer entry");
                    }
                    case 2 -> {
                        title = Component.translatableWithFallback("zap_hosting.config.server_ip.title", "Server IP / Domain");
                        desc = Component.translatableWithFallback("zap_hosting.config.server_ip.desc", "Minecraft server host address");
                    }
                    case 3 -> {
                        title = Component.translatableWithFallback("zap_hosting.config.server_motd.title", "Server MOTD / Description");
                        desc = Component.translatableWithFallback("zap_hosting.config.server_motd.desc", "Subtitle shown under server title");
                    }
                    default -> {
                        title = Component.translatableWithFallback("zap_hosting.config.custom_join.title", "Custom Connect Screen");
                        desc = Component.translatableWithFallback("zap_hosting.config.custom_join.desc", "Themed ZAP loading screen on connect");
                    }
                }
            }

            int titleY = currentY + (rowH >= 32 ? 2 : 1);
            int descY = currentY + 13;

            graphics.text(this.font, Component.empty().append(title).withStyle(ChatFormatting.BOLD), labelX, titleY, 0xFFFFFFFF);
            if (rowH >= 30) {
                graphics.text(this.font, desc, labelX, descY, 0xFF8E9396);
            }
        }

        // Draw Custom Frames around EditBoxes
        for (BoxContainer container : editBoxContainers) {
            if (container.box.visible) {
                int bx = container.containerX;
                int by = container.containerY;
                int bw = container.containerW;
                int bh = container.containerH;

                boolean hovered = mouseX >= bx && mouseX <= bx + bw && mouseY >= by && mouseY <= by + bh;
                int bg = 0xFF181A1B;
                int border = container.box.isFocused()
                        ? ZHBaseWizardScreen.COLOR_ZAP_GREEN
                        : (hovered ? 0xFF666E73 : ZHBaseWizardScreen.COLOR_CARD_BORDER);

                graphics.fill(bx, by, bx + bw, by + bh, bg);
                graphics.fill(bx, by, bx + bw, by + 1, border);
                graphics.fill(bx, by + bh - 1, bx + bw, by + bh, border);
                graphics.fill(bx, by + 1, bx + 1, by + bh, border);
                graphics.fill(bx + bw - 1, by, bx + bw, by + bh, border);
            }
        }

        // Draw interactive widgets
        for (GuiEventListener child : this.children()) {
            if (child instanceof Renderable renderable) {
                renderable.extractRenderState(graphics, mouseX, mouseY, partialTick);
            }
        }
    }

    // Custom ZAP Toggle Switch Button
    public static class ZHToggleButton extends Button {
        private boolean value;
        private final Consumer<Boolean> onChange;

        public ZHToggleButton(int x, int y, int width, int height, boolean initialValue, Consumer<Boolean> onChange) {
            super(x, y, width, height, getButtonText(initialValue), b -> {
                ZHToggleButton tb = (ZHToggleButton) b;
                tb.value = !tb.value;
                tb.setMessage(getButtonText(tb.value));
                if (tb.onChange != null) {
                    tb.onChange.accept(tb.value);
                }
            }, DEFAULT_NARRATION);
            this.value = initialValue;
            this.onChange = onChange;
        }

        private static Component getButtonText(boolean val) {
            return val
                    ? Component.translatableWithFallback("zap_hosting.config.enabled", "\u2714 Enabled").copy().withStyle(ChatFormatting.BOLD)
                    : Component.translatableWithFallback("zap_hosting.config.disabled", "\u2716 Disabled");
        }

        public boolean getValue() {
            return this.value;
        }

        public void setValue(boolean val) {
            this.value = val;
            this.setMessage(getButtonText(val));
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            boolean hovered = this.isHoveredOrFocused();
            int x = getX();
            int y = getY();
            int w = getWidth();
            int h = getHeight();

            int bg;
            int border;
            int textColor;

            if (value) {
                bg = hovered ? 0xFF4EB84E : 0xFF3E9B3E;
                border = hovered ? 0xFF8AE58A : ZHBaseWizardScreen.COLOR_ZAP_GREEN;
                textColor = 0xFFFFFFFF;
            } else {
                bg = hovered ? 0xFF35393C : 0xFF242729;
                border = hovered ? 0xFF666666 : 0xFF3E4446;
                textColor = 0xFF888888;
            }

            graphics.fill(x, y, x + w, y + h, bg);
            graphics.fill(x, y, x + w, y + 1, border);
            graphics.fill(x, y + h - 1, x + w, y + h, border);
            graphics.fill(x, y + 1, x + 1, y + h, border);
            graphics.fill(x + w - 1, y, x + w, y + h, border);

            int textY = y + (h - 8) / 2;
            graphics.centeredText(Minecraft.getInstance().font, getMessage(), x + w / 2, textY, textColor);
        }
    }

    // Custom ZAP Discount Slider
    public static class ZHDiscountSlider extends ZHBaseWizardScreen.ZHCustomSlider {
        private int percent;
        private final Consumer<Integer> onChange;

        public ZHDiscountSlider(int x, int y, int width, int height, int initialPercent, Consumer<Integer> onChange) {
            super(x, y, width, height, Component.literal(initialPercent + "%"), initialPercent / 100.0);
            this.percent = initialPercent;
            this.onChange = onChange;
            setBubbleText(this.percent + "%");
        }

        @Override
        protected void updateMessage() {
            this.percent = (int) Math.round(this.value * 100.0);
            setMessage(Component.literal(this.percent + "%"));
            setBubbleText(this.percent + "%");
        }

        @Override
        protected void applyValue() {
            this.percent = (int) Math.round(this.value * 100.0);
            setBubbleText(this.percent + "%");
            if (this.onChange != null) {
                this.onChange.accept(this.percent);
            }
        }

        public int getPercent() {
            return this.percent;
        }

        public void setPercent(int pct) {
            this.percent = Math.max(0, Math.min(100, pct));
            this.value = this.percent / 100.0;
            updateMessage();
        }
    }
}
