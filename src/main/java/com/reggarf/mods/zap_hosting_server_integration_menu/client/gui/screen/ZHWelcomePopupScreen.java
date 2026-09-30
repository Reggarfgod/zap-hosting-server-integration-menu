package com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.reggarf.mods.zap_hosting_server_integration_menu.ZapHosting;
import com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.wizard.ZHBaseWizardScreen;
import com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.wizard.ZHBaseWizardScreen.ZHCustomButton;
import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHOrderConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ZHWelcomePopupScreen extends Screen {

    private static final ResourceLocation LOGO_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ZapHosting.MOD_ID, "textures/gui/logo.png");

    @Nullable
    private final Screen parentScreen;

    private int copiedFeedbackTicks = 0;
    private boolean dontShowAgain = false;

    private ZHCustomButton copyCodeButton;
    private ZHCustomButton configureInGameButton;
    private ZHCustomButton orderOnlineButton;
    private ZHCustomButton continueButton;

    public ZHWelcomePopupScreen(@Nullable Screen parentScreen) {
        super(Component.translatableWithFallback("zap_hosting.popup.title", "ZAP-Hosting Server Partner"));
        this.parentScreen = parentScreen;
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    protected void init() {
        super.init();

        int cardW = Math.min(360, this.width - 24);
        int cardH = Math.min(256, this.height - 24);
        int cardX = (this.width - cardW) / 2;
        int cardY = (this.height - cardH) / 2;

        String code = getPromoCode();
        String link = getAffiliateLink();
        int discount = getDiscountPercent();

        // 1. Copy Code Button inside the voucher card
        int vBoxX = cardX + 16;
        int vBoxY = cardY + 98;
        int vBoxW = cardW - 32;
        int vBoxH = 46;

        int copyBtnW = 75;
        int copyBtnH = 22;
        int copyBtnX = vBoxX + vBoxW - copyBtnW - 8;
        int copyBtnY = vBoxY + (vBoxH - copyBtnH) / 2;

        copyCodeButton = new ZHCustomButton(copyBtnX, copyBtnY, copyBtnW, copyBtnH,
                Component.translatableWithFallback("zap_hosting.popup.copy", "Copy Code"),
                b -> {
                    if (this.minecraft != null) {
                        this.minecraft.keyboardHandler.setClipboard(code);
                        this.copiedFeedbackTicks = 50; // 2.5 seconds
                        updateCopyButtonText();
                    }
                },
                ZHCustomButton.Style.ACTION
        );
        addRenderableWidget(copyCodeButton);

        // 2. Primary Action Button: Configure In-Game (prominent full-width button)
        int configBtnW = cardW - 32;
        int configBtnH = 24;
        int configBtnX = cardX + 16;
        int configBtnY = cardY + 152;

        Component configLabel = Component.translatableWithFallback(
                "zap_hosting.popup.order_wizard",
                "\u2699 Configure In-Game"
        );

        configureInGameButton = new ZHCustomButton(configBtnX, configBtnY, configBtnW, configBtnH,
                configLabel,
                b -> {
                    markSeen();
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new ZHLoadingScreen(new ZHOrderConfig()));
                    }
                },
                ZHCustomButton.Style.PRIMARY
        );
        addRenderableWidget(configureInGameButton);

        // 3. Row 2 buttons: "Order Server Online" and "Continue Playing"
        int subBtnW = (cardW - 38) / 2;
        int subBtnH = 22;
        int subBtnY = cardY + 182;

        Component orderLabel = Component.translatableWithFallback(
                "zap_hosting.popup.order_online",
                "\uD83D\uDE80 Order Server Online"
        );

        orderOnlineButton = new ZHCustomButton(cardX + 16, subBtnY, subBtnW, subBtnH,
                orderLabel,
                b -> {
                    try {
                        Util.getPlatform().openUri(URI.create(link));
                    } catch (Exception ignored) {}
                    markSeenAndClose();
                },
                ZHCustomButton.Style.ACTION
        );
        addRenderableWidget(orderOnlineButton);

        continueButton = new ZHCustomButton(cardX + 22 + subBtnW, subBtnY, subBtnW, subBtnH,
                Component.translatableWithFallback("zap_hosting.popup.continue", "Continue Playing"),
                b -> markSeenAndClose(),
                ZHCustomButton.Style.SECONDARY
        );
        addRenderableWidget(continueButton);
    }

    private void updateCopyButtonText() {
        if (copyCodeButton != null) {
            if (copiedFeedbackTicks > 0) {
                copyCodeButton.setMessage(Component.translatableWithFallback("zap_hosting.popup.copied", "\u2713 Copied!")
                        .copy().withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
            } else {
                copyCodeButton.setMessage(Component.translatableWithFallback("zap_hosting.popup.copy", "Copy Code"));
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (copiedFeedbackTicks > 0) {
            copiedFeedbackTicks--;
            if (copiedFeedbackTicks == 0) {
                updateCopyButtonText();
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int cardW = Math.min(360, this.width - 24);
        int cardH = Math.min(256, this.height - 24);
        int cardX = (this.width - cardW) / 2;
        int cardY = (this.height - cardH) / 2;

        // Checkbox click toggle in footer
        int chkX = cardX + 16;
        int chkY = cardY + 218;
        Component chkLabel = Component.translatableWithFallback("zap_hosting.popup.dont_show", "Don't show this again");
        int chkW = this.font.width(chkLabel) + 20;
        int chkH = 14;

        if (mouseX >= chkX && mouseX <= chkX + chkW && mouseY >= chkY && mouseY <= chkY + chkH) {
            dontShowAgain = !dontShowAgain;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC key
            markSeenAndClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void markSeen() {
        if (dontShowAgain && ZapHosting.CONFIG != null && ZapHosting.CONFIG.common != null) {
            ZapHosting.CONFIG.common.enableOverlay = false;
            ZapHosting.CONFIG.save();
        }
    }

    private void markSeenAndClose() {
        markSeen();
        onClose();
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parentScreen);
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Blur and dim the 3D in-game world behind the popup BEFORE drawing card content
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fillGradient(0, 0, this.width, this.height, 0x880A0C0E, 0xAA121517);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 1. Render background world blur and dimming FIRST
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        int cardW = Math.min(360, this.width - 24);
        int cardH = Math.min(256, this.height - 24);
        int cardX = (this.width - cardW) / 2;
        int cardY = (this.height - cardH) / 2;

        // 2. Card Panel
        ZHBaseWizardScreen.drawCard(graphics, cardX, cardY, cardW, cardH, false, false);

        // Top Accent Strip
        graphics.fill(cardX + 1, cardY + 1, cardX + cardW - 1, cardY + 3, ZHBaseWizardScreen.COLOR_ZAP_GREEN);

        // 3. Header: Logo + Title + Badge
        int logoSize = 28;
        int logoX = cardX + 16;
        int logoY = cardY + 12;

        // Logo background frame
        graphics.fill(logoX - 2, logoY - 2, logoX + logoSize + 2, logoY + logoSize + 2, 0xFF263228);
        graphics.renderOutline(logoX - 2, logoY - 2, logoSize + 4, logoSize + 4, ZHBaseWizardScreen.COLOR_ZAP_GREEN);

        RenderSystem.enableBlend();
        graphics.blit(LOGO_TEXTURE, logoX, logoY, 0.0F, 0.0F, logoSize, logoSize, logoSize, logoSize);
        RenderSystem.disableBlend();

        int headerTextX = logoX + logoSize + 10;
        graphics.drawString(this.font, Component.literal("ZAP-HOSTING").withStyle(ChatFormatting.BOLD),
                headerTextX, logoY + 3, ZHBaseWizardScreen.COLOR_ZAP_GREEN);

        Component subtitle = Component.translatableWithFallback("zap_hosting.popup.badge", "OFFICIAL SERVER PARTNER");
        graphics.drawString(this.font, subtitle, headerTextX, logoY + 16, 0xFF8E9396);

        // Header separator
        graphics.fill(cardX + 16, cardY + 48, cardX + cardW - 16, cardY + 49, 0xFF2A2D2F);

        // 4. Main Promo Headline & Description
        Component headline = Component.translatableWithFallback(
                "zap_hosting.popup.headline",
                "Ready to play with friends or host this modpack?"
        ).copy().withStyle(ChatFormatting.BOLD);
        graphics.drawString(this.font, headline, cardX + 16, cardY + 56, 0xFFFFFFFF);

        Component descLine1 = Component.translatableWithFallback(
                "zap_hosting.popup.desc1",
                "Get reliable DDoS-protected game servers with instant setup"
        );
        graphics.drawString(this.font, descLine1, cardX + 16, cardY + 69, 0xFFB0B5B8);

        Component descLine2 = Component.translatableWithFallback(
                "zap_hosting.popup.desc2",
                "and low ping worldwide from ZAP-Hosting!"
        );
        graphics.drawString(this.font, descLine2, cardX + 16, cardY + 80, 0xFFB0B5B8);

        // 5. Voucher Code Highlight Box
        int vBoxX = cardX + 16;
        int vBoxY = cardY + 98;
        int vBoxW = cardW - 32;
        int vBoxH = 46;

        // Voucher Box Background & Green Border
        graphics.fill(vBoxX, vBoxY, vBoxX + vBoxW, vBoxY + vBoxH, 0xFF19221C);
        graphics.renderOutline(vBoxX, vBoxY, vBoxW, vBoxH, ZHBaseWizardScreen.COLOR_ZAP_GREEN);

        // Voucher label
        graphics.drawString(this.font,
                Component.translatableWithFallback("zap_hosting.popup.voucher_label", "PROMO VOUCHER CODE").copy().withStyle(ChatFormatting.BOLD),
                vBoxX + 10, vBoxY + 7, 0xFF7AE67A);

        // Voucher Code (Golden bold text)
        String code = getPromoCode();
        Component codeComp = Component.literal(code).withStyle(ChatFormatting.BOLD);
        int codeX = vBoxX + 10;
        int codeY = vBoxY + 23;
        int codeW = this.font.width(codeComp);
        graphics.drawString(this.font, codeComp, codeX, codeY, 0xFFFFD700);

        // Discount Tag Pill Badge (dynamically positioned with proper padding to prevent any text overlap)
        int discount = getDiscountPercent();
        Component tagComp = Component.literal(discount + "% OFF").withStyle(ChatFormatting.BOLD);
        int tagTextW = this.font.width(tagComp);
        int tagPaddingX = 5;
        int tagW = tagTextW + tagPaddingX * 2;
        int tagH = 13;
        int tagX = codeX + codeW + 8;
        int tagY = vBoxY + 21;

        graphics.fill(tagX, tagY, tagX + tagW, tagY + tagH, 0xFF3E9B3E);
        graphics.renderOutline(tagX, tagY, tagW, tagH, 0xFF63C963);
        graphics.drawString(this.font, tagComp, tagX + tagPaddingX, tagY + 2, 0xFFFFFFFF);

        // 6. Don't Show Again Checkbox (at the bottom)
        int chkX = cardX + 16;
        int chkY = cardY + 218;
        int boxSize = 10;

        // Checkbox box
        graphics.fill(chkX, chkY, chkX + boxSize, chkY + boxSize, 0xFF242729);
        graphics.renderOutline(chkX, chkY, boxSize, boxSize, dontShowAgain ? ZHBaseWizardScreen.COLOR_ZAP_GREEN : 0xFF555555);
        if (dontShowAgain) {
            graphics.fill(chkX + 2, chkY + 2, chkX + boxSize - 2, chkY + boxSize - 2, ZHBaseWizardScreen.COLOR_ZAP_GREEN);
        }

        Component chkLabel = Component.translatableWithFallback("zap_hosting.popup.dont_show", "Don't show this again");
        graphics.drawString(this.font, chkLabel, chkX + boxSize + 6, chkY + 1, 0xFF9E9E9E);

        // 7. Render interactive widgets (drawn directly on top, completely sharp and unblurred)
        for (GuiEventListener child : this.children()) {
            if (child instanceof Renderable renderable) {
                renderable.render(graphics, mouseX, mouseY, partialTick);
            }
        }
    }

    private String getPromoCode() {
        return (ZapHosting.CONFIG != null && ZapHosting.CONFIG.common != null && ZapHosting.CONFIG.common.code != null)
                ? ZapHosting.CONFIG.common.code : "REGGARF-1047";
    }

    private int getDiscountPercent() {
        return (ZapHosting.CONFIG != null && ZapHosting.CONFIG.common != null)
                ? ZapHosting.CONFIG.common.discountPercent : 20;
    }

    private String getAffiliateLink() {
        String code = getPromoCode();
        String link = (ZapHosting.CONFIG != null && ZapHosting.CONFIG.common != null && ZapHosting.CONFIG.common.link != null)
                ? ZapHosting.CONFIG.common.link : "https://zap-hosting.com/createletscreate";

        if (code != null && !code.trim().isEmpty() && link != null && !link.contains("voucher=")) {
            link += (link.contains("?") ? "&voucher=" : "?voucher=") + URLEncoder.encode(code.trim(), StandardCharsets.UTF_8);
        }
        return link;
    }
}
