package com.flechazo.rememoryusagebar;

import com.sun.management.OperatingSystemMXBean;
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
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;

/**
 * Re-MemoryUsageBar Mod主类
 * 在游戏HUD上显示系统资源使用情况
 * @author Flechazo
 */
@Mod(ReMemoryUsageBar.MODID)
public class ReMemoryUsageBar {
    public static final String MODID = "rememoryusagebar";
    private static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    private final OperatingSystemMXBean osBean;

    public ReMemoryUsageBar() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::clientSetup);
        
        // 注册配置
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);
        
        // 初始化系统监控
        osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        
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
     * 渲染资源使用条
     * @param event GUI渲染事件
     */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onRenderGui(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        
        if (player == null) return;
        
        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        
        // 基础位置
        int baseX = 10;
        int baseY = screenHeight - 25;
        int spacing = Config.INSTANCE.barSpacing.get();

        // 根据配置决定渲染顺序
        boolean memoryOnTop = Config.INSTANCE.memoryOnTop.get();
        int memoryY = memoryOnTop ? baseY : baseY + spacing;
        int cpuY = memoryOnTop ? baseY + spacing : baseY;
        
        // 渲染内存使用率
        if (Config.INSTANCE.showMemory.get()) {
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            double memoryUsage = (double) usedMemory / runtime.maxMemory();
            renderUsageBar(guiGraphics, minecraft.font, screenWidth, screenHeight,
                    baseX, memoryY, memoryUsage, "MEM");
        }
        
        // 渲染CPU使用率
        if (Config.INSTANCE.showCPU.get()) {
            double cpuUsage = osBean.getProcessCpuLoad();
            if (cpuUsage >= 0) { // 小于0表示不可用
                renderUsageBar(guiGraphics, minecraft.font, screenWidth, screenHeight,
                        baseX, cpuY, cpuUsage, "CPU");
            }
        }
    }

    /**
     * 渲染单个使用率条
     * @param guiGraphics GUI渲染上下文
     * @param font 字体渲染器
     * @param screenWidth 屏幕宽度
     * @param screenHeight 屏幕高度
     * @param barX 条形图X坐标
     * @param barY 条形图Y坐标
     * @param usage 使用率
     * @param label 标签
     */
    private void renderUsageBar(GuiGraphics guiGraphics, Font font, int screenWidth, int screenHeight,
                              int barX, int barY, double usage, String label) {
        int barWidth = 90;
        int barHeight = 5;
        
        // 计算使用率
        int usageBarWidth = (int) (usage * (barWidth - 2));
        int usageBarColor = Config.INSTANCE.getUsageColor(usage);
        String usagePercentage = String.format("%s: %.0f%%", label, usage * 100.0);
        
        // 计算文本位置
        int percentageX = barX;
        int percentageY = barY - font.lineHeight - 1;
        
        // 绘制背景
        guiGraphics.fill(barX + 1, barY - 1, barX + 100, barY + barHeight + 1, 0x80000000);
        guiGraphics.fill(barX - 1, barY - 1, barX + usageBarWidth + 1, barY + barHeight + 1, 0x80000000);
        
        // 绘制使用率条
        guiGraphics.fill(barX, barY, barX + usageBarWidth, barY + barHeight, usageBarColor);
        guiGraphics.fill(barX + 1, barY + 1, barX + usageBarWidth + 1, barY + barHeight - 1, usageBarColor);
        
        // 绘制文本
        guiGraphics.drawString(font, usagePercentage, percentageX, percentageY, 0xFFFFFFFF, true);
    }
}
