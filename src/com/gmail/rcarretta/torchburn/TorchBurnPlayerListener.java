package com.gmail.rcarretta.torchburn;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

class TorchBurnPlayerListener extends PlayerListener {

    public TorchBurnPlayerListener() {
    }

    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (TorchBurn.updatePlayerLoc(player)) {
            TorchBurn.lightArea(player, TorchBurn.intensity, TorchBurn.falloff); // values for a torch
        }
    }

    @Override
    public void onItemHeldChange(PlayerItemHeldEvent event) {
        if (TorchBurn.isLit(event.getPlayer())) {
            TorchBurn.extinguish(event.getPlayer(), event.getPreviousSlot());
        }
    }

    @Override
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (TorchBurn.isLit(event.getPlayer())) {
            if (event.getItemDrop().getItemStack().getType() == Material.TORCH) {
                TorchBurn.extinguishNoRemove(event.getPlayer());
                event.getItemDrop().getItemStack().setDurability((short) 0);
                if (event.getItemDrop().getItemStack().getAmount() <= 1) {
                    event.getItemDrop().remove();
                } else {
                    event.getItemDrop().getItemStack().setAmount(event.getItemDrop().getItemStack().getAmount() - 1);
                }
            }
        }
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {

        if (event.getAction() == Action.LEFT_CLICK_AIR
                || event.getAction() == Action.RIGHT_CLICK_AIR) {
            // if not player already has lit torch, is holding a torch, and is holding shift
            if (!TorchBurn.isLit(event.getPlayer())
                    && event.getPlayer().getInventory().getItemInHand().getType() == Material.TORCH
                    && event.getPlayer().isSneaking()) {
                TorchBurn.lightTorch(event.getPlayer());
            }
        }
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (TorchBurn.isLit(event.getPlayer())) {
            TorchBurn.extinguish(event.getPlayer());
        }
    }
}
