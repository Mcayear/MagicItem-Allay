package cn.mcayear.magicitem.bean;

import cn.mcayear.magicitem.utils.MagicItemMana;
import lombok.Getter;
import org.allaymc.api.item.ItemStack;
import org.allaymc.api.item.enchantment.EnchantmentType;
import org.allaymc.api.item.type.ItemTypeSafeGetter;
import org.allaymc.api.item.type.ItemTypes;
import org.allaymc.api.registry.Registries;
import org.allaymc.api.utils.config.Config;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.cloudburstmc.nbt.NbtType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class ItemBean {
    private Config config;
    private String yamlName;
    private String showName;
    private String namespaceId;
    private int aux;
    private List<String> effect;
    private List<String> groupEffect;
    private int effectRange;
    private boolean thunder;
    private List<String> opCmd;
    private List<String> playerCmd;
    private List<String> lore;
    private boolean consume;
    private int cooldown;
    private String playerMsg;
    private String broadcastMsg;
    private List<String> ench;
    private String job;
    private double sellPrice;

    public Map<String, Object> attr;
    public Map<String, Integer> mana;

    public ItemBean(String yamlName, Config config) {
        this.yamlName = yamlName;
        this.config = config;
        init();
    }

    private void init() {
        this.showName = this.config.getString("showName");
        this.namespaceId = this.config.getString("namespaceId");
        this.aux = this.config.getInt("aux", 0);
        this.effect = this.config.getStringList("effect");
        this.groupEffect = this.config.getStringList("groupEffect");
        this.effectRange = this.config.getInt("effectRange");// 效果范围
        this.thunder = this.config.getBoolean("thunder", false);// 会雷击
        this.opCmd = this.config.getStringList("opCmd");
        this.playerCmd = this.config.getStringList("playerCmd");
        this.lore = this.config.getStringList("lore");
        this.consume = this.config.getBoolean("consume", false); // 是一次性消耗品
        this.cooldown = this.config.getInt("cooldown", 0); // 冷却 单位秒
        this.playerMsg = this.config.getString("playerMsg"); // 获取时的提示（仅玩家个人）
        this.broadcastMsg = this.config.getString("broadcastMsg"); // 获取时的全服提示
        this.ench = this.config.getStringList("enchantments"); // 物品附魔列表
        this.job = this.config.getString("jobRestriction"); // 职业限制
        this.sellPrice = this.config.getDouble("sellPrice", 0); // 出售价格

        this.attr = this.config.getSection("attributes").getAllMap(); // 物品的属性
        this.mana = MagicItemMana.readConfig(this.yamlName, this.config.getSection("mana").getAllMap()); // 物品的魔素
    }

    public ItemStack toItem() {
        return toItem(-1);
    }

    public ItemStack toItem(int quality) {
        return ItemBean.createItem(this, -1);
    }

    /**
     * 创建物品
     *
     * @param itemBean     ItemBean 对象
     * @param qualityIndex 这里是品质的索引从 0 开始，若没有可传入 -1
     * @return 物品堆，若物品不存在则返回空气物品堆
     */
    public static ItemStack createItem(ItemBean itemBean, int qualityIndex) {
        ItemStack AIR_ITEM = ItemTypes.AIR.createItemStack();

        ItemStack item = ItemTypeSafeGetter.name(itemBean.getNamespaceId()).itemType().createItemStack();

        if (AIR_ITEM.equals(item)) {
            return AIR_ITEM;
        }

        item.setMeta(itemBean.getAux());
        item.setCustomName(itemBean.getShowName());

        NbtMapBuilder tag = item.getCustomNBTContent().toBuilder();
        tag.putString("yamlName", itemBean.getYamlName());
        tag.putList("effect", NbtType.STRING, itemBean.getEffect().toArray(new String[0]));
        tag.putList("groupEffect", NbtType.STRING, itemBean.getGroupEffect().toArray(new String[0]));
        tag.putInt("range", itemBean.getEffectRange());
        tag.putBoolean("thunder", itemBean.isThunder());
        tag.putList("pCmd", NbtType.STRING, itemBean.getPlayerCmd().toArray(new String[0]));
        tag.putList("opCmd", NbtType.STRING, itemBean.getOpCmd().toArray(new String[0]));
        tag.putString("pMsg", itemBean.getPlayerMsg());
        tag.putString("bMsg", itemBean.getBroadcastMsg());
        tag.putBoolean("consume", itemBean.isConsume());
//        tag.putString("job", itemBean.getJob());
        tag.putInt("cooldown", itemBean.getCooldown());
        tag.putDouble("price", itemBean.getSellPrice());
        item.setCustomNBTContent(tag.build());

        List<String> loreList = new ArrayList<>(itemBean.getLore());
/*
//        if (!itemBean.getAttr().isEmpty()) {
//            tag.putInt("quality", qualityIndex);
//            for (int i = 0; i < loreList.size(); i++) {
//                String v = loreList.get(i);
//                if (v.contains("@quality")) {
//                    loreList.set(i, v.replaceFirst("@quality", MagicItem.getInstance().getMainConfig().getStringList("quality.list").get(qualityIndex)));
//                    break;
//                }
//            }
//
//            if (!hasRcRPG) {
//                MagicItem.getInstance().getLogger().error("出问题的是:" + itemBean.getYamlName() + "\n无法进行属性解析，前置插件 RcRPG 未安装，请先安装插件。");
//                return Item.AIR_ITEM;
//            }
//
//            float multiple = MagicItem.getInstance().getMainConfig().getFloatList("quality.m").get(qualityIndex);
//            MagicItemAttr magicItemAttr = new MagicItemAttr(itemBean.attr, multiple);
//            for (int i = 0; i < loreList.size(); i++) {
//                String v = loreList.get(i);
//                if (v.contains("{{")) {
//                    loreList.set(i, magicItemAttr.replaceAttrTemplate(v));
//                }
//            }
//            tag.putCompound("attr", magicItemAttr.getCompound());
//        }
//
//        if (!itemBean.getMana().isEmpty()) {
//            MagicItemMana magicItemMana = new MagicItemMana(itemBean.mana);
//            for (int i = 0; i < loreList.size(); i++) {
//                String v = loreList.get(i);
//                if (v.contains("{(")) {
//                    loreList.set(i, magicItemMana.replaceAttrTemplate(v));
//                }
//            }
//
//            tag.putCompound("mana", magicItemMana.getCompound());
//        }
*/

        item.setLore(loreList);

        if (!itemBean.getEnch().isEmpty()) {
            for (String enchantment : itemBean.getEnch()) {
                String[] s = enchantment.split(":");
                int enchId = Integer.parseInt(s[0]);
                int enchLevel = Integer.parseInt(s[1]);
                EnchantmentType enchantmentType = Registries.ENCHANTMENTS.getByK1(enchId).createInstance(enchLevel).getType();
                item.addEnchantment(enchantmentType, enchLevel);
            }
        }
        return item;
    }
}
