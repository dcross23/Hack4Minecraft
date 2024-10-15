package com.briangershon.demo;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.*;
import org.bukkit.block.Block;

import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import org.bukkit.scoreboard.*;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;

import eu.decentsoftware.holograms.api.DHAPI;

public class App extends JavaPlugin implements Listener {

    private MultiverseCore multiverseCore;
    private MVWorldManager worldManager;
    private static Plugin plugin;

    private HologramaManager hologramaManager;

    private WorldGuardPlugin WorldGuardPlugin;

    public static Plugin getPluginGabri() {
        return plugin;
    }


    private BlockFace direccionPalancas;
    private boolean s1, s2, s3, y;
    private String msg;

    private AtomicInteger contador = new AtomicInteger(0);
    private Runnable tareaComprobacion;
    private double porcentaje;
    private String porcentajeStr;

    @Override
    public void onEnable() {

        saveDefaultConfig();
        
        plugin = this;

        // Load Multiverse-Core Plugin
        this.multiverseCore = (MultiverseCore) getServer().getPluginManager().getPlugin("Multiverse-Core");
        this.WorldGuardPlugin = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");

        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
        

        if (multiverseCore != null) {
            this.worldManager = multiverseCore.getMVWorldManager();
            getLogger().info("Multiverse-Core encontrado, el plugin se ha iniciado correctamente.");

            // Obtener el archivo de configuración de Multiverse-Core
            File configFile = new File(multiverseCore.getDataFolder(), "config.yml");
            FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

            // Modificar el parámetro firstspawnoverride
            config.set("multiverse-configuration.firstspawnoverride", false); // o false según lo que desees
            config.set("multiverse-configuration.==:", "com.onarandombox.MultiverseCore.MultiverseCoreConfiguration"); // o
                                                                                                                       // false
                                                                                                                       // según
                                                                                                                       // lo
                                                                                                                       // que
                                                                                                                       // desees
            config.set("multiverse-configuration.enforceaccess", false); // o false según lo que desees
            config.set("multiverse-configuration.prefixchat", false); // o false según lo que desees
            config.set("multiverse-configuration.prefixchatformat", "[%world%]%chat%"); // o false según lo que desees
            config.set("multiverse-configuration.useasyncchat", true);
            config.set("multiverse-configuration.teleportintercept", true);
            config.set("multiverse-configuration.displaypermerrors", true);
            config.set("multiverse-configuration.enablebuscript", true);
            config.set("multiverse-configuration.globaldebug", 0);
            config.set("multiverse-configuration.silentstart", false);
            config.set("multiverse-configuration.messagecooldown", 5000);
            config.set("multiverse-configuration.version", 2.9);
            config.set("multiverse-configuration.firstspawnworld", "world");
            config.set("multiverse-configuration.teleportcooldown", 1000);
            config.set("multiverse-configuration.defaultportalsearch", true);
            config.set("multiverse-configuration.portalsearchradius", 128);
            config.set("multiverse-configuration.autopurge", true);
            config.set("multiverse-configuration.idonotwanttodonate", false);
            config.set("multiverse-configuration.firstspawnoverride", false);

            // Guardar la configuración
            try {
                config.save(configFile);

                getLogger().info("Se ha cambiado el parámetro firstspawnoverride correctamente.");

            } catch (IOException e) {
                e.printStackTrace();
                getLogger().severe("No se pudo guardar el archivo config.yml de Multiverse-Core.");
            }

        } else {
            getLogger().severe("Multiverse-Core no encontrado. El plugin no funcionará correctamente.");
        }


        //Crear Holograma
        // Suponiendo que quieres crear el holograma en una ubicación fija
        Location location = new Location(getServer().getWorld("world"), 29, -50, 105);

        // Crear el holograma y comenzar a refrescarlo
        if (DHAPI.getHologram("MarcadorGlobal") ==null){
            hologramaManager = new HologramaManager();
            hologramaManager.crearHolograma("MarcadorGlobal",location,this);
        }
        // Iniciar la tarea de refresco continuo
        HologramaRefrescador refrescador = new HologramaRefrescador(hologramaManager, this);
        refrescador.iniciarRefresco();
    }

    private BlockFace getAttachedFace(BlockData data) {
        FaceAttachable.AttachedFace attached = ((FaceAttachable) data).getAttachedFace();
        return switch (attached) {
            case WALL -> ((Directional) data).getFacing().getOppositeFace();
            case FLOOR -> BlockFace.DOWN;
            case CEILING -> BlockFace.UP;
            default -> throw new IllegalArgumentException("Unexpected value: " + attached);
        };
    }


