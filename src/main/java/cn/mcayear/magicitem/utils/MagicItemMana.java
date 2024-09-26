package cn.mcayear.magicitem.utils;

import cn.mcayear.magicitem.MagicItemMain;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class MagicItemMana {
    private Map<String, Integer> mana;

    public MagicItemMana(Map<String, Integer> mana) {
        this.mana = mana;
    }

    public String replaceAttrTemplate(String str) {
        Pattern pattern = Pattern.compile("\\{\\((.*?)\\)\\}");
        Matcher matcher = pattern.matcher(str);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String replacement = String.valueOf(this.mana.get(matcher.group(1)));// 魔素值
            matcher.appendReplacement(sb, replacement);
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    public static Map<String, Integer> readConfig(String name, Map<String, Object> tempMap) {
        Map<String, Integer> convertedMap = new HashMap<>();
        try {
            for (Map.Entry<String, Object> entry : tempMap.entrySet()) {
                if (entry.getValue() instanceof Integer) {
                    convertedMap.put(entry.getKey(), (Integer) entry.getValue());
                } else {
                    // 这里处理不能直接转换为Integer的情况，比如尝试转换字符串数字或者提供一个默认值/抛出异常
                    String valueStr = entry.getValue().toString();
                    try {
                        convertedMap.put(entry.getKey(), Integer.parseInt(valueStr));
                    } catch (NumberFormatException e) {
                        log.error("at " + name + " file, Value for key '" + entry.getKey() + "' cannot be converted to Integer: " + e.getMessage());
                        // 根据情况决定是否继续循环或抛出异常等
                        // convertedMap.put(entry.getKey(), 0); // 如果决定赋予默认值0
                    }
                }
            }
        } catch (Exception e) {
            // 处理可能的其他异常
            log.error(name + " 文件无法读取`魔素`");
            e.printStackTrace();
        }
        return convertedMap;
    }
}
