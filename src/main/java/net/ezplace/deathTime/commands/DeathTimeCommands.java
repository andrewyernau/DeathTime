package net.ezplace.deathTime.commands;


import net.ezplace.deathTime.DeathTime;
import net.ezplace.deathTime.core.ItemManager;
import net.ezplace.deathTime.config.MessagesManager;
import net.ezplace.deathTime.config.SettingsManager;
import net.ezplace.deathTime.data.CacheManager;
import net.ezplace.deathTime.data.DatabaseManager;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.data.type.Bell;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.*;

public class DeathTimeCommands implements CommandExecutor, TabCompleter {

    private final ItemManager itemHandler;
    private final CacheManager cacheManager;
    private final DatabaseManager databaseManager;

    public DeathTimeCommands(ItemManager itemHandler, CacheManager cacheManager) {
        this.itemHandler = itemHandler;
        this.cacheManager = cacheManager;
        this.databaseManager = DeathTime.getInstance().getDatabaseManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length > 0 && args[0].equalsIgnoreCase("time")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(MessagesManager.getInstance().getMessage("command.console.error"));
                return true;
            }

            Player player = (Player) sender;
            long timeLeft = cacheManager.getPlayerTime(player.getUniqueId());

            String formattedTime = formatTime(timeLeft);
            player.sendMessage(MessagesManager.getInstance().getMessage("command.time",
                    Map.of("time", formattedTime)));