    public void cloneRegions(String sourceWorldName, String targetWorldName) {
         // Obtener el RegionContainer desde WorldGuard
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();

        // Convertir org.bukkit.World a com.sk89q.worldedit.world.World
        com.sk89q.worldedit.world.World wgSourceWorld = BukkitAdapter.adapt(Bukkit.getWorld(sourceWorldName));
        com.sk89q.worldedit.world.World wgTargetWorld = BukkitAdapter.adapt(Bukkit.getWorld(targetWorldName));

        // Obtener el RegionManager de cada mundo
        RegionManager sourceManager = container.get(wgSourceWorld);
        RegionManager targetManager = container.get(wgTargetWorld);

        if (sourceManager == null || targetManager == null) {
            getLogger().severe("No se pudo cargar el RegionManager para uno de los mundos.");
            return;
        }

        // Clonar las regiones
        for (Map.Entry<String, ProtectedRegion> entry : sourceManager.getRegions().entrySet()) {
            String regionId = entry.getKey();
            ProtectedRegion region = entry.getValue();

            // Añadir la región al mundo destino
            targetManager.addRegion(region);
        }

        // Guardar los cambios en el mundo destino
        try {
            targetManager.save();
        } catch (StorageException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        getLogger().info("Regiones clonadas exitosamente de " + wgSourceWorld.getName() + " a " + targetManager.getName());
        
    }

     // Método para guardar un atributo personalizado (puntos) en el archivo de configuración
     public void guardarProgreso(Player jugador, int level) {
        FileConfiguration config = getConfig(); // Obtener la configuración actual
        String path = "jugadores." + jugador.getUniqueId().toString() + ".level"; // Ruta para los datos del jugador
        config.set(path, level); // Guardar los puntos en la ruta
        saveConfig(); // Guardar los cambios en el archivo
    }

    public int obtenerNivel(Player jugador) {
        FileConfiguration config = getConfig();
        String path = "jugadores." + jugador.getUniqueId().toString() + ".level"; // Ruta para los puntos del jugador
        return config.getInt(path, 0); // Obtener los puntos (por defecto 0 si no hay datos)
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void setLever(Block block, boolean state) {

        if (block.getType() == Material.LEVER) {
            block.setType(Material.AIR, false);
            block.setType(Material.LEVER);

            Powerable data = (Powerable) block.getBlockData();
            Directional directional = (Directional) data;
            ((Directional) data).setFacing(direccionPalancas);
            data.setPowered(state);
            block.setBlockData(data);

            // ACtualizar Vecinos Proximos Para El Tema RedStone
            BlockFace face = getAttachedFace(data);
            Block neighbor = block.getRelative(face);

            // Update the attached block
            BlockState st = neighbor.getState();
            neighbor.setType(Material.AIR, false);
            st.update(true);
        }
    }


    
    // Detectar cuando un jugador interactúa con un bloque
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        if (event.getPlayer().getWorld().getName().contains("Ejercicio1")) {
            // Comprobamos Ejercicio 1
            checkEjercicio1(event);

        }else if (event.getPlayer().getWorld().getName().contains("Ejercicio2")){
            checkEjercicio2(event);

        }else if (event.getPlayer().getWorld().getName().contains("Ejercicio3")){
            checkEjercicio3(event);

        }else if (event.getPlayer().getWorld().getName().contains("Ejercicio4")){
            checkEjercicio4(event);

        }else if (event.getPlayer().getWorld().getName().contains("Ejercicio5")){
            checkEjercicio5(event);

        }

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        
        // Verificar si el jugador es nuevo
        if (!event.getPlayer().hasPlayedBefore()) {
            // Clonar el mundo solo si es la primera vez del jugador en el servidor
            //cloneWorldForPlayer(event.getPlayer());
             // Crear el primer componente de texto
             TextComponent parte1 = new TextComponent("¡ Bienvenido Al Mundo Paralelo de Computadores I---> ");
             parte1.setColor(ChatColor.BLUE);

             // Crear el segundo componente de texto
             TextComponent parte2 = new TextComponent("¡ Si tienes problemas ..... gvg@usal.es  !");
             parte2.setColor(ChatColor.GOLD);
             parte2.setBold(true);

             parte1.addExtra(parte2);
             event.getPlayer().spigot().sendMessage(parte1);

        }

        // Crear Scoreboard
        // CreateScoreBoard(p);
    }

    private void cloneWorldForPlayer(Player player, String mundoaClonar) {
        if (worldManager != null) {
            String originalWorldName = mundoaClonar; // Nombre del mundo base a clonar
            String newWorldName = player.getName() + "_" + mundoaClonar; // Mundo clonado para el jugador

            // Verificar si ya existe el mundo clonado
            if (worldManager.getMVWorld(newWorldName) == null) {
                // Clonar el mundo usando Multiverse-Core
                boolean success = worldManager.cloneWorld(originalWorldName, newWorldName);

                if (success) {
                    getLogger().info("Se ha clonado el mundo " + originalWorldName + " para el jugador " + player.getName());
                    cloneRegions(mundoaClonar, newWorldName);

                    // Cargar el nuevo mundo clonado
                    World newWorld = Bukkit.getWorld(newWorldName);
                    if (newWorld == null) {
                        newWorld = Bukkit.createWorld(new org.bukkit.WorldCreator(newWorldName));
                    }

                    // Teletransportar al jugador al nuevo mundo
                    teleportPlayerToNewWorld(player, newWorld);

                } else {
                    getLogger().severe(
                            "No se pudo clonar el mundo " + originalWorldName + " para el jugador " + player.getName());
                }
            } else {
                getLogger().info("El mundo " + newWorldName + " ya existe.");

                // Si el mundo ya existe, cargarlo y teletransportar al jugador
                World existingWorld = Bukkit.getWorld(newWorldName);
                if (existingWorld == null) {
                    existingWorld = Bukkit.createWorld(new org.bukkit.WorldCreator(newWorldName));
                }

                teleportPlayerToNewWorld(player, existingWorld);
            }
        } else {
            getLogger().severe("Multiverse-Core no está disponible.");
        }
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        // Obtiene al jugador que atravesó el portal
        var player = event.getPlayer();

        // Obtiene la ubicación del portal (la ubicación de entrada del portal)
        Location portalLocation = event.getFrom();

        getLogger().info( "El jugador " + player.getName() + " ha atravesado un portal en: X: " + (int) portalLocation.getX()        + " Y: " + (int) portalLocation.getY() + " Z: " + (int) portalLocation.getZ());

        // PORTAL A LECCION 1
        if ( ( (int)portalLocation.getX() == 18  || (int)portalLocation.getX() == 19 ) && ( (int)portalLocation.getY() == -56 || (int)portalLocation.getY() == -57 ) && (int)portalLocation.getZ() == 82 && event.getPlayer().getWorld().getName().contains("world")) {
           
            TextComponent parte1 = new TextComponent("Teletransportando al....");
            parte1.setColor(ChatColor.BLUE);

            // Envía un mensaje al jugador (opcional)
            TextComponent parte2 = new TextComponent(" Ejercicio1!");
            parte2.setColor(ChatColor.GREEN);
            parte2.setBold(true);

            parte1.addExtra(parte2);

            player.spigot().sendMessage(parte1);

            //Clonamos Mundo
            cloneWorldForPlayer(player,"plantillaEjercicio1");

           
            // Imprime las coordenadas del portal en la consola
            getLogger().info( "El jugador " + player.getName() + " ha atravesado un portal en: X: " + (int) portalLocation.getX()        + " Y: " + (int) portalLocation.getY() + " Z: " + (int) portalLocation.getZ());
        
        }else if ((int)portalLocation.getX() == 53 && ((int)portalLocation.getY() == -59 || (int)portalLocation.getY() == -60) && (int)portalLocation.getZ() == -95 && event.getPlayer().getWorld().getName().contains("Ejercicio1")) {
            // PORTAL DE LECCION 1 A BASE
            World newWorld = Bukkit.getWorld("world");

            TextComponent parte1 = new TextComponent("Teletransportando ....");
            parte1.setColor(ChatColor.BLUE);

            // Envía un mensaje al jugador (opcional)
            TextComponent parte2 = new TextComponent(" Volviendo a Base!");
            parte2.setColor(ChatColor.GREEN);
            parte2.setBold(true);

            parte1.addExtra(parte2);

            player.spigot().sendMessage(parte1);

            // Teletransportar al jugador al nuevo mundo
            teleportPlayerToNewWorld(player, newWorld);

        }else if ( ((int) portalLocation.getX() == 39 || (int) portalLocation.getX() == 40) && ((int) portalLocation.getY() == -57  || (int) portalLocation.getY() == -56) &&  (int) portalLocation.getZ() == 82 && event.getPlayer().getWorld().getName().contains("world")) {
            // PORTAL A LECCION 2
            if (obtenerNivel(player) < 1){
                // Crear el primer componente de texto
                TextComponent parte1 = new TextComponent("Teletransportando ....");
                parte1.setColor(ChatColor.BLUE);

                // Envía un mensaje al jugador (opcional)
                TextComponent parte2 = new TextComponent("'Haz Primero Los Ejercicios Mas Fáciles!");
                parte2.setColor(ChatColor.GREEN);
                parte2.setBold(true);

                parte1.addExtra(parte2);

                player.spigot().sendMessage(parte1);



            } else{
                TextComponent parte1 = new TextComponent("Teletransportando al....");
                parte1.setColor(ChatColor.BLUE);

                // Envía un mensaje al jugador (opcional)
                TextComponent parte2 = new TextComponent(" Ejercicio2!");
                parte2.setColor(ChatColor.GREEN);
                parte2.setBold(true);

                parte1.addExtra(parte2);

                player.spigot().sendMessage(parte1);

                //Clonamos Mundo
                cloneWorldForPlayer(player,"plantillaEjercicio2");
            }

        }else if ((int)portalLocation.getX() == 53 && ((int)portalLocation.getY() == -59 || (int)portalLocation.getY() == -60) && (int)portalLocation.getZ() == -95 && event.getPlayer().getWorld().getName().contains("Ejercicio2")) {
            // PORTAL DE LECCION 2 A BASE
            World newWorld = Bukkit.getWorld("world");

            TextComponent parte1 = new TextComponent("Teletransportando ....");
            parte1.setColor(ChatColor.BLUE);

            // Envía un mensaje al jugador (opcional)
            TextComponent parte2 = new TextComponent(" Volviendo a Base!");
            parte2.setColor(ChatColor.GREEN);
            parte2.setBold(true);

            parte1.addExtra(parte2);

            player.spigot().sendMessage(parte1);

            // Teletransportar al jugador al nuevo mundo
            teleportPlayerToNewWorld(player, newWorld);


        }else if ((int) portalLocation.getX() == 52 && ((int) portalLocation.getY() == -57  || (int) portalLocation.getY() == -56) &&  ((int) portalLocation.getZ() == 94 || (int) portalLocation.getZ() == 95) && event.getPlayer().getWorld().getName().contains("world")) {
            // PORTAL A LECCION 3
            if (obtenerNivel(player) < 2){
                // Crear el primer componente de texto
                TextComponent parte1 = new TextComponent("Teletransportando ....");
                parte1.setColor(ChatColor.BLUE);

                // Envía un mensaje al jugador (opcional)
                TextComponent parte2 = new TextComponent("'Haz Primero Los Ejercicios Mas Fáciles!");
                parte2.setColor(ChatColor.GREEN);
                parte2.setBold(true);

                parte1.addExtra(parte2);

                player.spigot().sendMessage(parte1);



            } else{
                TextComponent parte1 = new TextComponent("Teletransportando al....");
                parte1.setColor(ChatColor.BLUE);

                // Envía un mensaje al jugador (opcional)
                TextComponent parte2 = new TextComponent(" Ejercicio3!");
                parte2.setColor(ChatColor.GREEN);
                parte2.setBold(true);

                parte1.addExtra(parte2);

                player.spigot().sendMessage(parte1);

                //Clonamos Mundo
                cloneWorldForPlayer(player,"plantillaEjercicio3");
            }

        }else if ((int)portalLocation.getX() == 53 && ((int)portalLocation.getY() == -59 || (int)portalLocation.getY() == -60) && (int)portalLocation.getZ() == -95 && event.getPlayer().getWorld().getName().contains("Ejercicio3")) {
            // PORTAL DE LECCION 3 A BASE
            World newWorld = Bukkit.getWorld("world");

            TextComponent parte1 = new TextComponent("Teletransportando ....");
            parte1.setColor(ChatColor.BLUE);

            // Envía un mensaje al jugador (opcional)
            TextComponent parte2 = new TextComponent(" Volviendo a Base!");
            parte2.setColor(ChatColor.GREEN);
            parte2.setBold(true);

            parte1.addExtra(parte2);

            player.spigot().sendMessage(parte1);

            // Teletransportar al jugador al nuevo mundo
            teleportPlayerToNewWorld(player, newWorld);

        }

//PORTALES A EJERCICIO 4
/*
        else if ( (int) portalLocation.getX() == 52 && ((int) portalLocation.getY() == -57  || (int) portalLocation.getY() == -56) && ((int) portalLocation.getZ() == 115 || (int) portalLocation.getZ() == 116) && event.getPlayer().getWorld().getName().contains("world")) {
            // PORTAL A LECCION 4
            if (obtenerNivel(player) < 3){
                // Crear el primer componente de texto
                TextComponent parte1 = new TextComponent("Teletransportando ....");
                parte1.setColor(ChatColor.BLUE);

                // Envía un mensaje al jugador (opcional)
                TextComponent parte2 = new TextComponent("'Haz Primero Los Ejercicios Mas Fáciles!");
                parte2.setColor(ChatColor.GREEN);
                parte2.setBold(true);

                parte1.addExtra(parte2);

                player.spigot().sendMessage(parte1);

            } else{
                TextComponent parte1 = new TextComponent("Teletransportando al....");
                parte1.setColor(ChatColor.BLUE);

                // Envía un mensaje al jugador (opcional)
                TextComponent parte2 = new TextComponent(" Ejercicio4!");
                parte2.setColor(ChatColor.GREEN);
                parte2.setBold(true);

                parte1.addExtra(parte2);

                player.spigot().sendMessage(parte1);

                //Clonamos Mundo
                cloneWorldForPlayer(player,"plantillaEjercicio4");
            }

        }

        else if ((int)portalLocation.getX() == -94 && ((int)portalLocation.getY() == -54 || (int)portalLocation.getY() == -55) && (int)portalLocation.getZ() == 0 && event.getPlayer().getWorld().getName().contains("Ejercicio4")) {
            // PORTAL DE LECCION 4 A BASE
            World newWorld = Bukkit.getWorld("world");

            TextComponent parte1 = new TextComponent("Teletransportando ....");
            parte1.setColor(ChatColor.BLUE);

            // Envía un mensaje al jugador (opcional)
            TextComponent parte2 = new TextComponent(" Volviendo a Base!");
            parte2.setColor(ChatColor.GREEN);
            parte2.setBold(true);

            parte1.addExtra(parte2);

            player.spigot().sendMessage(parte1);

            // Teletransportar al jugador al nuevo mundo
            teleportPlayerToNewWorld(player, newWorld);

        }
*/



//PORTALES A EJERCICIO 5
/*
        else if ( ((int) portalLocation.getX() == 39 || (int) portalLocation.getX() == 40 ) && ((int) portalLocation.getY() == -57  || (int) portalLocation.getY() == -56) && (int) portalLocation.getZ() == 128 && event.getPlayer().getWorld().getName().contains("world")) {
            // PORTAL A LECCION 4
            if (obtenerNivel(player) < 3){
                // Crear el primer componente de texto
                TextComponent parte1 = new TextComponent("Teletransportando ....");
                parte1.setColor(ChatColor.BLUE);

                // Envía un mensaje al jugador (opcional)
                TextComponent parte2 = new TextComponent("'Haz Primero Los Ejercicios Mas Fáciles!");
                parte2.setColor(ChatColor.GREEN);
                parte2.setBold(true);

                parte1.addExtra(parte2);

                player.spigot().sendMessage(parte1);

            } else{
                TextComponent parte1 = new TextComponent("Teletransportando al....");
                parte1.setColor(ChatColor.BLUE);

                // Envía un mensaje al jugador (opcional)
                TextComponent parte2 = new TextComponent(" Ejercicio5!");
                parte2.setColor(ChatColor.GREEN);
                parte2.setBold(true);

                parte1.addExtra(parte2);

                player.spigot().sendMessage(parte1);

                //Clonamos Mundo
                cloneWorldForPlayer(player,"plantillaEjercicio5");
            }

        }

        else if ((int)portalLocation.getX() == 53 && ((int)portalLocation.getY() == -59 || (int)portalLocation.getY() == -60) && (int)portalLocation.getZ() == -95 && event.getPlayer().getWorld().getName().contains("Ejercicio5")) {
            // PORTAL DE LECCION 4 A BASE
            World newWorld = Bukkit.getWorld("world");

            TextComponent parte1 = new TextComponent("Teletransportando ....");
            parte1.setColor(ChatColor.BLUE);

            // Envía un mensaje al jugador (opcional)
            TextComponent parte2 = new TextComponent(" Volviendo a Base!");
            parte2.setColor(ChatColor.GREEN);
            parte2.setBold(true);

            parte1.addExtra(parte2);

            player.spigot().sendMessage(parte1);

            // Teletransportar al jugador al nuevo mundo
            teleportPlayerToNewWorld(player, newWorld);

        }
*/


//PORTALES A EJERCICIO 6
/*
        else if ( ((int) portalLocation.getX() == 18 || (int) portalLocation.getX() == 19 ) && ((int) portalLocation.getY() == -57  || (int) portalLocation.getY() == -56) && (int) portalLocation.getZ() == 128 && event.getPlayer().getWorld().getName().contains("world")) {
            // PORTAL A LECCION 4
            if (obtenerNivel(player) < 4){
                // Crear el primer componente de texto
                TextComponent parte1 = new TextComponent("Teletransportando ....");
                parte1.setColor(ChatColor.BLUE);

                // Envía un mensaje al jugador (opcional)
                TextComponent parte2 = new TextComponent("'Haz Primero Los Ejercicios Mas Fáciles!");
                parte2.setColor(ChatColor.GREEN);
                parte2.setBold(true);

                parte1.addExtra(parte2);

                player.spigot().sendMessage(parte1);

            } else{
                TextComponent parte1 = new TextComponent("Teletransportando al....");
                parte1.setColor(ChatColor.BLUE);

                // Envía un mensaje al jugador (opcional)
                TextComponent parte2 = new TextComponent(" Ejercicio6!");
                parte2.setColor(ChatColor.GREEN);
                parte2.setBold(true);

                parte1.addExtra(parte2);

                player.spigot().sendMessage(parte1);

                //Clonamos Mundo
                cloneWorldForPlayer(player,"plantillaEjercicio6");
            }

        }

        else if ((int)portalLocation.getX() == 53 && ((int)portalLocation.getY() == -59 || (int)portalLocation.getY() == -60) && (int)portalLocation.getZ() == -95 && event.getPlayer().getWorld().getName().contains("Ejercicio6")) {
            // PORTAL DE LECCION 5 A BASE
            World newWorld = Bukkit.getWorld("world");

            TextComponent parte1 = new TextComponent("Teletransportando ....");
            parte1.setColor(ChatColor.BLUE);

            // Envía un mensaje al jugador (opcional)
            TextComponent parte2 = new TextComponent(" Volviendo a Base!");
            parte2.setColor(ChatColor.GREEN);
            parte2.setBold(true);

            parte1.addExtra(parte2);

            player.spigot().sendMessage(parte1);

            // Teletransportar al jugador al nuevo mundo
            teleportPlayerToNewWorld(player, newWorld);

        }
*/

        else {

                TextComponent parte1 = new TextComponent("No te impacientes ....");
                parte1.setColor(ChatColor.BLUE);
    
                // Envía un mensaje al jugador (opcional)
                TextComponent parte2 = new TextComponent("Vete Primero a Los Portales Con Libro!");
                parte2.setColor(ChatColor.GREEN);
                parte2.setBold(true);
    
                parte1.addExtra(parte2);
    
                player.spigot().sendMessage(parte1);
        }


        event.setCancelled(true);

    }

    private void teleportPlayerToNewWorld(Player player, World world) {
        // Coordenadas de spawn del mundo
        Location spawnLocation = world.getSpawnLocation();

        // Teletransportar al jugador
        player.teleport(spawnLocation);

        // Confirmar en la consola que el jugador fue teletransportado
        getLogger().info("El jugador " + player.getName() + " fue teletransportado al mundo " + world.getName());

    }

    public void CreateScoreBoard(Player player) {
        // Obtener el ScoreboardManager
        ScoreboardManager manager = Bukkit.getScoreboardManager();

        // Crear un nuevo Scoreboard
        Scoreboard scoreboard = manager.getNewScoreboard();

        // Crear un nuevo Objective en el Scoreboard con el nombre "test" y tipo "dummy"
        // (tipo personalizado)
        Objective objective = scoreboard.registerNewObjective("stats", "dummy", ChatColor.GREEN + "Puntuación");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR); // Mostrar el scoreboard en el lateral de la pantalla

        Score deathsScore = objective.getScore(ChatColor.BLUE + "Eje. Completados");
        deathsScore.setScore(5); // Establecer el valor de deaths (5 en este caso)

        // Asignar el Scoreboard al jugador
        player.setScoreboard(scoreboard);
    }

