package com.fakepixel.fpu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinionOverlayListener {
    private static int scrollOffset = 0;
    private static final int MAX_VISIBLE_ITEMS = 3;

    @SubscribeEvent
    public void onGuiDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
        boolean showPrices = FakepixelUtilities.isMinionOverlayEnabled;
        boolean showAdvice = FakepixelUtilities.isMinionUpgradeAdviceEnabled;
        if (!showPrices && !showAdvice) return; 

        Minecraft mc = Minecraft.getMinecraft();
        if (!(event.gui instanceof GuiChest)) return;
        GuiChest chestGui = (GuiChest) event.gui;
        ContainerChest container = (ContainerChest) chestGui.inventorySlots;
        IInventory lowerChestInventory = container.getLowerChestInventory();
        String chestTitle = lowerChestInventory.getDisplayName().getUnformattedText();
        if (!chestTitle.contains("Minion") && !chestTitle.contains("Tier")) return;

        int rightX = (chestGui.width + 176) / 2 + 6; 
        int leftX = (chestGui.width - 176) / 2 - 171; 
        int y = (chestGui.height - 166) / 2 + 4;
        
        int priceBoxY = y - 45;

        Map<String, Integer> itemQuantities = new HashMap<>();
        Map<String, String> itemDisplayNames = new HashMap<>();
        double totalMinionCoins = 0.0;
        
        boolean hasDiamondSpreading = false, hasSuperCompactor = false, hasFuel = false;
        boolean hasCorruptSoil = false, hasFlycatcher = false, hasExpander = false;

        for (int i = 0; i < lowerChestInventory.getSizeInventory(); i++) {
            ItemStack slotItem = lowerChestInventory.getStackInSlot(i);
            if (slotItem != null && slotItem.getItem() != null) {
                String cleanItemName = EnumChatFormatting.getTextWithoutFormattingCodes(slotItem.getDisplayName());
                
                if (cleanItemName.contains("Diamond Spreading")) hasDiamondSpreading = true;
                if (cleanItemName.contains("Super Compactor") || cleanItemName.contains("Dwarven Super Compactor")) hasSuperCompactor = true;
                if (cleanItemName.contains("Lava Bucket") || cleanItemName.contains("Plasma Bucket") || cleanItemName.contains("Magma Bucket") || cleanItemName.contains("Hamster Wheel") || cleanItemName.contains("Foul Flesh") || cleanItemName.contains("Catalyst")) hasFuel = true;
                if (cleanItemName.contains("Corrupt Soil")) hasCorruptSoil = true;
                if (cleanItemName.contains("Flycatcher")) hasFlycatcher = true;
                if (cleanItemName.contains("Minion Expander")) hasExpander = true;

                boolean isUpgradeOrUI = false;
                if (cleanItemName.contains("Diamond Spreading") || cleanItemName.contains("Compactor") || cleanItemName.contains("Bucket") || cleanItemName.contains("Wheel") || cleanItemName.contains("Foul Flesh") || cleanItemName.contains("Catalyst") || cleanItemName.contains("Soil") || cleanItemName.contains("Flycatcher") || cleanItemName.contains("Expander")) isUpgradeOrUI = true;
                if (cleanItemName.contains("Upgrade") || cleanItemName.contains("Fuel") || cleanItemName.contains("Skin") || cleanItemName.contains("Close") || cleanItemName.contains("Next Page") || cleanItemName.contains("Minion") || cleanItemName.contains("Hopper") || cleanItemName.contains("Collect All") || cleanItemName.contains("Recipe") || cleanItemName.contains("Ideal Layout")) isUpgradeOrUI = true;
                
                if (isUpgradeOrUI) continue;

                String internalName = cleanItemName.toUpperCase().replace(" ", "_");
                itemQuantities.put(internalName, itemQuantities.getOrDefault(internalName, 0) + slotItem.stackSize);
                itemDisplayNames.put(internalName, slotItem.getDisplayName());
            }
        }

        List<String> dynamicItemLines = new ArrayList<>();
        boolean dataTracked = false;

        if (showPrices) {
            for (Map.Entry<String, Integer> entry : itemQuantities.entrySet()) {
                String internalName = entry.getKey();
                PriceManager.ItemData data = PriceManager.getCachedPrice(internalName);
                if (data != null && data.bzSell != null && !data.bzSell.equals("N/A")) {
                    dataTracked = true;
                    try {
                        double sellPrice = Double.parseDouble(data.bzSell.replace(",", ""));
                        double totalVal = sellPrice * entry.getValue();
                        totalMinionCoins += totalVal;
                        dynamicItemLines.add(EnumChatFormatting.YELLOW + itemDisplayNames.get(internalName));
                        dynamicItemLines.add(EnumChatFormatting.GRAY + " -> Units: " + EnumChatFormatting.AQUA + entry.getValue());
                        dynamicItemLines.add(EnumChatFormatting.GRAY + " -> Per unit: " + EnumChatFormatting.YELLOW + String.format("%,.1f", sellPrice) + " Coins"); 
                        dynamicItemLines.add(EnumChatFormatting.GRAY + " -> Total: " + EnumChatFormatting.GREEN + String.format("%,.1f", totalVal) + " Coins"); 
                        dynamicItemLines.add(""); 
                    } catch (Exception ignored) {}
                }
            }
        }

        int totalItems = dynamicItemLines.size() / 5; 
        int dWheel = Mouse.getDWheel();
        if (dWheel > 0) scrollOffset--; else if (dWheel < 0) scrollOffset++;
        if (scrollOffset < 0) scrollOffset = 0;
        if (scrollOffset > Math.max(0, totalItems - MAX_VISIBLE_ITEMS)) scrollOffset = Math.max(0, totalItems - MAX_VISIBLE_ITEMS);

      
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F); 

        if (showPrices) {
            List<String> profitLines = new ArrayList<>();
            profitLines.add(EnumChatFormatting.GOLD + "FPU MINION METRICS");
            profitLines.add(EnumChatFormatting.GOLD + "--------------------");

            if (!PriceManager.isServerOnline) {
                profitLines.add(EnumChatFormatting.RED + "Network Reconnecting...");
            } else if (!dataTracked) {
                profitLines.add(EnumChatFormatting.RED + "No price data synced.");
                profitLines.add(EnumChatFormatting.GRAY + "Open dynamic page once.");
            } else {
                int startIdx = scrollOffset * 5;
                int endIdx = Math.min(startIdx + (MAX_VISIBLE_ITEMS * 5), dynamicItemLines.size());
                for (int i = startIdx; i < endIdx; i++) profitLines.add(dynamicItemLines.get(i));
                if (totalItems > MAX_VISIBLE_ITEMS) profitLines.add(EnumChatFormatting.DARK_GRAY + "  (Scroll to see more)");
            }
            
            profitLines.add(EnumChatFormatting.GOLD + "--------------------");
            profitLines.add(EnumChatFormatting.AQUA + "Total Value: " + EnumChatFormatting.GREEN + String.format("%,.1f", totalMinionCoins) + " Coins");

            int boxWidth = 165;
            int boxHeight = profitLines.size() * 11 + 8;
            
            GuiChest.drawRect(rightX, priceBoxY, rightX + boxWidth, priceBoxY + boxHeight, 0xFF000000);
            
            int currentY = priceBoxY + 5;
            for (String line : profitLines) {
                mc.fontRendererObj.drawStringWithShadow(line, rightX + 6, currentY, 0xFFFFFFFF);
                currentY += 11;
            }
        }

        if (showAdvice) {
            List<String> adviceLines = new ArrayList<>();
            adviceLines.add(EnumChatFormatting.GOLD + "FPU SMART ADVICE");
            adviceLines.add(EnumChatFormatting.GOLD + "--------------------");
            
            String tier = "I";
            String[] parts = chestTitle.split(" ");
            if (parts.length > 0) tier = parts[parts.length - 1];
            boolean isMaxTier = tier.equals("XI") || tier.equals("XII");

            if (!isMaxTier) adviceLines.add(EnumChatFormatting.RED + " -> Not Maxed (" + tier + ")");
            else adviceLines.add(EnumChatFormatting.GREEN + " -> Minion Maxed (" + tier + ")");

            if (!hasSuperCompactor) adviceLines.add(EnumChatFormatting.RED + " -> No Super Compactor");
            else adviceLines.add(EnumChatFormatting.GREEN + " -> Compactor Active");

            if (!hasFuel) adviceLines.add(EnumChatFormatting.RED + " -> Missing Fuel Source");
            else adviceLines.add(EnumChatFormatting.GREEN + " -> Fuel System Active");

            if (chestTitle.contains("Slime")) {
                if (!hasCorruptSoil) adviceLines.add(EnumChatFormatting.RED + " -> No Corrupt Soil");
                else adviceLines.add(EnumChatFormatting.GREEN + " -> Corrupt Soil Active");
            }

            if (hasFlycatcher) adviceLines.add(EnumChatFormatting.GREEN + " -> Flycatcher Active");
            else if (hasExpander) adviceLines.add(EnumChatFormatting.GREEN + " -> Expander Active");
            else adviceLines.add(EnumChatFormatting.RED + " -> No Upgrade Slot 2");
            
            if (chestTitle.contains("Snow") || chestTitle.contains("Clay")) {
                if (!hasDiamondSpreading) adviceLines.add(EnumChatFormatting.RED + " -> No Dia Spreading");
                else adviceLines.add(EnumChatFormatting.GREEN + " -> Spreading Active");
            }

            adviceLines.add(EnumChatFormatting.GOLD + "--------------------");
            adviceLines.add(EnumChatFormatting.LIGHT_PURPLE + "Status: Ready to grind");

            int boxWidth = 165;
            int boxHeight = adviceLines.size() * 11 + 8;
            
            GuiChest.drawRect(leftX, y, leftX + boxWidth, y + boxHeight, 0xFF000000); 
            
            int currentY = y + 5;
            for (String line : adviceLines) {
                mc.fontRendererObj.drawStringWithShadow(line, leftX + 6, currentY, 0xFFFFFFFF);
                currentY += 11;
            }
        }
        
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}