package fun.tcsmp.killconfirmation;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/** Only loaded on Folia, so Spigot/Bukkit never touch the entity scheduler. */
final class FoliaSupport {
    private FoliaSupport() {}

    static void run(Plugin plugin, Player player, Runnable task) {
        player.getScheduler().run(plugin, t -> task.run(), null);
    }
}
