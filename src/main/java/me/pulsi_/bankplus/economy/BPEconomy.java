package me.pulsi_.bankplus.economy;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayerManager;
import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankManager;
import me.pulsi_.bankplus.events.BPAfterTransactionEvent;
import me.pulsi_.bankplus.events.BPPreTransactionEvent;
import me.pulsi_.bankplus.mySQL.SQLPlayerManager;
import me.pulsi_.bankplus.utils.BPFormatter;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.Values;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

public class BPEconomy {

    private final String bankName;
    private final HashMap<UUID, Holder> balances = new HashMap<>();
    private final Set<UUID> transactions = new HashSet<>();

    private final String moneyPath, interestPath, debtPath, levelPath;

    public BPEconomy(String bankName) {
        this.bankName = bankName;

        this.moneyPath = "banks." + bankName + ".money";
        this.interestPath = "banks." + bankName + ".interest";
        this.debtPath = "banks." + bankName + ".debt";
        this.levelPath = "banks." + bankName + ".level";
    }

    public static BPEconomy get(String bankName) {
        BankPlus pl = BankPlus.INSTANCE();
        if (pl == null) return null;

        return pl.getBankGuiRegistry().getBanks().get(bankName).getBankEconomy();
    }

    public static List<BPEconomy> list() {
        List<BPEconomy> economies = new ArrayList<>();
        BankPlus pl = BankPlus.INSTANCE();
        if (pl == null) return economies;

        for (Bank bank : pl.getBankGuiRegistry().getBanks().values()) economies.add(bank.getBankEconomy());
        return economies;
    }

    public static List<String> nameList() {
        List<String> economies = new ArrayList<>();
        BankPlus pl = BankPlus.INSTANCE();
        if (pl == null) return economies;

        economies.addAll(pl.getBankGuiRegistry().getBanks().keySet());
        return economies;
    }

