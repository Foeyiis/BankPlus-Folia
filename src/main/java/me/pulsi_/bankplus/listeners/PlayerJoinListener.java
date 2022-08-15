package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BankPlusPlayer;
import me.pulsi_.bankplus.account.BankPlusPlayerFilesUtils;
import me.pulsi_.bankplus.account.economy.MultiEconomyManager;
import me.pulsi_.bankplus.account.economy.OfflineInterestManager;
import me.pulsi_.bankplus.account.economy.SingleEconomyManager;
import me.pulsi_.bankplus.banks.BanksManager;
import me.pulsi_.bankplus.utils.BPChat;
import me.pulsi_.bankplus.utils.BPMethods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.math.BigDecimal;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        BankPlusPlayerFilesUtils.registerPlayer(p);

        saveStatistics(p);
        offlineInterestMessage(p);
    }

    private void saveStatistics(Player p) {
        FileConfiguration config = BankPlusPlayerFilesUtils.getPlayerConfig(p);

        String sOfflineInterest = config.getString("Offline-Interest");
        String sName = config.getString("Account-Name");
        boolean hasChanges = false;

        if (Values.CONFIG.isNotifyOfflineInterest() && sOfflineInterest == null) {
            config.set("Offline-Interest", BPMethods.formatBigDouble(BigDecimal.valueOf(0)));
            hasChanges = true;
        }
        if (sName == null) {
            config.set("Account-Name", p.getName());
            hasChanges = true;
        }

        if (!Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) {
            String sBalance = config.getString("Money");
            if (sBalance == null) {
                config.set("Money", BPMethods.formatBigDouble(Values.CONFIG.getStartAmount()));
                hasChanges = true;
            }
            for (String bankName : BankPlus.instance().getBanks().keySet()) {
                String sLevel = config.getString("Banks." + bankName + ".Level");
                if (sLevel == null) {
                    config.set("Banks." + bankName + ".Level", 1);
                    hasChanges = true;
                }
            }
            if (hasChanges) BankPlusPlayerFilesUtils.savePlayerFile(p, true);
            new SingleEconomyManager(p).loadBankBalance();
        } else {
            for (String bankName : BankPlus.instance().getBanks().keySet()) {
                String sBalance = config.getString("Banks." + bankName + ".Money");
                String sLevel = config.getString("Banks." + bankName + ".Level");
                if (sBalance == null) {
                    if (!Values.CONFIG.getMainGuiName().equals(bankName)) config.set("Banks." + bankName + ".Money", "0.00");
                    else config.set("Banks." + bankName + ".Money", BPMethods.formatBigDouble(Values.CONFIG.getStartAmount()));
                    hasChanges = true;
                }
                if (sLevel == null) {
                    config.set("Banks." + bankName + ".Level", 1);
                    hasChanges = true;
                }
            }
            if (hasChanges) BankPlusPlayerFilesUtils.savePlayerFile(p, true);
            new MultiEconomyManager(p).loadBankBalance();
        }
        BankPlus.instance().getPlayers().put(p.getUniqueId(), new BankPlusPlayer(p));
    }

    private void offlineInterestMessage(Player p) {
        if (!Values.CONFIG.isNotifyOfflineInterest()) return;
        BigDecimal offlineInterest = OfflineInterestManager.getOfflineInterest(p);
        if (offlineInterest.doubleValue() <= 0) return;

        long delay = Values.CONFIG.getNotifyOfflineInterestDelay();
        String message = BPChat.color(Values.CONFIG.getNotifyOfflineInterestMessage()
                .replace("%amount%", BPMethods.formatCommas(offlineInterest))
                .replace("%amount_formatted%", BPMethods.format(offlineInterest))
                .replace("%amount_formatted_long%", BPMethods.formatLong(offlineInterest)));

        if (delay == 0) p.sendMessage(message);
        else Bukkit.getScheduler().runTaskLater(BankPlus.instance(), () -> p.sendMessage(message), delay * 20L);

        OfflineInterestManager.setOfflineInterest(p, new BigDecimal(0), true);
    }
}