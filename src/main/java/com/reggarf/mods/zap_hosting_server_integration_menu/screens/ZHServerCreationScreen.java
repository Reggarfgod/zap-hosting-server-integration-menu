//package com.reggarf.mods.zap_hosting_server_integration_menu.screens;
//
//
//
//import com.reggarf.mods.zap_hosting_server_integration_menu.tool.ZHScrollableTextList;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.components.Button;
//import net.minecraft.client.gui.components.EditBox;
//import net.minecraft.client.gui.screens.Screen;
//import net.minecraft.network.chat.Component;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class ZHServerCreationScreen extends Screen {
//    private EditBox searchBox;
//    private ZHScrollableTextList packList;
//    private Button selectButton;
//    private Button backButton;
//
//    private final List<Component> allModpacks = List.of(
//            Component.literal("A Bit Of Everything 3 1.16.5"),
//            Component.literal("A Bit of Everything Classic 1.7.10"),
//            Component.literal("A Furcraftian Tail - FOODCraft Goes West 1.12.2"),
//            Component.literal("A Lot Of Everything 2 1.16.5")
//    );
//
//    public ZHServerCreationScreen() {
//        super(Component.literal("Let's create your Minecraft Server."));
//    }
//
//    @Override
//    protected void init() {
//        int centerX = this.width / 2;
//
//        // Search box
//        searchBox = new EditBox(this.font, centerX - 200, 40, 180, 20, Component.literal("Filter by name"));
//        searchBox.setResponder(text -> updatePackList(text));
//        this.addRenderableWidget(searchBox);
//
//        // Scrollable pack list
//        packList = new ZHScrollableTextList(centerX - 10, 70, 210, 100, new ArrayList<>(allModpacks));
//        this.addRenderableWidget(packList);
//
//        // Select button
//        selectButton = Button.builder(Component.literal("Select"), button -> {
//            // Handle selection
//        }).bounds(centerX - 50, 180, 100, 20).build();
//        this.addRenderableWidget(selectButton);
//
//        // Back button
//        backButton = Button.builder(Component.literal("Back"), button -> {
//            Minecraft.getInstance().setScreen(null);
//        }).bounds(centerX - 50, 210, 100, 20).build();
//        this.addRenderableWidget(backButton);
//    }
//
//    private void updatePackList(String filter) {
//        List<Component> filtered = allModpacks.stream()
//                .filter(c -> c.getString().toLowerCase().contains(filter.toLowerCase()))
//                .toList();
//        packList = new ZHScrollableTextList(this.width / 2 - 10, 70, 210, 100, filtered);
//        this.children().removeIf(w -> w instanceof ZHScrollableTextList);
//        this.addRenderableWidget(packList);
//    }
//
//    @Override
//    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
//        if (packList != null && packList.mouseScrolled(mouseX, mouseY, verticalAmount)) {
//            return true;
//        }
//        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
//    }
//
//    @Override
//    public void render(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
//        //this.renderBackground(graphics);
//        graphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
//        super.render(graphics, mouseX, mouseY, partialTick);
//    }
//}
