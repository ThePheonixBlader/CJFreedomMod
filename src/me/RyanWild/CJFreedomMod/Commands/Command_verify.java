package me.RyanWild.CJFreedomMod.Commands;

import me.StevenLawson.TotalFreedomMod.TFM_PlayerData;
import me.StevenLawson.TotalFreedomMod.TFM_AdminList;
import me.StevenLawson.TotalFreedomMod.TFM_Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = AdminLevel.SENIOR, source = SourceType.BOTH)
@CommandParameters(description = "Verify an admin.", usage = "/<command>")
public class Command_verify extends CJFM_Command
{
    @Override
    public boolean run(CommandSender sender, Player sender_p, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        Player player = server.getPlayer(args[0]);
        if (!TFM_AdminList.isAdminImpostor(player))
        {
            playerMsg(sender, "This is not an imposter");
            return true;
        }
        else
        {
            TFM_PlayerData playerdata = TFM_PlayerData.getPlayerData(player);
            playerdata.setCaged(false);
            TFM_Util.adminAction(sender.getName(), "Verifying " + player.getName() + " as a Super Admin", false);
            TFM_AdminList.addSuperadmin(player);
        }
        return true;
    }
}