    /**
     * Return a list with all player balances of all banks.
     *
     * @return A hashmap with the player name as KEY and the sum of all the player bank balances as VALUE.
     */
    public static LinkedHashMap<String, BigDecimal> getAllEconomiesBankBalances() {
        LinkedHashMap<String, BigDecimal> balances = new LinkedHashMap<>();
        for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
            BigDecimal balance = new BigDecimal(0);
            for (String bankName : BankManager.getAvailableBanks(p)) balance = balance.add(get(bankName).getBankBalance(p));
            balances.put(p.getName(), balance);
        }
        return balances;
    }

    public boolean isPlayerLoaded(OfflinePlayer p) {
        return balances.containsKey(p.getUniqueId());
    }

    public Set<UUID> getLoadedPlayers() {
        return new HashSet<>(balances.keySet());
    }

    public void loadPlayer(OfflinePlayer p) {
        if (isPlayerLoaded(p)) return;

        Holder holder = new Holder();
        holder.debt = getDebt(p);
        holder.money = getBankBalance(p);
        holder.offlineInterest = getOfflineInterest(p);
        holder.bankLevel = getBankLevel(p);

        balances.put(p.getUniqueId(), holder);
    }

    public void unloadPlayer(UUID uuid) {
        balances.remove(uuid);
    }

    /**
     * Return a list with all player balances of that bank.
     *
     * @return A hashmap with the player name as KEY and the sum of all the player bank balances as VALUE.
     */
    public LinkedHashMap<String, BigDecimal> getAllBankBalances() {
        LinkedHashMap<String, BigDecimal> balances = new LinkedHashMap<>();
        for (OfflinePlayer p : Bukkit.getOfflinePlayers()) balances.put(p.getName(), getBankBalance(p));
        return balances;
    }

    /**
     * Get the sum of player bank balances of all banks.
     *
     * @param p The player.
     */
    public static BigDecimal getBankBalancesSum(OfflinePlayer p) {
        BigDecimal amount = BigDecimal.valueOf(0);
        for (BPEconomy economy : list())
            amount = amount.add(economy.getBankBalance(p));
        return amount;
    }

    /**
     * Get the player bank balance of the selected bank.
     *
     * @param uuid The UUID of the player.
     */
    public BigDecimal getBankBalance(UUID uuid) {
        return getBankBalance(Bukkit.getOfflinePlayer(uuid));
    }

    /**
     * Get the player bank balance of the selected bank.
     *
     * @param p The player.
     */
    public BigDecimal getBankBalance(OfflinePlayer p) {
        if (balances.containsKey(p.getUniqueId())) return balances.get(p.getUniqueId()).money;
        if (BankPlus.INSTANCE().getMySql().isConnected()) return new SQLPlayerManager(p).getMoney(bankName);

        String bal = new BPPlayerManager(p).getPlayerConfig().getString(moneyPath);
        return new BigDecimal(bal == null ? "0" : bal);
    }

    /**
     * Set the selected amount in the selected bank.
     *
     * @return Number representing the actual amount set.
     */
    public BigDecimal setBankBalance(OfflinePlayer p, BigDecimal amount) {
        return setBankBalance(p, amount, TransactionType.SET);
    }

    /**
     * Set the selected amount in the selected bank.
     *
     * @param ignoreEvents Choose if ignoring or not the bankplus transaction event.
     * @return Number representing the actual amount set.
     */
    public BigDecimal setBankBalance(OfflinePlayer p, BigDecimal amount, boolean ignoreEvents) {
        return setBankBalance(p, amount, ignoreEvents, TransactionType.SET);
    }

    /**
     * Set the selected amount in the selected bank.
     *
     * @param type Override the transaction type with the one you choose.
     * @return Number representing the actual amount set.
     */
    public BigDecimal setBankBalance(OfflinePlayer p, BigDecimal amount, TransactionType type) {
        return setBankBalance(p, amount, false, type);
    }

    private BigDecimal setBankBalance(OfflinePlayer p, BigDecimal amount, boolean ignoreEvents, TransactionType type) {
        BigDecimal result = new BigDecimal(0);
        if (startTransaction(p)) return result;

        if (!ignoreEvents) {
            BPPreTransactionEvent event = preTransactionEvent(p, type, amount, bankName);
            if (event.isCancelled()) {
                endTransaction(p);
                return result;
            }

            amount = event.getTransactionAmount();
        }

        result = result.max(amount.min(BankManager.getCapacity(bankName, p)));
        set(p, result);

        if (!ignoreEvents) afterTransactionEvent(p, type, amount, bankName);
        return result;
    }

    /**
     * Add the selected amount to the selected bank.
     *
     * @return Number representing the actual amount added.
     */
    public BigDecimal addBankBalance(OfflinePlayer p, BigDecimal amount) {
        return addBankBalance(p, amount, false, TransactionType.ADD, false);
    }

    /**
     * Add the selected amount to the selected bank.
     *
     * @param ignoreEvents Choose if ignoring or not the bankplus transaction event.
     * @return Number representing the actual amount added.
     */
    public BigDecimal addBankBalance(OfflinePlayer p, BigDecimal amount, boolean ignoreEvents) {
        return addBankBalance(p, amount, ignoreEvents, TransactionType.ADD, false);
    }

    /**
     * Add the selected amount to the selected bank.
     *
     * @param type Override the transaction type with the one you choose.
     * @return Number representing the actual amount added.
     */
    public BigDecimal addBankBalance(OfflinePlayer p, BigDecimal amount, TransactionType type) {
        return addBankBalance(p, amount, false, type, false);
    }

    /**
     * Add the selected amount to the selected bank.
     *
     * @param type               Override the transaction type with the one you choose.
     * @param addOfflineInterest Choose if updating the offline interest with this transaction.
     * @return Number representing the actual amount added.
     */
    public BigDecimal addBankBalance(OfflinePlayer p, BigDecimal amount, TransactionType type, boolean addOfflineInterest) {
        return addBankBalance(p, amount, false, type, addOfflineInterest);
    }

    private BigDecimal addBankBalance(OfflinePlayer p, BigDecimal amount, boolean ignoreEvents, TransactionType type, boolean addOfflineInterest) {
        BigDecimal result = new BigDecimal(0);
        if (startTransaction(p)) return result;

        if (!ignoreEvents) {
            BPPreTransactionEvent event = preTransactionEvent(p, type, amount, bankName);
            if (event.isCancelled()) {
                endTransaction(p);
                return result;
            }

            amount = event.getTransactionAmount();
        }

        BigDecimal capacity = BankManager.getCapacity(bankName, p), balance = getBankBalance(p);
        if (capacity.doubleValue() <= 0D || balance.add(amount).doubleValue() < capacity.doubleValue()) {
            result = amount;
            BigDecimal newBalance = balance.add(result);
            if (addOfflineInterest) set(p, newBalance, result);
            else set(p, newBalance);
        } else {
            result = capacity.subtract(balance);
            if (addOfflineInterest) set(p, capacity, result);
            else set(p, capacity);
        }

        if (!ignoreEvents) afterTransactionEvent(p, type, amount, bankName);
        return result;
    }

    /**
     * Remove the selected amount.
     *
     * @return Number representing the actual amount removed.
     */
    public BigDecimal removeBankBalance(OfflinePlayer p, BigDecimal amount) {
        return removeBankBalance(p, amount, false, TransactionType.REMOVE);
    }

    /**
     * Remove the selected amount.
     *
     * @param ignoreEvents Choose if ignoring or not the bankplus transaction event.
     * @return Number representing the actual amount removed.
     */
    public BigDecimal removeBankBalance(OfflinePlayer p, BigDecimal amount, boolean ignoreEvents) {
        return removeBankBalance(p, amount, ignoreEvents, TransactionType.REMOVE);
    }

    /**
     * Remove the selected amount.
     *
     * @param type Override the transaction type with the one you choose.
     * @return Number representing the actual amount removed.
     */
    public BigDecimal removeBankBalance(OfflinePlayer p, BigDecimal amount, TransactionType type) {
        return removeBankBalance(p, amount, false, type);
    }

    private BigDecimal removeBankBalance(OfflinePlayer p, BigDecimal amount, boolean ignoreEvents, TransactionType type) {
        BigDecimal result = new BigDecimal(0);
        if (startTransaction(p)) return result;

        if (!ignoreEvents) {
            BPPreTransactionEvent event = preTransactionEvent(p, type, amount, bankName);
            if (event.isCancelled()) {
                endTransaction(p);
                return result;
            }

            amount = event.getTransactionAmount();
        }

        BigDecimal balance = getBankBalance(p);
        if (balance.subtract(amount).doubleValue() < 0D) result = balance;
        else result = amount;

        set(p, balance.subtract(result));

        if (!ignoreEvents) afterTransactionEvent(p, type, result, bankName);
        return result;
    }

    /**
     * Get the offline interest earned from the selected player in the selected bank.
     *
     * @param p The player.
     * @return Offline interest.
     */
    public BigDecimal getOfflineInterest(OfflinePlayer p) {
        return getOfflineInterest(p.getUniqueId());
    }

    /**
     * Get the offline interest earned from the selected player in the selected bank.
     *
     * @param uuid The player UUID.
     * @return Offline interest.
     */
    public BigDecimal getOfflineInterest(UUID uuid) {
        if (balances.containsKey(uuid)) return balances.get(uuid).offlineInterest;
        if (BankPlus.INSTANCE().getMySql().isConnected()) return new SQLPlayerManager(uuid).getOfflineInterest(bankName);

        String interest = new BPPlayerManager(uuid).getPlayerConfig().getString(interestPath);
        return new BigDecimal(interest == null ? "0" : interest);
    }

    /**
     * Set the offline interest to the selected amount in the selected bank.
     *
     * @param p      The player.
     * @param amount The new amount.
     */
    public void setOfflineInterest(OfflinePlayer p, BigDecimal amount) {
        if (startTransaction(p)) return;

        if (balances.containsKey(p.getUniqueId())) {
            balances.get(p.getUniqueId()).setOfflineInterest(amount);
            endTransaction(p);
            return;
        }

        if (BankPlus.INSTANCE().getMySql().isConnected()) {
            Bukkit.getScheduler().runTaskAsynchronously(BankPlus.INSTANCE(), () -> {
                new SQLPlayerManager(p).setOfflineInterest(amount, bankName);
                endTransaction(p);
            });
            return;
        }

        configSet(p, interestPath, amount);
        endTransaction(p);
    }

    /**
     * Get the player bank debt of the selected bank.
     *
     * @param p The player.
     */
    public BigDecimal getDebt(OfflinePlayer p) {
        return getDebt(p.getUniqueId());
    }

    /**
     * Get the player bank debt of the selected bank.
     *
     * @param uuid The player UUID.
     */
    public BigDecimal getDebt(UUID uuid) {
        if (balances.containsKey(uuid)) return balances.get(uuid).debt;
        if (BankPlus.INSTANCE().getMySql().isConnected()) return new SQLPlayerManager(uuid).getDebt(bankName);

        String bal = new BPPlayerManager(uuid).getPlayerConfig().getString(debtPath);
        return new BigDecimal(bal == null ? "0" : bal);
    }

    /**
     * Set the player bank debt to the selected amount.
     *
     * @param p      The player.
     * @param amount The new debt amount.
     */
    public void setDebt(OfflinePlayer p, BigDecimal amount) {
        if (startTransaction(p)) return;

        if (balances.containsKey(p.getUniqueId())) {
            balances.get(p.getUniqueId()).setDebt(amount);
            endTransaction(p);
            return;
        }

        if (BankPlus.INSTANCE().getMySql().isConnected()) {
            Bukkit.getScheduler().runTaskAsynchronously(BankPlus.INSTANCE(), () -> {
                new SQLPlayerManager(p).setDebt(amount, bankName);
                endTransaction(p);
            });
            return;
        }

        configSet(p, debtPath, amount);
        endTransaction(p);
    }

    /**
     * Get the current bank level of that player.
     * @param p The player.
     * @return The current bank level.
     */
    public int getBankLevel(OfflinePlayer p) {
        return getBankLevel(p.getUniqueId());
    }

    /**
     * Get the current bank level of that player.
     * @param uuid The player UUID.
     * @return The current bank level.
     */
    public int getBankLevel(UUID uuid) {
        if (balances.containsKey(uuid)) return balances.get(uuid).bankLevel;
        if (BankPlus.INSTANCE().getMySql().isConnected()) return new SQLPlayerManager(uuid).getLevel(bankName);
        return Math.max(new BPPlayerManager(uuid).getPlayerConfig().getInt(levelPath), 1);
    }

    public void setBankLevel(OfflinePlayer p, int level) {
        if (startTransaction(p)) return;

        if (balances.containsKey(p.getUniqueId())) {
            balances.get(p.getUniqueId()).setBankLevel(level);
            endTransaction(p);
            return;
        }

        if (BankPlus.INSTANCE().getMySql().isConnected()) {
            Bukkit.getScheduler().runTaskAsynchronously(BankPlus.INSTANCE(), () -> {
                new SQLPlayerManager(p).setLevel(level, bankName);
                endTransaction(p);
            });
            return;
        }

        configSet(p, levelPath, level);
        endTransaction(p);
    }

    /**
     * Method internally used to simplify the transactions.
     */
    private void set(OfflinePlayer p, BigDecimal amount) {
        set(p, amount, new BigDecimal(0));
    }

    /**
     * Method internally used to simplify the transactions.
     */
    private void set(OfflinePlayer p, BigDecimal amount, BigDecimal offlineInterest) {
        boolean changeOfflineInterest = offlineInterest.doubleValue() > 0d;

        if (balances.containsKey(p.getUniqueId())) {
            Holder holder = balances.get(p.getUniqueId());
            holder.setMoney(amount);
            if (changeOfflineInterest) holder.setOfflineInterest(offlineInterest);
            endTransaction(p);
            return;
        }

        if (BankPlus.INSTANCE().getMySql().isConnected()) {
            Bukkit.getScheduler().runTaskAsynchronously(BankPlus.INSTANCE(), () -> {
                SQLPlayerManager pManager = new SQLPlayerManager(p);
                pManager.setMoney(amount, bankName);
                if (changeOfflineInterest) pManager.setOfflineInterest(offlineInterest, bankName);
                endTransaction(p);
            });
            return;
        }

        configSet(p, moneyPath, amount, offlineInterest);
        endTransaction(p);
    }

    public void deposit(Player p, BigDecimal amount) {
        if (isInTransaction(p)) return;

        BPPreTransactionEvent event = preTransactionEvent(p, TransactionType.DEPOSIT, amount, bankName);
        if (event.isCancelled()) return;

        amount = event.getTransactionAmount();
        if (amount.doubleValue() < Values.CONFIG.getDepositMinimumAmount().doubleValue()) {
            BPMessages.send(p, "Minimum-Number", "%min%$" + Values.CONFIG.getDepositMinimumAmount());
            return;
        }

        Economy economy = BankPlus.INSTANCE().getVaultEconomy();
        BigDecimal wallet = BigDecimal.valueOf(economy.getBalance(p));
        if (!BPUtils.checkPreRequisites(wallet, amount, p) || BPUtils.isBankFull(p, bankName)) return;

        if (wallet.doubleValue() < amount.doubleValue()) amount = wallet;

        BigDecimal maxDepositAmount = Values.CONFIG.getMaxDepositAmount();
        if (maxDepositAmount.doubleValue() != 0 && amount.doubleValue() >= maxDepositAmount.doubleValue())
            amount = maxDepositAmount;

        BigDecimal taxes = new BigDecimal(0);
        if (Values.CONFIG.getDepositTaxes().doubleValue() > 0 && !p.hasPermission("bankplus.deposit.bypass-taxes"))
            taxes = amount.multiply(Values.CONFIG.getDepositTaxes().divide(BigDecimal.valueOf(100)));

        BigDecimal capacity = BankManager.getCapacity(bankName, p);
        BigDecimal newBankBalance = getBankBalance(p).add(amount);

        /*
        Make it possible so when depositing all your money with taxes, the money will have the ability
        to FILL the bank instead of always depositing a bit less and never filling up the bank.
        */
        if (capacity.doubleValue() > 0d && newBankBalance.doubleValue() >= capacity.doubleValue()) {
            BigDecimal moneyToFull = capacity.subtract(getBankBalance(p));
            amount = moneyToFull.add(taxes);
        }

        EconomyResponse depositResponse = economy.withdrawPlayer(p, amount.doubleValue());
        if (BPUtils.hasFailed(p, depositResponse)) return;

        addBankBalance(p, amount.subtract(taxes), true);
        BPMessages.send(p, "Success-Deposit", BPUtils.placeValues(p, amount.subtract(taxes)), BPUtils.placeValues(taxes, "taxes"));
        BPUtils.playSound("DEPOSIT", p);

        afterTransactionEvent(p, TransactionType.DEPOSIT, amount, bankName);
    }

    public void withdraw(Player p, BigDecimal amount) {
        if (isInTransaction(p)) return;

        BPPreTransactionEvent event = preTransactionEvent(p, TransactionType.WITHDRAW, amount, bankName);
        if (event.isCancelled()) return;

        amount = event.getTransactionAmount();
        if (amount.doubleValue() < Values.CONFIG.getWithdrawMinimumAmount().doubleValue()) {
            BPMessages.send(p, "Minimum-Number", "%min%$" + Values.CONFIG.getWithdrawMinimumAmount());
            return;
        }

        BigDecimal bankBal = getBankBalance(p);
        if (!BPUtils.checkPreRequisites(bankBal, amount, p)) return;

        if (bankBal.doubleValue() < amount.doubleValue()) amount = bankBal;

        BigDecimal maxWithdrawAmount = Values.CONFIG.getMaxWithdrawAmount();
        if (maxWithdrawAmount.doubleValue() > 0 && amount.doubleValue() >= maxWithdrawAmount.doubleValue())
            amount = maxWithdrawAmount;

        BigDecimal taxes = new BigDecimal(0);
        if (Values.CONFIG.getWithdrawTaxes().doubleValue() > 0 && !p.hasPermission("bankplus.withdraw.bypass-taxes"))
            taxes = amount.multiply(Values.CONFIG.getWithdrawTaxes().divide(BigDecimal.valueOf(100)));

        EconomyResponse withdrawResponse = BankPlus.INSTANCE().getVaultEconomy().depositPlayer(p, amount.subtract(taxes).doubleValue());
        if (BPUtils.hasFailed(p, withdrawResponse)) return;

        removeBankBalance(p, amount, true);
        BPMessages.send(p, "Success-Withdraw", BPUtils.placeValues(p, amount.subtract(taxes)), BPUtils.placeValues(taxes, "taxes"));
        BPUtils.playSound("WITHDRAW", p);

        afterTransactionEvent(p, TransactionType.WITHDRAW, amount, bankName);
    }

    /**
     * Method used to execute the pay transaction.
     *
     * @param from     The player that will give the money.
     * @param to       The player that will receive your money.
     * @param amount   How much money you want to pay.
     * @param toBank   The bank where the money will be added.
     */
    public void pay(Player from, Player to, BigDecimal amount, String toBank) {
        if (isInTransaction(from)) return;

        BigDecimal senderBalance = getBankBalance(from);
        // Check if the sender has at least more than 0 money
        if (senderBalance.compareTo(amount) < 0) {
            BPMessages.send(from, "Insufficient-Money");
            return;
        }

        BPEconomy toEconomy = get(toBank);
        // Check if the receiver of the payment has the bank full
        if (toEconomy.getBankBalance(to).compareTo(BankManager.getCapacity(toBank, to)) >= 0) {
            BPMessages.send(from, "Bank-Full", "%player%$" + to.getName());
            return;
        }

        BigDecimal added = toEconomy.addBankBalance(to, amount, TransactionType.PAY), extra = amount.subtract(added);
        BPMessages.send(to, "Payment-Received", BPUtils.placeValues(from, added));

        BigDecimal removed = removeBankBalance(from, amount.subtract(extra), TransactionType.PAY);
        BPMessages.send(from, "Payment-Sent", BPUtils.placeValues(to, removed));
    }

    /**
     * Returns the name of the bank that own this economy.
     * @return A string representing the bank name.
     */
    public String getBankName() {
        return bankName;
    }

    public BPPreTransactionEvent preTransactionEvent(OfflinePlayer p, TransactionType type, BigDecimal amount, String bankName) {
        BPPreTransactionEvent event = new BPPreTransactionEvent(
                p, type, getBankBalance(p), BankPlus.INSTANCE().getVaultEconomy().getBalance(p), amount, bankName
        );
        BPUtils.callEvent(event);
        return event;
    }

    public void afterTransactionEvent(OfflinePlayer p, TransactionType type, BigDecimal amount, String bankName) {
        BPAfterTransactionEvent event = new BPAfterTransactionEvent(
                p, type, getBankBalance(p), BankPlus.INSTANCE().getVaultEconomy().getBalance(p), amount, bankName
        );
        BPUtils.callEvent(event);
    }

    private boolean startTransaction(OfflinePlayer p) {
        if (transactions.contains(p.getUniqueId())) return true;
        transactions.add(p.getUniqueId());
        return false;
    }

    private boolean isInTransaction(OfflinePlayer p) {
        return transactions.contains(p.getUniqueId());
    }

    private void endTransaction(OfflinePlayer p) {
        transactions.remove(p.getUniqueId());
    }

    private void configSet(OfflinePlayer p, String path, BigDecimal value) {
        configSet(p, path, value, BigDecimal.valueOf(0));
    }

    private void configSet(OfflinePlayer p, String path, int value) {
        configSet(p, path, value, BigDecimal.valueOf(0));
    }

    private void configSet(OfflinePlayer p, String path, BigDecimal value, BigDecimal offlineInterest) {
        BPPlayerManager files = new BPPlayerManager(p);
        File file = files.getPlayerFile();
        FileConfiguration config = files.getPlayerConfig(file);
        config.set(path, BPFormatter.formatBigDecimal(BPFormatter.getBigDoubleFormatted(value).max(BigDecimal.valueOf(0))));
        if (offlineInterest.doubleValue() > 0d) config.set(interestPath, BPFormatter.formatBigDecimal(BPFormatter.getBigDoubleFormatted(offlineInterest).max(BigDecimal.valueOf(0))));
        files.savePlayerFile(config, file, true);
    }

    private void configSet(OfflinePlayer p, String path, int value, BigDecimal offlineInterest) {
        BPPlayerManager files = new BPPlayerManager(p);
        File file = files.getPlayerFile();
        FileConfiguration config = files.getPlayerConfig(file);
        config.set(path, Math.max(1, value));
        if (offlineInterest.doubleValue() > 0d) config.set(interestPath, BPFormatter.formatBigDecimal(BPFormatter.getBigDoubleFormatted(offlineInterest).max(BigDecimal.valueOf(0))));
        files.savePlayerFile(config, file, true);
    }

    private static class Holder {
        private BigDecimal money = new BigDecimal(0), offlineInterest = new BigDecimal(0), debt = new BigDecimal(0);
        private int bankLevel = 1;

        public void setMoney(BigDecimal money) {
            this.money = BPFormatter.getBigDoubleFormatted(money).max(BigDecimal.valueOf(0));
        }

        public void setOfflineInterest(BigDecimal offlineInterest) {
            this.debt = BPFormatter.getBigDoubleFormatted(offlineInterest).max(BigDecimal.valueOf(0));
        }

        public void setDebt(BigDecimal debt) {
            this.debt = BPFormatter.getBigDoubleFormatted(debt).max(BigDecimal.valueOf(0));
        }

        public void setBankLevel(int bankLevel) {
            this.bankLevel = Math.max(1, bankLevel);
        }
    }
}