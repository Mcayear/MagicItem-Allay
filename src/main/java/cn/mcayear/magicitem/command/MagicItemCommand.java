package cn.mcayear.magicitem.command;

import org.allaymc.api.command.SimpleCommand;
import org.allaymc.api.command.tree.CommandTree;
import org.allaymc.api.i18n.TrKeys;
import org.allaymc.api.server.Server;

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
                    String playerName = context.getResult(0);
                    var player = Server.getInstance().findOnlinePlayerByName(playerName);
                    if (player == null) {
                        context.addError("%" + TrKeys.M_COMMANDS_GENERIC_PLAYER_NOTFOUND);
                        return context.fail();
                    }
                    context.addOutput("成功给予物品");
                    return context.success();

                });
    }
}