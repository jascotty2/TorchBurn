package com.gmail.rcarretta.torchburn;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;

class TorchBurnEntityListener extends EntityListener {

	@Override
	public void onEntityDeath (EntityDeathEvent event) {
		if ( event.getEntity() instanceof Player ) {
			if ( TorchBurn.isLit((Player)event.getEntity())) {
				System.out.println("player death");
					TorchBurn.extinguish((Player)event.getEntity());
			}
		}
	}

	public void onEntityDamageByEntity (EntityDamageByEntityEvent event) {
		if ( event.getDamager() instanceof Player) {
			if ( TorchBurn.isLit((Player)event.getDamager()) ) {
				System.out.println("Player attack");
				TorchBurn.extinguish((Player)event.getDamager());
				event.getEntity().setFireTicks(120);
			}
		}
	}
}