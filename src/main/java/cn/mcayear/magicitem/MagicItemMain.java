package cn.mcayear.magicitem;

import cn.mcayear.magicitem.command.MagicItemCommand;
import lombok.extern.slf4j.Slf4j;
import org.allaymc.api.plugin.Plugin;
import org.allaymc.api.registry.Registries;

@Slf4j
public class MagicItemMain extends Plugin {
    @Override
    public void onLoad() {
        log.info("MagicItem loaded!");
        Registries.COMMANDS.register(new MagicItemCommand());
    }

    @Override
    public void onEnable() {
        log.info("MagicItem enabled!");
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
    }
}