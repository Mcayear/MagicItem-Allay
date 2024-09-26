package cn.mcayear.magicitem.config;

import cn.mcayear.magicitem.MagicItemMain;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.allaymc.api.utils.config.Config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Slf4j
@Getter
public class MainConfig {
    private final Config config;

    @Setter
    private boolean globalItemCooldown;

    @Setter
    private int itemDisplayCooldown;

    @Setter
    private QualityConfig qualityConfig;

    @Setter
    private List<String> restrictedWorlds;

    public MainConfig() {
        File file = new File(MagicItemMain.getInstance().getPluginContainer().dataFolder().toFile(), "config.yml");

        if (!file.exists()) {
            try (InputStream in = MagicItemMain.class.getClassLoader().getResourceAsStream("config.yml")) {
                if (in == null) {
                    throw new IOException("默认配置文件未找到");
                }
                Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                file = new File(MagicItemMain.getInstance().getPluginContainer().dataFolder().toFile(), "config.yml");
            } catch (IOException e) {
                log.error("保存默认配置文件时出错: " + e.getMessage(), e);
            }
        }

        config = new Config(file, Config.YAML);

        globalItemCooldown = config.getBoolean("globalItemCooldown", false);
        itemDisplayCooldown = config.getInt("itemDisplayCooldown", 15);

        qualityConfig = new QualityConfig(config);

        // 读取禁用世界
        restrictedWorlds = config.getStringList("restrictedWorlds");
    }

    public void save() {
        config.save();
    }

    @Getter
    public static class QualityConfig {
        private final List<String> names;
        private final List<Double> probabilities;
        private final List<Double> multipliers;

        public QualityConfig(Config config) {
            names = config.getStringList("quality.list");
            probabilities = config.getDoubleList("quality.p");
            multipliers = config.getDoubleList("quality.m");

            double sum = probabilities.stream().mapToDouble(Double::doubleValue).sum();
            if (sum != 1.0) {
                throw new IllegalArgumentException("config.yml 文件 quality.list 概率总和必须为 1 当前值: "+sum);
            }
        }
    }
}
