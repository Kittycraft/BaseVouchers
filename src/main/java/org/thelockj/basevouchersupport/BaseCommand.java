package org.thelockj.basevouchersupport;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.massivecore.ps.PS;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.schematic.MCEditSchematicFormat;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;


public class BaseCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender.hasPermission("baseboucher.use")) {
            if (strings.length != 2) {
                commandSender.sendMessage("Error loading command arguments!");
                return false;
            }

            for (Player p : BukkitUtil.getOnlinePlayers()) {
                if (p.getName().equals(strings[0])) {
                    if (!p.getWorld().getName().equals("factionsworld")) {
                        p.sendMessage("That cant be used in this world!");
                        return false;
                    }
                    WorldEditPlugin we = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
                    File schematic = new File("plugins/WorldEdit/schematics/" + strings[1] + ".schematic");

                    if (!schematic.exists()) {
                        p.sendMessage("Could not find base to paste!");
                        return false;
                    }

                    EditSession session = we.getWorldEdit().getEditSessionFactory().getEditSession((World) new BukkitWorld(p.getWorld()), 50000);
                    try {
                        CuboidClipboard format = MCEditSchematicFormat.getFormat(schematic).load(schematic);
                        if (isValid(p, format, p.getWorld(), format.getOffset().add(BukkitUtil.toVector(p.getLocation())))) {
                            p.sendMessage("Loading object into world...");
                            format.paste(session, BukkitUtil.toVector(p.getLocation()), false);
                            return true;
                        }
                        return false;
                    } catch (MaxChangedBlocksException | DataException | IOException e2) {
                        commandSender.sendMessage("Something went wrong while pasting base! Go check the console!");
                        e2.printStackTrace();
                    }

                    return true;
                }
            }

            commandSender.sendMessage("Player not found!");
            return false;
        }
        commandSender.sendMessage("No perms. :(");
        return false;
    }

    private boolean isValid(Player player, CuboidClipboard cuboidClipboard, org.bukkit.World world, Vector offset) {
        Vector size = cuboidClipboard.getSize();
        RegionManager regionManager = ((WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard")).getRegionManager(world);
        BoardColl boardColl = BoardColl.get();
        FactionColl factionColl = FactionColl.get();
        Vector worldPos = new Vector(0, 0, 0);
        for (int x = 0; x < size.getBlockX(); x++) {
            for (int z = 0; z < size.getBlockZ(); z++) {
                for (int y = 0; y < size.getBlockY(); y++) {
                    worldPos = new Vector(x, y, z).add(offset);
                    ApplicableRegionSet set = regionManager.getApplicableRegions(worldPos);
                    if (set.size() > 1) {
                        player.sendMessage("Can not create base in claimed area!");
                        return false;
                    }
                }
                player.sendMessage("Checking: " + worldPos.toString());
                if (!boardColl.get().getFactionAt(PS.valueOf(toBukkitVector(worldPos).toLocation(world))).equals(factionColl.getNone())) {
                    player.sendMessage("Can not create base in faction territory!");
                    return false;
                }
            }
        }

        return true;
    }

    private static org.bukkit.util.Vector toBukkitVector(Vector vector) {
        return new org.bukkit.util.Vector(vector.getX(), vector.getY(), vector.getZ());
    }
}