            return true;
        }


        if (!sender.hasPermission("deathtime.command.admin")) {
            sender.sendMessage("§f-----------§6Death§0Time§f-----------");
            sender.sendMessage("§8Made by §4AndrewYerNau");
            sender.sendMessage("§8Version 1.0");
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
                        sender.sendMessage(MessagesManager.getInstance().getMessage("command.console.error"));
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
                        sender.sendMessage(MessagesManager.getInstance().getMessage("command.playernotonline",Map.of("user",args[1])));
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
                if (args.length == 3) {
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage(MessagesManager.getInstance().getMessage("command.playernotonline",Map.of("user",args[1])));
                        return true;
                    }
                    try {
                        int value = Integer.parseInt(args[2]);
                        cacheManager.updatePlayerTime(target.getUniqueId(), value);
                        sender.sendMessage(MessagesManager.getInstance().getMessage("command.set.success",Map.of("user",args[1])));
                    } catch (NumberFormatException e) {
                        sender.sendMessage(MessagesManager.getInstance().getMessage("command.item.notint"));
                    }
                    return true;
                }
                return true;
            case "add":
                if (args.length == 3) {
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage(MessagesManager.getInstance().getMessage("command.playernotonline",Map.of("user",args[1])));
                        return true;
                    }
                    try {
                        int value = Integer.parseInt(args[2]);

                        cacheManager.updatePlayerTime(target.getUniqueId(), value + cacheManager.getPlayerTime(target.getUniqueId()));
                        sender.sendMessage(MessagesManager.getInstance().getMessage("command.set.success",Map.of("user",args[1])));
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
                        sender.sendMessage(MessagesManager.getInstance().getMessage("command.playernotonline",Map.of("user", args[1])));
                        return true;
                    }
                    long time = cacheManager.getPlayerTime(target.getUniqueId());
                    sender.sendMessage(MessagesManager.getInstance().getMessage("command.check",Map.of("user",args[1],"time",String.valueOf(time))));
                    return true;
                }
                return true;
            case "pardon":
                if (args.length == 2) {
                    String targetName = args[1];
                    UUID uuid;
                    Player onlinePlayer = Bukkit.getPlayer(targetName);
                    if (onlinePlayer != null) {
                        uuid = onlinePlayer.getUniqueId();
                    } else {
                        uuid = databaseManager.getCachedUUID(targetName);
                        if (uuid == null) {
                            uuid = databaseManager.getUUIDFromMojang(targetName);
                            if (uuid == null) {
                                sender.sendMessage(MessagesManager.getInstance().getMessage("command.playernotfound", Map.of("user", targetName)));
                                return true;
                            }
                        }
                    }
                    databaseManager.updateBanStatus(uuid, 0);
                    cacheManager.updatePlayerTime(uuid, SettingsManager.INITIAL_TIME);
                    Bukkit.getBanList(BanList.Type.NAME).pardon(uuid.toString());

                    sender.sendMessage(MessagesManager.getInstance().getMessage("command.pardon", Map.of("user", targetName)));
                    return true;
                }
                return true;

            case "bypass":
                if (args.length == 2) {
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage(MessagesManager.getInstance().getMessage("command.playernotonline", Map.of("user", args[1])));
                        return true;
                    }
                    // Add bypass
                    cacheManager.addBypass(target.getUniqueId());
                    target.addAttachment(DeathTime.getInstance(), "deathtime.bypass", true);  // Opcional: permiso para placeholders

                    sender.sendMessage(MessagesManager.getInstance().getMessage("command.bypass.success", Map.of("user", target.getName())));
                    target.sendMessage(MessagesManager.getInstance().getMessage("command.bypass.received"));
                    return true;
                }
                return true;

            case "unbypass":
                if (args.length == 2) {
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage(MessagesManager.getInstance().getMessage("command.playernotonline", Map.of("user", args[1])));
                        return true;
                    }

                    // Remove bypass
                    cacheManager.removeBypass(target.getUniqueId());

                    for (PermissionAttachmentInfo info : target.getEffectivePermissions()) {
                        PermissionAttachment attachment = info.getAttachment();
                        if (attachment != null && attachment.getPlugin() instanceof DeathTime && info.getPermission().equals("deathtime.bypass")) {
                            target.removeAttachment(attachment);
                            break;
                        }
                    }

                    sender.sendMessage(MessagesManager.getInstance().getMessage("command.unbypass.success", Map.of("user", target.getName())));
                    target.sendMessage(MessagesManager.getInstance().getMessage("command.unbypass.received"));
                    return true;
                }
                return true;

            default:
                sender.sendMessage(MessagesManager.getInstance().getMessage("command.notfound"));
                return true;
        }
    }

    private String formatTime(long seconds) {
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        return String.format("%d días, %02d:%02d:%02d", days, hours, minutes, secs);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("deathtime.command.admin")) {
            return completions;
        }

        if (args.length == 1) {
            completions.addAll(Arrays.asList("help", "reload", "item", "set","add", "check","pardon","bypass","unbypass","time"));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("add") ||args[0].equalsIgnoreCase("check")|| args[0].equalsIgnoreCase("bypass")|| args[0].equalsIgnoreCase("unbypass")|| args[0].equalsIgnoreCase("pardon")) {
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
        player.sendMessage(MessagesManager.getInstance().getMessage("command.help.reload"));
        player.sendMessage(MessagesManager.getInstance().getMessage("command.help.itemget"));
        player.sendMessage(MessagesManager.getInstance().getMessage("command.help.itemplayer"));
        player.sendMessage(MessagesManager.getInstance().getMessage("command.help.set"));
        player.sendMessage(MessagesManager.getInstance().getMessage("command.help.check"));
        player.sendMessage(MessagesManager.getInstance().getMessage("command.help.pardon"));
        player.sendMessage(MessagesManager.getInstance().getMessage("command.help.time"));
        player.sendMessage(MessagesManager.getInstance().getMessage("command.help.bypass"));
        player.sendMessage(MessagesManager.getInstance().getMessage("command.help.unbypass"));
    }

    private void handleItemGetCommand(Player player, int value) {
        ItemStack timeItem = itemHandler.createItem(value);
        player.getInventory().addItem(timeItem);
        player.sendMessage(MessagesManager.getInstance().getMessage("command.item.get"));
    }
}