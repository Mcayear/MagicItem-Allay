package cn.mcayear.magicitem.config;

import cn.mcayear.magicitem.MagicItemMain;
import cn.mcayear.magicitem.bean.ItemBean;
import org.allaymc.api.utils.config.Config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class ItemsConfig {

    public static final HashMap<String, ItemBean> ITEMS_MAP = new HashMap<>();

    public static void init() {
        ITEMS_MAP.clear();
        Path itemsPath = Paths.get(MagicItemMain.getInstance().getPluginContainer().dataFolder().toString(), "items");
        if (Files.notExists(itemsPath)) {
            try {
                Files.createDirectories(itemsPath);
            } catch (IOException e) {
                MagicItemMain.getInstance().getPluginLogger().error("无法创建 ./plugins/MagicItem/items 文件夹");
                return;
            }
        }

        loadAll(itemsPath);
    }

    public static void loadAll(Path itemsPath) {
        try {
            Files.list(itemsPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".yml"))
                    .forEach(path -> {
                        String fileName = path.getFileName().toString().substring(0, path.getFileName().toString().lastIndexOf('.'));
                        ITEMS_MAP.put(fileName, new ItemBean(fileName, new Config(path.toFile(), Config.YAML)));
                    });
        } catch (IOException e) {
            MagicItemMain.getInstance().getPluginLogger().error("加载 YAML 文件时出错: " + e.getMessage());
        }
    }
}
