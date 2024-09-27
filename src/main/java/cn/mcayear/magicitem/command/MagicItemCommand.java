package cn.mcayear.magicitem.command;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.SimpleCommand;
import org.allaymc.api.command.tree.CommandTree;
import org.allaymc.api.container.FullContainerType;
import org.allaymc.api.container.impl.PlayerInventoryContainer;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.item.ItemStack;
import org.allaymc.api.server.Server;

import java.util.Collection;
import java.util.HashMap;

import static cn.mcayear.magicitem.MagicItemMain.*;
import static cn.mcayear.magicitem.config.ItemsConfig.ITEMS_MAP;
import static org.allaymc.api.item.type.ItemTypes.WRITTEN_BOOK;

public class MagicItemCommand extends SimpleCommand {

    private HashMap<Integer, Long> useTime = new HashMap<>();

    public MagicItemCommand() {
        super("mi", "魔法物品");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot()
                .key("help")
                .exec(context -> {
                    context.addOutput("magicitem:magicitem.commands.help");
                    context.addOutput("magicitem:magicitem.commands.reload.help");
                    context.addOutput("magicitem:magicitem.commands.add.help");
                    context.addOutput("magicitem:magicitem.commands.give.help");
                    context.addOutput("magicitem:magicitem.commands.show.help");
                    context.addOutput("magicitem:magicitem.commands.sell.help");
                    return context.success();

                });
        tree.getRoot()
                .key("give")
                .playerTarget("player")
                .str("itemName")
                .intNum("count", 1)
                .optional()
                .intNum("quality", -1)
                .optional()
                .exec(context -> {
                    Collection<EntityPlayer> players = context.getResult(1);
                    if (players.isEmpty()) {
                        context.addNoTargetMatchError();
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

                    players.forEach(p -> p.getContainer(FullContainerType.PLAYER_INVENTORY).tryAddItem(itemStack));
                    context.addOutput("成功给予物品");
                    return context.success();
                });
        tree.getRoot()
                .key("show")
                .exec((context, player) -> {
                    long time = System.currentTimeMillis();
                    ItemStack showItem = player.getItemInHand();
                    if (!showItem.getCustomNBTContent().isEmpty()) {
                        Server.getInstance().getOnlinePlayers().values().forEach(p -> {
                            p.sendTr("magicitem:magicitem.usage.showItem", player.getOriginName(), showItem.getCustomName());
                        });
                        return context.success();
                    }
                    if (!this.useTime.containsKey(player.getUUID().hashCode())) {
                        this.useTime.put(player.getUUID().hashCode(), time);
                    } else if ((time - this.useTime.get(player.getUUID().hashCode())) / 1000 < MAIN_CONFIG.getItemDisplayCooldown()) {
                        long seconds = MAIN_CONFIG.getItemDisplayCooldown() - ((time - this.useTime.get(player.getUUID().hashCode())) / 1000);
                        context.addOutput("magicitem:magicitem.usage.showItem.cooldown", seconds);
                        return context.fail();
                    } else {
                        this.useTime.put(player.getUUID().hashCode(), time);
                    }
                    Server.getInstance().getOnlinePlayers().values().forEach(p -> {
                        String itemName = showItem.getCustomName().isEmpty() ? showItem.getItemType().getIdentifier().path() : showItem.getCustomName();
                        if (showItem.getItemType().equals(WRITTEN_BOOK)) {
                            itemName = showItem.getCustomNBTContent().getString("title");
                        }
                        if (showItem.getCount() > 1) {
                            itemName += "§r§f *" + showItem.getCount();
                        }
                        p.sendTr("magicitem:magicitem.usage.showItem", player.getOriginName(), itemName);
                    });
                    return context.success();
                }, SenderType.PLAYER);
        tree.getRoot()
                .key("sell")
                .exec((context, player) -> {
                    PlayerInventoryContainer bag = player.getContainer(FullContainerType.PLAYER_INVENTORY);
                    double total = 0.0d;
                    for (int slot = 0; slot < bag.getContainerType().size(); slot++) {
                        ItemStack itemStack = bag.getItemStack(slot);
                        if (!itemStack.getCustomNBTContent().isEmpty()) {
                            continue;
                        }
                        if (ITEMS_MAP.containsKey(itemStack.getCustomNBTContent().getString("yamlName"))) {
                            double price = itemStack.getCustomNBTContent().getDouble("price");
                            if (price != 0.0d) {
                                double money = ((double) itemStack.getCount()) * price;
                                // EconomyAPI.getInstance().addMoney(player, money);
                                bag.clearSlot(slot);
                                total += money;
                            }
                        }
                    }
                    context.addOutput("magicitem:magicitem.usage.sell.success", total);
                    return context.success();
                }, SenderType.PLAYER);
    }
}