    public void checkEjercicio1(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            // El boton comprobar de la puerta AND del primer recinto
            if (block != null && (block.getType() == Material.WARPED_BUTTON)
                    && (block.getX() == 27 && block.getY() == -60 && block.getZ() == -96)) {

                // Crear el primer componente de texto
                TextComponent parte1 = new TextComponent("¡Comprobando Circuito Puerta And ---> ");
                parte1.setColor(ChatColor.BLUE);

                // Crear el segundo componente de texto
                TextComponent parte2 = new TextComponent("¡ Se Paciente  !");
                parte2.setColor(ChatColor.GOLD);
                parte2.setBold(true);

                parte1.addExtra(parte2);
                event.getPlayer().spigot().sendMessage(parte1);

                direccionPalancas = BlockFace.WEST;

                Block inA = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(27, -60, -97);
                Block inB = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(27, -60, -95);
                Block out = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(42, -59, -96);
                 
              

                Bukkit.getScheduler().runTaskLater(this, () -> {
                    // Movemos Palancas
                    setLever(inA, false);
                    setLever(inB, false);

                    // Dejamos un tiempo para que se cambien las palancas
                    Bukkit.getScheduler().runTaskLater(this, () -> {
                        parte1.getExtra().clear();
                        parte1.setText("->");

                        parte1.addExtra("A=0 y B=0 ");
                        parte1.setColor(ChatColor.YELLOW);

                        Bukkit.getScheduler().runTaskLater(this, () -> {
                            
                            if (((Lightable) out.getBlockData()).isLit()) {
                                parte1.addExtra(" S=1 [ERROR]");
                                parte1.setColor(ChatColor.RED);
                                event.getPlayer().spigot().sendMessage(parte1);

                                parte1.getExtra().clear();

                                parte1.addExtra("!!CIRCUITO INVALIDO¡¡");
                                parte1.setColor(ChatColor.LIGHT_PURPLE);
                                parte1.setBold(true);
                                event.getPlayer().spigot().sendMessage(parte1);
                            } else {
                                parte1.addExtra(" ");
                                parte1.addExtra(" S=0 [OK]     [25%]");
                                parte1.setColor(ChatColor.YELLOW);
                                event.getPlayer().spigot().sendMessage(parte1);

                                // Si está bien hacemos siguiente comprobación
                                // Movemos Palancas Con Un Retardo
                                Bukkit.getScheduler().runTaskLater(this, () -> {
                                    // Movemos Palancas
                                    setLever(inA, false);
                                    setLever(inB, true);

                                    // Dejamos un tiempo para que se cambien las palancas
                                    Bukkit.getScheduler().runTaskLater(this, () -> {
                                        parte1.getExtra().clear();
                                        parte1.setText("->");

                                        parte1.addExtra("A=0 y B=1 ");
                                        parte1.setColor(ChatColor.YELLOW);

                                        Bukkit.getScheduler().runTaskLater(this, () -> {
                                            if (((Lightable) out.getBlockData()).isLit()) {
                                                parte1.addExtra(" S=1 [ERROR]");
                                                parte1.setColor(ChatColor.RED);
                                                event.getPlayer().spigot().sendMessage(parte1);

                                                parte1.getExtra().clear();

                                                parte1.addExtra("!!CIRCUITO INVALIDO¡¡");
                                                parte1.setColor(ChatColor.LIGHT_PURPLE);
                                                parte1.setBold(true);
                                                event.getPlayer().spigot().sendMessage(parte1);
                                            } else {
                                                parte1.addExtra(" ");
                                                parte1.addExtra(" S=0 [OK]     [50%]");
                                                parte1.setColor(ChatColor.YELLOW);
                                                event.getPlayer().spigot().sendMessage(parte1);

                                                // Si está bien hacemos siguiente comprobación
                                                // Movemos Palancas Con Un Retardo
                                                Bukkit.getScheduler().runTaskLater(getPluginGabri(), () -> {
                                                    // Movemos Palancas
                                                    setLever(inA, true);
                                                    setLever(inB, false);

                                                    // Dejamos un tiempo para que se cambien las palancas
                                                    Bukkit.getScheduler().runTaskLater(this, () -> {
                                                        parte1.getExtra().clear();
                                                        parte1.setText("->");

                                                        parte1.addExtra("A=1 y B=0 ");
                                                        parte1.setColor(ChatColor.YELLOW);

                                                        Bukkit.getScheduler().runTaskLater(this, () -> {
                                                            if (((Lightable) out.getBlockData()).isLit()) {
                                                                parte1.addExtra(" S=1 [ERROR]");
                                                                parte1.setColor(ChatColor.RED);
                                                                event.getPlayer().spigot().sendMessage(parte1);

                                                                parte1.getExtra().clear();

                                                                parte1.addExtra("!!CIRCUITO INVALIDO¡¡");
                                                                parte1.setColor(ChatColor.LIGHT_PURPLE);
                                                                parte1.setBold(true);
                                                                event.getPlayer().spigot().sendMessage(parte1);
                                                            } else {
                                                                parte1.addExtra(" ");
                                                                parte1.addExtra(" S=0 [OK]     [75%]");
                                                                parte1.setColor(ChatColor.YELLOW);
                                                                event.getPlayer().spigot().sendMessage(parte1);

                                                                // Si está bien hacemos siguiente comprobación
                                                                // Movemos Palancas Con Un Retardo
                                                                Bukkit.getScheduler().runTaskLater(getPluginGabri(), () -> {
                                                                    // Movemos Palancas
                                                                    setLever(inA, true);
                                                                    setLever(inB, true);

                                                                    // Dejamos un tiempo para que se cambien las palancas
                                                                    Bukkit.getScheduler().runTaskLater(this, () -> {
                                                                        parte1.getExtra().clear();
                                                                        parte1.setText("->");

                                                                        parte1.addExtra("A=1 y B=1 ");
                                                                        parte1.setColor(ChatColor.YELLOW);
                                                                        if (((Lightable) out.getBlockData()).isLit()) {
                                                                            parte1.addExtra(" ");
                                                                            parte1.addExtra(" S=0 [OK]     [100%]");
                                                                            parte1.setColor(ChatColor.YELLOW);
                                                                            event.getPlayer().spigot().sendMessage(parte1);

                                                                            parte1.getExtra().clear();
                                                                            parte1.setText("");
                                                                            parte1.addExtra("Ejercicio Correcto ");
                                                                            parte1.setColor(ChatColor.BLUE);
                                                                            parte1.setBold(true);
                                                                           
                                                                            
                                                                            parte2.setText("");
                                                                            parte2.addExtra("[Puerta Abierta]");
                                                                            parte2.setColor(ChatColor.GOLD);
                                                                            
                                                                            Block puerta = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(47, -60, -96);
                                                                            Openable openable = (Openable) puerta.getBlockData();
                                                                            openable.setOpen(true); // Establecer la puerta como abierta
                                                                            puerta.setBlockData(openable);



                                                                            guardarProgreso(event.getPlayer(), 1);

                                                                            parte1.addExtra(parte2);
                                                                            event.getPlayer().spigot().sendMessage(parte1);


                                                                        }else{
                                                                            parte1.addExtra(" ");
                                                                            parte1.addExtra(" S=0 [ERROR]");
                                                                            parte1.setColor(ChatColor.RED);
                                                                            event.getPlayer().spigot().sendMessage(parte1);
                                                                            
                                                                        }
                                                                    },10L);

                                                                }, 20L);

                                                            }

                                                        }, 10L);

                                                    }, 20L);

                                                }, 20L);

                                            }

                                        }, 10L);

                                    }, 20L);

                                }, 20L);

                            }

                        }, 10L);

                    }, 20L);

                }, 10L);
            }
        }

    }

    public void checkEjercicio2(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            // El boton comprobar de la puerta AND del primer recinto
            if (block != null && (block.getType() == Material.WARPED_BUTTON)  && (block.getX() == 27 && block.getY() == -60 && block.getZ() == -102)) {
 
                // Crear el primer componente de texto
                TextComponent parte1 = new TextComponent("¡Comprobando asdfjhkosdf---> ");
                parte1.setColor(ChatColor.BLUE);
 
                // Crear el segundo componente de texto
                TextComponent parte2 = new TextComponent("¡ Se Paciente  !");
                parte2.setColor(ChatColor.GOLD);
                parte2.setBold(true);
 
                parte1.addExtra(parte2);
                event.getPlayer().spigot().sendMessage(parte1);

                direccionPalancas = BlockFace.WEST;

                Block inA = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(27, -60, -99);
                Block inB = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(27, -60, -97);
                Block inC = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(27, -60, -95);
 
                Block out = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(42, -59, -96);
 
 
 
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    // Movemos Palancas
                    setLever(inA, false);
                    setLever(inB, false);
                    setLever(inC, false);
 
                    // Dejamos un tiempo para que se cambien las palancas
                    Bukkit.getScheduler().runTaskLater(this, () -> {
                        parte1.getExtra().clear();
                        parte1.setText("->");
 
                        parte1.addExtra("A=0 B=0 C=0");
                        parte1.setColor(ChatColor.YELLOW);
 
                        Bukkit.getScheduler().runTaskLater(this, () -> {
 
                            if (((Lightable) out.getBlockData()).isLit()) {
                                parte1.addExtra(" S=1 [ERROR]");
                                parte1.setColor(ChatColor.RED);
                                event.getPlayer().spigot().sendMessage(parte1);
 
                                parte1.getExtra().clear();
 
                                parte1.addExtra("!!CIRCUITO INVALIDO¡¡");
                                parte1.setColor(ChatColor.LIGHT_PURPLE);
                                parte1.setBold(true);
                                event.getPlayer().spigot().sendMessage(parte1);
                            } else {
                                parte1.addExtra(" ");
                                parte1.addExtra(" S=0 [OK]     [12,5%]");
                                parte1.setColor(ChatColor.YELLOW);
                                event.getPlayer().spigot().sendMessage(parte1);
 
                                // Si está bien hacemos siguiente comprobación
                                // Movemos Palancas Con Un Retardo
                                Bukkit.getScheduler().runTaskLater(this, () -> {
                                    // Movemos Palancas
                                    setLever(inA, false);
                                    setLever(inB, false);
                                    setLever(inC, true);
 
                                    Bukkit.getScheduler().runTaskLater(this, () -> {
                                        parte1.getExtra().clear();
                                        parte1.setText("->");
 
                                        parte1.addExtra("A=0 B=0 C=1");
                                        parte1.setColor(ChatColor.YELLOW);
 
                                        Bukkit.getScheduler().runTaskLater(this, () -> {
 
                                            if (((Lightable) out.getBlockData()).isLit()) {
                                                parte1.addExtra(" ");
                                                parte1.addExtra(" S=1 [OK]     [25%]");
                                                parte1.setColor(ChatColor.YELLOW);
                                                event.getPlayer().spigot().sendMessage(parte1);
 
                                                Bukkit.getScheduler().runTaskLater(this, () -> {
                                                    // Movemos Palancas
                                                    setLever(inA, false);
                                                    setLever(inB, true);
                                                    setLever(inC, false);
 
                                                    Bukkit.getScheduler().runTaskLater(this, () -> {
                                                        parte1.getExtra().clear();
                                                        parte1.setText("->");
 
                                                        parte1.addExtra("A=0 B=1 C=0");
                                                        parte1.setColor(ChatColor.YELLOW);
 
                                                        Bukkit.getScheduler().runTaskLater(this, () -> {
                                                            if (((Lightable) out.getBlockData()).isLit()) {
                                                                parte1.addExtra(" S=1 [ERROR]");
                                                                parte1.setColor(ChatColor.RED);
                                                                event.getPlayer().spigot().sendMessage(parte1);
 
                                                                parte1.getExtra().clear();
 
                                                                parte1.addExtra("!!CIRCUITO INVALIDO¡¡");
                                                                parte1.setColor(ChatColor.LIGHT_PURPLE);
                                                                parte1.setBold(true);
                                                                event.getPlayer().spigot().sendMessage(parte1);
 
 
                                                            }else{
                                                                parte1.addExtra(" ");
                                                                parte1.addExtra(" S=0 [OK]     [37,5%]");
                                                                parte1.setColor(ChatColor.YELLOW);
                                                                event.getPlayer().spigot().sendMessage(parte1);
 
                                                                Bukkit.getScheduler().runTaskLater(this, () -> {
                                                                    // Movemos Palancas
                                                                    setLever(inA, false);
                                                                    setLever(inB, true);
                                                                    setLever(inC, true);
 
                                                                    Bukkit.getScheduler().runTaskLater(this, () -> {
                                                                        parte1.getExtra().clear();
                                                                        parte1.setText("->");
 
                                                                        parte1.addExtra("A=0 B=1 C=1");
                                                                        parte1.setColor(ChatColor.YELLOW);
 
                                                                        Bukkit.getScheduler().runTaskLater(this, () -> {
                                                                            if (((Lightable) out.getBlockData()).isLit()) {
                                                                                parte1.addExtra(" ");
                                                                                parte1.addExtra(" S=1 [OK]     [50%]");
                                                                                parte1.setColor(ChatColor.YELLOW);
                                                                                event.getPlayer().spigot().sendMessage(parte1);
 
                                                                                Bukkit.getScheduler().runTaskLater(this, () -> {
                                                                                    // Movemos Palancas
                                                                                    setLever(inA, true);
                                                                                    setLever(inB, false);
                                                                                    setLever(inC, false);
 
                                                                                    Bukkit.getScheduler().runTaskLater(this, () -> {
                                                                                        parte1.getExtra().clear();
                                                                                        parte1.setText("->");
 
                                                                                        parte1.addExtra("A=1 B=0 C=0");
                                                                                        parte1.setColor(ChatColor.YELLOW);

                                                                                        Bukkit.getScheduler().runTaskLater(this, () -> {
                                                                                            if (!((Lightable) out.getBlockData()).isLit()) {
                                                                                                parte1.addExtra(" ");
                                                                                                parte1.addExtra(" S=0 [OK]     [62,5%]");
                                                                                                parte1.setColor(ChatColor.YELLOW);
                                                                                                event.getPlayer().spigot().sendMessage(parte1);

                                                                                                Bukkit.getScheduler().runTaskLater(this, () -> {
                                                                                                    // Movemos Palancas
                                                                                                    setLever(inA, true);
                                                                                                    setLever(inB, false);
                                                                                                    setLever(inC, true);

                                                                                                    Bukkit.getScheduler().runTaskLater(this, () -> {
                                                                                                        parte1.getExtra().clear();
                                                                                                        parte1.setText("->");
                 
                                                                                                        parte1.addExtra("A=1 B=0 C=1");
                                                                                                        parte1.setColor(ChatColor.YELLOW);

                                                                                                        Bukkit.getScheduler().runTaskLater(this, () -> {
                                                                                                            if (((Lightable) out.getBlockData()).isLit()) {
                                                                                                                parte1.addExtra(" ");
                                                                                                                parte1.addExtra(" S=1 [OK]     [75%]");
                                                                                                                parte1.setColor(ChatColor.YELLOW);
                                                                                                                event.getPlayer().spigot().sendMessage(parte1);


                                                                                                                Bukkit.getScheduler().runTaskLater(this, () -> {
                                                                                                                    // Movemos Palancas
                                                                                                                    setLever(inA, true);
                                                                                                                    setLever(inB, true);
                                                                                                                    setLever(inC, false);

                                                                                                                    Bukkit.getScheduler().runTaskLater(this, () -> {
                                                                                                                        parte1.getExtra().clear();
                                                                                                                        parte1.setText("->");
                                                                                                                        parte1.setColor(ChatColor.YELLOW);
                                                                                                                        parte1.addExtra("A=1 B=1 C=0");
                                                                                                                        parte1.setColor(ChatColor.YELLOW);

                                                                                                                        Bukkit.getScheduler().runTaskLater(this, () -> {
                                                                                                                            if (((Lightable) out.getBlockData()).isLit()) {
                                                                                                                                parte1.addExtra(" ");
                                                                                                                                parte1.addExtra(" S=1 [OK]     [87,5%]");
                                                                                                                                parte1.setColor(ChatColor.YELLOW);
                                                                                                                                event.getPlayer().spigot().sendMessage(parte1);


                                                                                                                                Bukkit.getScheduler().runTaskLater(this, () -> {
                                                                                                                                    // Movemos Palancas
                                                                                                                                    setLever(inA, true);
                                                                                                                                    setLever(inB, true);
                                                                                                                                    setLever(inC, true);

                                                                                                                                    Bukkit.getScheduler().runTaskLater(this, () -> {
                                                                                                                                        parte1.getExtra().clear();
                                                                                                                                        parte1.setText("->");

                                                                                                                                        parte1.addExtra("A=1 B=1 C=1");
                                                                                                                                        parte1.setColor(ChatColor.YELLOW);

                                                                                                                                        Bukkit.getScheduler().runTaskLater(this, () -> {
                                                                                                                                            if (((Lightable) out.getBlockData()).isLit()) {
                                                                                                                                                parte1.addExtra(" ");
                                                                                                                                                parte1.addExtra(" S=1 [OK]     [100%]");
                                                                                                                                                parte1.setColor(ChatColor.YELLOW);
                                                                                                                                                event.getPlayer().spigot().sendMessage(parte1);


                                                                                                                                                parte1.getExtra().clear();
                                                                                                                                                parte1.setText("");
                                                                                                                                                parte1.addExtra("Ejercicio Correcto ");
                                                                                                                                                parte1.setColor(ChatColor.BLUE);
                                                                                                                                                parte1.setBold(true);


                                                                                                                                                parte2.setText("");
                                                                                                                                                parte2.addExtra("[Puerta Abierta]");
                                                                                                                                                parte2.setColor(ChatColor.GOLD);

                                                                                                                                                Block puerta = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(47, -60, -96);
                                                                                                                                                Openable openable = (Openable) puerta.getBlockData();
                                                                                                                                                openable.setOpen(true); // Establecer la puerta como abierta
                                                                                                                                                puerta.setBlockData(openable);



                                                                                                                                                guardarProgreso(event.getPlayer(), 2);

                                                                                                                                                parte1.addExtra(parte2);
                                                                                                                                                event.getPlayer().spigot().sendMessage(parte1);




                                                                                                                                            } else {
                                                                                                                                                parte1.addExtra(" S=0 [ERROR]");
                                                                                                                                                parte1.setColor(ChatColor.RED);
                                                                                                                                                event.getPlayer().spigot().sendMessage(parte1);

                                                                                                                                                parte1.getExtra().clear();

                                                                                                                                                parte1.addExtra("!!CIRCUITO INVALIDO¡¡");
                                                                                                                                                parte1.setColor(ChatColor.LIGHT_PURPLE);
                                                                                                                                                parte1.setBold(true);
                                                                                                                                                event.getPlayer().spigot().sendMessage(parte1);
                                                                                                                                            }
                                                                                                                                        },20l);


                                                                                                                                    },20l);

                                                                                                                                },20L);

                                                                                                                            } else {
                                                                                                                                parte1.addExtra(" S=0 [ERROR]");
                                                                                                                                parte1.setColor(ChatColor.RED);
                                                                                                                                event.getPlayer().spigot().sendMessage(parte1);

                                                                                                                                parte1.getExtra().clear();

                                                                                                                                parte1.addExtra("!!CIRCUITO INVALIDO¡¡");
                                                                                                                                parte1.setColor(ChatColor.LIGHT_PURPLE);
                                                                                                                                parte1.setBold(true);
                                                                                                                                event.getPlayer().spigot().sendMessage(parte1);
                                                                                                                            }
                                                                                                                        },20L);

                                                                                                                    },20L);
                                                                                                                },20L);

                                                                                                               

                                                                                                            }else{
                                                                                                                parte1.addExtra(" S=1 [ERROR]");
                                                                                                                parte1.setColor(ChatColor.RED);
                                                                                                                event.getPlayer().spigot().sendMessage(parte1);
                                                
                                                                                                                parte1.getExtra().clear();
                                                
                                                                                                                parte1.addExtra("!!CIRCUITO INVALIDO¡¡");
                                                                                                                parte1.setColor(ChatColor.LIGHT_PURPLE);
                                                                                                                parte1.setBold(true);
                                                                                                                event.getPlayer().spigot().sendMessage(parte1);        


                                                                                                            }

                                                                                                        },20L);

                                                                                                    },20L);


                                                                                                },20L);


                                                                                            }else{
                                                                                                parte1.addExtra(" S=1 [ERROR]");
                                                                                                parte1.setColor(ChatColor.RED);
                                                                                                event.getPlayer().spigot().sendMessage(parte1);
                                
                                                                                                parte1.getExtra().clear();
                                
                                                                                                parte1.addExtra("!!CIRCUITO INVALIDO¡¡");
                                                                                                parte1.setColor(ChatColor.LIGHT_PURPLE);
                                                                                                parte1.setBold(true);
                                                                                                event.getPlayer().spigot().sendMessage(parte1);
                                                                                            }
                                                                                        },20L);
 
                                                                                        
 
                                                                                    },20L);
 
                                                                                },20L);
 
                                                                            }else{
                                                                                parte1.addExtra(" S=1 [ERROR]");
                                                                                parte1.setColor(ChatColor.RED);
                                                                                event.getPlayer().spigot().sendMessage(parte1);
 
                                                                                parte1.getExtra().clear();
 
                                                                                parte1.addExtra("!!CIRCUITO INVALIDO¡¡");
                                                                                parte1.setColor(ChatColor.LIGHT_PURPLE);
                                                                                parte1.setBold(true);
                                                                                event.getPlayer().spigot().sendMessage(parte1);
 
                                                                            }
                                                                        },20L);
 
 
                                                                    },20L);
 
 
                                                                },20L);
 
 
                                                            }
 
 
                                                        },20L);
 
 
                                                    },20L);
 
 
                                                },20L);
 
                                            }else{
                                                parte1.addExtra(" S=0 [ERROR]");
                                                parte1.setColor(ChatColor.RED);
                                                event.getPlayer().spigot().sendMessage(parte1);
 
                                                parte1.getExtra().clear();
 
                                                parte1.addExtra("!!CIRCUITO INVALIDO¡¡");
                                                parte1.setColor(ChatColor.LIGHT_PURPLE);
                                                parte1.setBold(true);
                                                event.getPlayer().spigot().sendMessage(parte1);
 
                                            }
 
                                        },20L);
 
                                    },20L);
 
                                }, 20L);
                            }
 
                        }, 10L);
 
                    }, 20L);
 
                }, 10L);
            }
        }
    }

    public void checkEjercicio3(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            // El boton comprobar de la puerta AND del primer recinto
            if (block != null && (block.getType() == Material.WARPED_BUTTON)
                    && (block.getX() == 27 && block.getY() == -60 && block.getZ() == -96)) {

                // Crear el primer componente de texto
                TextComponent parte1 = new TextComponent("¡Comprobando Circuito Modulo Comparador---> ");
                parte1.setColor(ChatColor.BLUE);

                // Crear el segundo componente de texto
                TextComponent parte2 = new TextComponent("¡ Se Paciente  !");
                parte2.setColor(ChatColor.GOLD);
                parte2.setBold(true);

                parte1.addExtra(parte2);
                event.getPlayer().spigot().sendMessage(parte1);

                direccionPalancas = BlockFace.WEST;

                Block inA = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(27, -60, -97);
                Block inB = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(27, -60, -95);

                Block out1 = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(42, -59, -98);
                Block out2 = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(42, -59, -96);
                Block out3 = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(42, -59, -94);

                Bukkit.getScheduler().runTaskLater(this, () -> {
                    // Movemos Palancas
                    setLever(inA, false);
                    setLever(inB, false);

                    // Dejamos un tiempo para que se cambien las palancas
                    Bukkit.getScheduler().runTaskLater(this, () -> {
                        parte1.getExtra().clear();
                        parte1.setText("->");

                        parte1.addExtra("A=0 y B=0 ");
                        parte1.setColor(ChatColor.YELLOW);

                        Bukkit.getScheduler().runTaskLater(this, () -> {
                            s1 = (((Lightable) out1.getBlockData()).isLit()) ? true : false;
                            s2 = (((Lightable) out2.getBlockData()).isLit()) ? true : false;
                            s3 = (((Lightable) out3.getBlockData()).isLit()) ? true : false;

                            // Si la que no esta encendida es la 2a lampara, mal
                            if ( s1 || !s2 || s3) {
                                msg = "(A>B)-" + (s1?"1":"0") + " (a=b)=" + (s2?"1":"0") + " (A<B)=" + (s3?"1":"0") + " [ERROR]";
                                parte1.addExtra(msg);
                                parte1.setColor(ChatColor.RED);
                                event.getPlayer().spigot().sendMessage(parte1);

                                parte1.getExtra().clear();

                                parte1.addExtra("!!CIRCUITO INVALIDO¡¡");
                                parte1.setColor(ChatColor.LIGHT_PURPLE);
                                parte1.setBold(true);
                                event.getPlayer().spigot().sendMessage(parte1);

                            } else {
                                parte1.addExtra(" ");
                                msg = "S1=" + (s1?"1":"0") + " S2=" + (s2?"1":"0") + " S3=" + (s3?"1":"0") + " [OK]     [25%]";
                                parte1.addExtra(msg);
                                parte1.setColor(ChatColor.YELLOW);
                                event.getPlayer().spigot().sendMessage(parte1);

                                // Si está bien hacemos siguiente comprobación
                                // Movemos Palancas Con Un Retardo
                                Bukkit.getScheduler().runTaskLater(this, () -> {
                                    // Movemos Palancas
                                    setLever(inA, false);
                                    setLever(inB, true);

                                    // Dejamos un tiempo para que se cambien las palancas
                                    Bukkit.getScheduler().runTaskLater(this, () -> {
                                        parte1.getExtra().clear();
                                        parte1.setText("->");

                                        parte1.addExtra("A=0 y B=1 ");
                                        parte1.setColor(ChatColor.YELLOW);

                                        Bukkit.getScheduler().runTaskLater(this, () -> {
                                            s1 = (((Lightable) out1.getBlockData()).isLit()) ? true : false;
                                            s2 = (((Lightable) out2.getBlockData()).isLit()) ? true : false;
                                            s3 = (((Lightable) out3.getBlockData()).isLit()) ? true : false;


                                            if (s1 || s2 || !s3) {
                                                msg = "(A>B)-" + (s1?"1":"0") + " (a=b)=" + (s2?"1":"0") + " (A<B)=" + (s3?"1":"0") + " [ERROR]";
                                                parte1.addExtra(msg);
                                                parte1.setColor(ChatColor.RED);
                                                event.getPlayer().spigot().sendMessage(parte1);

                                                parte1.getExtra().clear();

                                                parte1.addExtra("!!CIRCUITO INVALIDO¡¡");
                                                parte1.setColor(ChatColor.LIGHT_PURPLE);
                                                parte1.setBold(true);
                                                event.getPlayer().spigot().sendMessage(parte1);
                                            } else {
                                                parte1.addExtra(" ");
                                                msg = "S1=" + (s1?"1":"0") + " S2=" + (s2?"1":"0") + " S3=" + (s3?"1":"0") + " [OK]     [50%]";
                                                parte1.addExtra(msg);
                                                parte1.setColor(ChatColor.YELLOW);
                                                event.getPlayer().spigot().sendMessage(parte1);

                                                // Si está bien hacemos siguiente comprobación
                                                // Movemos Palancas Con Un Retardo
                                                Bukkit.getScheduler().runTaskLater(getPluginGabri(), () -> {
                                                    // Movemos Palancas
                                                    setLever(inA, true);
                                                    setLever(inB, false);

                                                    // Dejamos un tiempo para que se cambien las palancas
                                                    Bukkit.getScheduler().runTaskLater(this, () -> {
                                                        parte1.getExtra().clear();
                                                        parte1.setText("->");

                                                        parte1.addExtra("A=1 y B=0 ");
                                                        parte1.setColor(ChatColor.YELLOW);

                                                        Bukkit.getScheduler().runTaskLater(this, () -> {
                                                            s1 = (((Lightable) out1.getBlockData()).isLit()) ? true : false;
                                                            s2 = (((Lightable) out2.getBlockData()).isLit()) ? true : false;
                                                            s3 = (((Lightable) out3.getBlockData()).isLit()) ? true : false;

                                                            if (!s1 || s2 || s3) {
                                                                msg = "(A>B)-" + (s1?"1":"0") + " (a=b)=" + (s2?"1":"0") + " (A<B)=" + (s3?"1":"0") + " [ERROR]";
                                                                parte1.addExtra(msg);
                                                                parte1.setColor(ChatColor.RED);
                                                                event.getPlayer().spigot().sendMessage(parte1);

                                                                parte1.getExtra().clear();

                                                                parte1.addExtra("!!CIRCUITO INVALIDO¡¡");
                                                                parte1.setColor(ChatColor.LIGHT_PURPLE);
                                                                parte1.setBold(true);
                                                                event.getPlayer().spigot().sendMessage(parte1);
                                                            } else {
                                                                parte1.addExtra(" ");
                                                                msg = "S1=" + (s1?"1":"0") + " S2=" + (s2?"1":"0") + " S3=" + (s3?"1":"0") + " [OK]     [75%]";
                                                                parte1.addExtra(msg);
                                                                parte1.setColor(ChatColor.YELLOW);
                                                                event.getPlayer().spigot().sendMessage(parte1);

                                                                // Si está bien hacemos siguiente comprobación
                                                                // Movemos Palancas Con Un Retardo
                                                                Bukkit.getScheduler().runTaskLater(getPluginGabri(), () -> {
                                                                    // Movemos Palancas
                                                                    setLever(inA, true);
                                                                    setLever(inB, true);

                                                                    // Dejamos un tiempo para que se cambien las palancas
                                                                    Bukkit.getScheduler().runTaskLater(this, () -> {
                                                                        parte1.getExtra().clear();
                                                                        parte1.setText("->");

                                                                        parte1.addExtra("A=1 y B=1 ");
                                                                        parte1.setColor(ChatColor.YELLOW);

                                                                        Bukkit.getScheduler().runTaskLater(this, () -> {

                                                                            s1 = (((Lightable) out1.getBlockData()).isLit()) ? true : false;
                                                                            s2 = (((Lightable) out2.getBlockData()).isLit()) ? true : false;
                                                                            s3 = (((Lightable) out3.getBlockData()).isLit()) ? true : false;
                                                                            if (!s1 && s2 && !s3) {
                                                                                parte1.addExtra(" ");
                                                                                msg = "S1=" + (s1 ? "1" : "0") + " S2=" + (s2 ? "1" : "0") + " S3=" + (s3 ? "1" : "0") + " [OK]     [100%]";
                                                                                parte1.addExtra(msg);
                                                                                parte1.setColor(ChatColor.YELLOW);
                                                                                event.getPlayer().spigot().sendMessage(parte1);

                                                                                parte1.getExtra().clear();
                                                                                parte1.setText("");
                                                                                parte1.addExtra("Ejercicio Correcto ");
                                                                                parte1.setColor(ChatColor.BLUE);
                                                                                parte1.setBold(true);


                                                                                parte2.setText("");
                                                                                parte2.addExtra("[Puerta Abierta]");
                                                                                parte2.setColor(ChatColor.GOLD);

                                                                                Block puerta = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(47, -60, -96);
                                                                                Openable openable = (Openable) puerta.getBlockData();
                                                                                openable.setOpen(true); // Establecer la puerta como abierta
                                                                                puerta.setBlockData(openable);

                                                                                guardarProgreso(event.getPlayer(), 3);

                                                                                parte1.addExtra(parte2);
                                                                                event.getPlayer().spigot().sendMessage(parte1);


                                                                            } else {
                                                                                parte1.addExtra(" ");
                                                                                msg = "(A>B)-" + (s1?"1":"0") + " (a=b)=" + (s2?"1":"0") + " (A<B)=" + (s3?"1":"0") + " [ERROR]";
                                                                                parte1.addExtra(msg);
                                                                                parte1.setColor(ChatColor.RED);
                                                                                event.getPlayer().spigot().sendMessage(parte1);
                                                                            }

                                                                        }, 20L);

                                                                    },10L);

                                                                }, 20L);

                                                            }

                                                        }, 10L);

                                                    }, 20L);

                                                }, 20L);

                                            }

                                        }, 10L);

                                    }, 20L);

                                }, 20L);

                            }

                        }, 10L);

                    }, 20L);

                }, 10L);
            }
        }

    }

    // Genera una combinación aleatoria
    public static boolean[] generarCombinacionPalancas() {
        Random random = new Random();
        boolean[] arrayBooleano = new boolean[6];

        for (int i = 0; i < arrayBooleano.length; i++) {
            arrayBooleano[i] = random.nextBoolean(); // Genera true o false aleatoriamente
        }

        return arrayBooleano;
    }

    public void checkEjercicio4(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            // El boton comprobar de la puerta AND del primer recinto
            if (block != null && (block.getType() == Material.WARPED_BUTTON)
                    && (block.getX() == -48 && block.getY() == -60 && block.getZ() == 6)) {

                // Crear el primer componente de texto
                TextComponent parte1 = new TextComponent("¡Comprobando Circuito Multiplexor ---> ");
                parte1.setColor(ChatColor.BLUE);

                // Crear el segundo componente de texto
                TextComponent parte2 = new TextComponent("¡ Se Paciente  !");
                parte2.setColor(ChatColor.GOLD);
                parte2.setBold(true);

                parte1.addExtra(parte2);
                event.getPlayer().spigot().sendMessage(parte1);

                direccionPalancas = BlockFace.EAST;

                Block inD0 = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(-50, -60, 5);
                Block inD1 = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(-50, -60, 3);
                Block inD2 = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(-50, -60, 1);
                Block inD3 = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(-50, -60, -1);
                Block inS0 = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(-50, -60, -3);
                Block inS1 = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(-50, -60, -5);

                Block out = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(-80, -59, 0);


                porcentaje = 12.5;
                contador.set(0);

                tareaComprobacion = new Runnable() {
                    @Override
                    public void run() {
                        if (contador.get() >= 8) {
                            // Si todas las iteraciones completaron exitosamente, mostramos el mensaje final.
                            parte1.getExtra().clear();
                            parte1.setText("");
                            parte1.addExtra("Ejercicio Correcto ");
                            parte1.setColor(ChatColor.BLUE);
                            parte1.setBold(true);

                            parte2.setText("");
                            parte2.addExtra("[Puerta Abierta]");
                            parte2.setColor(ChatColor.GOLD);

                            Block puerta = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(-91, -54, 0);
                            Openable openable = (Openable) puerta.getBlockData();
                            openable.setOpen(true); // Establecer la puerta como abierta
                            puerta.setBlockData(openable);

                            guardarProgreso(event.getPlayer(), 4);

                            parte1.addExtra(parte2);
                            event.getPlayer().spigot().sendMessage(parte1);
                            return;
                        }

                        // Genera una combinación aleatoria para probar
                        boolean[] combinacion = generarCombinacionPalancas();

                        // Movemos Palancas
                        setLever(inD0, combinacion[0]);
                        setLever(inD1, combinacion[1]);
                        setLever(inD2, combinacion[2]);
                        setLever(inD3, combinacion[3]);
                        setLever(inS0, combinacion[4]);
                        setLever(inS1, combinacion[5]);

                        // Dejamos un tiempo para que se cambien las palancas
                        Bukkit.getScheduler().runTaskLater(App.this, () -> {
                            parte1.getExtra().clear();
                            parte1.setText("->");

                            msg = "D0=" + (combinacion[0] ? "1" : "0") + " D1=" + (combinacion[1] ? "1" : "0");
                            msg += " D2=" + (combinacion[2] ? "1" : "0") + " D3=" + (combinacion[3] ? "1" : "0");
                            msg += " S0=" + (combinacion[4] ? "1" : "0") + " S1=" + (combinacion[5] ? "1" : "0");
                            parte1.addExtra(msg);
                            parte1.setColor(ChatColor.YELLOW);

                            // Dejamos un tiempo para leer el estado de la salida
                            Bukkit.getScheduler().runTaskLater(App.this, () -> {
                                y = (((Lightable) out.getBlockData()).isLit()) ? true : false;

                                if(!combinacion[4] && !combinacion[5]){
                                    // S0=0 S1=0 -> salida es D0

                                    if(combinacion[0] == y){
                                        porcentajeStr = String.format("%.1f%%", porcentaje);
                                        porcentaje = porcentaje + 12.5;

                                        parte1.addExtra(" ");
                                        msg = "     D0=" + (combinacion[0] ? "1" : "0") + "  ==  Y=" + (y ? "1" : "0") + " [OK]     " + porcentajeStr;
                                        parte1.addExtra(msg);
                                        parte1.setColor(ChatColor.YELLOW);
                                        event.getPlayer().spigot().sendMessage(parte1);

                                        contador.incrementAndGet();
                                        Bukkit.getScheduler().runTaskLater(App.this, this, 20L);

                                    }else{
                                        msg = "     D0=" + (combinacion[0] ? "1" : "0") + "  !=  Y=" + (y ? "1" : "0") + " [ERROR]";
                                        parte1.addExtra(msg);
                                        parte1.setColor(ChatColor.RED);
                                        event.getPlayer().spigot().sendMessage(parte1);

                                        parte1.getExtra().clear();

                                        parte1.addExtra("!!CIRCUITO INVALIDO¡¡");
                                        parte1.setColor(ChatColor.LIGHT_PURPLE);
                                        parte1.setBold(true);
                                        event.getPlayer().spigot().sendMessage(parte1);
                                    }
                                }
                                else if(!combinacion[4] && combinacion[5]){
                                    // S0=0 S1=1 -> salida es D2
                                    if(combinacion[2] == y){
                                        porcentajeStr = String.format("%.1f%%", porcentaje);
                                        porcentaje = porcentaje + 12.5;

                                        parte1.addExtra(" ");
                                        msg = "     D2=" + (combinacion[2] ? "1" : "0") + "  ==  Y=" + (y ? "1" : "0") + " [OK]     " + porcentajeStr;
                                        parte1.addExtra(msg);
                                        parte1.setColor(ChatColor.YELLOW);
                                        event.getPlayer().spigot().sendMessage(parte1);

                                        contador.incrementAndGet();
                                        Bukkit.getScheduler().runTaskLater(App.this, this, 20L);

                                    }else{
                                        msg = "     D2=" + (combinacion[2] ? "1" : "0") + "  !=  Y=" + (y ? "1" : "0") + " [ERROR]";
                                        parte1.addExtra(msg);
                                        parte1.setColor(ChatColor.RED);
                                        event.getPlayer().spigot().sendMessage(parte1);

                                        parte1.getExtra().clear();

                                        parte1.addExtra("!!CIRCUITO INVALIDO¡¡");
                                        parte1.setColor(ChatColor.LIGHT_PURPLE);
                                        parte1.setBold(true);
                                        event.getPlayer().spigot().sendMessage(parte1);
                                    }
                                }
                                else if(combinacion[4] && !combinacion[5]){
                                    // S0=1 S1=0 -> salida es D1
                                    if(combinacion[1] == y){
                                        porcentajeStr = String.format("%.1f%%", porcentaje);
                                        porcentaje = porcentaje + 12.5;

                                        parte1.addExtra(" ");
                                        msg = "     D1=" + (combinacion[1] ? "1" : "0") + "  ==  Y=" + (y ? "1" : "0") + " [OK]     " + porcentajeStr;
                                        parte1.addExtra(msg);
                                        parte1.setColor(ChatColor.YELLOW);
                                        event.getPlayer().spigot().sendMessage(parte1);

                                        contador.incrementAndGet();
                                        Bukkit.getScheduler().runTaskLater(App.this, this, 20L);

                                    }else{
                                        msg = "     D1=" + (combinacion[1] ? "1" : "0") + "  !=  Y=" + (y ? "1" : "0") + " [ERROR]";
                                        parte1.addExtra(msg);
                                        parte1.setColor(ChatColor.RED);
                                        event.getPlayer().spigot().sendMessage(parte1);

                                        parte1.getExtra().clear();

                                        parte1.addExtra("!!CIRCUITO INVALIDO¡¡");
                                        parte1.setColor(ChatColor.LIGHT_PURPLE);
                                        parte1.setBold(true);
                                        event.getPlayer().spigot().sendMessage(parte1);
                                    }
                                }
                                else if(combinacion[4] && combinacion[5]){
                                    // S0=1 S1=1 -> salida es D3
                                    if(combinacion[3] == y){
                                        porcentajeStr = String.format("%.1f%%", porcentaje);
                                        porcentaje = porcentaje + 12.5;

                                        parte1.addExtra(" ");
                                        msg = "     D3=" + (combinacion[3] ? "1" : "0") + "  ==  Y=" + (y ? "1" : "0") + " [OK]     " + porcentajeStr;
                                        parte1.addExtra(msg);
                                        parte1.setColor(ChatColor.YELLOW);
                                        event.getPlayer().spigot().sendMessage(parte1);

                                        contador.incrementAndGet();
                                        Bukkit.getScheduler().runTaskLater(App.this, this, 20L);

                                    }else{
                                        msg = "     D3=" + (combinacion[3] ? "1" : "0") + "  !=  Y=" + (y ? "1" : "0") + " [ERROR]";
                                        parte1.addExtra(msg);
                                        parte1.setColor(ChatColor.RED);
                                        event.getPlayer().spigot().sendMessage(parte1);

                                        parte1.getExtra().clear();

                                        parte1.addExtra("!!CIRCUITO INVALIDO¡¡");
                                        parte1.setColor(ChatColor.LIGHT_PURPLE);
                                        parte1.setBold(true);
                                        event.getPlayer().spigot().sendMessage(parte1);
                                    }
                                }
                            }, 30L);

                        }, 30L);
                    }
                };

                Bukkit.getScheduler().runTaskLater(this, tareaComprobacion, 20L);

                /*
                for(contador=0; contador<iteracciones; contador++) {

                    if(!estadoComprobacion)
                        break;

                    Bukkit.getScheduler().runTaskLater(this, () -> {
                        //Genera una combinación aleatoria para probar
                        boolean[] combinacion = generarCombinacionPalancas();

                        // Movemos Palancas
                        setLever(inD0, combinacion[0]);
                        setLever(inD1, combinacion[1]);
                        setLever(inD2, combinacion[2]);
                        setLever(inD3, combinacion[3]);
                        setLever(inS0, combinacion[4]);
                        setLever(inS1, combinacion[5]);

                        // Dejamos un tiempo para que se cambien las palancas
                        Bukkit.getScheduler().runTaskLater(this, () -> {
                            parte1.getExtra().clear();
                            parte1.setText("->");

                            msg = "D0=" + (combinacion[0] ? "1" : "0") + " D1=" + (combinacion[1] ? "1" : "0");
                            msg += " D2=" + (combinacion[2] ? "1" : "0") + " D3=" + (combinacion[3] ? "1" : "0");
                            msg += " S0=" + (combinacion[4] ? "1" : "0") + " S1=" + (combinacion[5] ? "1" : "0");
                            parte1.addExtra(msg);
                            parte1.setColor(ChatColor.YELLOW);
                            parte1.getExtra().clear();

                            Bukkit.getScheduler().runTaskLater(this, () -> {
                                y = (((Lightable) out.getBlockData()).isLit()) ? true : false;

                                if(!combinacion[4] && !combinacion[5]){
                                // S0=0 S1=0 -> salida es D0

                                    if(combinacion[0] == y){
                                        porcentajeStr = String.format("%.1f%%", porcentaje);
                                        porcentaje = porcentaje + 12.5;

                                        parte1.addExtra(" ");
                                        msg = "D0=" + (combinacion[0] ? "1" : "0") + "  !=  Y=" + (y ? "1" : "0") + " [OK]     " + porcentajeStr;
                                        parte1.addExtra(msg);
                                        parte1.setColor(ChatColor.YELLOW);
                                        event.getPlayer().spigot().sendMessage(parte1);

                                    }else{
                                        msg = "D0=" + (combinacion[0] ? "1" : "0") + "  !=  Y=" + (y ? "1" : "0") + " [ERROR]";
                                        parte1.addExtra(msg);
                                        parte1.setColor(ChatColor.RED);
                                        event.getPlayer().spigot().sendMessage(parte1);

                                        parte1.getExtra().clear();

                                        parte1.addExtra("!!CIRCUITO INVALIDO¡¡");
                                        parte1.setColor(ChatColor.LIGHT_PURPLE);
                                        parte1.setBold(true);
                                        event.getPlayer().spigot().sendMessage(parte1);

                                        estadoComprobacion = false;
                                    }
                                }
                                else if(!combinacion[4] && combinacion[5]){
                                // S0=0 S1=1 -> salida es D2
                                    if(combinacion[2] == y){
                                        porcentajeStr = String.format("%.1f%%", porcentaje);
                                        porcentaje = porcentaje + 12.5;

                                        parte1.addExtra(" ");
                                        msg = "D2=" + (combinacion[0] ? "1" : "0") + "  !=  Y=" + (y ? "1" : "0") + " [OK]     " + porcentajeStr;
                                        parte1.addExtra(msg);
                                        parte1.setColor(ChatColor.YELLOW);
                                        event.getPlayer().spigot().sendMessage(parte1);

                                    }else{
                                        msg = "D2=" + (combinacion[0] ? "1" : "0") + "  !=  Y=" + (y ? "1" : "0") + " [ERROR]";
                                        parte1.addExtra(msg);
                                        parte1.setColor(ChatColor.RED);
                                        event.getPlayer().spigot().sendMessage(parte1);

                                        parte1.getExtra().clear();

                                        parte1.addExtra("!!CIRCUITO INVALIDO¡¡");
                                        parte1.setColor(ChatColor.LIGHT_PURPLE);
                                        parte1.setBold(true);
                                        event.getPlayer().spigot().sendMessage(parte1);

                                        estadoComprobacion = false;
                                    }
                                }
                                else if(combinacion[4] && !combinacion[5]){
                                    // S0=1 S1=0 -> salida es D1
                                    if(combinacion[1] == y){
                                        porcentajeStr = String.format("%.1f%%", porcentaje);
                                        porcentaje = porcentaje + 12.5;

                                        parte1.addExtra(" ");
                                        msg = "D1=" + (combinacion[0] ? "1" : "0") + "  !=  Y=" + (y ? "1" : "0") + " [OK]     " + porcentajeStr;
                                        parte1.addExtra(msg);
                                        parte1.setColor(ChatColor.YELLOW);
                                        event.getPlayer().spigot().sendMessage(parte1);

                                    }else{
                                        msg = "D1=" + (combinacion[0] ? "1" : "0") + "  !=  Y=" + (y ? "1" : "0") + " [ERROR]";
                                        parte1.addExtra(msg);
                                        parte1.setColor(ChatColor.RED);
                                        event.getPlayer().spigot().sendMessage(parte1);

                                        parte1.getExtra().clear();

                                        parte1.addExtra("!!CIRCUITO INVALIDO¡¡");
                                        parte1.setColor(ChatColor.LIGHT_PURPLE);
                                        parte1.setBold(true);
                                        event.getPlayer().spigot().sendMessage(parte1);

                                        estadoComprobacion = false;
                                    }
                                }
                                else if(combinacion[4] && combinacion[5]){
                                    // S0=1 S1=1 -> salida es D3
                                    if(combinacion[3] == y){
                                        porcentajeStr = String.format("%.1f%%", porcentaje);
                                        porcentaje = porcentaje + 12.5;

                                        parte1.addExtra(" ");
                                        msg = "D3=" + (combinacion[0] ? "1" : "0") + "  !=  Y=" + (y ? "1" : "0") + " [OK]     " + porcentajeStr;
                                        parte1.addExtra(msg);
                                        parte1.setColor(ChatColor.YELLOW);
                                        event.getPlayer().spigot().sendMessage(parte1);

                                    }else{
                                        msg = "D3=" + (combinacion[0] ? "1" : "0") + "  !=  Y=" + (y ? "1" : "0") + " [ERROR]";
                                        parte1.addExtra(msg);
                                        parte1.setColor(ChatColor.RED);
                                        event.getPlayer().spigot().sendMessage(parte1);

                                        parte1.getExtra().clear();

                                        parte1.addExtra("!!CIRCUITO INVALIDO¡¡");
                                        parte1.setColor(ChatColor.LIGHT_PURPLE);
                                        parte1.setBold(true);
                                        event.getPlayer().spigot().sendMessage(parte1);

                                        estadoComprobacion = false;
                                    }
                                }

                                //Si se llega al final y todo okay, se abre la puerta
                                if(contador==iteracciones-1 && estadoComprobacion){
                                    parte1.getExtra().clear();
                                    parte1.setText("");
                                    parte1.addExtra("Ejercicio Correcto ");
                                    parte1.setColor(ChatColor.BLUE);
                                    parte1.setBold(true);


                                    parte2.setText("");
                                    parte2.addExtra("[Puerta Abierta]");
                                    parte2.setColor(ChatColor.GOLD);

                                    Block puerta = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(-91, -54, 0);
                                    Openable openable = (Openable) puerta.getBlockData();
                                    openable.setOpen(true); // Establecer la puerta como abierta
                                    puerta.setBlockData(openable);

                                    guardarProgreso(event.getPlayer(), 4);

                                    parte1.addExtra(parte2);
                                    event.getPlayer().spigot().sendMessage(parte1);
                                }

                            }, 20L);

                        }, 20L);

                    }, contador * 20L);
                }
                */
            }
        }

    }


    public void checkEjercicio5(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            // El boton comprobar de la puerta AND del primer recinto
            if (block != null && (block.getType() == Material.WARPED_BUTTON)
                    && (block.getX() == 27 && block.getY() == -60 && block.getZ() == -96)) {

                // Crear el primer componente de texto
                TextComponent parte1 = new TextComponent("¡Comprobando Circuito Semi sumador ---> ");
                parte1.setColor(ChatColor.BLUE);

                // Crear el segundo componente de texto
                TextComponent parte2 = new TextComponent("¡ Se Paciente  !");
                parte2.setColor(ChatColor.GOLD);
                parte2.setBold(true);

                parte1.addExtra(parte2);
                event.getPlayer().spigot().sendMessage(parte1);

                direccionPalancas = BlockFace.WEST;

                Block inA = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(27, -60, -97);
                Block inB = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(27, -60, -95);

                Block out1 = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(42, -59, -97);
                Block out2 = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(42, -59, -95);

                Bukkit.getScheduler().runTaskLater(this, () -> {
                    // Movemos Palancas
                    setLever(inA, false);
                    setLever(inB, false);

                    // Dejamos un tiempo para que se cambien las palancas
                    Bukkit.getScheduler().runTaskLater(this, () -> {
                        parte1.getExtra().clear();
                        parte1.setText("->");

                        parte1.addExtra("E1=0 y E2=0 ");
                        parte1.setColor(ChatColor.YELLOW);

                        Bukkit.getScheduler().runTaskLater(this, () -> {
                            s1 = (((Lightable) out1.getBlockData()).isLit()) ? true : false;
                            s2 = (((Lightable) out2.getBlockData()).isLit()) ? true : false;

                            if ( s1 || s2) {
                                msg = "S=" + (s1?"1":"0") + "  C=" + (s2?"1":"0") + " [ERROR]";
                                parte1.addExtra(msg);
                                parte1.setColor(ChatColor.RED);
                                event.getPlayer().spigot().sendMessage(parte1);

                                parte1.getExtra().clear();

                                parte1.addExtra("!!CIRCUITO INVALIDO¡¡");
                                parte1.setColor(ChatColor.LIGHT_PURPLE);
                                parte1.setBold(true);
                                event.getPlayer().spigot().sendMessage(parte1);

                            } else {
                                parte1.addExtra(" ");
                                msg = "S=" + (s1?"1":"0") + "  C=" + (s2?"1":"0") + " [OK]     [25%]";
                                parte1.addExtra(msg);
                                parte1.setColor(ChatColor.YELLOW);
                                event.getPlayer().spigot().sendMessage(parte1);

                                // Si está bien hacemos siguiente comprobación
                                // Movemos Palancas Con Un Retardo
                                Bukkit.getScheduler().runTaskLater(this, () -> {
                                    // Movemos Palancas
                                    setLever(inA, false);
                                    setLever(inB, true);

                                    // Dejamos un tiempo para que se cambien las palancas
                                    Bukkit.getScheduler().runTaskLater(this, () -> {
                                        parte1.getExtra().clear();
                                        parte1.setText("->");

                                        parte1.addExtra("E1=0 y E2=1 ");
                                        parte1.setColor(ChatColor.YELLOW);

                                        Bukkit.getScheduler().runTaskLater(this, () -> {
                                            s1 = (((Lightable) out1.getBlockData()).isLit()) ? true : false;
                                            s2 = (((Lightable) out2.getBlockData()).isLit()) ? true : false;

                                            if (!s1 || s2) {
                                                msg = "S=" + (s1?"1":"0") + "  C=" + (s2?"1":"0") + " [ERROR]";
                                                parte1.addExtra(msg);
                                                parte1.setColor(ChatColor.RED);
                                                event.getPlayer().spigot().sendMessage(parte1);

                                                parte1.getExtra().clear();

                                                parte1.addExtra("!!CIRCUITO INVALIDO¡¡");
                                                parte1.setColor(ChatColor.LIGHT_PURPLE);
                                                parte1.setBold(true);
                                                event.getPlayer().spigot().sendMessage(parte1);
                                            } else {
                                                parte1.addExtra(" ");
                                                msg = "S=" + (s1?"1":"0") + "  C=" + (s2?"1":"0") + " [OK]     [50%]";
                                                parte1.addExtra(msg);
                                                parte1.setColor(ChatColor.YELLOW);
                                                event.getPlayer().spigot().sendMessage(parte1);

                                                // Si está bien hacemos siguiente comprobación
                                                // Movemos Palancas Con Un Retardo
                                                Bukkit.getScheduler().runTaskLater(getPluginGabri(), () -> {
                                                    // Movemos Palancas
                                                    setLever(inA, true);
                                                    setLever(inB, false);

                                                    // Dejamos un tiempo para que se cambien las palancas
                                                    Bukkit.getScheduler().runTaskLater(this, () -> {
                                                        parte1.getExtra().clear();
                                                        parte1.setText("->");

                                                        parte1.addExtra("E1=1 y E2=0 ");
                                                        parte1.setColor(ChatColor.YELLOW);

                                                        Bukkit.getScheduler().runTaskLater(this, () -> {
                                                            s1 = (((Lightable) out1.getBlockData()).isLit()) ? true : false;
                                                            s2 = (((Lightable) out2.getBlockData()).isLit()) ? true : false;

                                                            if (!s1 || s2) {
                                                                msg = "S=" + (s1?"1":"0") + "  C=" + (s2?"1":"0") + " [ERROR]";
                                                                parte1.addExtra(msg);
                                                                parte1.setColor(ChatColor.RED);
                                                                event.getPlayer().spigot().sendMessage(parte1);

                                                                parte1.getExtra().clear();

                                                                parte1.addExtra("!!CIRCUITO INVALIDO¡¡");
                                                                parte1.setColor(ChatColor.LIGHT_PURPLE);
                                                                parte1.setBold(true);
                                                                event.getPlayer().spigot().sendMessage(parte1);
                                                            } else {
                                                                parte1.addExtra(" ");
                                                                msg = "S=" + (s1?"1":"0") + "  C=" + (s2?"1":"0") + " [OK]     [75%]";
                                                                parte1.addExtra(msg);
                                                                parte1.setColor(ChatColor.YELLOW);
                                                                event.getPlayer().spigot().sendMessage(parte1);

                                                                // Si está bien hacemos siguiente comprobación
                                                                // Movemos Palancas Con Un Retardo
                                                                Bukkit.getScheduler().runTaskLater(getPluginGabri(), () -> {
                                                                    // Movemos Palancas
                                                                    setLever(inA, true);
                                                                    setLever(inB, true);

                                                                    // Dejamos un tiempo para que se cambien las palancas
                                                                    Bukkit.getScheduler().runTaskLater(this, () -> {
                                                                        parte1.getExtra().clear();
                                                                        parte1.setText("->");

                                                                        parte1.addExtra("E1=1 y E2=1 ");
                                                                        parte1.setColor(ChatColor.YELLOW);

                                                                        Bukkit.getScheduler().runTaskLater(this, () -> {

                                                                            s1 = (((Lightable) out1.getBlockData()).isLit()) ? true : false;
                                                                            s2 = (((Lightable) out2.getBlockData()).isLit()) ? true : false;

                                                                            if (!s1 && s2) {
                                                                                parte1.addExtra(" ");
                                                                                msg = "S=" + (s1?"1":"0") + "  C=" + (s2?"1":"0") + " [OK]     [100%]";
                                                                                parte1.addExtra(msg);
                                                                                parte1.setColor(ChatColor.YELLOW);
                                                                                event.getPlayer().spigot().sendMessage(parte1);

                                                                                parte1.getExtra().clear();
                                                                                parte1.setText("");
                                                                                parte1.addExtra("Ejercicio Correcto ");
                                                                                parte1.setColor(ChatColor.BLUE);
                                                                                parte1.setBold(true);


                                                                                parte2.setText("");
                                                                                parte2.addExtra("[Puerta Abierta]");
                                                                                parte2.setColor(ChatColor.GOLD);

                                                                                Block puerta = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(47, -60, -96);
                                                                                Openable openable = (Openable) puerta.getBlockData();
                                                                                openable.setOpen(true); // Establecer la puerta como abierta
                                                                                puerta.setBlockData(openable);

                                                                                guardarProgreso(event.getPlayer(), 5);

                                                                                parte1.addExtra(parte2);
                                                                                event.getPlayer().spigot().sendMessage(parte1);


                                                                            } else {
                                                                                parte1.addExtra(" ");
                                                                                msg = "S=" + (s1?"1":"0") + "  C=" + (s2?"1":"0") + " [OK]     [ERROR]";
                                                                                parte1.addExtra(msg);
                                                                                parte1.setColor(ChatColor.RED);
                                                                                event.getPlayer().spigot().sendMessage(parte1);
                                                                            }

                                                                        }, 20L);

                                                                    },10L);

                                                                }, 20L);

                                                            }

                                                        }, 10L);

                                                    }, 20L);

                                                }, 20L);

                                            }

                                        }, 10L);

                                    }, 20L);

                                }, 20L);

                            }

                        }, 10L);

                    }, 20L);

                }, 10L);
            }
        }

    }


    public void checkEjercicio6(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            // El boton comprobar de la puerta AND del primer recinto
            if (block != null && (block.getType() == Material.WARPED_BUTTON)
                    && (block.getX() == 25 && block.getY() == -60 && block.getZ() == -100)) {

                // Crear el primer componente de texto
                TextComponent parte1 = new TextComponent("¡Comprobando Circuito Sumador completo ---> ");
                parte1.setColor(ChatColor.BLUE);

                // Crear el segundo componente de texto
                TextComponent parte2 = new TextComponent("¡ Se Paciente  !");
                parte2.setColor(ChatColor.GOLD);
                parte2.setBold(true);

                parte1.addExtra(parte2);
                event.getPlayer().spigot().sendMessage(parte1);

                direccionPalancas = BlockFace.WEST;

                Block inA = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(26, -60, -98);
                Block inB = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(26, -60, -96);
                Block inC = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(26, -60, -94);

                Block out1 = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(42, -59, -97);
                Block out2 = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(42, -59, -95);

                Bukkit.getScheduler().runTaskLater(this, () -> {
                    // Movemos Palancas
                    setLever(inA, false);
                    setLever(inB, false);
                    setLever(inC, false);

                    // Dejamos un tiempo para que se cambien las palancas
                    Bukkit.getScheduler().runTaskLater(this, () -> {
                        parte1.getExtra().clear();
                        parte1.setText("->");

                        parte1.addExtra("Ci=0 E1=0 E2=0");
                        parte1.setColor(ChatColor.YELLOW);

                        Bukkit.getScheduler().runTaskLater(this, () -> {
                            s1 = (((Lightable) out1.getBlockData()).isLit()) ? true : false;
                            s2 = (((Lightable) out2.getBlockData()).isLit()) ? true : false;

                            if (s1 || s2) {
                                msg = "S=" + (s1?"1":"0") + "  Co=" + (s2?"1":"0") + " [ERROR]";
                                parte1.addExtra(msg);
                                parte1.setColor(ChatColor.RED);
                                event.getPlayer().spigot().sendMessage(parte1);

                                parte1.getExtra().clear();

                                parte1.addExtra("!!CIRCUITO INVALIDO¡¡");
                                parte1.setColor(ChatColor.LIGHT_PURPLE);
                                parte1.setBold(true);
                                event.getPlayer().spigot().sendMessage(parte1);

                            } else {
                                parte1.addExtra(" ");
                                msg = "S=" + (s1?"1":"0") + "  Co=" + (s2?"1":"0") + " [OK]     [12,5%]";
                                parte1.addExtra(msg);
                                parte1.setColor(ChatColor.YELLOW);
                                event.getPlayer().spigot().sendMessage(parte1);

                                // Si está bien hacemos siguiente comprobación
                                // Movemos Palancas Con Un Retardo
                                Bukkit.getScheduler().runTaskLater(this, () -> {
                                    // Movemos Palancas
                                    setLever(inA, false);
                                    setLever(inB, false);
                                    setLever(inC, true);

                                    // Dejamos un tiempo para que se cambien las palancas
                                    Bukkit.getScheduler().runTaskLater(this, () -> {
                                        parte1.getExtra().clear();
                                        parte1.setText("->");

                                        parte1.addExtra("Ci=0 E1=0 E2=1");
                                        parte1.setColor(ChatColor.YELLOW);

                                        Bukkit.getScheduler().runTaskLater(this, () -> {
                                            s1 = (((Lightable) out1.getBlockData()).isLit()) ? true : false;
                                            s2 = (((Lightable) out2.getBlockData()).isLit()) ? true : false;

                                            if (!s1 || s2) {
                                                msg = "S=" + (s1?"1":"0") + "  Co=" + (s2?"1":"0") + " [ERROR]";
                                                parte1.addExtra(msg);
                                                parte1.setColor(ChatColor.RED);
                                                event.getPlayer().spigot().sendMessage(parte1);

                                                parte1.getExtra().clear();

                                                parte1.addExtra("!!CIRCUITO INVALIDO¡¡");
                                                parte1.setColor(ChatColor.LIGHT_PURPLE);
                                                parte1.setBold(true);
                                                event.getPlayer().spigot().sendMessage(parte1);
                                            } else {
                                                parte1.addExtra(" ");
                                                msg = "S=" + (s1?"1":"0") + "  Co=" + (s2?"1":"0") + " [OK]     [25%]";
                                                parte1.addExtra(msg);
                                                parte1.setColor(ChatColor.YELLOW);
                                                event.getPlayer().spigot().sendMessage(parte1);

                                                // Si está bien hacemos siguiente comprobación
                                                // Movemos Palancas Con Un Retardo
                                                Bukkit.getScheduler().runTaskLater(getPluginGabri(), () -> {
                                                    // Movemos Palancas
                                                    setLever(inA, false);
                                                    setLever(inB, true);
                                                    setLever(inC, false);

                                                    // Dejamos un tiempo para que se cambien las palancas
                                                    Bukkit.getScheduler().runTaskLater(this, () -> {
                                                        parte1.getExtra().clear();
                                                        parte1.setText("->");

                                                        parte1.addExtra("Ci=0 E1=1 E2=0");
                                                        parte1.setColor(ChatColor.YELLOW);

                                                        Bukkit.getScheduler().runTaskLater(this, () -> {
                                                            s1 = (((Lightable) out1.getBlockData()).isLit()) ? true : false;
                                                            s2 = (((Lightable) out2.getBlockData()).isLit()) ? true : false;

                                                            if (!s1 || s2) {
                                                                msg = "S=" + (s1?"1":"0") + "  Co=" + (s2?"1":"0") + " [ERROR]";
                                                                parte1.addExtra(msg);
                                                                parte1.setColor(ChatColor.RED);
                                                                event.getPlayer().spigot().sendMessage(parte1);

                                                                parte1.getExtra().clear();

                                                                parte1.addExtra("!!CIRCUITO INVALIDO¡¡");
                                                                parte1.setColor(ChatColor.LIGHT_PURPLE);
                                                                parte1.setBold(true);
                                                                event.getPlayer().spigot().sendMessage(parte1);
                                                            } else {
                                                                parte1.addExtra(" ");
                                                                msg = "S=" + (s1?"1":"0") + "  Co=" + (s2?"1":"0") + " [OK]     [37,5%]";
                                                                parte1.addExtra(msg);
                                                                parte1.setColor(ChatColor.YELLOW);
                                                                event.getPlayer().spigot().sendMessage(parte1);

                                                                // Si está bien hacemos siguiente comprobación
                                                                // Movemos Palancas Con Un Retardo
                                                                Bukkit.getScheduler().runTaskLater(getPluginGabri(), () -> {
                                                                    // Movemos Palancas
                                                                    setLever(inA, false);
                                                                    setLever(inB, true);
                                                                    setLever(inC, true);

                                                                    // Dejamos un tiempo para que se cambien las palancas
                                                                    Bukkit.getScheduler().runTaskLater(this, () -> {
                                                                        parte1.getExtra().clear();
                                                                        parte1.setText("->");

                                                                        parte1.addExtra("Ci=0 E1=1 E2=1");
                                                                        parte1.setColor(ChatColor.YELLOW);

                                                                        Bukkit.getScheduler().runTaskLater(this, () -> {

                                                                            s1 = (((Lightable) out1.getBlockData()).isLit()) ? true : false;
                                                                            s2 = (((Lightable) out2.getBlockData()).isLit()) ? true : false;

                                                                            if (s1 || !s2) {
                                                                                msg = "S=" + (s1?"1":"0") + "  Co=" + (s2?"1":"0") + " [ERROR]";
                                                                                parte1.addExtra(msg);
                                                                                parte1.setColor(ChatColor.RED);
                                                                                event.getPlayer().spigot().sendMessage(parte1);

                                                                                parte1.getExtra().clear();

                                                                                parte1.addExtra("!!CIRCUITO INVALIDO¡¡");
                                                                                parte1.setColor(ChatColor.LIGHT_PURPLE);
                                                                                parte1.setBold(true);
                                                                                event.getPlayer().spigot().sendMessage(parte1);
                                                                            } else {
                                                                                parte1.addExtra(" ");
                                                                                msg = "S=" + (s1 ? "1" : "0") + "  Co=" + (s2 ? "1" : "0") + " [OK]     [50%]";
                                                                                parte1.addExtra(msg);
                                                                                parte1.setColor(ChatColor.YELLOW);
                                                                                event.getPlayer().spigot().sendMessage(parte1);

                                                                                Bukkit.getScheduler().runTaskLater(getPluginGabri(), () -> {
                                                                                    // Movemos Palancas
                                                                                    setLever(inA, true);
                                                                                    setLever(inB, false);
                                                                                    setLever(inC, false);

                                                                                    Bukkit.getScheduler().runTaskLater(this, () -> {
                                                                                        parte1.getExtra().clear();
                                                                                        parte1.setText("->");

                                                                                        parte1.addExtra("Ci=1 E1=0 E2=0");
                                                                                        parte1.setColor(ChatColor.YELLOW);

                                                                                        Bukkit.getScheduler().runTaskLater(this, () -> {

                                                                                            s1 = (((Lightable) out1.getBlockData()).isLit()) ? true : false;
                                                                                            s2 = (((Lightable) out2.getBlockData()).isLit()) ? true : false;

                                                                                            if (!s1 || s2) {
                                                                                                msg = "S=" + (s1 ? "1" : "0") + "  Co=" + (s2 ? "1" : "0") + " [ERROR]";
                                                                                                parte1.addExtra(msg);
                                                                                                parte1.setColor(ChatColor.RED);
                                                                                                event.getPlayer().spigot().sendMessage(parte1);

                                                                                                parte1.getExtra().clear();

                                                                                                parte1.addExtra("!!CIRCUITO INVALIDO¡¡");
                                                                                                parte1.setColor(ChatColor.LIGHT_PURPLE);
                                                                                                parte1.setBold(true);
                                                                                                event.getPlayer().spigot().sendMessage(parte1);
                                                                                            } else {
                                                                                                parte1.addExtra(" ");
                                                                                                msg = "S=" + (s1 ? "1" : "0") + "  Co=" + (s2 ? "1" : "0") + " [OK]     [62,5%]";
                                                                                                parte1.addExtra(msg);
                                                                                                parte1.setColor(ChatColor.YELLOW);
                                                                                                event.getPlayer().spigot().sendMessage(parte1);

                                                                                                Bukkit.getScheduler().runTaskLater(getPluginGabri(), () -> {
                                                                                                    // Movemos Palancas
                                                                                                    setLever(inA, true);
                                                                                                    setLever(inB, false);
                                                                                                    setLever(inC, true);

                                                                                                    Bukkit.getScheduler().runTaskLater(this, () -> {
                                                                                                        parte1.getExtra().clear();
                                                                                                        parte1.setText("->");

                                                                                                        parte1.addExtra("Ci=1 E1=0 E2=1");
                                                                                                        parte1.setColor(ChatColor.YELLOW);

                                                                                                        Bukkit.getScheduler().runTaskLater(this, () -> {

                                                                                                            s1 = (((Lightable) out1.getBlockData()).isLit()) ? true : false;
                                                                                                            s2 = (((Lightable) out2.getBlockData()).isLit()) ? true : false;

                                                                                                            if (s1 || !s2) {
                                                                                                                msg = "S=" + (s1 ? "1" : "0") + "  Co=" + (s2 ? "1" : "0") + " [ERROR]";
                                                                                                                parte1.addExtra(msg);
                                                                                                                parte1.setColor(ChatColor.RED);
                                                                                                                event.getPlayer().spigot().sendMessage(parte1);

                                                                                                                parte1.getExtra().clear();

                                                                                                                parte1.addExtra("!!CIRCUITO INVALIDO¡¡");
                                                                                                                parte1.setColor(ChatColor.LIGHT_PURPLE);
                                                                                                                parte1.setBold(true);
                                                                                                                event.getPlayer().spigot().sendMessage(parte1);
                                                                                                            } else {
                                                                                                                parte1.addExtra(" ");
                                                                                                                msg = "S=" + (s1 ? "1" : "0") + "  Co=" + (s2 ? "1" : "0") + " [OK]     [75%]";
                                                                                                                parte1.addExtra(msg);
                                                                                                                parte1.setColor(ChatColor.YELLOW);
                                                                                                                event.getPlayer().spigot().sendMessage(parte1);

                                                                                                                Bukkit.getScheduler().runTaskLater(getPluginGabri(), () -> {
                                                                                                                    // Movemos Palancas
                                                                                                                    setLever(inA, true);
                                                                                                                    setLever(inB, true);
                                                                                                                    setLever(inC, false);

                                                                                                                    Bukkit.getScheduler().runTaskLater(this, () -> {
                                                                                                                        parte1.getExtra().clear();
                                                                                                                        parte1.setText("->");

                                                                                                                        parte1.addExtra("Ci=1 E1=1 E2=0");
                                                                                                                        parte1.setColor(ChatColor.YELLOW);

                                                                                                                        Bukkit.getScheduler().runTaskLater(this, () -> {

                                                                                                                            s1 = (((Lightable) out1.getBlockData()).isLit()) ? true : false;
                                                                                                                            s2 = (((Lightable) out2.getBlockData()).isLit()) ? true : false;

                                                                                                                            if (s1 || !s2) {
                                                                                                                                msg = "S=" + (s1 ? "1" : "0") + "  Co=" + (s2 ? "1" : "0") + " [ERROR]";
                                                                                                                                parte1.addExtra(msg);
                                                                                                                                parte1.setColor(ChatColor.RED);
                                                                                                                                event.getPlayer().spigot().sendMessage(parte1);

                                                                                                                                parte1.getExtra().clear();

                                                                                                                                parte1.addExtra("!!CIRCUITO INVALIDO¡¡");
                                                                                                                                parte1.setColor(ChatColor.LIGHT_PURPLE);
                                                                                                                                parte1.setBold(true);
                                                                                                                                event.getPlayer().spigot().sendMessage(parte1);
                                                                                                                            } else {
                                                                                                                                parte1.addExtra(" ");
                                                                                                                                msg = "S=" + (s1 ? "1" : "0") + "  Co=" + (s2 ? "1" : "0") + " [OK]     [87,5%]";
                                                                                                                                parte1.addExtra(msg);
                                                                                                                                parte1.setColor(ChatColor.YELLOW);
                                                                                                                                event.getPlayer().spigot().sendMessage(parte1);

                                                                                                                                Bukkit.getScheduler().runTaskLater(getPluginGabri(), () -> {
                                                                                                                                    // Movemos Palancas
                                                                                                                                    setLever(inA, true);
                                                                                                                                    setLever(inB, true);
                                                                                                                                    setLever(inC, true);

                                                                                                                                    Bukkit.getScheduler().runTaskLater(this, () -> {
                                                                                                                                        parte1.getExtra().clear();
                                                                                                                                        parte1.setText("->");

                                                                                                                                        parte1.addExtra("Ci=1 E1=1 E2=1");
                                                                                                                                        parte1.setColor(ChatColor.YELLOW);

                                                                                                                                        Bukkit.getScheduler().runTaskLater(this, () -> {

                                                                                                                                            s1 = (((Lightable) out1.getBlockData()).isLit()) ? true : false;
                                                                                                                                            s2 = (((Lightable) out2.getBlockData()).isLit()) ? true : false;

                                                                                                                                            if (!s1 || !s2) {
                                                                                                                                                msg = "S=" + (s1 ? "1" : "0") + "  Co=" + (s2 ? "1" : "0") + " [ERROR]";
                                                                                                                                                parte1.addExtra(msg);
                                                                                                                                                parte1.setColor(ChatColor.RED);
                                                                                                                                                event.getPlayer().spigot().sendMessage(parte1);

                                                                                                                                                parte1.getExtra().clear();

                                                                                                                                                parte1.addExtra("!!CIRCUITO INVALIDO¡¡");
                                                                                                                                                parte1.setColor(ChatColor.LIGHT_PURPLE);
                                                                                                                                                parte1.setBold(true);
                                                                                                                                                event.getPlayer().spigot().sendMessage(parte1);
                                                                                                                                            } else {
                                                                                                                                                parte1.addExtra(" ");
                                                                                                                                                msg = "S=" + (s1?"1":"0") + "  Co=" + (s2?"1":"0") + " [OK]     [100%]";
                                                                                                                                                parte1.addExtra(msg);
                                                                                                                                                parte1.setColor(ChatColor.YELLOW);
                                                                                                                                                event.getPlayer().spigot().sendMessage(parte1);

                                                                                                                                                parte1.getExtra().clear();
                                                                                                                                                parte1.setText("");
                                                                                                                                                parte1.addExtra("Ejercicio Correcto ");
                                                                                                                                                parte1.setColor(ChatColor.BLUE);
                                                                                                                                                parte1.setBold(true);


                                                                                                                                                parte2.setText("");
                                                                                                                                                parte2.addExtra("[Puerta Abierta]");
                                                                                                                                                parte2.setColor(ChatColor.GOLD);

                                                                                                                                                Block puerta = Bukkit.getWorld(event.getPlayer().getWorld().getName()).getBlockAt(47, -60, -96);
                                                                                                                                                Openable openable = (Openable) puerta.getBlockData();
                                                                                                                                                openable.setOpen(true); // Establecer la puerta como abierta
                                                                                                                                                puerta.setBlockData(openable);

                                                                                                                                                guardarProgreso(event.getPlayer(), 6);

                                                                                                                                                parte1.addExtra(parte2);
                                                                                                                                                event.getPlayer().spigot().sendMessage(parte1);
                                                                                                                                            }
                                                                                                                                        }, 20L);


                                                                                                                                    },10L);


                                                                                                                                }, 20L);
                                                                                                                            }
                                                                                                                        }, 20L);

                                                                                                                    }, 10L);

                                                                                                                },20L);

                                                                                                            }
                                                                                                        },20L);
                                                                                                    },10L);

                                                                                                },20L);

                                                                                            }
                                                                                        },20L);


                                                                                    },10L);
                                                                                }, 20L);
                                                                            }

                                                                        }, 20L);

                                                                    },10L);

                                                                }, 20L);
                                                            }
                                                        }, 10L);

                                                    }, 20L);

                                                }, 20L);

                                            }

                                        }, 10L);

                                    }, 20L);

                                }, 20L);

                            }

                        }, 10L);

                    }, 20L);

                }, 10L);
            }
        }

    }
}