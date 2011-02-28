package com.gmail.rcarretta.torchburn;

import org.bukkit.entity.Player;

public class TorchBurnLightLevelOwner {
	private Player owner;
	private Integer level;
	
	TorchBurnLightLevelOwner(Player newOwner, Integer newLevel) {
		owner = newOwner;
		level = newLevel;
	}
	
	public Player getPlayer() { return owner; }
	public Integer getLevel() { return level; }
}