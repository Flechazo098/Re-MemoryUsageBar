package com.flechazo.rememoryusagebar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Re-MemoryUsageBar Mod主类
 * 在游戏HUD上显示内存使用情况
 * @author Flechazo
 */
@Mod(ReMemoryUsageBar.MODID)
public class ReMemoryUsageBar {
    public static final String MODID = "rememoryusagebar";
    private static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public ReMemoryUsageBar() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::clientSetup);
        LOGGER.info("MemoryUsageBar Mod Load!");
    }

    /**
     * 客户端设置
     * @param event 客户端设置事件
     */
    @OnlyIn(Dist.CLIENT)
    private void clientSetup(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * 获取内存条的颜色
     * @param usedMemory 已使用内存
     * @param maxMemory 最大内存
     * @return 颜色值
     */
    @OnlyIn(Dist.CLIENT)
    private int getHealthBarColor(long usedMemory, long maxMemory) {
        float percentage = (float) usedMemory / (float) maxMemory;
        if (percentage < 0.2) {
            return 0xFF00FF00; // 绿色
        } else if (percentage < 0.5) {
            return 0xFFFF8000; // 橙色
        } else {
            return 0xFFFF0000; // 红色
        }
    }

    /**
     * 渲染内存使用条
     * @param event GUI渲染事件
     */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onRenderGui(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        
        if (player == null) return;
        
        GuiGraphics guiGraphics = event.getGuiGraphics();
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // 内存使用量变量
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int barWidth = 90;
        int barHeight = 5;
        int barX = 10;
        int barY = screenHeight - 10;
        
        // 计算内存使用百分比
        int memoryBarWidth = (int) ((float) usedMemory / (float) runtime.maxMemory() * (barWidth - 2));
        int memoryBarColor = getHealthBarColor(usedMemory, runtime.maxMemory());
        String memoryUsagePercentage = String.format("%.0f%%", (double)usedMemory / (double)runtime.maxMemory() * 100.0);
        
        Font font = minecraft.font;
        int percentageX = barX + memoryBarWidth - font.width(memoryUsagePercentage) / 2;
        int percentageY = barY - font.lineHeight - 1;
        
        // 绘制背景
        guiGraphics.fill(barX + 1, barY - 1, barX + 100, barY + barHeight + 1, 0x80000000);
        guiGraphics.fill(barX - 1, barY - 1, barX + memoryBarWidth + 1, barY + barHeight + 1, 0x80000000);
        
        // 绘制内存条
        guiGraphics.fill(barX, barY, barX + memoryBarWidth, barY + barHeight, memoryBarColor);
        guiGraphics.fill(barX + 1, barY + 1, barX + memoryBarWidth + 1, barY + barHeight - 1, memoryBarColor);
        
        // 绘制百分比文本
        guiGraphics.drawString(font, memoryUsagePercentage, percentageX, percentageY, 0xFFFFFFFF, true);
    }
}
