package com.fakepixel.fpu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import java.lang.reflect.Field;

public class FPUSafeModeListener {
    
    private static final ResourceLocation LOCK_ICON = new ResourceLocation("fpu", "textures/gui/lock.png");
    private long lastAlertTime = 0; 
    private long pendingTradeCloseTime = 0;

    private boolean shouldBlockDrop() {
        return FakepixelUtilities.inSkyblock && FakepixelUtilities.isSafeModeEnabled;
    }

    private void printDropWarning() {
        long now = System.currentTimeMillis();
        if (now - lastAlertTime > 1000 && Minecraft.getMinecraft().thePlayer != null) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                EnumChatFormatting.RED + "[FPU] Safe mode is on! You can't drop any item!"
            ));
            lastAlertTime = now;
        }
    }

    
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (!shouldBlockDrop()) return;
        
        int dropKey = Minecraft.getMinecraft().gameSettings.keyBindDrop.getKeyCode();
        if (Keyboard.getEventKey() == dropKey && Keyboard.getEventKeyState()) {
            net.minecraft.client.settings.KeyBinding.setKeyBindState(dropKey, false);
            while (Minecraft.getMinecraft().gameSettings.keyBindDrop.isPressed()) {}
            printDropWarning();
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
      
        if (event.phase != TickEvent.Phase.START) return;

        if (!shouldBlockDrop()) {
            pendingTradeCloseTime = 0;
            return;
        }
        
        while (Minecraft.getMinecraft().gameSettings.keyBindDrop.isPressed()) {
            printDropWarning();
        }

        if (pendingTradeCloseTime > 0 && System.currentTimeMillis() >= pendingTradeCloseTime) {
            pendingTradeCloseTime = 0;
            if (Minecraft.getMinecraft().thePlayer != null) {
                Minecraft.getMinecraft().thePlayer.closeScreen();
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.RED + "[FPU] Safe mode is on! You can't trade!"
                ));
            }
        }
    }

    @SubscribeEvent
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!shouldBlockDrop() || !(event.gui instanceof GuiContainer)) return;

        if (Mouse.getEventButton() != -1) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer != null && mc.thePlayer.inventory.getItemStack() != null) {
                GuiContainer gui = (GuiContainer) event.gui;
                int mouseX = Mouse.getEventX() * gui.width / mc.displayWidth;
                int mouseY = gui.height - Mouse.getEventY() * gui.height / mc.displayHeight - 1;

                int guiLeft = 0, guiTop = 0, xSize = 176, ySize = 166;
                try {
                    Field fLeft = GuiContainer.class.getDeclaredField("field_147003_i"); fLeft.setAccessible(true); guiLeft = fLeft.getInt(gui);
                    Field fTop = GuiContainer.class.getDeclaredField("field_147009_r"); fTop.setAccessible(true); guiTop = fTop.getInt(gui);
                    Field fXSize = GuiContainer.class.getDeclaredField("field_146999_f"); fXSize.setAccessible(true); xSize = fXSize.getInt(gui);
                    Field fYSize = GuiContainer.class.getDeclaredField("field_147000_g"); fYSize.setAccessible(true); ySize = fYSize.getInt(gui);
                } catch (Exception e) { 
                    try {
                        Field fLeft = GuiContainer.class.getDeclaredField("guiLeft"); fLeft.setAccessible(true); guiLeft = fLeft.getInt(gui);
                        Field fTop = GuiContainer.class.getDeclaredField("guiTop"); fTop.setAccessible(true); guiTop = fTop.getInt(gui);
                        Field fXSize = GuiContainer.class.getDeclaredField("xSize"); fXSize.setAccessible(true); xSize = fXSize.getInt(gui);
                        Field fYSize = GuiContainer.class.getDeclaredField("ySize"); fYSize.setAccessible(true); ySize = fYSize.getInt(gui);
                    } catch (Exception ignored) {}
                }

                if (mouseX < guiLeft || mouseX >= guiLeft + xSize || mouseY < guiTop || mouseY >= guiTop + ySize) {
                    event.setCanceled(true);
                    if (Mouse.getEventButtonState()) { 
                        printDropWarning();
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onGuiKey(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (!shouldBlockDrop()) return;
        
        int dropKey = Minecraft.getMinecraft().gameSettings.keyBindDrop.getKeyCode();
        if (Keyboard.getEventKey() == dropKey) {
            event.setCanceled(true);
            if (Keyboard.getEventKeyState()) { 
                printDropWarning(); 
            }
        }
    }

    @SubscribeEvent
    public void onDrawGui(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!shouldBlockDrop() || !(event.gui instanceof GuiContainer)) return;
        GuiContainer gui = (GuiContainer) event.gui;
        
        if (gui instanceof GuiChest) {
            GuiChest chest = (GuiChest) gui;
            try {
                IInventory lowerChest = null;
                try {
                    Field fieldLower = GuiChest.class.getDeclaredField("field_147012_x");
                    fieldLower.setAccessible(true);
                    lowerChest = (IInventory) fieldLower.get(chest);
                } catch (Exception e) {
                    if (chest.inventorySlots instanceof ContainerChest) {
                        lowerChest = ((ContainerChest) chest.inventorySlots).getLowerChestInventory();
                    }
                }

                if (lowerChest != null && lowerChest.getSizeInventory() == 54) {
                    ItemStack middleSlot = lowerChest.getStackInSlot(4);
                    if (middleSlot != null && middleSlot.getItem() != null) {
                        String name = EnumChatFormatting.getTextWithoutFormattingCodes(middleSlot.getDisplayName()).toLowerCase();
                        if (name.contains("your stuff") || name.contains("their stuff") || name.contains("trading")) {
                            if (pendingTradeCloseTime == 0) pendingTradeCloseTime = System.currentTimeMillis() + 1000;
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        renderLocks(gui);
    }

    private void renderLocks(GuiContainer gui) {
        int guiLeft = 0, guiTop = 0;
        try {
            Field fLeft = GuiContainer.class.getDeclaredField("field_147003_i"); fLeft.setAccessible(true); guiLeft = fLeft.getInt(gui);
            Field fTop = GuiContainer.class.getDeclaredField("field_147009_r"); fTop.setAccessible(true); guiTop = fTop.getInt(gui);
        } catch (Exception e) { 
            try {
                Field fLeft = GuiContainer.class.getDeclaredField("guiLeft"); fLeft.setAccessible(true); guiLeft = fLeft.getInt(gui);
                Field fTop = GuiContainer.class.getDeclaredField("guiTop"); fTop.setAccessible(true); guiTop = fTop.getInt(gui);
            } catch (Exception ignored) {}
        }

        for (Slot slot : gui.inventorySlots.inventorySlots) {
            if (slot.getHasStack()) {
                int x = guiLeft + slot.xDisplayPosition;
                int y = guiTop + slot.yDisplayPosition;
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 0.6F); 
                Minecraft.getMinecraft().getTextureManager().bindTexture(LOCK_ICON);
                Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (!shouldBlockDrop() || event.gui == null) return;
        
        if (event.gui instanceof GuiChest) {
            GuiChest chest = (GuiChest) event.gui;
            String title = "";
            try {
                Field fieldLower = GuiChest.class.getDeclaredField("field_147012_x");
                fieldLower.setAccessible(true);
                IInventory lowerChest = (IInventory) fieldLower.get(chest);
                title = lowerChest.getDisplayName().getUnformattedText().toLowerCase();
            } catch (Exception e) { 
                if (chest.inventorySlots instanceof ContainerChest) {
                    title = ((ContainerChest) chest.inventorySlots).getLowerChestInventory().getDisplayName().getUnformattedText().toLowerCase();
                }
            }
            
            if (title.contains("trade") || title.contains("trading") || title.contains("deal") || title.startsWith("you ")) {
                if (pendingTradeCloseTime == 0) pendingTradeCloseTime = System.currentTimeMillis() + 1000;
            }
        }
    }

    @SubscribeEvent
    public void onCommandSend(CommandEvent event) {
        if (!shouldBlockDrop() || event.command == null) return;
        if (event.command.getCommandName().toLowerCase().equals("trade")) {
            event.setCanceled(true);
            if (Minecraft.getMinecraft().thePlayer != null) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.RED + "[FPU] Safe mode is on! You can't trade!"
                ));
            }
        }
    }
}