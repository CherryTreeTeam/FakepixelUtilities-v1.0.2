package com.fakepixel.fpu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;
import java.io.IOException;

public class FpuGuiScreen extends GuiScreen {
    private int currentPage = 0;
    private GuiTextField searchField;

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        
        int boxWidth = 260;
        int boxHeight = 240;
        int x = (this.width - boxWidth) / 2;
        int y = ((this.height - boxHeight) / 2) - 15;

        this.buttonList.add(new GuiButton(10, x + 10, y + 30, 78, 20, (currentPage == 0 ? EnumChatFormatting.GOLD + "▶ FPU" : "FPU")));
        this.buttonList.add(new GuiButton(11, x + 91, y + 30, 78, 20, (currentPage == 1 ? EnumChatFormatting.GOLD + "▶ Price" : "Price")));
        this.buttonList.add(new GuiButton(12, x + 172, y + 30, 78, 20, (currentPage == 2 ? EnumChatFormatting.GOLD + "▶ About" : "About")));
        
        if (currentPage == 0) {
            String tooltipStatus = FakepixelUtilities.isShowTooltip ? EnumChatFormatting.GREEN + "ON" : EnumChatFormatting.RED + "OFF";
            this.buttonList.add(new GuiButton(1, x + 30, y + 65, 200, 20, "Show Tooltips: " + tooltipStatus));

            String minionStatus = FakepixelUtilities.isMinionOverlayEnabled ? EnumChatFormatting.GREEN + "ON" : EnumChatFormatting.RED + "OFF";
            this.buttonList.add(new GuiButton(3, x + 30, y + 105, 200, 20, "Minion Chest Prices: " + minionStatus));

            String adviceStatus = FakepixelUtilities.isMinionUpgradeAdviceEnabled ? EnumChatFormatting.GREEN + "ON" : EnumChatFormatting.RED + "OFF";
            this.buttonList.add(new GuiButton(4, x + 30, y + 145, 200, 20, "Minion Upgrades: " + adviceStatus));
            
            String safeModeStatus = FakepixelUtilities.isSafeModeEnabled ? EnumChatFormatting.GREEN + "ON" : EnumChatFormatting.RED + "OFF";
            this.buttonList.add(new GuiButton(6, x + 30, y + 185, 200, 20, "Safe Mode: " + safeModeStatus));
        } else if (currentPage == 1) { 
            this.searchField = new GuiTextField(0, this.fontRendererObj, x + 30, y + 65, 200, 20);
            this.searchField.setMaxStringLength(30);
            this.searchField.setFocused(true);
            this.searchField.setText("");
        } 
        else if (currentPage == 2) {
            String sendInfoStatus = FakepixelUtilities.isSendInfoEnabled ? EnumChatFormatting.GREEN + "ON" : EnumChatFormatting.RED + "OFF";
            this.buttonList.add(new GuiButton(5, x + 30, y + 175, 200, 20, "Send Info: " + sendInfoStatus));

            String debugStatus = FakepixelUtilities.isDebugMode ? EnumChatFormatting.GREEN + "ON" : EnumChatFormatting.RED + "OFF";
            this.buttonList.add(new GuiButton(2, x + 30, y + 205, 200, 20, "Debug Logs: " + debugStatus));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        
        int boxWidth = 260;
        int boxHeight = 240;
        int x = (this.width - boxWidth) / 2;
        int y = ((this.height - boxHeight) / 2) - 15;

        drawRect(x, y, x + boxWidth, y + boxHeight, 0xF5101014); 
        drawRect(x - 1, y - 1, x + boxWidth + 1, y, 0xFF4A4A5A); 
        drawRect(x - 1, y, x, y + boxHeight, 0xFF4A4A5A);
        drawRect(x + boxWidth, y, x + boxWidth + 1, y + boxHeight, 0xFF4A4A5A);
        drawRect(x - 1, y + boxHeight, x + boxWidth + 1, y + boxHeight + 1, 0xFF4A4A5A);

        this.drawCenteredString(this.fontRendererObj, EnumChatFormatting.WHITE + "FakepixelUtilities v1.0.2", this.width / 2, y + 12, 0xFFFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);

        if (currentPage == 1 && searchField != null) {
            searchField.drawTextBox();
            String query = searchField.getText().trim().toUpperCase().replace(" ", "_");
            int renderY = y + 95;

            if (!query.isEmpty()) {
                PriceManager.ItemData data = PriceManager.getCachedPrice(query);
                if (data != null) {
                    this.fontRendererObj.drawStringWithShadow(EnumChatFormatting.GOLD + "[FPU LIVE LOOKUP]", x + 30, renderY, 0xFFFFFFFF);
                    this.fontRendererObj.drawStringWithShadow(EnumChatFormatting.GRAY + "Bazaar Buy: " + EnumChatFormatting.YELLOW + data.bzBuy, x + 30, renderY + 15, 0xFFFFFFFF);
                    this.fontRendererObj.drawStringWithShadow(EnumChatFormatting.GRAY + "Bazaar Sell: " + EnumChatFormatting.YELLOW + data.bzSell, x + 30, renderY + 28, 0xFFFFFFFF);
                    this.fontRendererObj.drawStringWithShadow(EnumChatFormatting.GRAY + "Ah Highest: " + EnumChatFormatting.YELLOW + data.ahHigh, x + 30, renderY + 41, 0xFFFFFFFF);
                    this.fontRendererObj.drawStringWithShadow(EnumChatFormatting.GRAY + "Ah Lowest: " + EnumChatFormatting.YELLOW + data.ahLow, x + 30, renderY + 54, 0xFFFFFFFF);
                    this.fontRendererObj.drawStringWithShadow(EnumChatFormatting.GRAY + "Ah Average: " + EnumChatFormatting.YELLOW + data.ahAvg, x + 30, renderY + 67, 0xFFFFFFFF);
                    this.fontRendererObj.drawStringWithShadow(EnumChatFormatting.DARK_GRAY + "Last updated: " + data.lastUpdated, x + 30, renderY + 82, 0xFFFFFFFF);
                } else {
                    this.fontRendererObj.drawStringWithShadow(EnumChatFormatting.DARK_GRAY + "Indexing database metrics...", x + 30, renderY, 0xFFFFFFFF);
                }
            } else {
                this.fontRendererObj.drawStringWithShadow(EnumChatFormatting.DARK_GRAY + "Type item name (e.g., WHEAT)", x + 30, renderY, 0xFFFFFFFF);
            }
        } 
        else if (currentPage == 2) {
           
            int renderY = y + 55; 
            this.fontRendererObj.drawStringWithShadow(EnumChatFormatting.GOLD + "CherryTree Team's Members:", x + 25, renderY, 0xFFFFFFFF);
            drawRect(x + 25, renderY + 11, x + boxWidth - 25, renderY + 12, 0xFF4A4A5A);
            this.fontRendererObj.drawStringWithShadow(EnumChatFormatting.LIGHT_PURPLE + "c1727.c", x + 30, renderY + 18, 0xFFFFFFFF);
            this.fontRendererObj.drawStringWithShadow(EnumChatFormatting.GRAY + " -> " + EnumChatFormatting.WHITE + "Project Founder", x + 30, renderY + 29, 0xFFFFFFFF);
            this.fontRendererObj.drawStringWithShadow(EnumChatFormatting.AQUA + "_jatin_e", x + 30, renderY + 44, 0xFFFFFFFF);
            this.fontRendererObj.drawStringWithShadow(EnumChatFormatting.GRAY + " -> " + EnumChatFormatting.WHITE + "Project Manager", x + 30, renderY + 55, 0xFFFFFFFF);
            drawRect(x + 25, renderY + 70, x + boxWidth - 25, renderY + 71, 0xFF4A4A5A);
            
            this.drawCenteredString(this.fontRendererObj, EnumChatFormatting.GRAY + "Made with " + EnumChatFormatting.RED + "❤" + EnumChatFormatting.GRAY + " by " + EnumChatFormatting.LIGHT_PURPLE + "CherryTree Team", this.width / 2, renderY + 80, 0xFFFFFFFF);
            this.drawCenteredString(this.fontRendererObj, EnumChatFormatting.GREEN + "Great People Great Community", this.width / 2, renderY + 93, 0xFFFFFFFF);
            
            this.drawCenteredString(this.fontRendererObj, EnumChatFormatting.AQUA + "—— Developer Only ——", this.width / 2, y + 160, 0xFFFFFFFF);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 10) { currentPage = 0; initGui(); }
        else if (button.id == 11) { currentPage = 1; initGui(); }
        else if (button.id == 12) { currentPage = 2; initGui(); }
        else if (currentPage == 0) {
            if (button.id == 1) FakepixelUtilities.isShowTooltip = !FakepixelUtilities.isShowTooltip;
            else if (button.id == 3) FakepixelUtilities.isMinionOverlayEnabled = !FakepixelUtilities.isMinionOverlayEnabled;
            else if (button.id == 4) FakepixelUtilities.isMinionUpgradeAdviceEnabled = !FakepixelUtilities.isMinionUpgradeAdviceEnabled;
            else if (button.id == 6) FakepixelUtilities.isSafeModeEnabled = !FakepixelUtilities.isSafeModeEnabled;
            initGui();
        }
        else if (currentPage == 2) {
            if (button.id == 5) {
                if (FakepixelUtilities.isDeveloper()) FakepixelUtilities.isSendInfoEnabled = !FakepixelUtilities.isSendInfoEnabled;
                else showAccessDenied("Send Info");
                initGui();
            }
            else if (button.id == 2) {
                if (FakepixelUtilities.isDeveloper()) FakepixelUtilities.isDebugMode = !FakepixelUtilities.isDebugMode;
                else showAccessDenied("Debug Logs");
                initGui();
            }
        }
    }

    private void showAccessDenied(String feature) {
        if (Minecraft.getMinecraft().thePlayer != null) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText(
                EnumChatFormatting.GOLD + "[FPU] " + EnumChatFormatting.RED + "You can't toggle " + feature + " feature because it's only for our Developers!"
            ));
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (currentPage == 1 && searchField != null && searchField.isFocused()) {
            if (keyCode == Keyboard.KEY_ESCAPE) { super.keyTyped(typedChar, keyCode); } 
            else { searchField.textboxKeyTyped(typedChar, keyCode); }
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (currentPage == 1 && searchField != null) searchField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (currentPage == 1 && searchField != null) searchField.updateCursorCounter();
    }

    @Override
    public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}