package net.neferett.LinarisKits;

import net.neferett.LinarisKits.api.LinarisKitsAPI;
import net.neferett.LinarisKits.api.User;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandCoins
implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage((Object)ChatColor.RED + "Vous devez être administrateur !");
            return true;
        }
        if (args.length == 3 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("set"))) {
            int amount;
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage((Object)ChatColor.RED + "Le joueur n'est pas connecté !");
                return true;
            }
            try {
                amount = Integer.parseInt(args[2]);
            }
            catch (Exception e) {
                return false;
            }
            User user = LinarisKitsAPI.getUser(target);
            if (args[0].equalsIgnoreCase("give")) {
                user.addCoins(amount);
                sender.sendMessage((Object)ChatColor.GREEN + "Opération effectué !");
            } else if (args[0].equalsIgnoreCase("set")) {
                user.setCoins(amount);
                sender.sendMessage((Object)ChatColor.GREEN + "Opération effectué !");
            }
            return true;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("see")) {
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage((Object)ChatColor.RED + "Le joueur n'est pas connecté !");
                return true;
            }
            User user = LinarisKitsAPI.getUser(target);
            sender.sendMessage((Object)ChatColor.GOLD + target.getName() + " : " + user.getCoins() + " coins");
            return true;
        }
        return false;
    }
}

