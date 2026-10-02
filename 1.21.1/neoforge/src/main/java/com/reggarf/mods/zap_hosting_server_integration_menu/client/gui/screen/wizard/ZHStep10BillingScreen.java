package com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.wizard;

import com.reggarf.mods.zap_hosting_server_integration_menu.api.ZHOrderLinkGenerator;
import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHLiveDataProvider;
import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHOrderConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;

public class ZHStep10BillingScreen extends ZHBaseWizardScreen {

    private List<ZHLiveDataProvider.OptionStep> billingSteps;
    private BillingSlider slider;
    private ZHCustomButton orderButton;
    private boolean isGenerating = false;
    private String statusMessage = "";

    public ZHStep10BillingScreen(ZHOrderConfig config) {
        super(config, getTotalSteps(config), getTotalSteps(config), "Billing Interval & Checkout");
        this.billingSteps = ZHLiveDataProvider.getBillingSteps();
    }

    @Override
    protected void init() {
        super.init();
        this.billingSteps = ZHLiveDataProvider.getBillingSteps();

        int centerX = this.width / 2;

        int initialIndex = 0;
        for (int i = 0; i < billingSteps.size(); i++) {
            if (billingSteps.get(i).id().equals(config.billingOptionId) || billingSteps.get(i).numericValue() == config.billingIntervalDays) {
                initialIndex = i;
                break;
            }
        }

        int sliderW = Math.min(300, this.width - 40);
        slider = new BillingSlider(centerX - sliderW / 2, 45, sliderW, 20, (double) initialIndex / Math.max(1, billingSteps.size() - 1));
        addWidget(slider);

        removeWidget(nextButton);

        int btnY = this.height - 27;
        int orderBtnW = 135;
        orderButton = new ZHCustomButton(centerX + 75, btnY, orderBtnW, 20,
                Component.literal("🚀 Order Server Now"),
                b -> startOrderProcess(),
                ZHCustomButton.Style.PRIMARY);
        addRenderableWidget(orderButton);
    }

