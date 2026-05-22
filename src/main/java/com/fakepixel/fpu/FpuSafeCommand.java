package com.fakepixel.fpu;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class FpuSafeCommand extends CommandBase {

    @Override
    public String getCommandName() { return "fpusafe"; }

    @Override
    public String getCommandUsage(ICommandSender sender) { return "/fpusafe"; }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) { return true; }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
     
        FakepixelUtilities.isSafeModeEnabled = !FakepixelUtilities.isSafeModeEnabled;
        
        if (Minecraft.getMinecraft().thePlayer != null) {
            String status = FakepixelUtilities.isSafeModeEnabled ? EnumChatFormatting.GREEN + "ON" : EnumChatFormatting.RED + "OFF";
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                EnumChatFormatting.GOLD + "[FPU] " + EnumChatFormatting.YELLOW + "Safe Mode is now " + status
            ));
        }
    }

    @Override
    public int getRequiredPermissionLevel() { return 0; }
}