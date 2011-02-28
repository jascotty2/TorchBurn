package com.gmail.rcarretta.torchburn;

import org.bukkit.Material;
import org.bukkit.entity.Player;


class TorchBurnSchedule implements Runnable {
	Player player;
	
	TorchBurnSchedule(Player newPlayer) { this.player = newPlayer; }
	
	@Override
	public void run() {
		if ( TorchBurn.isLit(player) ) {
			if ( player.getInventory().getItemInHand().getType() == Material.TORCH ) {
				player.getInventory().getItemInHand().setDurability((short)(player.getInventory().getItemInHand().getDurability()+1));
				if ( player.getInventory().getItemInHand().getDurability() >= 32 ) {
					TorchBurn.extinguish(player);
					// remove the torch from the player's inventory and return light levels
				}
				else {
					TorchBurn.myPlugin.getServer().getScheduler().scheduleSyncDelayedTask(TorchBurn.myPlugin, new TorchBurnSchedule(player), 40);
				} 	
			}
		}
	}
}