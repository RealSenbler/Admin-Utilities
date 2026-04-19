package org.senbler.adminUtilities.npc;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.NPC;
import org.senbler.adminUtilities.AdminUtilities;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class npcFile {

    private static ArrayList<npc> npcs = new ArrayList<>();

    public static void saveNPCs() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File file = new File(AdminUtilities.getPlugin().getDataFolder(), "NPCs.json");
        file.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(npcs, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void readNPCs() {
        Gson gson = new Gson();
        File file = new File(AdminUtilities.getPlugin().getDataFolder(), "NPCs.json");

        if (!file.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<npc>>(){}.getType();
            ArrayList<npc> loaded = gson.fromJson(reader, listType);
            if (loaded != null) {
                npcs.clear();
                npcs.addAll(loaded);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ArrayList<npc> getNpcs() {
        return npcs;
    }
    public static void addNpc(npc npc) {
        npcs.add(npc);
    }
    public static void removeNpc(npc npc) {
        World world = npc.getEntity().getWorld();
        Location location = npc.getEntity().getLocation();
        location = new Location(world, location.getX(), location.getY()+1, location.getZ());
        Particle.DustOptions dust = new Particle.DustOptions(Color.WHITE, 1.0f);
        world.spawnParticle(Particle.DUST, location, 5, dust);
        npc.getEntity().remove();
        npcs.remove(npc);
    }
}
