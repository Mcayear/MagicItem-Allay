package cn.mcayear.magicitem.command;

import cn.mcayear.magicitem.MagicItemMain;
import org.allaymc.api.command.SimpleCommand;
import org.allaymc.api.command.tree.CommandTree;
import org.allaymc.api.container.FullContainerType;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.i18n.TrKeys;
import org.allaymc.api.item.ItemStack;
import org.allaymc.api.server.Server;

import static cn.mcayear.magicitem.config.ItemsConfig.ITEMS_MAP;

public class MagicItemCommand extends SimpleCommand {

    public MagicItemCommand() {
        super("mi", "魔法物品");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot()
                .key("help")
                .exec(context -> {
                    context.addOutput(
                            "§7------ MagicItem Help ------\n" +
                                    "§2/mi reload:§f 重载配置文件\n" +
                                    "§2/mi add <String: itemName> <String: itemId>:§f 添加（创建）新的魔法物品\n" +
                                    "§2/mi give <Player: player> <String: itemName> [Int: count] [Int: quality]:§f 给予玩家指定的魔法物品\n" +
                                    "§2/mi show:§f 展示手中物品\n" +
                                    "§2/mi sell:§f 出售背包的魔法物品"
                    );
                    return context.success();

                });
        tree.getRoot()
                .key("give")
                .str("playerName")
                .str("itemName")
                .intNum("count", 1)
                .optional()
                .intNum("quality", -1)
                .optional()
                .exec(context -> {
                    String playerName = context.getResult(1);
                    EntityPlayer player = Server.getInstance().findOnlinePlayerByName(playerName);
                    if (player == null) {
                        context.addError(playerName);
                        return context.fail();
                    }
                    String itemName = context.getResult(2);
                    if (!ITEMS_MAP.containsKey(itemName)) {
                        context.addError("不存在物品 " + itemName);
                        return context.fail();
                    }
                    int count = context.getResult(3);
                    int quality = context.getResult(4);
                    ItemStack itemStack = ITEMS_MAP.get(itemName).toItem(quality);
                    itemStack.setCount(count);

                    player.getContainer(FullContainerType.PLAYER_INVENTORY).tryAddItem(itemStack);
                    context.addOutput("成功给予物品");
                    return context.success();
                });
    }
}