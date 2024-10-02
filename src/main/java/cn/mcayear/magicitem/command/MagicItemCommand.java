package cn.mcayear.magicitem.command;

import cn.mcayear.magicitem.MagicItemMain;
import lombok.extern.slf4j.Slf4j;
import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.SimpleCommand;
import org.allaymc.api.command.tree.CommandTree;
import org.allaymc.api.container.FullContainerType;
import org.allaymc.api.container.impl.PlayerInventoryContainer;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.item.ItemStack;
import org.allaymc.api.item.type.ItemType;
import org.allaymc.api.server.Server;
import org.allaymc.api.utils.config.Config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static cn.mcayear.magicitem.MagicItemMain.*;
import static cn.mcayear.magicitem.config.ItemsConfig.ITEMS_MAP;
import static org.allaymc.api.item.type.ItemTypes.WRITTEN_BOOK;

@Slf4j
public class MagicItemCommand extends SimpleCommand {

    private final HashMap<Integer, Long> useTime = new HashMap<>();

    public MagicItemCommand() {
        super("mi", "magicitem:commands.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot()
                .key("help")
                .exec((context, sender) -> {
                    context.addOutput("magicitem:commands.help");
                    context.addOutput("magicitem:commands.reload.help");
                    context.addOutput("magicitem:commands.create.help");
                    context.addOutput("magicitem:commands.give.help");
                    context.addOutput("magicitem:commands.show.help");
                    context.addOutput("magicitem:commands.sell.help");
                    return context.success();
                }, SenderType.ANY);
        tree.getRoot()
                .key("create")
                .str("name")
                .itemType("itemName")
                .exec(context -> {
                    var name = context.getResult(1);
                    ItemType<?> itemType = context.getResult(2);
                    Path itemPath = Paths.get(MagicItemMain.getInstance().getPluginContainer().dataFolder().toString(), "items", name+".yml");
                    if (Files.exists(itemPath)) {
                        context.addError("magicitem:commands.create.error", name);
                        return context.fail();
                    }

                    try (InputStream in = MagicItemMain.class.getClassLoader().getResourceAsStream("items/example.yml")) {
                        if (in == null) {
                            throw new IOException("资源 items/example.yml 未找到");
                        }
                        Files.copy(in, itemPath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        log.error("保存默认配置文件时出错: {}", e.getMessage(), e);
                    }

                    var cfg = new Config(itemPath.toFile(), Config.YAML);
                    LinkedHashMap<String, Object> cfgSection = new LinkedHashMap<>(cfg.getAll());
                    cfgSection.put("showName", name);
                    cfgSection.put("namespaceId", itemType.getIdentifier().namespace());
                    cfg.setAll(cfgSection);
                    cfg.save();

                    context.addOutput("magicitem:commands.create.success", name);
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
                        context.addError("magicitem:commands.give.error ", itemName);
                        return context.fail();
                    }
                    int count = context.getResult(3);
                    int quality = context.getResult(4);
                    ItemStack itemStack = ITEMS_MAP.get(itemName).toItem(quality);
                    itemStack.setCount(count);

                    players.forEach(p -> p.getContainer(FullContainerType.PLAYER_INVENTORY).tryAddItem(itemStack));
                    context.addOutput("magicitem:commands.give.success", itemName);
                    return context.success();
                });
        tree.getRoot()
                .key("show")
                .exec((context, player) -> {
                    long time = System.currentTimeMillis();
                    ItemStack showItem = player.getItemInHand();
                    if (!showItem.getCustomNBTContent().isEmpty()) {
                        Server.getInstance().getOnlinePlayers().values()
                                .forEach(p -> p.sendTr("magicitem:usage.showItem", player.getOriginName(), showItem.getCustomName()));
                        return context.success();
                    }
                    if (!this.useTime.containsKey(player.getUUID().hashCode())) {
                        this.useTime.put(player.getUUID().hashCode(), time);
                    } else if ((time - this.useTime.get(player.getUUID().hashCode())) / 1000 < MAIN_CONFIG.getItemDisplayCooldown()) {
                        long seconds = MAIN_CONFIG.getItemDisplayCooldown() - ((time - this.useTime.get(player.getUUID().hashCode())) / 1000);
                        context.addOutput("magicitem:usage.showItem.cooldown", seconds);
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
                        p.sendTr("magicitem:usage.showItem", player.getOriginName(), itemName);
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
                    context.addOutput("magicitem:usage.sell.success", total);
                    return context.success();
                }, SenderType.PLAYER);
    }
}