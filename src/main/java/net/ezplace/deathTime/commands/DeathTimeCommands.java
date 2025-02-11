package net.ezplace.deathTime.commands;


import net.ezplace.deathTime.core.ItemManager;
import net.ezplace.deathTime.config.MessagesManager;
import net.ezplace.deathTime.config.SettingsManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeathTimeCommands implements CommandExecutor, TabCompleter {


    private final ItemManager itemHandler;

    public DeathTimeCommands(ItemManager itemHandler) {
        this.itemHandler = itemHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("deathtime.command.admin")) {
            sender.sendMessage("§f-----------§6Death§0Time§f-----------");
            sender.sendMessage("§8Made by §4AndrewYerNau");
            sender.sendMessage("§8Version 1.0.0");
            return true;
        }

        if (args.length == 0) {
            return false;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                sendHelpMessage(sender);
                return true;

            case "item":
                if (args.length == 3 && args[1].equalsIgnoreCase("get")) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(MessagesManager.getInstance().getMessage("command.console.error.item"));
                        return true;
                    }
                    Player player = (Player) sender;
                    try {
                        int a = Integer.parseInt(args[2]);
                        handleItemGetCommand(player, a);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(MessagesManager.getInstance().getMessage("command.item.notint"));
                    }
                    return true;
                }

                if (args.length == 4 && args[1].equalsIgnoreCase("player")) {
                    Player target = Bukkit.getPlayer(args[2]);
                    if (target == null) {
                        sender.sendMessage(MessagesManager.getInstance().getMessage("command.item.notonline"));
                        return true;
                    }
                    try {
                        int value = Integer.parseInt(args[3]);
                        handleItemGetCommand(target, value);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(MessagesManager.getInstance().getMessage("command.item.notint"));
                    }
                    return true;
                }

                sender.sendMessage(MessagesManager.getInstance().getMessage("command.give.usage"));
                sender.sendMessage(MessagesManager.getInstance().getMessage("command.give.usage1"));
                sender.sendMessage(MessagesManager.getInstance().getMessage("command.give.usage2"));
                return true;

            case "reload":
                SettingsManager.getInstance().load();
                sender.sendMessage(MessagesManager.getInstance().getMessage("plugin.reload"));
                return true;
            case "set":
                    if(args.length == 2){
                        Player target = Bukkit.getPlayer(args[0]);
                        if (target == null) {
                            sender.sendMessage(MessagesManager.getInstance().getMessage("command.item.notonline"));
                            return true;
                        }
                        try {
                            int value = Integer.parseInt(args[1]);
                            /**
                             * IMPLEMENT CHANGE PLAYER TIMER VALUE
                             * */
                        } catch (NumberFormatException e) {
                            sender.sendMessage(MessagesManager.getInstance().getMessage("command.item.notint"));
                        }
                        return true;
                    }
                return true;
            case "check":
                if (args.length == 2) {
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage(MessagesManager.getInstance().getMessage("command.item.notonline"));
                        return true;
                    }
                    /**
                     * ADD HERE STATUS OF USER X
                     * */

                    return true;
                }
                return true;
            default:
                sender.sendMessage(MessagesManager.getInstance().getMessage("command.notfound"));
                return true;
        }
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("")) {
            completions.add("");
            return completions;
        }

        if (args.length == 1) {
            completions.addAll(Arrays.asList("help", "reload", "item", "set", "check"));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("get")) {
                completions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
            } else if (args[0].equalsIgnoreCase("item")) {
                completions.addAll(Arrays.asList("get", "player"));
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("item") && args[1].equalsIgnoreCase("player")) {
            completions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
        }
        return completions;
    }

    private void sendHelpMessage(CommandSender player) {
        player.sendMessage(MessagesManager.getInstance().getMessage("command.help.title"));
        player.sendMessage(MessagesManager.getInstance().getMessage("command.help.help"));
        player.sendMessage(MessagesManager.getInstance().getMessage("command.help.create"));
        player.sendMessage(MessagesManager.getInstance().getMessage("command.help.load"));
        player.sendMessage(MessagesManager.getInstance().getMessage("command.help.itemget"));
        player.sendMessage(MessagesManager.getInstance().getMessage("command.help.itemplayer"));
    }



    private void handleItemGetCommand(Player player, int vaule) {

        ItemStack timeItem = itemHandler.createItem(vaule);
        player.getInventory().addItem(timeItem);
        player.sendMessage(MessagesManager.getInstance().getMessage("command.item.get"));

    }
}