package com.songoda.epicvouchers.voucher;

import com.mysql.jdbc.Buffer;
import com.songoda.epicvouchers.EpicVouchers;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class ClickListener implements Listener {

    private final EpicVouchers instance;

    public ClickListener(EpicVouchers instance) {
        this.instance = instance;
    }

    @EventHandler
    public void voucherListener(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        for (Voucher voucher : instance.getVoucherManager().getVouchers()) {
            Player player = event.getPlayer();
            if (!player.hasPermission(voucher.getPermission())) {
                return;
            }
            ItemStack item = event.getPlayer().getItemInHand();
            if (item.getType() != voucher.getMaterial() || item.getDurability() != voucher.getData()) {
                return;
            }

            ItemMeta meta = item.getItemMeta();
            if (!meta.hasDisplayName() || !meta.getDisplayName().equals(voucher.getName()) || !meta.getLore().equals(voucher.getLore())) {
                return;
            }
            UUID uuid = player.getUniqueId();
            if (!instance.getCooldowns().getEntries().containsKey(uuid)) {
                if (voucher.isConfirm()) {
                    EpicVouchers.getInstance().getConfirmation().confirmVoucher(player, voucher, item);
                } else {
                    VoucherExecutor.redeemVoucher(player, voucher, item, true);
                }
                event.setCancelled(true);
            } else {
                String message = instance.getLocale().getMessage("event.general.cooldown", String.valueOf(instance.getCooldowns().getEntries().get(uuid) + 1), voucher.getName());
                player.sendMessage(message);
            }
        }
    }
}