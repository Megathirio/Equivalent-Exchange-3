package com.pahimar.ee3.command;

import com.pahimar.ee3.api.EnergyValue;
import com.pahimar.ee3.exchange.EnergyValueRegistry;
import com.pahimar.ee3.exchange.WrappedStack;
import com.pahimar.ee3.network.PacketHandler;
import com.pahimar.ee3.network.message.MessageSetEnergyValue;
import com.pahimar.ee3.reference.Files;
import com.pahimar.ee3.util.LogHelper;
import com.pahimar.ee3.util.SerializationHelper;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Map;


public class CommandSetCurrentItemValue extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "ee3-set-current-item-value";
    }

    @Override
    public String getCommandUsage(ICommandSender commandSender)
    {
        return "command.ee3.set-current-item-value.usage";
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args)
    {
        for (int i = 0; i < args.length; i++)
        {
            LogHelper.info(String.format("args[%s]: %s", i, args[i]));
        }

        if (args.length < 2)
        {
            throw new WrongUsageException("command.ee3.set-current-item-value.usage");
        }
        else
        {
            ItemStack itemStack = ((EntityPlayer) commandSender).getCurrentEquippedItem().copy();
            double energyValue = 0;

            if (args.length >= 2)
            {
                energyValue = parseDoubleWithMin(commandSender, args[1], 0);
            }

            if (itemStack != null && Double.compare(energyValue, 0) > 0)
            {
                WrappedStack wrappedStack = new WrappedStack(itemStack);
                EnergyValue newEnergyValue = new EnergyValue(energyValue);

                if (args[0].equalsIgnoreCase("pre"))
                {
                    Map<WrappedStack, EnergyValue> preAssignedValues = SerializationHelper.readEnergyValueStackMapFromJsonFile(Files.PRE_ASSIGNED_ENERGY_VALUES);
                    preAssignedValues.put(wrappedStack, newEnergyValue);
                    SerializationHelper.writeEnergyValueStackMapToJsonFile(Files.PRE_ASSIGNED_ENERGY_VALUES, preAssignedValues);
                    EnergyValueRegistry.getInstance().setShouldRegenNextRestart(true);
                }
                else if (args[0].equalsIgnoreCase("post"))
                {
                    EnergyValueRegistry.getInstance().setEnergyValue(wrappedStack, newEnergyValue);
                    Map<WrappedStack, EnergyValue> postAssignedValues = SerializationHelper.readEnergyValueStackMapFromJsonFile(Files.POST_ASSIGNED_ENERGY_VALUES);
                    postAssignedValues.put(wrappedStack, newEnergyValue);
                    SerializationHelper.writeEnergyValueStackMapToJsonFile(Files.POST_ASSIGNED_ENERGY_VALUES, postAssignedValues);
                    PacketHandler.INSTANCE.sendToAll(new MessageSetEnergyValue(wrappedStack, newEnergyValue));
                }

                // Notify admins and log the value change
                func_152373_a(commandSender, this, "command.ee3.set-current-item-value.success", new Object[]{commandSender.getCommandSenderName(), args[0], wrappedStack.toString(), newEnergyValue.toString()});
                LogHelper.info(String.format("%s set the EnergyValue of %s to %s", commandSender.getCommandSenderName(), wrappedStack, newEnergyValue));
            }
            else
            {
                throw new WrongUsageException("command.ee3.set-current-item-value.usage");
            }
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args)
    {
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, "pre", "post");
        }

        return null;
    }
}
