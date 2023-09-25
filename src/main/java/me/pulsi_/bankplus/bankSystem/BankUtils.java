package me.pulsi_.bankplus.bankSystem;

import me.clip.placeholderapi.PlaceholderAPI;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.utils.BPChat;
import me.pulsi_.bankplus.utils.BPItems;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class BankUtils {

    public static void openBank(Player p) {
        openBank(p, Values.CONFIG.getMainGuiName(), false);
    }

    public static void openBank(Player p, String identifier) {
        openBank(p, identifier, false);
    }

    public static void openBank(Player p, String identifier, boolean bypass) {
        BPPlayer player = BankPlus.INSTANCE.getPlayerRegistry().get(p);
        BankGuiRegistry registry = BankPlus.INSTANCE.getBankGuiRegistry();

        if (identifier.equals(BankListGui.multipleBanksGuiID)) {
            BankListGui.openMultipleBanksGui(p);
            return;
        }

        if (!registry.getBanks().containsKey(identifier)) {
            BPMessages.send(p, "Invalid-Bank");
            return;
        }

        if (!new BankReader(identifier).isAvailable(p) && !bypass) {
            BPMessages.send(p, "Cannot-Access-Bank");
            return;
        }

        Bank openedBank = player.getOpenedBank();
        if (openedBank != null) {
            BukkitTask task = openedBank.getInventoryUpdateTask();
            if (task != null) task.cancel();
        }

        Bank baseBank = registry.getBanks().get(identifier);

        String title = baseBank.getTitle();
        if (!BankPlus.INSTANCE.isPlaceholderAPIHooked()) title = BPChat.color(title);
        else title = PlaceholderAPI.setPlaceholders(p, BPChat.color(title));

        Inventory bank = Bukkit.createInventory(new BankHolder(), baseBank.getSize(), title);
        bank.setContents(baseBank.getContent());
        placeHeads(bank, p, identifier);
        updateMeta(bank, p, identifier);

        long delay = baseBank.getUpdateDelay();
        if (delay >= 0) baseBank.setInventoryUpdateTask(Bukkit.getScheduler().runTaskTimer(BankPlus.INSTANCE, () -> updateMeta(bank, p, identifier), delay, delay));

        player.setOpenedBank(baseBank);
        BPUtils.playSound("PERSONAL", p);
        p.openInventory(bank);
    }

    private static void placeHeads(Inventory bank, Player p, String identifier) {
        ConfigurationSection items = new BankReader(identifier).getBank().getItems();
        if (items == null) return;

        for (String item : items.getKeys(false)) {
            ConfigurationSection itemValues = items.getConfigurationSection(item);
            if (itemValues == null) continue;

            String material = itemValues.getString("Material");
            if (material == null || !material.startsWith("HEAD")) continue;

            try {
                bank.setItem(itemValues.getInt("Slot") - 1, BPItems.getHead(material, p));
            } catch (ArrayIndexOutOfBoundsException e) {
                bank.addItem(BPItems.getHead(material, p));
            }
        }
    }

    private static void updateMeta(Inventory bank, Player p, String identifier) {
        ConfigurationSection items = new BankReader(identifier).getBank().getItems();
        if (items == null) return;

        for (String item : items.getKeys(false)) {
            ConfigurationSection itemValues = items.getConfigurationSection(item);
            if (itemValues == null) continue;

            ItemStack i = bank.getItem(itemValues.getInt("Slot") - 1);
            if (i != null) setMeta(itemValues, i, p);
        }
    }

    private static void setMeta(ConfigurationSection itemValues, ItemStack item, Player p) {
        ItemMeta meta = item.getItemMeta();

        String displayName = itemValues.getString("Displayname") == null ? "&c&l*CANNOT FIND DISPLAYNAME*" : itemValues.getString("Displayname");
        List<String> lore = new ArrayList<>();
        for (String lines : itemValues.getStringList("Lore")) lore.add(BPChat.color(lines));

        if (BankPlus.INSTANCE.isPlaceholderAPIHooked()) {
            meta.setDisplayName(PlaceholderAPI.setPlaceholders(p, BPChat.color(displayName)));
            meta.setLore(PlaceholderAPI.setPlaceholders(p, lore));
        } else {
            meta.setDisplayName(BPChat.color(displayName));
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
    }
}