package fun.tcsmp.killconfirmation;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

final class ConfirmListener implements Listener {
    private final KillConfirmation plugin;

    ConfirmListener(KillConfirmation plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof GuiMenu.Holder h)) return;
        e.setCancelled(true);
        if (e.getClickedInventory() != e.getInventory()) return;
        if (!(e.getWhoClicked() instanceof Player p)) return;

        GuiMenu.Action a = h.actions.getOrDefault(e.getSlot(), GuiMenu.Action.NONE);
        if (a == GuiMenu.Action.YES) {
            p.closeInventory();
            plugin.confirm(p, h.target);
        } else if (a == GuiMenu.Action.CANCEL) {
            p.closeInventory();
            plugin.cancel(p);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getInventory().getHolder() instanceof GuiMenu.Holder) e.setCancelled(true);
    }
}
