package com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.wizard;

import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHOrderConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public abstract class ZHBaseWizardScreen extends Screen {

    public static final int COLOR_ZAP_GREEN = 0xFF5CB85C;
    public static final int COLOR_ZAP_DARK_GREEN = 0xFF449D44;
    public static final int COLOR_ZAP_BG = 0xF0181A1B;
    public static final int COLOR_CARD_BG = 0xFF242729;
    public static final int COLOR_CARD_BORDER = 0xFF3E4446;
    public static final int COLOR_CARD_HOVER = 0xFF35393C;
    public static final int COLOR_CARD_SELECTED = 0xFF5CB85C;

    protected final ZHOrderConfig config;
    protected final int currentStep;
    protected final int totalSteps;
    protected final String stepTitle;

    protected ZHCustomButton backButton;
    protected ZHCustomButton nextButton;

    public static int getTotalSteps(ZHOrderConfig config) {
        if (config == null || config.launcherKey == null) return 10;
        if ("vps".equals(config.launcherKey)) return 6;
        if ("dedicated-server".equals(config.launcherKey)) return 5;
        return 10;
    }

    public static class ZHCustomButton extends Button {

        public enum Style {
            PRIMARY,
            SECONDARY,
            ACTION
        }

        private final Style style;

        public ZHCustomButton(int x, int y, int width, int height, Component message, OnPress onPress, Style style) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
            this.style = style;
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

            if (!this.active) {
                bg = 0xFF1C1E20;
                border = 0xFF2A2D2F;
                textColor = 0xFF555555;
            } else if (style == Style.PRIMARY) {
                if (hovered) {
                    bg = 0xFF4EB84E;
                    border = 0xFF8AE58A;
                    textColor = 0xFFFFFFFF;
                } else {
                    bg = 0xFF449D44;
                    border = 0xFF5CB85C;
                    textColor = 0xFFFFFFFF;
                }
            } else if (style == Style.ACTION) {
                if (hovered) {
                    bg = 0xFF353A3D;
                    border = 0xFF5CB85C;
                    textColor = 0xFF5CB85C;
                } else {
                    bg = 0xFF242729;
                    border = 0xFF3E4446;
                    textColor = 0xFFDDDDDD;
                }
            } else {
                if (hovered) {
                    bg = 0xFF353A3D;
                    border = 0xFF5CB85C;
                    textColor = 0xFFFFFFFF;
                } else {
                    bg = 0xFF242729;
                    border = 0xFF3E4446;
                    textColor = 0xFFCCCCCC;
                }
            }

            graphics.fill(x, y, x + w, y + h, bg);
            graphics.fill(x, y, x + w, y + 1, border);
            graphics.fill(x, y + h - 1, x + w, y + h, border);
            graphics.fill(x, y + 1, x + 1, y + h, border);
            graphics.fill(x + w - 1, y, x + width, y + height, border);

            if (this.active && !hovered) {
                int highlight = (style == Style.PRIMARY) ? 0x30FFFFFF : 0x18FFFFFF;
                graphics.fill(x + 1, y + 1, x + w - 1, y + 2, highlight);
            }

            int textY = y + (h - 8) / 2;
            graphics.centeredText(Minecraft.getInstance().font, getMessage(), x + w / 2, textY, textColor);
        }
    }

    public static abstract class ZHCustomSlider extends AbstractSliderButton {

        protected String bubbleText = null;

        public ZHCustomSlider(int x, int y, int width, int height, Component message, double value) {
            super(x, y, width, height, message, value);
        }

        public double getNormalizedValue() {
            return this.value;
        }

        public void setBubbleText(String text) {
            this.bubbleText = text;
        }

        @Override
        public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            int x = getX();
            int y = getY();
            int w = getWidth();
            int h = getHeight();

            boolean hovered = this.isHoveredOrFocused();

            int trackH = 6;
            int trackY = y + (h - trackH) / 2;

            graphics.fill(x, trackY, x + w, trackY + trackH, 0xFF1C1E20);
            graphics.fill(x, trackY, x + w, trackY + 1, 0xFF2F3438);
            graphics.fill(x, trackY + trackH - 1, x + w, trackY + trackH, 0xFF2F3438);

            int thumbW = 12;
            int thumbX = (int) (x + this.value * (w - thumbW));
            int fillW = thumbX + thumbW / 2 - x;
            if (fillW > 0) {
                graphics.fill(x, trackY, x + fillW, trackY + trackH, COLOR_ZAP_GREEN);
                graphics.fill(x, trackY, x + fillW, trackY + 1, 0xFF7CD47C);
            }

            int thumbH = h - 2;
            int thumbY = y + 1;

            int thumbBg = hovered ? 0xFF353C40 : 0xFF242729;
            int thumbBorder = hovered ? 0xFF8AE58A : COLOR_ZAP_GREEN;

            graphics.fill(thumbX, thumbY, thumbX + thumbW, thumbY + thumbH, thumbBg);
            graphics.fill(thumbX, thumbY, thumbX + thumbW, thumbY + 1, thumbBorder);
            graphics.fill(thumbX, thumbY + thumbH - 1, thumbX + thumbW, thumbY + thumbH, thumbBorder);
            graphics.fill(thumbX, thumbY + 1, thumbX + 1, thumbY + thumbH, thumbBorder);
            graphics.fill(thumbX + thumbW - 1, thumbY, thumbX + thumbW, thumbY + thumbH, thumbBorder);

            int midX = thumbX + thumbW / 2;
            graphics.fill(midX - 1, thumbY + 4, midX, thumbY + thumbH - 4, 0xFF5CB85C);
            graphics.fill(midX + 1, thumbY + 4, midX + 2, thumbY + thumbH - 4, 0xFF5CB85C);

            if (bubbleText != null && !bubbleText.isEmpty()) {
                int bubbleW = Minecraft.getInstance().font.width(bubbleText) + 12;
                int bubbleH = 14;
                int bubbleY = y - 17;
                int bubbleX = Mth.clamp(thumbX + thumbW / 2 - bubbleW / 2, x, x + w - bubbleW);

                graphics.fill(bubbleX, bubbleY, bubbleX + bubbleW, bubbleY + bubbleH, COLOR_ZAP_GREEN);
                graphics.fill(bubbleX, bubbleY, bubbleX + bubbleW, bubbleY + 1, 0xFF8AE58A);

                int arrowX = thumbX + thumbW / 2;
                if (arrowX >= bubbleX + 3 && arrowX <= bubbleX + bubbleW - 4) {
                    graphics.fill(arrowX - 2, bubbleY + bubbleH, arrowX + 2, bubbleY + bubbleH + 1, COLOR_ZAP_GREEN);
                    graphics.fill(arrowX - 1, bubbleY + bubbleH + 1, arrowX + 1, bubbleY + bubbleH + 2, COLOR_ZAP_GREEN);
                }

                graphics.centeredText(Minecraft.getInstance().font, bubbleText, bubbleX + bubbleW / 2, bubbleY + 3, 0xFFFFFFFF);
            }
        }
    }

    public ZHBaseWizardScreen(ZHOrderConfig config, int currentStep, int totalSteps, String stepTitle) {
        super(Component.literal("ZAP-Hosting Server"));
        this.config = config;
        this.currentStep = currentStep;
        this.totalSteps = totalSteps;
        this.stepTitle = stepTitle;
    }

    public ZHBaseWizardScreen(ZHOrderConfig config, int currentStep, String stepTitle) {
        this(config, currentStep, getTotalSteps(config), stepTitle);
    }

    @Override
    protected void init() {
        super.init();
        int btnY = this.height - 27;
        int btnW = 85;
        int btnH = 20;
        int centerX = this.width / 2;

        backButton = new ZHCustomButton(centerX - 175, btnY, btnW, btnH,
                Component.literal(currentStep == 1 ? "Exit" : "< Back"),
                b -> onBack(),
                ZHCustomButton.Style.SECONDARY);
        addRenderableWidget(backButton);

        Component nextText = currentStep == totalSteps ? Component.literal("Order Server ->") : Component.literal("Next >");
        int nextW = currentStep == totalSteps ? 115 : btnW;
        nextButton = new ZHCustomButton(centerX + 85, btnY, nextW, btnH,
                nextText,
                b -> onNext(),
                ZHCustomButton.Style.PRIMARY);
        addRenderableWidget(nextButton);
    }

    protected void onBack() {
        if (currentStep == 1) {
            Minecraft.getInstance().setScreen(new JoinMultiplayerScreen(null));
        } else {
            goToStep(currentStep - 1);
        }
    }

    protected void onNext() {
        if (currentStep < totalSteps) {
            goToStep(currentStep + 1);
        }
    }

    public void goToStep(int step) {
        if ("vps".equals(config.launcherKey)) {
            Screen nextScreen = switch (step) {
                case 1 -> new ZHStep1LauncherScreen(config);
                case 2 -> new ZHStep2PreinstalledGameScreen(config);
                case 3 -> new ZHStep3LocationScreen(config);
                case 4 -> new ZHVpsStep4CpuScreen(config);
                case 5 -> new ZHVpsStep5ResourcesScreen(config);
                case 6 -> new ZHStep10BillingScreen(config);
                default -> new ZHStep1LauncherScreen(config);
            };
            Minecraft.getInstance().setScreen(nextScreen);
            return;
        }

        if ("dedicated-server".equals(config.launcherKey)) {
            Screen nextScreen = switch (step) {
                case 1 -> new ZHStep1LauncherScreen(config);
                case 2 -> new ZHStep2PreinstalledGameScreen(config);
                case 3 -> new ZHStep3LocationScreen(config);
                case 4 -> new ZHDediStep4OsScreen(config);
                case 5 -> new ZHStep10BillingScreen(config);
                default -> new ZHStep1LauncherScreen(config);
            };
            Minecraft.getInstance().setScreen(nextScreen);
            return;
        }

        // Cloud Gameservers (10 steps)
        Screen nextScreen = switch (step) {
            case 1 -> new ZHStep1LauncherScreen(config);
            case 2 -> new ZHStep2PreinstalledGameScreen(config);
            case 3 -> new ZHStep3LocationScreen(config);
            case 4 -> new ZHStep4SlotsScreen(config);
            case 5 -> new ZHStep5MemoryBoostScreen(config);
            case 6 -> new ZHStep6DiskSpaceScreen(config);
            case 7 -> new ZHStep7CpuScreen(config);
            case 8 -> new ZHStep8IpAddressScreen(config);
            case 9 -> new ZHStep9DdosScreen(config);
            case 10 -> new ZHStep10BillingScreen(config);
            default -> new ZHStep1LauncherScreen(config);
        };
        Minecraft.getInstance().setScreen(nextScreen);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        // Draw solid dark opaque background behind every wizard step to prevent bleeding
        graphics.fill(0, 0, this.width, this.height, 0xFF121415);
        graphics.fillGradient(0, 0, this.width, this.height, 0xFF121415, 0xFF0A0B0C);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);

        int centerX = this.width / 2;

        String stepInfo = "Step " + currentStep + " of " + totalSteps + ": " + stepTitle;
        graphics.centeredText(this.font, "ZAP-Hosting Server", centerX, 6, COLOR_ZAP_GREEN);
        graphics.centeredText(this.font, stepInfo, centerX, 17, 0xFFFFFFFF);

        int dotSpacing = Math.min(26, Math.max(14, (this.width - 40) / (totalSteps + 1)));
        int startX = centerX - ((totalSteps - 1) * dotSpacing) / 2;
        int barY = 30;

        graphics.fill(startX, barY + 2, startX + (totalSteps - 1) * dotSpacing, barY + 4, 0xFF444444);
        int activeLength = (currentStep - 1) * dotSpacing;
        if (activeLength > 0) {
            graphics.fill(startX, barY + 2, startX + activeLength, barY + 4, COLOR_ZAP_GREEN);
        }

        for (int i = 1; i <= totalSteps; i++) {
            int dotX = startX + (i - 1) * dotSpacing;
            if (i < currentStep) {
                graphics.fill(dotX - 3, barY, dotX + 3, barY + 6, COLOR_ZAP_GREEN);
            } else if (i == currentStep) {
                graphics.fill(dotX - 5, barY - 2, dotX + 5, barY + 8, COLOR_ZAP_GREEN);
                graphics.fill(dotX - 3, barY, dotX + 3, barY + 6, 0xFFFFFFFF);
            } else {
                graphics.fill(dotX - 3, barY, dotX + 3, barY + 6, 0xFF555555);
            }
        }

        int footerY = this.height - 21;
        String priceText = String.format("Est. Total: $%.2f / Month", config.getTotalMonthlyPrice());
        int textW = this.font.width(priceText);
        int textX = centerX - textW / 2;

        int leftLimit = (backButton != null) ? backButton.getX() + backButton.getWidth() + 8 : 0;
        int rightLimit = this.width;
        if (nextButton != null && nextButton.visible) {
            rightLimit = nextButton.getX() - 8;
        } else if (this instanceof ZHStep10BillingScreen) {
            rightLimit = centerX + 85 - 8;
        }

        if (textX < leftLimit || (textX + textW) > rightLimit) {
            graphics.centeredText(this.font, priceText, centerX, this.height - 38, COLOR_ZAP_GREEN);
        } else {
            graphics.centeredText(this.font, priceText, centerX, footerY, COLOR_ZAP_GREEN);
        }
    }

    public static void drawCard(GuiGraphicsExtractor graphics, int x, int y, int width, int height, boolean selected, boolean hovered) {
        int bg = selected ? 0xFF283B28 : (hovered ? COLOR_CARD_HOVER : COLOR_CARD_BG);
        int border = selected ? COLOR_CARD_SELECTED : (hovered ? 0xFF666666 : COLOR_CARD_BORDER);

        graphics.fill(x, y, x + width, y + height, bg);
        graphics.fill(x, y, x + width, y + 1, border);
        graphics.fill(x, y + height - 1, x + width, y + height, border);
        graphics.fill(x, y + 1, x + 1, y + height, border);
        graphics.fill(x + width - 1, y, x + width, y + height, border);

        if (selected) {
            int badgeSize = 12;
            int bx = x + width - badgeSize - 3;
            int by = y + 3;
            graphics.fill(bx, by, bx + badgeSize, by + badgeSize, COLOR_ZAP_GREEN);
            graphics.text(Minecraft.getInstance().font, "\u2713", bx + 3, by + 2, 0xFFFFFFFF);
        }
    }
}
