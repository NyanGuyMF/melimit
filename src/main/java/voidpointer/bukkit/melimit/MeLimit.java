/*
 * This file is part of MeLimit Bukkit, Spigot and PaperSpigot compatable plug-in.
 *
 * MeLimit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MeLimit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MeLimit. If not, see <https://www.gnu.org/licenses/>.
 */
package voidpointer.bukkit.melimit;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/** @author NyanGuyMF - Vasiliy Bely */
public final class MeLimit extends JavaPlugin {
    private static final String DEFAULT_CONFIG_FILENAME = "config.yml";
    private static final String BROADCAST_COMMAND = "me";
    private static final String DEFAULT_MESSAGE_FORMAT = "* %s %s";
    private static final String CONSOLE_SENDER_NAME = "Server";
    private static final long DEFAULT_DISTANCE = 50;

    private FileConfiguration config;

    @Override public void onLoad() {
        loadConfiguration();
        config = super.getConfig();
    }

    private void loadConfiguration() {
        if (!super.getDataFolder().exists())
            super.getDataFolder().mkdirs();
        if (!new File(super.getDataFolder(), DEFAULT_CONFIG_FILENAME).exists())
            super.saveDefaultConfig();
    }

    @Override public void onEnable() {
        super.getCommand(BROADCAST_COMMAND).setExecutor((sender, cmd, alias, args) -> {
            if (args.length == 0)
                return false;

            Collection<? extends CommandSender> messageReceivers;
            String senderName;
            if (sender instanceof ConsoleCommandSender) {
                messageReceivers = Bukkit.getOnlinePlayers();
                senderName = CONSOLE_SENDER_NAME;
            } else {
                Player player = (Player) sender;
                senderName = player.getDisplayName();
                long maxDistance = config.getLong("max-distance", DEFAULT_DISTANCE);
                messageReceivers = getNearbyPlayers(player.getLocation(), maxDistance);
            }

            String message = join(args, " ");
            String formattedMessage = String.format(DEFAULT_MESSAGE_FORMAT, senderName, message);
            for (CommandSender messageReceiver : messageReceivers)
                messageReceiver.sendMessage(formattedMessage);
            Bukkit.getConsoleSender().sendMessage(formattedMessage);

            return true;
        });
    }

    private String join(final String[] arr, final String delimiter) {
        return Arrays.stream(arr).parallel().collect(Collectors.joining(delimiter));
    }

    private Collection<Player> getNearbyPlayers(final Location target, final long maxDistance) {
        List<Player> nearbyPlayers = new ArrayList<>();

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.getWorld().getName().equals(target.getWorld().getName()))
                continue;
            if (onlinePlayer.getLocation().distance(target) > maxDistance)
                continue;
            nearbyPlayers.add(onlinePlayer);
        }

        return nearbyPlayers;
    }
}