    private void startOrderProcess() {
        if (isGenerating) return;
        isGenerating = true;
        statusMessage = "Generating prefilled order link...";
        orderButton.active = false;

        ZHOrderLinkGenerator.generateAndOpenOrder(config, success -> {
            isGenerating = false;
            statusMessage = success ? "Opened in browser!" : "Redirecting to shop...";
            if (orderButton != null) {
                orderButton.active = true;
            }
        });
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int sliderW = Math.min(300, this.width - 40);
        int topY = 45;

        ZHLiveDataProvider.OptionStep step = getSelectedStep();
        if (step != null) {
            config.billingIntervalDays = (int) step.numericValue();
            config.billingOptionId = step.id();
            config.vpsBillingOptionId = switch (config.billingIntervalDays) {
                case 90 -> "615";
                case 180 -> "616";
                case 360, 365 -> "617";
                default -> "614";
            };
            config.dediBillingOptionId = step.id();
        }

        // Summary Card
        String voucherCode = config.getVoucherCode();
        int voucherDiscount = config.getVoucherDiscountPercent();
        boolean hasVoucher = !voucherCode.isEmpty() && voucherDiscount > 0;

        int summaryW = sliderW;
        int summaryH = hasVoucher ? 98 : 84;
        int summaryY = topY + 26;
        int summaryX = centerX - summaryW / 2;

        drawCard(graphics, summaryX, summaryY, summaryW, summaryH, false, false);

        int padX = summaryX + 12;
        int lineY = summaryY + 9;

        // 1. Billing Cycle
        graphics.drawString(this.font, "Billing Cycle:", padX, lineY, 0xFFAAAAAA);
        String cycleText = step != null ? step.label() : config.billingIntervalDays + " Days";
        graphics.drawString(this.font, cycleText, summaryX + summaryW - 12 - this.font.width(cycleText), lineY, 0xFFFFFFFF);

        // 2. Monthly Server Price
        lineY += 14;
        graphics.drawString(this.font, "Monthly Server Price:", padX, lineY, 0xFFAAAAAA);
        String priceText = String.format("$%.2f / Mo", config.getTotalMonthlyPrice());
        graphics.drawString(this.font, priceText, summaryX + summaryW - 12 - this.font.width(priceText), lineY, 0xFFCCCCCC);

        // 3. Pre-payment Discount
        lineY += 14;
        int discount = config.getDiscountPercent();
        graphics.drawString(this.font, "Pre-payment Discount:", padX, lineY, 0xFFAAAAAA);
        String discountText = discount > 0 ? "-" + discount + "% Save!" : "None";
        int discountColor = discount > 0 ? COLOR_ZAP_GREEN : 0xFF888888;
        graphics.drawString(this.font, discountText, summaryX + summaryW - 12 - this.font.width(discountText), lineY, discountColor);

        // 4. Voucher / Promo Code
        if (hasVoucher) {
            lineY += 14;
            graphics.drawString(this.font, "Voucher Code:", padX, lineY, 0xFFAAAAAA);
            String voucherText = voucherCode + " (-" + voucherDiscount + "% Save!)";
            graphics.drawString(this.font, voucherText, summaryX + summaryW - 12 - this.font.width(voucherText), lineY, COLOR_ZAP_GREEN);
        }

        // Divider
        lineY += 14;
        graphics.fill(padX, lineY, summaryX + summaryW - 12, lineY + 1, 0xFF353A3D);

        // 5. Total Due Today
        lineY += 5;
        graphics.drawString(this.font, "Total Due Today:", padX, lineY, 0xFFFFFFFF);
        String dueToday = String.format("$%.2f", config.getDueToday());
        int dueX = summaryX + summaryW - 12 - this.font.width(dueToday);
        graphics.drawString(this.font, dueToday, dueX, lineY, COLOR_ZAP_GREEN);

        if (hasVoucher && config.getVoucherSavings() > 0) {
            String originalDue = String.format("$%.2f", config.getSubtotalDue());
            int origW = this.font.width(originalDue);
            int origX = dueX - origW - 6;
            graphics.drawString(this.font, originalDue, origX, lineY, 0xFF777777);
            int strkY = lineY + 4;
            graphics.fill(origX - 1, strkY, origX + origW + 1, strkY + 1, 0xFF888888);
        }

        // Status or auto-applied notice
        int noticeY = summaryY + summaryH + 6;
        if (noticeY + 10 < this.height - 30) {
            if (!statusMessage.isEmpty()) {
                graphics.drawCenteredString(this.font, statusMessage, centerX, noticeY, COLOR_ZAP_GREEN);
            } else if (hasVoucher) {
                graphics.drawCenteredString(this.font, "✔ Code " + voucherCode + " will be auto-applied at checkout!", centerX, noticeY, 0xFF8AE58A);
            }
        }

        // Render slider ON TOP
        if (slider != null) {
            slider.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    private ZHLiveDataProvider.OptionStep getSelectedStep() {
        if (slider == null || billingSteps.isEmpty()) return null;
        int idx = (int) Math.round(slider.getNormalizedValue() * (billingSteps.size() - 1));
        idx = Mth.clamp(idx, 0, billingSteps.size() - 1);
        return billingSteps.get(idx);
    }

    private class BillingSlider extends ZHCustomSlider {

        public BillingSlider(int x, int y, int width, int height, double value) {
            super(x, y, width, height, Component.empty(), value);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            int idx = (int) Math.round(this.value * (billingSteps.size() - 1));
            idx = Mth.clamp(idx, 0, billingSteps.size() - 1);
            if (idx < billingSteps.size()) {
                ZHLiveDataProvider.OptionStep s = billingSteps.get(idx);
                setMessage(Component.literal("Billing: " + s.label()));
            }
        }

        @Override
        protected void applyValue() {
            updateMessage();
        }
    }
}
