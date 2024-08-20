package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.account.PlayerRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
<<<<<<< HEAD
import org.bukkit.scheduler.BukkitTask;
=======
import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
>>>>>>> afb2ba7 (-)

public class InventoryCloseListener implements Listener {

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();

        BPPlayer player = PlayerRegistry.get(p);
        if (player == null) return;

<<<<<<< HEAD
        BukkitTask updating = player.getBankUpdatingTask();
=======
        MyScheduledTask updating = player.getBankUpdatingTask();
>>>>>>> afb2ba7 (-)
        if (updating != null) updating.cancel();

        player.setOpenedBankGui(null);
    }
}