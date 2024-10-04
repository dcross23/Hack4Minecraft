package com.briangershon.demo;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import net.md_5.bungee.api.ChatColor;


public class HologramaManager {

    private Hologram holograma;
    private Plugin plugin;


     // Crear una lista de ejemplos
    List<Map.Entry<String, Integer>> lista = new ArrayList<>();
    

    public void crearHolograma(String name, Location location, Plugin plugin){
       
        List<String> lines = new ArrayList<>();
        for (int i=0;i<25;i++){
            lines.add(" ");
        }

        holograma = DHAPI.createHologram(name,location,lines);
        this.plugin = plugin;
        

    }


    public int obtenerNivel(UUID jugador) {
        FileConfiguration config = plugin.getConfig();
        String path = "jugadores." + jugador.toString() + ".level"; // Ruta para los puntos del jugador
        return config.getInt(path, 0); // Obtener los puntos (por defecto 0 si no hay datos)
    }


    
    // Método para actualizar el texto del holograma
    public void actualizarHolograma() {
        lista.clear();
        

        // Recorrer todos los jugadores online
        for (Player jugadorOnline : Bukkit.getOnlinePlayers()) {
            // Ejecutar alguna acción con cada jugador online
            lista.add(new AbstractMap.SimpleEntry<>(jugadorOnline.getName(), obtenerNivel(jugadorOnline.getUniqueId())));
        }

        // Recorrer todos los jugadores offline
        for (OfflinePlayer jugadorOffline : Bukkit.getOfflinePlayers()) {
            // Verificar si el jugador está desconectado
            if (!jugadorOffline.isOnline()) {
                // Ejecutar alguna acción con cada jugador offline
                obtenerNivel(jugadorOffline.getUniqueId());
                lista.add(new AbstractMap.SimpleEntry<>(jugadorOffline.getName(),  obtenerNivel(jugadorOffline.getUniqueId())));
            }
        }

        //Ordenamos la Lista por Niveles
        lista.sort(Comparator.<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue).reversed());

        

        if (holograma!=null) {
            DHAPI.getHologramPage(holograma, 0).setLine(0, "§a👑" + ChatColor.RED + " Clasificación");
            int i=0;
            for (Map.Entry<String, Integer> entry : lista) {
                i++;
                DHAPI.getHologramPage(holograma,0).setLine(i, ChatColor.BLUE + "#" + i + "# " + ChatColor.GOLD +  entry.getKey() +  ChatColor.DARK_RED  + " "+ entry.getValue());
            }
            /*
            DHAPI.getHologramPage(holograma,0).setLine(1,  nuevoTexto);
            DHAPI.getHologramPage(holograma,0).setLine(2,  nuevoTexto);
            DHAPI.getHologramPage(holograma,0).setLine(3,  nuevoTexto);
            DHAPI.getHologramPage(holograma,0).setLine(4,  nuevoTexto);
            DHAPI.getHologramPage(holograma,0).setLine(5,  nuevoTexto);
            DHAPI.getHologramPage(holograma,0).setLine(6,  nuevoTexto);

            DHAPI.getHologramPage(holograma,0).setLine(7,  nuevoTexto);
            DHAPI.getHologramPage(holograma,0).setLine(8,  nuevoTexto);
            DHAPI.getHologramPage(holograma,0).setLine(9,  nuevoTexto);
            DHAPI.getHologramPage(holograma,0).setLine(10,  nuevoTexto);
            DHAPI.getHologramPage(holograma,0).setLine(11,  nuevoTexto);
            DHAPI.getHologramPage(holograma,0).setLine(12,  nuevoTexto);
            DHAPI.getHologramPage(holograma,0).setLine(13,  nuevoTexto);
            DHAPI.getHologramPage(holograma,0).setLine(14,  nuevoTexto);
            DHAPI.getHologramPage(holograma,0).setLine(15,  nuevoTexto);
            */
        }
        
    }
}