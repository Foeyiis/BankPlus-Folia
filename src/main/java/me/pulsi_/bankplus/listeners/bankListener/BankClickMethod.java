package me.pulsi_.bankplus.listeners.bankListener;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.account.PlayerRegistry;
import me.pulsi_.bankplus.bankSystem.*;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPFormatter;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.Values;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.math.BigDecimal;
import java.util.HashMap;

public class BankClickMethod {

    public static void process(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        Inventory bankInventory = e.getClickedInventory();
        if (bankInventory == null || bankInventory.getHolder() == null || !(bankInventory.getHolder() instanceof BankHolder)) return;
        e.setCancelled(true);

        BPPlayer player = PlayerRegistry.get(p);
        if (player.getOpenedBankGui() == null) return;

        int slot = e.getSlot();
        BankGui openedBank = player.getOpenedBankGui();

        if (openedBank instanceof BankListGui) {
            BankListGui bankListGui = (BankListGui) openedBank;
            Bank clickedBank = bankListGui.getBankListGuiClickHolder().get(slot);
            if (clickedBank != null) {
                p.closeInventory();
                clickedBank.openBankGui(p);
            }
            return;
        }

        Bank.BankItem clickedItem = openedBank.getBankItems().get(slot);
        if (clickedItem == null) return;

        String bankName = openedBank.getIdentifier();
        for (String action : clickedItem.getActions()) {
            String identifier = action.substring(action.indexOf("["), action.indexOf("]") + 1), value = action.replace(identifier + " ", "");

            switch (identifier.toLowerCase()) {
                case "[console]":
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), value);
                    break;

                case "[deposit]": {
                    if (Values.CONFIG.isGuiActionsNeedPermissions() && !BPUtils.hasPermission(p, "bankplus.deposit")) return;

                    if (value.equalsIgnoreCase("CUSTOM")) {
                        BPUtils.customDeposit(p, bankName);
                        continue;
                    }

                    BigDecimal amount;
                    try {
                        if (!value.endsWith("%")) amount = BPFormatter.getStyledBigDecimal(value);
                        else {
                            BigDecimal percentage = BPFormatter.getStyledBigDecimal(value.replace("%", "")).divide(BigDecimal.valueOf(100));
                            amount = BigDecimal.valueOf(BankPlus.INSTANCE().getVaultEconomy().getBalance(p)).multiply(percentage);
                        }
                    } catch (NumberFormatException ex) {
                        BPLogger.warn("Could not deposit because an invalid number has been specified! (Bank gui: " + bankName + ", Item slot: " + slot + ", Value: " + value + ")");
                        continue;
                    }
                    BPEconomy.get(bankName).deposit(p, amount);
                }
                break;

                case "[player]":
                    p.chat(value);
                    break;

                case "[withdraw]": {
                    if (Values.CONFIG.isGuiActionsNeedPermissions() && !BPUtils.hasPermission(p, "bankplus.withdraw")) return;

                    if (value.equals("CUSTOM")) {
                        BPUtils.customWithdraw(p, bankName);
                        continue;
                    }

                    BPEconomy economy = BPEconomy.get(bankName);
                    BigDecimal amount;

                    try {
                        if (!value.endsWith("%")) amount = new BigDecimal(value);
                        else {
                            BigDecimal percentage = BPFormatter.getStyledBigDecimal(value.replace("%", "")).divide(BigDecimal.valueOf(100));
                            amount = economy.getBankBalance(p).multiply(percentage);
                        }
                    } catch (NumberFormatException ex) {
                        BPLogger.warn("Could not withdraw because an invalid number has been specified! (Bank gui: " + bankName + ", Item slot: " + slot + ", Value: " + value + ")");
                        continue;
                    }
                    economy.withdraw(p, amount);
                }
                break;

                case "[upgrade]":
                    BankUtils.upgradeBank(BankUtils.getBank(bankName), p);
            }
        }
    }
}