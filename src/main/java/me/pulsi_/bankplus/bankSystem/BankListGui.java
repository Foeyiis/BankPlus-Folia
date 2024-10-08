package me.pulsi_.bankplus.bankSystem;

import me.clip.placeholderapi.PlaceholderAPI;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.account.PlayerRegistry;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.utils.texts.BPChat;
import me.pulsi_.bankplus.values.ConfigValues;
import me.pulsi_.bankplus.values.MultipleBanksValues;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
<<<<<<< HEAD
import org.bukkit.scheduler.BukkitTask;
=======
import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
>>>>>>> afb2ba7 (-)

import java.util.HashMap;
import java.util.List;

/**
 * This class is used to create different instances of the bank list gui based on the player-accessible banks.
 * It must be initialized, every gui can be different depending on the player.
 */
public class BankListGui extends BankGui {

    private final HashMap<Integer, Bank> bankListGuiClickHolder = new HashMap<>();

    public BankListGui() {
        super(null);
    }

    @Override
    public void openBankGui(Player p) {
        this.openBankGui(p, true);
    }

    @Override
    public void openBankGui(Player p, boolean bypass) {
        BPPlayer player = PlayerRegistry.get(p);

<<<<<<< HEAD
        BukkitTask updating = player.getBankUpdatingTask();
=======
        MyScheduledTask updating = player.getBankUpdatingTask();
>>>>>>> afb2ba7 (-)
        if (updating != null) updating.cancel();

        if (MultipleBanksValues.isDirectlyOpenIf1IsAvailable()) {
            List<Bank> availableBanks = BankUtils.getAvailableBanks(p);
            if (availableBanks.size() == 1) {
                availableBanks.get(0).getBankGui().openBankGui(p);
                return;
            }
        }

        String title = MultipleBanksValues.getBanksGuiTitle();
        if (!BankPlus.INSTANCE().isPlaceholderApiHooked()) title = BPChat.color(title);
        else title = PlaceholderAPI.setPlaceholders(p, BPChat.color(title));

        Inventory bankListInventory = Bukkit.createInventory(new BankHolder(), MultipleBanksValues.getBankListGuiLines(), title);
        placeContent(getBankItems(), bankListInventory, p);
        updateBankGuiMeta(bankListInventory, p);

        long delay = MultipleBanksValues.getUpdateDelay();
<<<<<<< HEAD
        if (delay >= 0) player.setBankUpdatingTask(Bukkit.getScheduler().runTaskTimer(BankPlus.INSTANCE(), () -> updateBankGuiMeta(bankListInventory, p), delay, delay));
=======
        if (delay >= 0) player.setBankUpdatingTask(BankPlus.INSTANCE().getScheduler().runTaskTimer(BankPlus.INSTANCE(), () -> updateBankGuiMeta(bankListInventory, p), delay, delay));
>>>>>>> afb2ba7 (-)

        player.setOpenedBankGui(this);
        if (ConfigValues.isPersonalSoundEnabled()) {
            if (!BPUtils.playSound(ConfigValues.getPersonalSound(), p))
                BPLogger.warn("Occurred while trying to play PERSONAL sound for player \"" + p.getName() + "\".");
        }
        p.openInventory(bankListInventory);
    }

    @Override
    public void placeContent(HashMap<Integer, BankItem> items, Inventory bankInventory, Player p) {
        int slot = 0;

        for (Bank bank : BankPlus.INSTANCE().getBankRegistry().getBanks().values()) {
            if (MultipleBanksValues.isShowNotAvailableBanks() && !BankUtils.isAvailable(bank, p)) continue;

            BankGui gui = bank.getBankGui();
            BankItem bankItem = BankUtils.isAvailable(bank, p) ? gui.getAvailableBankListItem() : gui.getUnavailableBankListItem();

            bankInventory.setItem(slot, bankItem.getItem());
            bankListGuiClickHolder.put(slot, bank);
            getBankItems().put(slot, bankItem);
            slot++;
        }
    }

    public HashMap<Integer, Bank> getBankListGuiClickHolder() {
        return bankListGuiClickHolder;
    }
}