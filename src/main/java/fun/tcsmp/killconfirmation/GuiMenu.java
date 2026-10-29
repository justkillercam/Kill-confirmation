package fun.tcsmp.killconfirmation;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

final class GuiMenu {
    enum Action { YES, CANCEL, NONE }

    static final class Holder implements InventoryHolder {
        final UUID target; // null = self
        final Map<Integer, Action> actions = new HashMap<>();
        Inventory inventory;

        Holder(UUID target) {
            this.target = target;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    private final KillConfirmation plugin;
    private FileConfiguration cfg;

    GuiMenu(KillConfirmation plugin) {
        this.plugin = plugin;
        reload();
    }

    void reload() {
        cfg = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "GUI/gui.yml"));
    }

    void open(Player viewer, Player target) {
        String targetName = target == null ? viewer.getName() : target.getName();
        int rows = Math.max(1, Math.min(6, cfg.getInt("rows", 3)));

        Holder holder = new Holder(target == null ? null : target.getUniqueId());
        Inventory inv = Bukkit.createInventory(holder, rows * 9,
                Text.color(cfg.getString("title", "<red>Kill Confirmation").replace("{target}", targetName)));
        holder.inventory = inv;

        ItemStack filler = build(cfg.getConfigurationSection("filler"), Material.BLACK_STAINED_GLASS_PANE, viewer, targetName);
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler.clone());
            holder.actions.put(i, Action.NONE);
        }

        ConfigurationSection slots = cfg.getConfigurationSection("slots");
        if (slots != null) {
            for (String key : slots.getKeys(false)) {
                int slot;
                try {
                    slot = Integer.parseInt(key);
                } catch (NumberFormatException e) {
                    continue;
                }
                if (slot < 0 || slot >= inv.getSize()) continue;
                ConfigurationSection s = slots.getConfigurationSection(key);
                if (s == null) continue;
                inv.setItem(slot, build(s, Material.BLACK_STAINED_GLASS_PANE, viewer, targetName));
                Action a;
                try {
                    a = Action.valueOf(s.getString("action", "NONE").toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException e) {
                    a = Action.NONE;
                }
                holder.actions.put(slot, a);
            }
        }
        viewer.openInventory(inv);
    }

    @SuppressWarnings("deprecation")
    private ItemStack build(ConfigurationSection s, Material fallback, Player viewer, String targetName) {
        Material m = fallback;
        String name = " ";
        List<String> lore = new ArrayList<>();
        int amount = 1;
        if (s != null) {
            Material parsed = Material.matchMaterial(s.getString("material", fallback.name()));
            if (parsed != null && parsed.isItem()) m = parsed;
            name = s.getString("name", " ");
            amount = Math.max(1, Math.min(64, s.getInt("amount", 1)));
            for (String l : s.getStringList("lore")) lore.add(Text.color(rep(l, viewer, targetName)));
        }
        ItemStack item = new ItemStack(m, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Text.color(rep(name, viewer, targetName)));
            if (!lore.isEmpty()) meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static String rep(String s, Player viewer, String target) {
        return s.replace("{target}", target).replace("{player}", viewer.getName());
    }
}
