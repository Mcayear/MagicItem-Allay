package cn.mcayear.magicitem.utils;

import cn.mcayear.magicitem.bean.ItemBean;
import org.allaymc.api.container.FullContainerType;
import org.allaymc.api.container.impl.PlayerInventoryContainer;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.item.ItemStack;
import org.allaymc.api.utils.TextFormat;
import org.cloudburstmc.nbt.NbtMap;

import static cn.mcayear.magicitem.bean.ItemBean.createItem;
import static cn.mcayear.magicitem.config.ItemsConfig.ITEMS_MAP;

public class UpdateBackupItem {
    public static void update(EntityPlayer player) {
        if (player == null) {
            return;
        }

        PlayerInventoryContainer bag = player.getContainer(FullContainerType.PLAYER_INVENTORY);

        for (int slot = 0; slot < bag.getContainerType().size(); slot++) {
            ItemStack itemStack = bag.getItemStack(slot);

            if (!itemStack.getCustomNBTContent().isEmpty()) {
                continue;
            }

            NbtMap tag = itemStack.getCustomNBTContent();
            if (!tag.containsKey("yamlName")) {
                continue;
            }
            if (!tag.containsKey("sell")) {
                continue;
            }

            String yamlName = tag.getString("yamlName");
            if (!ITEMS_MAP.containsKey(yamlName)) {
                bag.clearSlot(slot);
                player.notifySlotChange(bag, slot);
                player.sendText(TextFormat.YELLOW + "魔法物品配置文件不存在...已自动删除");
                continue;
            }
            ItemBean mapItem = ITEMS_MAP.get(yamlName);
            if (!mapItem.attr.isEmpty()) {
                continue;
            }

            int qualityIndex = tag.getInt("quality");

            ItemStack newItem = createItem(mapItem, qualityIndex);
            if (!itemStack.equals(newItem)) {
                newItem.setCount(itemStack.getCount());
                bag.setItemStack(slot, newItem);
                player.notifySlotChange(bag, slot);
            }
        }
    }

}
