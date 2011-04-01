package com.gmail.rcarretta.torchburn;

import org.bukkit.Material;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

class TorchBurnBlockListener extends BlockListener {

    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        if (TorchBurn.isLit(event.getPlayer()) && event.getItemInHand().getType() == Material.TORCH) {
            event.setCancelled(true);
        }
    }
}
