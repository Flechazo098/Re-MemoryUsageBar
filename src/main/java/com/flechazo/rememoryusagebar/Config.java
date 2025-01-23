package com.flechazo.rememoryusagebar;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// 一个示例配置类。这不是必须的，但为了保持配置整洁，拥有这样一个类是个好主意。
// 演示如何使用Forge的配置API
@Mod.EventBusSubscriber(modid = ReMemoryUsageBar.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // 定义是否在公共设置时记录泥土方块
    private static final ForgeConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER.comment("是否在公共设置时记录泥土方块")
            .define("logDirtBlock", true);

    // 定义一个魔法数字
    private static final ForgeConfigSpec.IntValue MAGIC_NUMBER = BUILDER.comment("一个魔法数字")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    // 魔法数字的介绍信息
    public static final ForgeConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER.comment("你想对魔法数字显示的介绍信息")
            .define("magicNumberIntroduction", "The magic number is... ");

    // 一个字符串列表，用于表示物品的资源位置
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER.comment("在公共设置时要记录的一系列物品。")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), Config::validateItemName);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    // 公共变量，从配置文件中加载值
    public static boolean logDirtBlock;
    public static int magicNumber;
    public static String magicNumberIntroduction;
    public static Set<Item> items;

    // 验证物品名称的方法，确保它们是有效的物品资源位置
    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemName));
    }

    // 当配置加载时调用此方法来更新静态变量
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        logDirtBlock = LOG_DIRT_BLOCK.get();
        magicNumber = MAGIC_NUMBER.get();
        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();

        // 将字符串列表转换为物品集合
        items = ITEM_STRINGS.get().stream()
                .map(itemName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName)))
                .collect(Collectors.toSet());
    }
}