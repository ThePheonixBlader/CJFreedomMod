package me.StevenLawson.TotalFreedomMod.Commands;

import me.StevenLawson.TotalFreedomMod.TFM_Util;
import me.StevenLawson.TotalFreedomMod.Bridge.TFM_WorldEditBridge;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = AdminLevel.SUPER, source = SourceType.BOTH)
@CommandParameters(description = "Sets everyone's Worldedit block modification limit to 500.", usage = "/<command>")
public class Command_setl extends TFM_Command
{
    @Override
    public boolean run(CommandSender sender, Player sender_p, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        int limit = 0;

        if (args.length == 0)
        {
            limit = 3000;
        }
        if (args.length == 1)
        {
            limit = Integer.parseInt(args[0]);
        }
        if (limit > 5000)
        {
            TFM_Util.playerMsg(sender, "You are not allowed to set a limit higher than 5000, what are you trying to do?");
        }
        TFM_Util.adminAction(sender.getName(), "Setting everyone's Worldedit block modification limit to " + limit + ".", true);
        TFM_WorldEditBridge web = TFM_WorldEditBridge.getInstance();
        for (final Player player : server.getOnlinePlayers())
        {
            web.setLimit(player, limit);
        }
        return true;
    }
}
