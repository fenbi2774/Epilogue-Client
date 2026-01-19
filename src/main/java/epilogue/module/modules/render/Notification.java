package epilogue.module.modules.render;

import epilogue.Epilogue;
import epilogue.events.Render2DEvent;
import epilogue.font.CustomFontRenderer;
import epilogue.font.FontTransformer;
import epilogue.module.Module;
import epilogue.ui.chat.GuiChat;
import epilogue.util.render.ColorUtil;
import epilogue.util.render.PostProcessing;
import epilogue.util.render.RenderUtil;
import epilogue.value.values.ModeValue;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;

import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Notification extends Module {

    private final ModeValue mode = new ModeValue("Mode", 1, new String[]{"Epilogue", "Exhibition"});

    private final Minecraft mc = Minecraft.getMinecraft();
    private final Map<ModuleToggleNotification, NotificationState> notificationStates = new HashMap<>();
    private final List<ModuleToggleNotification> displayQueue = new ArrayList<>();
    private static Notification instance;

    private static final float width = 99f;
    private static final float height = 28f;
    private static final float space = 6f;
    private static final float radius = 6f;
    private static final float animateTime = 600f;
    private static final long animateTime1 = 2000L;
    private static final ResourceLocation e = new ResourceLocation("epilogue/texture/noti/okay.png");
    private static final ResourceLocation d = new ResourceLocation("epilogue/texture/noti/warning.png");

    private float anchorX;
    private float anchorY;
    private float lastWidth = width;
    private float lastHeight = height;

    public Notification() {
        super("Notification", false);
        instance = this;
    }

    public static Notification getInstance() {
        return instance;
    }

    public void renderAt(float anchorX, float anchorY) {
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        onRender2D(new Render2DEvent(0.0f));
    }

    private boolean isExhibition() {
        return mode.getValue() == 1;
    }

    public void onModuleToggle(String moduleName, boolean enabled) {
        if (!this.isEnabled()) return;

        ModuleToggleNotification notification = new ModuleToggleNotification(moduleName, enabled);
        NotificationState state = new NotificationState();
        notificationStates.put(notification, state);
        displayQueue.add(0, notification);

        if (displayQueue.size() > 5) {
            ModuleToggleNotification oldest = displayQueue.get(displayQueue.size() - 1);
            NotificationState oldestState = notificationStates.get(oldest);
            if (oldestState != null) {
                oldestState.removing = true;
                oldestState.startTime = System.currentTimeMillis();
            }
        }
    }

    private static class ModuleToggleNotification {
        final String moduleName;
        final boolean enabled;
        final long timestamp;

        ModuleToggleNotification(String moduleName, boolean enabled) {
            this.moduleName = moduleName;
            this.enabled = enabled;
            this.timestamp = System.currentTimeMillis();
        }
    }

    private static class NotificationState {
        float offsetX = width + 20;
        float targetOffsetX = 0;
        float alpha = 0f;
        float targetAlpha = 1f;
        float scale = 1.1f;
        float targetScale = 1.1f;
        long startTime = System.currentTimeMillis();
        int position = 0;
        boolean removing = false;
        boolean justCreated = true;
    }

    public void onRender2D(Render2DEvent event) {
        if (!this.isEnabled()) {
            return;
        }

        updateNotificationStates();

        boolean preview = (mc.currentScreen instanceof net.minecraft.client.gui.GuiChat) || (mc.currentScreen instanceof GuiChat);
        if (preview && displayQueue.isEmpty()) {
            displayQueue.add(new ModuleToggleNotification("Fly", true));
            displayQueue.add(new ModuleToggleNotification("Sprint", true));
            displayQueue.add(new ModuleToggleNotification("KillAura", false));

            for (ModuleToggleNotification n : displayQueue) {
                NotificationState s = notificationStates.get(n);
                if (s == null) {
                    s = new NotificationState();
                    s.alpha = 1.0f;
                    s.targetAlpha = 1.0f;
                    s.offsetX = 0.0f;
                    s.targetOffsetX = 0.0f;
                    s.scale = 1.0f;
                    s.targetScale = 1.0f;
                    s.removing = false;
                    s.justCreated = false;
                    notificationStates.put(n, s);
                }
            }
        }

        boolean alignRight = anchorX >= (new ScaledResolution(mc).getScaledWidth() / 2.0f);
        float sideMargin = isExhibition() ? 0f : 15f;
        float bottomMargin = isExhibition() ? 0f : 15f;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

        int position = 0;
        int visibleCount = 0;
        final float exhiEnterMs = 150.0f;
        final float exhiLeaveMs = 150.0f;
        for (ModuleToggleNotification notification : displayQueue) {
            NotificationState state = notificationStates.get(notification);
            if (state == null) continue;

            state.position = position;

            float width = isExhibition() ? getExhibitionWidth(notification) : Notification.width;
            float height = isExhibition() ? getExhibitionHeight() : Notification.height;

            float yOffset = isExhibition() ? (position * height) : (position * (height + space));

            float targetX = isExhibition() ? (alignRight ? (anchorX - width) : anchorX) : (alignRight ? (anchorX - width - sideMargin) : (anchorX + sideMargin));

            float y = isExhibition() ? (anchorY - height - yOffset) : (anchorY - height - bottomMargin - yOffset);

            float x;
            if (isExhibition()) {
                long now = System.currentTimeMillis();
                long dt = now - notification.timestamp;

                boolean leaving = state.removing;
                long phaseTime = leaving ? (now - state.startTime) : dt;
                float tEnter = Math.min(1.0f, (float) dt / exhiEnterMs);
                float tLeave = Math.min(1.0f, (float) phaseTime / exhiLeaveMs);

                float offscreenX = new ScaledResolution(mc).getScaledWidth();
                float target = targetX;

                if (leaving) {
                    x = target + (offscreenX - target) * tLeave;
                } else {
                    x = offscreenX + (target - offscreenX) * tEnter;
                }

            } else {
                x = alignRight ? (anchorX - width - sideMargin + state.offsetX) : (anchorX + sideMargin - state.offsetX);
            }

            if (!isExhibition()) updateAnimation(state);

            if (isExhibition() || state.alpha > 0.02f) {
                visibleCount++;
                GlStateManager.pushMatrix();
                if (!isExhibition()) {
                    float centerX = x + width / 2;
                    float centerY = y + height / 2;
                    GlStateManager.translate(centerX, centerY, 0);
                    GlStateManager.scale(state.scale, state.scale, 1);
                    GlStateManager.translate(-centerX, -centerY, 0);
                }
                float renderAlpha;
                if (isExhibition()) {
                    long now = System.currentTimeMillis();
                    long dt = now - notification.timestamp;
                    if (!state.removing) {
                        renderAlpha = Math.min(1.0f, (float) dt / exhiEnterMs);
                    } else {
                        renderAlpha = 1.0f;
                    }
                } else {
                    renderAlpha = Math.max(0f, Math.min(1f, state.alpha));
                }
                Framebuffer bloomBuffer = drawNotification(notification, x, y, renderAlpha);

                GlStateManager.popMatrix();

                if (bloomBuffer != null) {
                    PostProcessing.endBloom(bloomBuffer);
                }
                position++;
            }
        }

        float count = Math.max(1, visibleCount);
        lastWidth = isExhibition() ? getExhibitionMaxWidth() : width;
        float baseH = isExhibition() ? getExhibitionHeight() : height;
        lastHeight = isExhibition() ? (baseH * count) : ((baseH * count) + (space * (count - 1)));

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        displayQueue.removeIf(notification -> {
            NotificationState state = notificationStates.get(notification);
            if (state == null) return true;
            if (isExhibition()) {
                if (!state.removing) return false;
                long removeElapsed = System.currentTimeMillis() - state.startTime;
                return removeElapsed > (long) exhiLeaveMs + 200L;
            }
            return state.removing && state.alpha < 0.01f;
        });

        if (preview) {
            displayQueue.clear();
            notificationStates.clear();
        }
    }

    public float getLastWidth() {
        return lastWidth <= 0 ? width : lastWidth;
    }

    public float getLastHeight() {
        return lastHeight <= 0 ? height : lastHeight;
    }

    private float getExhibitionHeight() {
        return 26f;
    }

    private float getExhibitionWidth(ModuleToggleNotification notification) {
        FontTransformer transformer = FontTransformer.getInstance();
        Font titleFont = transformer.getFont("Inter_Regular", 36);
        Font descFont = transformer.getFont("Inter_Regular", 28);
        if (titleFont == null || descFont == null) return 100f;

        String title = notification.moduleName;
        String desc = notification.enabled ? "Enabled" : "Disabled";

        float w1 = CustomFontRenderer.getStringWidth(title, titleFont) + 2.0f;
        float w2 = CustomFontRenderer.getStringWidth(desc, descFont);
        return Math.max(100.0f, Math.max(w1, w2) + 24.0f);
    }

    private float getExhibitionMaxWidth() {
        float max = 140f;
        for (ModuleToggleNotification n : displayQueue) {
            max = Math.max(max, getExhibitionWidth(n));
        }
        return max;
    }

    private void updateNotificationStates() {
        long currentTime = System.currentTimeMillis();

        List<ModuleToggleNotification> toRemove = new ArrayList<>();

        for (ModuleToggleNotification notification : displayQueue) {
            NotificationState state = notificationStates.get(notification);
            if (state != null) {
                long totalElapsed = currentTime - notification.timestamp;

                if (!state.removing) {
                    if (totalElapsed > animateTime1) {
                        state.removing = true;
                        state.startTime = currentTime;
                        if (!isExhibition()) {
                            state.targetAlpha = 0f;
                            state.targetOffsetX = width + 30;
                        }
                    }
                } else {
                    long removeElapsed = currentTime - state.startTime;

                    if (!isExhibition()) {
                        if (state.alpha <= 0.02f && state.offsetX >= width + 10) {
                            toRemove.add(notification);
                        } else if (removeElapsed > 1000) {
                            toRemove.add(notification);
                        }
                    }
                }

                if (!isExhibition() && totalElapsed > animateTime1 + 1500) {
                    toRemove.add(notification);
                }
            } else {
                toRemove.add(notification);
            }
        }

        for (ModuleToggleNotification notification : toRemove) {
            displayQueue.remove(notification);
            notificationStates.remove(notification);
        }
    }

    private void updateAnimation(NotificationState state) {
        long elapsed = System.currentTimeMillis() - state.startTime;
        float animationSpeed = 10;
        float speed = animationSpeed * 0.08f;

        if (state.justCreated && elapsed > 50) {
            state.justCreated = false;
        }

        float offsetDiff = state.targetOffsetX - state.offsetX;
        if (state.removing) {
            long removeElapsed = elapsed;
            float removeProgress = Math.min(1.0f, removeElapsed / 400f);
            float easing = easeInCubic(removeProgress);
            state.offsetX = easing * (width + 30);

            if (removeProgress > 0.6f) {
                float fadeProgress = (removeProgress - 0.6f) / 0.4f;
                state.alpha = 1.0f - fadeProgress;
            }
        } else {
            float easing = easeOutCubic(Math.min(1.0f, elapsed / animateTime));
            state.offsetX = (width + 20) * (1 - easing);
        }
        if (Math.abs(offsetDiff) < 0.3f && !state.removing) {
            state.offsetX = state.targetOffsetX;
        }

        if (!state.removing) {
            float alphaDiff = state.targetAlpha - state.alpha;
            if (Math.abs(alphaDiff) < 0.01f) {
                state.alpha = state.targetAlpha;
            } else {
                state.alpha += alphaDiff * speed * 2.0f;
                state.alpha = Math.max(0f, Math.min(1f, state.alpha));
            }
        }

        float scaleDiff = state.targetScale - state.scale;
        if (!state.removing && state.justCreated) {
            float scaleEasing = easeOutBack(Math.min(1.0f, elapsed / (animateTime * 0.7f)));
            state.scale = 0.935f + (state.targetScale - 0.935f) * scaleEasing;
        } else if (state.removing) {
            long removeElapsed = elapsed;
            float removeProgress = Math.min(1.0f, removeElapsed / 400f);
            state.scale = 1.1f - removeProgress * 0.165f;
        } else {
            state.scale += scaleDiff * speed * 1.2f;
        }
        if (Math.abs(scaleDiff) < 0.005f && !state.removing) {
            state.scale = state.targetScale;
        }
    }

    private float easeInCubic(float t) {
        return t * t * t;
    }

    private float easeOutCubic(float t) {
        return 1 - (float) Math.pow(1 - t, 3);
    }

    private float easeOutBack(float t) {
        float c1 = 1.40158f;
        float c3 = c1 + 1;
        return 1 + c3 * (float) Math.pow(t - 1, 3) + c1 * (float) Math.pow(t - 1, 2);
    }

    private Framebuffer drawNotification(ModuleToggleNotification notification, float x, float y, float alpha) {
        if (isExhibition()) {
            return drawExhibitionNotification(notification, x, y, alpha);
        }
        return drawEpilogueNotification(notification, x, y, alpha);
    }

    private Framebuffer drawEpilogueNotification(ModuleToggleNotification notification, float x, float y, float alpha) {
        Framebuffer bloomBuffer = PostProcessing.beginBloom();
        if (bloomBuffer != null) {
            RenderUtil.drawRoundedRect(x, y, width, height, radius, epilogue.module.modules.render.PostProcessing.getBloomColor());
            mc.getFramebuffer().bindFramebuffer(false);
        }

        int bgAlphaValue = (int) (185 * alpha);
        int textAlphaValue = (int) (255 * alpha);

        PostProcessing.drawBlur(x, y, x + width, y + height, () -> () -> RenderUtil.drawRoundedRect(x, y, width, height, radius, -1));

        Color bgColor = new Color(10, 10, 10, bgAlphaValue);
        RenderUtil.drawRoundedRect(x, y, width, height, radius, bgColor);

        float padding = 6.0f;
        float titleY = y + padding;

        FontTransformer transformer = FontTransformer.getInstance();
        Font titleFont = transformer.getFont("OpenSansSemiBold", 30);
        float titleH = titleFont != null ? CustomFontRenderer.getFontHeight(titleFont) : 10.0f;

        float sidebarW = 2.5f;
        float sidebarX = x;
        float sidebarY = titleY;
        float sidebarH = titleH;
        int sidebarColor = ColorUtil.swapAlpha(getInterfaceColor(0), (int) (220.0f * alpha));
        RenderUtil.drawRect(sidebarX, sidebarY, sidebarW, sidebarH, sidebarColor);

        drawEpilogueContent(notification, x, y, textAlphaValue, titleFont, titleY, padding);
        return bloomBuffer;
    }

    private void drawEpilogueContent(ModuleToggleNotification notification, float x, float y, int alpha, Font titleFont, float titleY, float padding) {
        FontTransformer transformer = FontTransformer.getInstance();
        Font contentFont = transformer.getFont("OpenSansSemiBold", 24);

        float textX = x + padding;

        int textCol = ColorUtil.swapAlpha(0xFFFFFFFF, alpha);
        String title = notification.moduleName;
        CustomFontRenderer.drawStringWithShadow(title, textX, titleY, textCol, titleFont);

        String stateText = notification.enabled ? "Enabled" : "Disabled";
        int stateColBase = notification.enabled ? new Color(120, 255, 120).getRGB() : new Color(255, 120, 120).getRGB();
        int stateCol = ColorUtil.swapAlpha(stateColBase, alpha);
        float messageY = titleY + CustomFontRenderer.getFontHeight(titleFont) + 2.0f;
        CustomFontRenderer.drawStringWithShadow(stateText, textX, messageY, stateCol, contentFont);

        float barH = 1.0f;
        float barPadding = 2.0f;
        float barX = x + barPadding;
        float barY = y + height - barH - barPadding;
        float barW = width - barPadding * 2.0f;

        long elapsed = System.currentTimeMillis() - notification.timestamp;
        float progress = 1.0f - Math.min(1.0f, (float) elapsed / (float) animateTime1);
        if (progress > 0.0f) {
            int baseColor = getInterfaceColor(0);
            int progressCol = ColorUtil.swapAlpha(baseColor, (int) (alpha * 0.75f));
            RenderUtil.drawRect(barX, barY, barW * progress, barH, progressCol);
        }
    }

    private Framebuffer drawExhibitionNotification(ModuleToggleNotification notification, float x, float y, float alpha) {
        float width = getExhibitionWidth(notification);
        float height = getExhibitionHeight();
        int bg = new Color(0, 0, 0, (int) (220.0f * alpha)).getRGB();
        RenderUtil.drawRect(x, y, width, height, bg);

        long elapsed = System.currentTimeMillis() - notification.timestamp;
        float percentage = Math.min(1.0f, (float) elapsed / (float) animateTime1);

        int typeColor = notification.enabled ? new Color(65, 252, 65).getRGB() : new Color(226, 87, 76).getRGB();
        float barX = x + (width * percentage);
        float barY = y + height - 1.0f;
        RenderUtil.drawRect(barX, barY, width - (width * percentage), 1.0f, ColorUtil.swapAlpha(typeColor, (int) (255.0f * alpha)));

        float iconX = x + 2.0f;
        float iconY = y + (height - 18.0f) / 2.0f - 1.0f;
        ResourceLocation icon = notification.enabled ? e : d;
        mc.getTextureManager().bindTexture(icon);
        int tex = mc.getTextureManager().getTexture(icon).getGlTextureId();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);
        int prevMin = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER);
        int prevMag = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1.0f, 1.0f, 1.0f, alpha);
        Gui.drawModalRectWithCustomSizedTexture((int) iconX, (int) iconY, 0, 0, 18, 18, 18.0f, 18.0f);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, prevMin);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, prevMag);

        FontTransformer transformer = FontTransformer.getInstance();
        Font titleFont = transformer.getFont("Inter_Regular", 36);
        Font descFont = transformer.getFont("Inter_Regular", 28);
        int textCol = ColorUtil.swapAlpha(0xFFFFFFFF, (int) (255.0f * alpha));

        float textX = x + 21.5f;
        if (titleFont != null) {
            CustomFontRenderer.drawStringWithShadow("Module", textX, y + 4.5f, textCol, titleFont);
        }
        if (descFont != null) {
            String stateText = notification.enabled ? "Enabled" : "Disabled";
            int stateCol = notification.enabled ? new Color(65, 252, 65).getRGB() : new Color(226, 87, 76).getRGB();

            String prefix = notification.moduleName + " ";
            CustomFontRenderer.drawStringWithShadow(prefix, textX, y + 15.5f, textCol, descFont);
            float stateX = textX + CustomFontRenderer.getStringWidth(prefix, descFont);
            CustomFontRenderer.drawStringWithShadow(stateText, stateX, y + 15.5f, ColorUtil.swapAlpha(stateCol, (int) (255.0f * alpha)), descFont);
        }
        float barH = 1.0f;
        float barPadding = 2.0f;
        float barX1 = x + barPadding;
        float barY1 = y + height - barH - barPadding;
        float barW = width - barPadding * 2.0f;

        long elapsed1 = System.currentTimeMillis() - notification.timestamp;
        float progress = 1.0f - Math.min(1.0f, (float) elapsed1 / (float) animateTime1);
        if (progress > 0.0f) {
            int baseColor = getInterfaceColor(0);
            int progressCol = ColorUtil.swapAlpha(baseColor, (int) (alpha * 0.75f));
            RenderUtil.drawRect(barX1, barY1, barW * progress, barH, progressCol);
        }
        return null;
    }

    private int getInterfaceColor(int index) {
        Module m = Epilogue.moduleManager.getModule("Interface");
        if (m instanceof Interface) {
            return ((Interface) m).color(index);
        }
        return 0x8080FF;
    }
}