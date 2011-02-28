package com.gmail.rcarretta.torchburn;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.PlayerInventory;

class TorchBurnPlayerListener extends PlayerListener {
	public TorchBurnPlayerListener () { }
	
	@Override
	public void onPlayerMove (PlayerMoveEvent event) {
			Player player = event.getPlayer();

			if ( TorchBurn.updatePlayerLoc(player) ) {
				TorchBurn.lightArea(player, TorchBurn.intensity, TorchBurn.falloff); // values for a torch
			}
	}
		
	@Override
	public void onItemHeldChange (PlayerItemHeldEvent event) {
		if ( TorchBurn.isLit(event.getPlayer())) {
			TorchBurn.extinguish(event.getPlayer(), event.getPreviousSlot());
		}
	}
	
	@Override
	public void onPlayerDropItem (PlayerDropItemEvent event) {
		if ( TorchBurn.isLit(event.getPlayer())) {
			if ( event.getItemDrop().getItemStack().getType() == Material.TORCH ) {
				TorchBurn.extinguishNoRemove(event.getPlayer());
				event.getItemDrop().getItemStack().setDurability((short)0);
				if ( event.getItemDrop().getItemStack().getAmount() <= 1 ) {
					event.getItemDrop().remove();
				}
				else {
					event.getItemDrop().getItemStack().setAmount(event.getItemDrop().getItemStack().getAmount()-1);
				}
			}
		}
	}
		
	@Override
	public void onPlayerItem (PlayerItemEvent event) {
		// check if a torch to light
		if ( TorchBurn.isLit(event.getPlayer()) ) {
			// player already has lit torch
			return;
		}
		PlayerInventory inv = event.getPlayer().getInventory();
		if ( inv.getItemInHand().getType() == Material.TORCH ) {
			TorchBurn.lightTorch(event.getPlayer());
		}
	}
		
	@Override
	public void onPlayerQuit (PlayerEvent event) {
		if ( TorchBurn.isLit(event.getPlayer()) ) {
			TorchBurn.extinguish(event.getPlayer());
		}
	}
}