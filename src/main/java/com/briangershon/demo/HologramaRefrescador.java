package com.briangershon.demo;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class HologramaRefrescador extends BukkitRunnable {

    private final HologramaManager hologramaManager;
    private final JavaPlugin plugin;

    // Constructor que recibe el administrador del holograma y el plugin principal
    public HologramaRefrescador(HologramaManager hologramaManager, JavaPlugin plugin) {
        this.hologramaManager = hologramaManager;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        // Aquí puedes actualizar el texto del holograma con la información que desees
        if (hologramaManager!=null)
        hologramaManager.actualizarHolograma(); // Actualiza el holograma con el nuevo texto
    }

    // Método para iniciar el refresco periódico
    public void iniciarRefresco() {
        this.runTaskTimer(plugin, 0, 20L); // Refresca el holograma cada segundo (20 ticks = 1 segundo)
    }
}