package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayerManager;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.mySQL.SQLPlayerManager;
import me.pulsi_.bankplus.utils.texts.BPArgs;
import me.pulsi_.bankplus.utils.texts.BPFormatter;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TransferCmd extends BPCommand {

    public TransferCmd(FileConfiguration commandsConfig, String... aliases) {
        super(commandsConfig, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Arrays.asList(
                "%prefix% &cUsage: &7/bank transfer [mode] | Specify a mode between &a\"filesToDatabase\" &7and &a\"databaseToFiles\"&7.",
                "&7Use this command to &a&ntransfer&7 the playerdata from a place to another in case you &a&nswitch&7 saving mode."
        );
    }

    @Override
    public int defaultConfirmCooldown() {
        return 5;
    }

    @Override
    public List<String> defaultConfirmMessage() {
        return Collections.singletonList("%prefix% &cThis command will overwrite the data from a place to another, type the command again within 5 seconds to confirm this action.");
    }

    @Override
    public int defaultCooldown() {
        return 0;
    }

    @Override
    public List<String> defaultCooldownMessage() {
        return Collections.emptyList();
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

    @Override
    public boolean skipUsageWarn() {
        return false;
    }

    @Override
    public boolean preCmdChecks(CommandSender s, String[] args) {
        String mode = args[1].toLowerCase();

        if (!mode.equals("filestodatabase") && !mode.equals("databasetofiles")) {
            BPMessages.send(s, "Invalid-Action");
            return false;
        }

        if (!ConfigValues.isSqlEnabled()) {
            BPMessages.send(s, "%prefix% &cCould not initialize the task, MySQL hasn't been enabled in the config file!", true);
            return false;
        }

        if (!BankPlus.INSTANCE().getMySql().isConnected()) {
            BPMessages.send(s, "%prefix% &cCould not initialize the task, MySQL hasn't been connected to it's database yet! &8(Try typing /bp reload)", true);
            return false;
        }
        return true;
    }

    @Override
    public void onExecution(CommandSender s, String[] args) {
        BPMessages.send(s, "%prefix% &7Task initialized, wait a few moments...", true);

<<<<<<< HEAD
        Bukkit.getScheduler().runTaskAsynchronously(BankPlus.INSTANCE(), () -> {
=======
        BankPlus.INSTANCE().getScheduler().runTaskAsynchronously(BankPlus.INSTANCE(), () -> {
>>>>>>> afb2ba7 (-)
            if (args[1].equalsIgnoreCase("filestodatabase")) filesToDatabase();
            else databaseToFile();

            BPMessages.send(s, "%prefix% &2Task finished!", true);
        });
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 1)
            return BPArgs.getArgs(args, "databaseToFiles", "filesToDatabase");
        return null;
    }

    private void filesToDatabase() {
        List<BPEconomy> economies = BPEconomy.list();
        for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
            BPPlayerManager pManager = new BPPlayerManager(p);
            SQLPlayerManager sqlManager = new SQLPlayerManager(p);

            FileConfiguration pConfig = pManager.getPlayerConfig();
            for (BPEconomy economy : economies) {
                String bankName = economy.getOriginBank().getIdentifier();
                sqlManager.updatePlayer(
                        economy.getOriginBank().getIdentifier(),
                        BPFormatter.getStyledBigDecimal(pConfig.getString("banks." + bankName + ".debt")),
                        BPFormatter.getStyledBigDecimal(pConfig.getString("banks." + bankName + ".money")),
                        pConfig.getInt("banks." + bankName + ".level"),
                        BPFormatter.getStyledBigDecimal(pConfig.getString("banks." + bankName + ".interest"))
                );
            }
        }
    }

    private void databaseToFile() {
        Set<String> banks = BPEconomy.nameList();
        for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
            BPPlayerManager pManager = new BPPlayerManager(p);
            if (!pManager.isPlayerRegistered()) continue;

            FileConfiguration config = pManager.getPlayerConfig();
            SQLPlayerManager sqlManager = new SQLPlayerManager(p);

            for (String bankName : banks) {
                int level = sqlManager.getLevel(bankName);
                String money = BPFormatter.styleBigDecimal(sqlManager.getMoney(bankName));
                String debt = BPFormatter.styleBigDecimal(sqlManager.getDebt(bankName));
                String interest = BPFormatter.styleBigDecimal(sqlManager.getOfflineInterest(bankName));

                config.set("banks." + bankName + ".debt", debt);
                config.set("banks." + bankName + ".interest", interest);
                config.set("banks." + bankName + ".level", level);
                config.set("banks." + bankName + ".money", money);
            }

            pManager.savePlayerFile(config, pManager.getPlayerFile());
        }
    }
}