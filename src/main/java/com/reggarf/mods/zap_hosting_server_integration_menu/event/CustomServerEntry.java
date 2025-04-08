//package com.reggarf.mods.zap_hosting_server_integration_menu.event;
//
//
//
//import com.mojang.blaze3d.systems.RenderSystem;
//import com.mojang.blaze3d.vertex.PoseStack;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.GuiGraphics;
//import net.minecraft.client.gui.components.Renderable;
//import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
//import net.minecraft.client.multiplayer.ServerData;
//import net.minecraft.resources.ResourceLocation;
//import net.neoforged.api.distmarker.Dist;
//import net.neoforged.api.distmarker.OnlyIn;
//
//@OnlyIn(Dist.CLIENT)
//public class CustomServerEntry extends ServerSelectionList.Entry {
//
//    private final Minecraft minecraft;
//    private final ServerData serverData;
//    private final ResourceLocation customIcon;
//
//    public CustomServerEntry(ServerSelectionList list, ServerData serverData) {
//        this.minecraft = Minecraft.getInstance();
//        this.serverData = serverData;
//        this.customIcon = new ResourceLocation("yourmodid", "textures/gui/custom_icon.png");
//    }
//
//    @Override
//    public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTicks) {
//        // Draw server name
//        guiGraphics.drawString(minecraft.font, serverData.name, left + 36, top + 4, 0xFFFFFF, false);
//
//        // Draw custom icon
//        RenderSystem.enableBlend();
//        guiGraphics.blit(customIcon, left + 2, top + 2, 0, 0, 32, 32, 32, 32);
//        RenderSystem.disableBlend();
//    }
//
//    @Override
//    public boolean mouseClicked(double mouseX, double mouseY, int button) {
//        // Optional: handle clicks if needed
//        return super.mouseClicked(mouseX, mouseY, button);
//    }
//}
