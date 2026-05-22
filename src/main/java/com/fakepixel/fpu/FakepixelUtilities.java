package com.fakepixel.fpu;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mod(modid = FakepixelUtilities.MODID, version = FakepixelUtilities.VERSION, acceptedMinecraftVersions = "[1.8.9]")
public class FakepixelUtilities {
    public static final String MODID = "fpu";
    public static final String VERSION = "1.0.2";
    
    private static final String API_KEY_B64 = "API_KEY_HERE"; 
    private static final String API_URL_B64 = "API_URL_HERE"; 

    public static String getApiKey() { return new String(Base64.getDecoder().decode(API_KEY_B64)); }
    public static String getApiUrl() { return new String(Base64.getDecoder().decode(API_URL_B64)); }

    public static boolean inSkyblock = false;
    public static boolean isShowTooltip = true;
    public static boolean isDebugMode = false;
    public static boolean isMinionOverlayEnabled = true;
    public static boolean isMinionUpgradeAdviceEnabled = true;
    public static boolean isSendInfoEnabled = false;
    public static boolean isSafeModeEnabled = false;
    public static boolean isDeveloper() {
    return Minecraft.getMinecraft().thePlayer != null && 
           Minecraft.getMinecraft().thePlayer.getName().equalsIgnoreCase("DEVELOPER_IGN_HERE");
}

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new TooltipListener());
        MinecraftForge.EVENT_BUS.register(new MinionOverlayListener());
        MinecraftForge.EVENT_BUS.register(new ChatListener());
        MinecraftForge.EVENT_BUS.register(new InventoryScraperListener());
        MinecraftForge.EVENT_BUS.register(new FPUSafeModeListener());
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new FpuCommand());
        ClientCommandHandler.instance.registerCommand(new FpuSafeCommand());
    }

    @SubscribeEvent
    public void onPlayerJoin(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        inSkyblock = false;
        new Thread(() -> {
            try {
                Thread.sleep(4000); 
                if (Minecraft.getMinecraft().thePlayer != null) {
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "[FPU] Mod Loaded Successfully! You are using v" + VERSION + "."));
                }
            } catch (InterruptedException ignored) {}
        }).start();
    }

    public static String getSkyblockId(ItemStack item) {
        if (item == null) return "AIR";
        String cleanName = EnumChatFormatting.getTextWithoutFormattingCodes(item.getDisplayName()).toUpperCase().trim().replace(" ", "_");

        if (item.hasTagCompound() && item.getTagCompound().hasKey("ExtraAttributes")) {
            NBTTagCompound ea = item.getTagCompound().getCompoundTag("ExtraAttributes");
            if (ea.hasKey("id")) {
                String baseId = ea.getString("id").toUpperCase().trim();
                
                if (baseId.equals("PET") && ea.hasKey("petInfo")) {
                    String petInfo = ea.getString("petInfo");
                    String type = "UNKNOWN", tier = "COMMON";
                    Matcher mType = Pattern.compile("\"type\":\"([^\"]+)\"").matcher(petInfo);
                    if (mType.find()) type = mType.group(1);
                    Matcher mTier = Pattern.compile("\"tier\":\"([^\"]+)\"").matcher(petInfo);
                    if (mTier.find()) tier = mTier.group(1);
                    return "PET_" + type + "_" + tier;
                }
                
                if (baseId.contains(";")) {
                    String[] parts = baseId.split(";");
                    if (parts.length >= 2) {
                        String namePart = parts[0];
                        String tierNum = parts[1];
                        String tierSuffix = "_COMMON";
                        if (tierNum.equals("5")) tierSuffix = "_MYTHIC";
                        else if (tierNum.equals("4")) tierSuffix = "_LEGENDARY";
                        else if (tierNum.equals("3")) tierSuffix = "_EPIC";
                        else if (tierNum.equals("2")) tierSuffix = "_RARE";
                        else if (tierNum.equals("1")) tierSuffix = "_UNCOMMON";
                        else if (tierNum.equals("0")) tierSuffix = "_COMMON";
                        
                        if (!namePart.startsWith("PET_")) namePart = "PET_" + namePart;
                        return namePart + tierSuffix;
                    }
                }
                
                if (baseId.equals("ENCHANTED_BOOK") && ea.hasKey("enchantments")) {
                    NBTTagCompound enchants = ea.getCompoundTag("enchantments");
                    if (!enchants.hasNoTags()) {
                        String firstEnchant = enchants.getKeySet().iterator().next();
                        int level = enchants.getInteger(firstEnchant);
                        return "ENCHANTMENT_" + firstEnchant.toUpperCase() + "_" + level;
                    }
                }
                
                if (baseId.contains("POTION") || ea.hasKey("potion")) {
                    if (cleanName.contains("POTION") || cleanName.contains("SPLASH") || cleanName.contains("BOTTLE")) {
                        return cleanName;
                    }
                }
                return baseId;
            }
        }
        return cleanName;
    }
}