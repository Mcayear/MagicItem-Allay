package cn.mcayear.magicitem.event;

import cn.mcayear.magicitem.utils.UpdateBackupItem;
import org.allaymc.api.eventbus.EventHandler;
import org.allaymc.api.eventbus.event.player.PlayerJoinEvent;

public class NormalEventListener {
    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        UpdateBackupItem.update(event.getPlayer());
    }
}