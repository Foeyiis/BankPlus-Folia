
![image](https://cdn.discordapp.com/attachments/909417966548971590/1005582095248281700/5.7.png)

The new Multiple-Banks system is now out! You will be able to create as many banks as you want, with upgrades and per-bank permission!

Default bank file:
```yaml
# If you want to know more about bank guis: https://github.com/Pulsih/BankPlus/wiki
Title: "&a&lBANK"
Lines: 4
Update-Delay: 10 #In ticks, put 0 to disable
Filler:
  Enabled: true
  Material: "WHITE_STAINED_GLASS_PANE"
  Glowing: false

# If the settings are not specified and the multiple
# bank module is enabled, the plugin will use the
# default item format to show the bank.
Settings:
  # The permission needed to open the bank, remove
  # or put "" to make it accessible to everyone.
  Permission: "bankplus.use"

  # This is the item that will be showed up in the
  # banks-gui if the multiple-banks module is enabled.
  #
  # You can also use heads and placeholders.
  BanksGuiItem:
    # The item showed in the banks gui
    # if this bank is available.
    Available:
      Material: "CHEST"
      Displayname: "&a&lBANK"
      Lore:
        - "&aYour main bank :)"
        - ""
        - "&7Bank capacity: &a%bankplus_capacity{bank}%"
        - "&7Bank level: &a%bankplus_level{bank}%"
        - "&7Bank money: &a%bankplus_balance_formatted{bank}%"
        - ""
        - "&aAvailable: &2YES"
      Glowing: true

    # The item showed in the banks gui
    # if this bank is unavailable.
    Unavailable:
      Material: "CHEST"
      Displayname: "&a&lBANK"
      Lore:
        - "&aYour main bank :)"
        - ""
        - "&7Bank capacity: &a%bankplus_capacity{bank}%"
        - "&7Bank level: &a%bankplus_level{bank}%"
        - "&7Bank money: &a%bankplus_balance_formatted{bank}%"
        - ""
        - "&aAvailable: &cNO"
        - "&aNeeded permission: &bbankplus.use"
      Glowing: true

# These are the bank upgrades, you can upgrade a bank
# by running the command /bank upgrade <bank> or by
# using the action in the gui "Upgrade".
#
# If the Upgrades section isn't specified, the bank will
# have no upgrades and will use the default values set
# in the config.yml
Upgrades:
  # This must be a number an indicates the bank level.
  1: # 1 Is the default level, it can't be lower than 1.
    Capacity: "50000"
  2:
    Cost: "5000"
    # If the capacity is not specified, the bank will
    # have the default capacity set in the config.yml
    Capacity: "100000"
  3:
    Cost: "10000"
    Capacity: "200000"

Items:
  # You can add as many items as you want
  # and not only for make the bank work
  Withdraw:
    Material: "EMERALD"
    Amount: 1
    Displayname: "&aWithdraw"
    Slot: 11
    # You can not edit the custom
    # model data of an item.
    #
    # Working only on 1.14.4+
    #CustomModelData: 10
    Lore:
      - ""
      - "&7Withdraw &a500$ &7from your bank"
      - ""
    Glowing: true
    Action:
      # You can choose a number or:
      # "ALL" -> All money
      # "HALF" -> Half of the money
      Action-Type: "Withdraw"
      Amount: "500"

  WithdrawHalf:
    Material: "EMERALD"
    Amount: 2
    Displayname: "&aWithdraw"
    Slot: 12
    Lore:
      - ""
      - "&7Withdraw &aHalf &7of the money from your bank"
      - ""
    Glowing: true
    Action:
      Action-Type: "Withdraw"
      Amount: "HALF"

  WithdrawAll:
    Material: "EMERALD"
    Amount: 3
    Displayname: "&aWithdraw"
    Slot: 21
    Lore:
      - ""
      - "&7Withdraw &aAll &7of the money from your bank"
      - ""
    Glowing: true
    Action:
      Action-Type: "Withdraw"
      Amount: "ALL"

  WithdrawCustom:
    Material: "EMERALD"
    Amount: 4
    Slot: 20
    Displayname: "&aWithdraw"
    Lore:
      - ""
      - "&7Withdraw a &acustom &7amount of money to your bank"
      - ""
    Glowing: true
    Action:
      Action-Type: "Withdraw"
      Amount: "CUSTOM"

  Personal-Info:
    Material: "HEAD-%PLAYER%"
    Displayname: "&aPersonal"
    Slot: 14
    Lore:
      - ""
      - "&7Account Name: &f%player_name%"
      - "&7Balance: &a%bankplus_balance_formatted%"
      - "&7Capacity: &a%bankplus_capacity_formatted%"
      - ""
      - "&7Wait &a%bankplus_interest_cooldown% &7to get interest!"
      - "&7Expected money from the next interest: &a%bankplus_next_interest_formatted%"
      - ""
      - "&7&o(( Taxes on Deposit and Withdraw: 2% ))"
    Glowing: true

  How-Does-It-Work:
    Material: "HEAD-<eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzU3NDcwMTBkODRhYTU2NDgzYjc1ZjYyNDNkOTRmMzRjNTM0NjAzNTg0YjJjYzY4YTQ1YmYzNjU4NDAxMDVmZCJ9fX0=>"
    Displayname: "&aHow Does It Work?"
    Slot: 23
    Lore:
      - ""
      - "&7This is a bank, you can &adeposit &7all your"
      - "&7money here to keep them &asafe&7, your money"
      - "&7kept in the bank will &aincrease &7thanks to"
      - "&7the &a&ninterest&7, which gives you a percentage of"
      - "&7your money in the &abank&7!"
      - ""
    Glowing: true

  Deposit:
    Material: "EMERALD"
    Amount: 1
    Slot: 17
    Displayname: "&aDeposit"
    Lore:
      - ""
      - "&7Deposit &a500$ &7to your bank"
      - ""
    Glowing: true
    Action:
      Action-Type: "Deposit"
      Amount: "500"

  DepositHalf:
    Material: "EMERALD"
    Amount: 2
    Slot: 16
    Displayname: "&aDeposit"
    Lore:
      - ""
      - "&7Deposit &aHalf &7of money to your bank"
      - ""
    Glowing: true
    Action:
      Action-Type: "Deposit"
      Amount: "HALF"

  DepositAll:
    Material: "EMERALD"
    Amount: 3
    Slot: 25
    Displayname: "&aDeposit"
    Lore:
      - ""
      - "&7Deposit &aAll &7of money to your bank"
      - ""
    Glowing: true
    Action:
      Action-Type: "Deposit"
      Amount: "ALL"

  DepositCustom:
    Material: "EMERALD"
    Amount: 4
    Slot: 26
    Displayname: "&aDeposit"
    Lore:
      - ""
      - "&7Deposit a &acustom &7amount of money to your bank"
      - ""
    Glowing: true
    Action:
      Action-Type: "Deposit"
      Amount: "CUSTOM"

  Upgrade:
    Material: "DIAMOND"
    Amount: 1
    Slot: 36
    Displayname: "&aUpgrade Bank"
    Lore:
      - ""
      - "&7Upgrade your bank to the next level!"
      - ""
      - "&7Current level: &a%bankplus_level%"
      - "&7Upgrade Cost: &a%bankplus_next_level_cost%"
      - ""
    Glowing: false
    Action:
      Action-Type: "Upgrade"
```
