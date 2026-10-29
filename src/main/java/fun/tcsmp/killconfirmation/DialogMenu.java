package fun.tcsmp.killconfirmation;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Only loaded when Paper's Dialog API exists. */
final class DialogMenu {
    private final KillConfirmation plugin;
    private FileConfiguration cfg;

    DialogMenu(KillConfirmation plugin) {
        this.plugin = plugin;
        reload();
    }

    void reload() {
        cfg = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "GUI/dialog.yml"));
    }

    private static Component comp(String s, String target) {
        return LegacyComponentSerializer.legacySection().deserialize(Text.color(s.replace("{target}", target)));
    }

    void open(Player viewer, Player target) {
        String tn = target == null ? viewer.getName() : target.getName();
        UUID targetId = target == null ? null : target.getUniqueId();

        List<DialogBody> body = new ArrayList<>();
        for (String line : cfg.getStringList("body")) body.add(DialogBody.plainMessage(comp(line, tn)));

        int width = Math.max(1, Math.min(1024, cfg.getInt("button-width", 150)));
        ClickCallback.Options opts = ClickCallback.Options.builder().build();

        ActionButton yes = ActionButton.create(
                comp(cfg.getString("yes.label", "<green>✔ Yes"), tn),
                comp(cfg.getString("yes.tooltip", ""), tn),
                width,
                DialogAction.customClick((view, audience) -> plugin.confirm(viewer, targetId), opts));

        ActionButton no = ActionButton.create(
                comp(cfg.getString("no.label", "<red>✘ Cancel"), tn),
                comp(cfg.getString("no.tooltip", ""), tn),
                width,
                DialogAction.customClick((view, audience) -> plugin.cancel(viewer), opts));

        Dialog dialog = Dialog.create(factory -> factory.empty()
                .base(DialogBase.builder(comp(cfg.getString("title", "<red>Kill Confirmation"), tn))
                        .canCloseWithEscape(cfg.getBoolean("can-close-with-escape", true))
                        .body(body)
                        .build())
                .type(DialogType.confirmation(yes, no)));

        viewer.showDialog(dialog);
    }
}
