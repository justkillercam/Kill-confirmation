package fun.tcsmp.killconfirmation;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.UUID;

public final class KillConfirmation extends JavaPlugin {
    private static final String DEFAULT_PREFIX = "<gray>[<red>K<orange>C<gray>]";

    private boolean folia;
    private boolean dialogSupported;
    private GuiMenu guiMenu;
    private DialogMenu dialogMenu;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveIfMissing("GUI/gui.yml");
        saveIfMissing("GUI/dialog.yml");

        folia = classExists("io.papermc.paper.threadedregions.RegionizedServer");
        dialogSupported = classExists("io.papermc.paper.dialog.Dialog");

        guiMenu = new GuiMenu(this);
        if (dialogSupported) dialogMenu = new DialogMenu(this);
        warnIfDialogUnsupported();

        KillCommand cmd = new KillCommand(this);
        for (String name : new String[]{"kill", "kc", "die", "killconf"}) {
            PluginCommand pc = getCommand(name);
            if (pc != null) {
                pc.setExecutor(cmd);
                pc.setTabCompleter(cmd);
            }
        }
        getServer().getPluginManager().registerEvents(new ConfirmListener(this), this);
    }

    private void saveIfMissing(String path) {
        if (!new File(getDataFolder(), path).exists()) saveResource(path, false);
    }

    private void warnIfDialogUnsupported() {
        if ("dialog".equalsIgnoreCase(getConfig().getString("type", "gui")) && !dialogSupported) {
            getLogger().warning("This server doesn't support Dialogs - falling back to the GUI.");
        }
    }

    private static boolean classExists(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /** /kc reload */
    void reloadAll() {
        saveDefaultConfig();
        saveIfMissing("GUI/gui.yml");
        saveIfMissing("GUI/dialog.yml");
        reloadConfig();
        guiMenu.reload();
        if (dialogMenu != null) dialogMenu.reload();
        warnIfDialogUnsupported();
    }

    /** Opens the confirmation menu. target == null means the viewer themself. */
    void openConfirm(Player viewer, Player target) {
        boolean useDialog = dialogSupported && "dialog".equalsIgnoreCase(getConfig().getString("type", "gui"));
        if (useDialog) dialogMenu.open(viewer, target);
        else guiMenu.open(viewer, target);
    }

    /** Called when YES is pressed (GUI or dialog). */
    void confirm(Player clicker, UUID targetId) {
        Player target = targetId == null ? clicker : Bukkit.getPlayer(targetId);
        if (target == null) {
            send(clicker, msg("player-not-found", "?"));
            return;
        }
        String perm = target.equals(clicker) ? "kc.self" : "kc.other";
        if (!clicker.hasPermission(perm)) {
            send(clicker, msg("no-permission", target.getName()));
            return;
        }
        kill(target);
        if (!target.equals(clicker)) send(clicker, msg("killed", target.getName()));
    }

    void cancel(Player p) {
        send(p, msg("cancelled", p.getName()));
    }

    void kill(Player target) {
        Runnable r = () -> {
            if (target.isOnline() && !target.isDead()) target.setHealth(0.0);
        };
        if (folia) FoliaSupport.run(this, target, r);
        else r.run();
    }

    String prefix() {
        String p = getConfig().getString("prefix", "");
        return Text.color(p == null || p.isBlank() ? DEFAULT_PREFIX : p);
    }

    String msg(String key, String target) {
        String m = getConfig().getString("messages." + key, key);
        return prefix() + " " + Text.color(m.replace("{target}", target));
    }

    void send(CommandSender s, String colored) {
        s.sendMessage(colored);
    }
}
