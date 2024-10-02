package cn.mcayear.magicitem;

import cn.mcayear.magicitem.command.MagicItemCommand;
import cn.mcayear.magicitem.config.ItemsConfig;
import cn.mcayear.magicitem.config.MainConfig;
import cn.mcayear.magicitem.event.NormalEventListener;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.allaymc.api.plugin.Plugin;
import org.allaymc.api.registry.Registries;
import org.allaymc.api.server.Server;

@Slf4j
public class MagicItemMain extends Plugin {

    @Getter
    private static Plugin instance;

    public static MainConfig MAIN_CONFIG;

    @Override
    public void onLoad() {
        log.info("MagicItem loaded!");
        // save Plugin Instance
        instance = this;

        // register the command of plugin
        Registries.COMMANDS.register(new MagicItemCommand());

        // init the config of plugin
        ItemsConfig.init();
        MAIN_CONFIG = new MainConfig();
    }

    @Override
    public void onEnable() {
        log.info("MagicItem enabled!");
        Server.getInstance().getEventBus().registerListener(new NormalEventListener());
    }

    @Override
    public void onDisable() {
        log.info("MagicItem disabled!");
    }

    @Override
    public boolean isReloadable() {
        return true;
    }

    @Override
    public void reload() {
        log.info("MagicItem reloaded!");
        Registries.COMMANDS.unregister("mi");
        Registries.COMMANDS.register(new MagicItemCommand());
        ItemsConfig.init();
        MAIN_CONFIG = new MainConfig();
        // Server.getInstance().getOnlinePlayers().values().forEach(UpdateBackupItem::update);
    }
}