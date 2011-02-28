package com.gmail.rcarretta.torchburn;

import org.bukkit.Location;

public class TorchBurnSimplePlayerLoc {
	private int x;
	private int y;
	private int z;
	
	TorchBurnSimplePlayerLoc(int newX, int newY, int newZ) {
		this.x = newX;
		this.y = newY;
		this.z = newZ;
	}
	
	public boolean equals (Location loc) {
		return (loc.getBlockX() == x && loc.getBlockY() == y & loc.getBlockZ() == z);
	}
	
	public void set(Location loc) {
		x = loc.getBlockX();
		y = loc.getBlockY();
		z = loc.getBlockZ();
	}
}