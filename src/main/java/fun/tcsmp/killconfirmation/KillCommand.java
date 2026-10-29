package fun.tcsmp.killconfirmation;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class KillCommand implements CommandExecutor, TabCompleter {
    private final KillConfirmation plugin;

    KillCommand(KillConfirmation plugin) {
        this.plugin = plugin;
    }

    private static boolean isKcLabel(Command command) {
        String n = command.getName();
        return n.equalsIgnoreCase("kc") || n.equalsIgnoreCase("killconf");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // /kc reload
        if (args.length >= 1 && args[0].equalsIgnoreCase("reload") && isKcLabel(command)) {
            if (!sender.hasPermission("kc.reload")) {
                sender.sendMessage(plugin.msg("no-permission", ""));
                return true;
            }
            plugin.reloadAll();
            sender.sendMessage(plugin.msg("reloaded", ""));
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(plugin.msg("console-usage", ""));
                return true;
            }
            if (!p.hasPermission("kc.self")) {
                plugin.send(p, plugin.msg("no-permission", p.getName()));
                return true;
            }
            plugin.openConfirm(p, null);
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(plugin.msg("player-not-found", args[0]));
            return true;
        }

        // Console / command blocks: no menu possible, kill directly
        if (!(sender instanceof Player p)) {
            plugin.kill(target);
            sender.sendMessage(plugin.msg("killed", target.getName()));
            return true;
        }

        boolean self = p.equals(target);
        if (!p.hasPermission(self ? "kc.self" : "kc.other")) {
            plugin.send(p, plugin.msg("no-permission", target.getName()));
            return true;
        }

        if (self) {
            plugin.openConfirm(p, null);
        } else if (plugin.getConfig().getBoolean("confirm-other", true)) {
            plugin.openConfirm(p, target);
        } else {
            plugin.kill(target);
            plugin.send(p, plugin.msg("killed", target.getName()));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length != 1) return out;
        String start = args[0].toLowerCase(Locale.ROOT);
        if (isKcLabel(command) && sender.hasPermission("kc.reload") && "reload".startsWith(start)) out.add("reload");
        if (!sender.hasPermission("kc.other")) return out;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().toLowerCase(Locale.ROOT).startsWith(start)) out.add(p.getName());
        }
        return out;
    }
}
