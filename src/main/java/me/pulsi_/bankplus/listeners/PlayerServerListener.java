package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.account.BPPlayerManager;
import me.pulsi_.bankplus.account.PlayerRegistry;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.economy.EconomyUtils;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPSets;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
<<<<<<< HEAD
import org.bukkit.scheduler.BukkitTask;
=======
import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
>>>>>>> afb2ba7 (-)

import java.math.BigDecimal;
import java.util.UUID;

public class PlayerServerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        BPPlayerManager pManager = new BPPlayerManager(p);
        boolean wasRegistered = true;
        if (!pManager.isPlayerRegistered()) {
            pManager.registerPlayer();
            if (ConfigValues.isNotifyingNewPlayer()) BPLogger.info("Successfully registered " + p.getName() + "!");
            wasRegistered = false;
        }
        pManager.checkForFileFixes(p, pManager);
        PlayerRegistry.loadPlayer(p, wasRegistered);

        if (!ConfigValues.isNotifyingOfflineInterest()) return;

        BigDecimal amount = BigDecimal.ZERO;
        for (BPEconomy economy : BPEconomy.list()) {
            BigDecimal offlineInterest = economy.getOfflineInterest(p);
            amount = amount.add(offlineInterest);
            if (offlineInterest.compareTo(BigDecimal.ZERO) > 0) economy.setOfflineInterest(p, BigDecimal.ZERO);
        }

        BigDecimal finalAmount = amount;
        if (finalAmount.compareTo(BigDecimal.ZERO) > 0)
<<<<<<< HEAD
            Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE(), () ->
=======
            BankPlus.INSTANCE().getScheduler().runTaskLater(BankPlus.INSTANCE(), () ->
>>>>>>> afb2ba7 (-)
                            BPMessages.send(p, ConfigValues.getOfflineInterestMessage(), BPUtils.placeValues(finalAmount), true),
                    ConfigValues.getNotifyOfflineInterestDelay() * 20L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        BPPlayer player = PlayerRegistry.get(p);
        if (player != null) {
<<<<<<< HEAD
            BukkitTask updating = player.getBankUpdatingTask();
=======
            MyScheduledTask updating = player.getBankUpdatingTask();
>>>>>>> afb2ba7 (-)
            if (updating != null) updating.cancel();
        }

        BPSets.removePlayerFromDepositing(p);
        BPSets.removePlayerFromWithdrawing(p);

        if (!ConfigValues.isSavingOnQuit()) return;

        UUID uuid = p.getUniqueId();
        EconomyUtils.savePlayer(uuid, true);
        PlayerRegistry.unloadPlayer(uuid);
    }
}