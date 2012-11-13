package me.StevenLawson.TotalFreedomMod;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class TFM_SuperadminList
{
    private static Map<String, TFM_Superadmin> superadminList = new HashMap<String, TFM_Superadmin>();
    private static List<String> superadminNames = new ArrayList<String>();
    private static List<String> superadminIPs = new ArrayList<String>();
    private static List<String> superAwesomeAdminsConsole = new ArrayList<String>();

    private TFM_SuperadminList()
    {
        throw new AssertionError();
    }

    public static List<String> getSuperadminIPs()
    {
        return superadminIPs;
    }

    public static List<String> getSuperadminNames()
    {
        return superadminNames;
    }

    public static void loadSuperadminList()
    {
        convertV1List();

        superadminList.clear();

        TFM_Util.createDefaultConfiguration(TotalFreedomMod.SUPERADMIN_FILE, TotalFreedomMod.plugin_file);
        FileConfiguration config = YamlConfiguration.loadConfiguration(new File(TotalFreedomMod.plugin.getDataFolder(), TotalFreedomMod.SUPERADMIN_FILE));

        if (config.isConfigurationSection("superadmins"))
        {
            ConfigurationSection section = config.getConfigurationSection("superadmins");

            for (String admin_name : section.getKeys(false))
            {
                TFM_Superadmin superadmin = new TFM_Superadmin(admin_name, section.getConfigurationSection(admin_name));
                superadminList.put(admin_name.toLowerCase(), superadmin);
            }
        }
        else
        {
            TFM_Log.warning("Missing superadmins section in superadmin.yml.");
        }

        updateIndexLists();
    }

    public static void updateIndexLists()
    {
        superadminNames.clear();
        superadminIPs.clear();

        Iterator<Entry<String, TFM_Superadmin>> it = superadminList.entrySet().iterator();
        while (it.hasNext())
        {
            Entry<String, TFM_Superadmin> pair = it.next();

            String admin_name = pair.getKey().toLowerCase();
            TFM_Superadmin superadmin = pair.getValue();

            superadminNames.add(admin_name);

            for (String ip : superadmin.getIps())
            {
                superadminIPs.add(ip);
            }

            if (superadmin.isSuperAwesomeAdmin())
            {
                superAwesomeAdminsConsole.add(admin_name);

                for (String console_alias : superadmin.getConsoleAliases())
                {
                    superAwesomeAdminsConsole.add(console_alias.toLowerCase());
                }
            }
        }
    }

    public static void convertV1List()
    {
        superadminList.clear();

        TFM_Util.createDefaultConfiguration(TotalFreedomMod.SUPERADMIN_FILE, TotalFreedomMod.plugin_file);
        FileConfiguration config = YamlConfiguration.loadConfiguration(new File(TotalFreedomMod.plugin.getDataFolder(), TotalFreedomMod.SUPERADMIN_FILE));

        if (!config.isConfigurationSection("superadmins"))
        {
            for (String admin_name : config.getKeys(false))
            {
                TFM_Superadmin superadmin = new TFM_Superadmin(admin_name, config.getStringList(admin_name), new Date(), "", false, new ArrayList<String>());
                superadminList.put(admin_name.toLowerCase(), superadmin);
            }

            saveSuperadminList();
        }
    }

    public static void saveSuperadminList()
    {
        updateIndexLists();

        YamlConfiguration config = new YamlConfiguration();

        Iterator<Entry<String, TFM_Superadmin>> it = superadminList.entrySet().iterator();
        while (it.hasNext())
        {
            Entry<String, TFM_Superadmin> pair = it.next();

            String admin_name = pair.getKey().toLowerCase();
            TFM_Superadmin superadmin = pair.getValue();

            config.set("superadmins." + admin_name + ".ips", superadmin.getIps());
            config.set("superadmins." + admin_name + ".last_login", TFM_Util.dateToString(superadmin.getLastLogin()));
            config.set("superadmins." + admin_name + ".custom_login_message", superadmin.getCustomLoginMessage());
            config.set("superadmins." + admin_name + ".is_super_awesome_admin", superadmin.isSuperAwesomeAdmin());
            config.set("superadmins." + admin_name + ".console_aliases", superadmin.getConsoleAliases());
        }

        try
        {
            config.save(new File(TotalFreedomMod.plugin.getDataFolder(), TotalFreedomMod.SUPERADMIN_FILE));
        }
        catch (IOException ex)
        {
            TFM_Log.severe(ex);
        }
    }

    public static TFM_Superadmin getAdminEntry(String admin_name)
    {
        admin_name = admin_name.toLowerCase();

        if (superadminList.containsKey(admin_name))
        {
            return superadminList.get(admin_name);
        }
        else
        {
            return null;
        }
    }

    public static TFM_Superadmin getAdminEntry(Player p)
    {
        return getAdminEntry(p.getName().toLowerCase());
    }

    public static TFM_Superadmin getAdminEntryByIP(String ip)
    {
        Iterator<Entry<String, TFM_Superadmin>> it = superadminList.entrySet().iterator();
        while (it.hasNext())
        {
            Entry<String, TFM_Superadmin> pair = it.next();
            TFM_Superadmin superadmin = pair.getValue();
            if (superadmin.getIps().contains(ip))
            {
                return superadmin;
            }
        }
        return null;
    }

    public static void updateLastLogin(Player p)
    {
        TFM_Superadmin admin_entry = getAdminEntry(p);
        if (admin_entry != null)
        {
            admin_entry.setLastLogin(new Date());
            saveSuperadminList();
        }
    }

    public static boolean isSuperAwesomeAdmin(CommandSender user)
    {
        String user_name = user.getName().toLowerCase();

        if (!(user instanceof Player))
        {
            return superAwesomeAdminsConsole.contains(user_name);
        }

        TFM_Superadmin admin_entry = getAdminEntry((Player) user);
        if (admin_entry != null)
        {
            return admin_entry.isSuperAwesomeAdmin();
        }

        return false;
    }

    public static boolean isUserSuperadmin(CommandSender user)
    {
        if (!(user instanceof Player))
        {
            return true;
        }

        if (Bukkit.getOnlineMode())
        {
            if (superadminNames.contains(user.getName().toLowerCase()))
            {
                return true;
            }
        }

        try
        {
            String user_ip = ((Player) user).getAddress().getAddress().getHostAddress();
            if (user_ip != null && !user_ip.isEmpty())
            {
                if (superadminIPs.contains(user_ip))
                {
                    return true;
                }
            }
        }
        catch (Exception ex)
        {
            return false;
        }

        return false;
    }

    public static boolean checkPartialSuperadminIP(String user_ip)
    {
        user_ip = user_ip.trim();

        if (superadminIPs.contains(user_ip))
        {
            return true;
        }
        else
        {
            String[] user_octets = user_ip.split("\\.");
            if (user_octets.length != 4)
            {
                return false;
            }

            String match_ip = null;
            for (String test_ip : getSuperadminIPs())
            {
                String[] test_octets = test_ip.split("\\.");
                if (test_octets.length == 4)
                {
                    if (user_octets[0].equals(test_octets[0]) && user_octets[1].equals(test_octets[1]) && user_octets[2].equals(test_octets[2]))
                    {
                        match_ip = test_ip;
                        break;
                    }
                }
            }

            if (match_ip != null)
            {
                TFM_Superadmin admin_entry = getAdminEntryByIP(match_ip);

                if (admin_entry != null)
                {
                    List<String> ips = admin_entry.getIps();
                    ips.add(user_ip);
                    admin_entry.setIps(ips);
                    saveSuperadminList();
                }

                return true;
            }
        }

        return false;
    }

    public static boolean isSuperadminImpostor(CommandSender user)
    {
        if (!(user instanceof Player))
        {
            return false;
        }

        Player p = (Player) user;

        if (superadminNames.contains(p.getName().toLowerCase()))
        {
            return !isUserSuperadmin(p);
        }

        return false;
    }

    public static void addSuperadmin(String admin_name, List<String> ips)
    {
        Date last_login = new Date();
        String custom_login_message = "";
        boolean is_super_awesome_admin = false;
        List<String> console_aliases = new ArrayList<String>();

        TFM_Superadmin superadmin = new TFM_Superadmin(admin_name, ips, last_login, custom_login_message, is_super_awesome_admin, console_aliases);
        superadminList.put(admin_name.toLowerCase(), superadmin);

        saveSuperadminList();
    }

    public static void addSuperadmin(Player p)
    {
        String admin_name = p.getName().toLowerCase();
        List<String> ips = Arrays.asList(p.getAddress().getAddress().getHostAddress());

        addSuperadmin(admin_name, ips);
    }

    public static void addSuperadmin(String admin_name)
    {
        addSuperadmin(admin_name, new ArrayList<String>());
    }

    public static void removeSuperadmin(String admin_name)
    {
        admin_name = admin_name.toLowerCase();

        if (superadminList.containsKey(admin_name))
        {
            superadminList.remove(admin_name);
        }

        saveSuperadminList();
    }

    public static void removeSuperadmin(Player p)
    {
        removeSuperadmin(p.getName());
    }
}
