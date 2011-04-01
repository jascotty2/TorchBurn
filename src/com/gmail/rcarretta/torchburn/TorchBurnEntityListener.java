package com.gmail.rcarretta.torchburn;

import com.jascotty2.Rand;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;

class TorchBurnEntityListener extends EntityListener {

    @Override
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            if (TorchBurn.isLit((Player) event.getEntity())) {
                TorchBurn.extinguish((Player) event.getEntity());
            }
        }
    }

    @Override
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getCause() == DamageCause.ENTITY_ATTACK
                && ((EntityDamageByEntityEvent) event).getDamager() instanceof Player) {
            if (TorchBurn.isLit((Player) ((EntityDamageByEntityEvent) event).getDamager())) {
                TorchBurn.extinguish((Player) ((EntityDamageByEntityEvent) event).getDamager());
                event.getEntity().setFireTicks(Rand.RandomInt(60, 300));
            }
        }
    }
}
