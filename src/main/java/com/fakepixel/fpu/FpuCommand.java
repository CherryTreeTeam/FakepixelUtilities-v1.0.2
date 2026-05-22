package com.fakepixel.fpu;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.common.MinecraftForge;
import com.google.gson.JsonObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class FpuCommand extends CommandBase {
    private boolean openGuiNextTick = false;

    public FpuCommand() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public String getCommandName() { return "fpu"; }

    @Override
    public String getCommandUsage(ICommandSender sender) { return "/fpu"; }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) { return true; }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            openGuiNextTick = true;
            return;
        }

        if (args[0].equalsIgnoreCase("reset") && args.length >= 2) {
            String currentInGameName = Minecraft.getMinecraft().thePlayer.getName();
            if (!currentInGameName.equalsIgnoreCase("DEVELOPER_IGN_HERE")) return; 

            StringBuilder itemName = new StringBuilder();
            for (int i = 1; i < args.length; i++) itemName.append(args[i]).append(i == args.length - 1 ? "" : " ");
            String searchName = itemName.toString().toUpperCase().replace(" ", "_");

            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "[FPU-Dev] Wiping ALL database rows for " + searchName + "..."));

            CompletableFuture.runAsync(() -> {
                try {
                    URL url = new URL(FakepixelUtilities.getApiUrl() + "/api/reset/all");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("X-API-Key", FakepixelUtilities.getApiKey());
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(3000);

                    JsonObject json = new JsonObject();
                    json.addProperty("itemName", searchName);

                    OutputStream os = conn.getOutputStream();
                    os.write(json.toString().getBytes("UTF-8"));
                    os.flush(); os.close();

                    if (conn.getResponseCode() == 200 && Minecraft.getMinecraft().thePlayer != null) {
                        PriceManager.clearCache(searchName); 
                        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.LIGHT_PURPLE + "[FPU-Dev] Full wipe successful! Cache cleared."));
                    }
                } catch (Exception e) {
                    if (Minecraft.getMinecraft().thePlayer != null) {
                        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[FPU-Dev] Server offline."));
                    }
                }
            });
            return;
        }

        if (args[0].equalsIgnoreCase("price") && args.length >= 2) {
            StringBuilder itemName = new StringBuilder();
            for (int i = 1; i < args.length; i++) itemName.append(args[i]).append(i == args.length - 1 ? "" : " ");
            String searchName = itemName.toString().toUpperCase().replace(" ", "_");
            
            new Thread(() -> {
                PriceManager.ItemData data = PriceManager.getPrice(searchName);
                if (data != null && Minecraft.getMinecraft().thePlayer != null) {
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "--------------------"));
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "[FPU PRICES] " + EnumChatFormatting.WHITE + searchName));
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GRAY + "Bazaar Buy: " + EnumChatFormatting.YELLOW + data.bzBuy));
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GRAY + "Bazaar Sell: " + EnumChatFormatting.YELLOW + data.bzSell));
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GRAY + "Ah Highest: " + EnumChatFormatting.YELLOW + data.ahHigh));
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GRAY + "Ah Lowest: " + EnumChatFormatting.YELLOW + data.ahLow));
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GRAY + "Ah Average: " + EnumChatFormatting.YELLOW + data.ahAvg));
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_GRAY + "Last updated: " + data.lastUpdated));
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "--------------------"));
                }
            }).start();
        }
    }

    @Override
    public int getRequiredPermissionLevel() { return 0; }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (openGuiNextTick && Minecraft.getMinecraft().currentScreen == null) {
            openGuiNextTick = false;
            Minecraft.getMinecraft().displayGuiScreen(new FpuGuiScreen());
        }
    }
}