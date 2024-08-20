package me.pulsi_.bankplus.account;

<<<<<<< HEAD
import me.pulsi_.bankplus.bankSystem.BankGui;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
=======
import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import me.pulsi_.bankplus.bankSystem.BankGui;
import org.bukkit.entity.Player;
>>>>>>> afb2ba7 (-)

public class BPPlayer {

    private final Player player;

    private BankGui openedBankGui;
<<<<<<< HEAD
    private BukkitTask bankUpdatingTask, closingTask;
=======
    private MyScheduledTask bankUpdatingTask, closingTask;
>>>>>>> afb2ba7 (-)

    public BPPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public BankGui getOpenedBankGui() {
        return openedBankGui;
    }

<<<<<<< HEAD
    public BukkitTask getBankUpdatingTask() {
        return bankUpdatingTask;
    }

    public BukkitTask getClosingTask() {
=======
    public MyScheduledTask getBankUpdatingTask() {
        return bankUpdatingTask;
    }

    public MyScheduledTask getClosingTask() {
>>>>>>> afb2ba7 (-)
        return closingTask;
    }

    public void setOpenedBankGui(BankGui openedBankGui) {
        this.openedBankGui = openedBankGui;
    }

<<<<<<< HEAD
    public void setBankUpdatingTask(BukkitTask bankUpdatingTask) {
        this.bankUpdatingTask = bankUpdatingTask;
    }

    public void setClosingTask(BukkitTask closingTask) {
=======
    public void setBankUpdatingTask(MyScheduledTask bankUpdatingTask) {
        this.bankUpdatingTask = bankUpdatingTask;
    }

    public void setClosingTask(MyScheduledTask closingTask) {
>>>>>>> afb2ba7 (-)
        this.closingTask = closingTask;
    }
}