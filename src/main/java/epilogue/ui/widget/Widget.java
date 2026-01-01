package epilogue.ui.widget;

import epilogue.util.render.ColorUtil;
import epilogue.util.render.RenderUtil;
import epilogue.util.render.animations.advanced.Animation;
import epilogue.util.render.animations.advanced.Direction;
import epilogue.util.render.animations.advanced.impl.DecelerateAnimation;
import epilogue.util.render.PostProcessing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

import java.awt.*;

public abstract class Widget {
    protected static final Minecraft mc = Minecraft.getMinecraft();

    public final String name;

    public float x;
    public float y;

    public float width;
    public float height;

    public float renderX;
    public float renderY;

    public boolean dragging;
    private int dragX;
    private int dragY;

    public int align;

    public final Animation hoverAnimation = new DecelerateAnimation(250, 1, Direction.BACKWARDS);

    protected Widget(String name) {
        this(name, WidgetAlign.LEFT | WidgetAlign.TOP);
    }

    protected Widget(String name, int align) {
        this.name = name;
        this.align = align;
        this.x = 0.0f;
        this.y = 0.0f;
        this.width = 80.0f;
        this.height = 20.0f;
    }

    public abstract boolean shouldRender();

    public abstract void render(float partialTicks);

    protected float getHoverOffsetX() {
        return 0.0f;
    }

    protected float getHoverOffsetY() {
        return 0.0f;
    }

    protected float getHoverExtraWidth() {
        return 0.0f;
    }

    public void updatePos(ScaledResolution sr) {
        float screenW = sr.getScaledWidth();
        float screenH = sr.getScaledHeight();

        float anchorX = x * screenW;
        float anchorY = y * screenH;

        float rx = anchorX;
        float ry = anchorY;

        if ((align & WidgetAlign.RIGHT) != 0) {
            rx -= width;
        } else if ((align & WidgetAlign.CENTER) != 0) {
            rx -= width / 2.0f;
        }

        if ((align & WidgetAlign.BOTTOM) != 0) {
            ry -= height;
        } else if ((align & WidgetAlign.MIDDLE) != 0) {
            ry -= height / 2.0f;
        }

        if (rx < 0) rx = 0;
        if (ry < 0) ry = 0;
        if (rx > screenW - width) rx = screenW - width;
        if (ry > screenH - height) ry = screenH - height;

        renderX = rx;
        renderY = ry;

        float nx = rx;
        float ny = ry;

        if ((align & WidgetAlign.RIGHT) != 0) {
            nx += width;
        } else if ((align & WidgetAlign.CENTER) != 0) {
            nx += width / 2.0f;
        }

        if ((align & WidgetAlign.BOTTOM) != 0) {
            ny += height;
        } else if ((align & WidgetAlign.MIDDLE) != 0) {
            ny += height / 2.0f;
        }

        x = nx / screenW;
        y = ny / screenH;
    }

    public void onChatGUI(ScaledResolution sr, int mouseX, int mouseY, boolean allowDrag) {
        boolean hovering = isHovered(mouseX, mouseY);
        hoverAnimation.setDirection(hovering ? Direction.FORWARDS : Direction.BACKWARDS);

        float alpha = (float) hoverAnimation.getOutput();
        if (alpha > 0.001f) {
            float bx = renderX - 2 + getHoverOffsetX();
            float by = renderY - 2 + getHoverOffsetY();
            float bw = width + 4 + getHoverExtraWidth();
            float bh = height + 4;

            float darkAlpha = Math.min(0.55f, 0.18f + alpha * 0.35f);
            int dark = ColorUtil.applyOpacity(new Color(0, 0, 0, 255).getRGB(), darkAlpha);
            RenderUtil.drawRect(bx, by, bw, bh, dark);

            if (epilogue.module.modules.render.PostProcessing.isBlurEnabled()) {
                PostProcessing.drawBlur(bx, by, bx + bw, by + bh, () -> () -> {
                    GlStateManager.enableBlend();
                    GlStateManager.disableTexture2D();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                    RenderUtil.setup2DRendering(() -> {
                        net.minecraft.client.gui.Gui.drawRect((int) bx, (int) by, (int) (bx + bw), (int) (by + bh), -1);
                    });
                    GlStateManager.enableTexture2D();
                    GlStateManager.disableBlend();
                });
            }

            int c = ColorUtil.applyOpacity(new Color(255, 255, 255, 255).getRGB(), alpha);
            float phase = (System.currentTimeMillis() % 1200L) / 1200.0f;
            float dashLength = 6.0f;
            float dashGap = 5.0f;
            float dashOffset = phase * (dashLength + dashGap);
            drawDashedRect(bx, by, bw, bh, dashLength, dashGap, dashOffset, c);

            float textAlpha = Math.min(1.0f, alpha * 1.15f);
            int textColor = ColorUtil.applyOpacity(new Color(255, 255, 255, 255).getRGB(), textAlpha);
            float textScale = 0.9f + (alpha * 0.1f);

            GlStateManager.pushMatrix();
            GlStateManager.translate(bx + 6, by + 6, 0);
            GlStateManager.scale(textScale, textScale, 1.0f);
            mc.fontRendererObj.drawStringWithShadow(name, 0, 0, textColor);
            GlStateManager.popMatrix();
        }

        if (hovering && Mouse.isButtonDown(0) && !dragging && allowDrag) {
            dragging = true;
            dragX = mouseX;
            dragY = mouseY;
        }

        if (!Mouse.isButtonDown(0)) {
            dragging = false;
        }

        if (dragging) {
            float dx = (mouseX - dragX);
            float dy = (mouseY - dragY);

            float newRenderX = renderX + dx;
            float newRenderY = renderY + dy;

            float screenW = sr.getScaledWidth();
            float screenH = sr.getScaledHeight();

            if (newRenderX < 0) newRenderX = 0;
            if (newRenderY < 0) newRenderY = 0;
            if (newRenderX > screenW - width) newRenderX = screenW - width;
            if (newRenderY > screenH - height) newRenderY = screenH - height;

            float nx = newRenderX;
            float ny = newRenderY;

            if ((align & WidgetAlign.RIGHT) != 0) {
                nx += width;
            } else if ((align & WidgetAlign.CENTER) != 0) {
                nx += width / 2.0f;
            }

            if ((align & WidgetAlign.BOTTOM) != 0) {
                ny += height;
            } else if ((align & WidgetAlign.MIDDLE) != 0) {
                ny += height / 2.0f;
            }

            x = nx / screenW;
            y = ny / screenH;

            dragX = mouseX;
            dragY = mouseY;
        }
    }

    private void drawDashedRect(float x, float y, float w, float h, float dashLength, int color) {
        float x2 = x + w;
        float y2 = y + h;
        float step = dashLength * 2.0f;

        for (float i = 0; i < w; i += step) {
            float ex = Math.min(x + i + dashLength, x2);
            RenderUtil.drawRect(x + i, y, ex - (x + i), 1.0f, color);
            RenderUtil.drawRect(x + i, y2, ex - (x + i), 1.0f, color);
        }

        for (float i = 0; i < h; i += step) {
            float ey = Math.min(y + i + dashLength, y2);
            RenderUtil.drawRect(x, y + i, 1.0f, ey - (y + i), color);
            RenderUtil.drawRect(x2, y + i, 1.0f, ey - (y + i), color);
        }
    }

    private void drawDashedRect(float x, float y, float w, float h, float dashLength, float dashGap, float dashOffset, int color) {
        float x2 = x + w;
        float y2 = y + h;
        float step = dashLength + dashGap;

        for (float i = -dashOffset; i < w; i += step) {
            float sx = x + Math.max(0, i);
            float ex = x + Math.min(w, i + dashLength);
            if (ex > sx) {
                RenderUtil.drawRect(sx, y, ex - sx, 1.0f, color);
                RenderUtil.drawRect(sx, y2, ex - sx, 1.0f, color);
            }
        }

        for (float i = -dashOffset; i < h; i += step) {
            float sy = y + Math.max(0, i);
            float ey = y + Math.min(h, i + dashLength);
            if (ey > sy) {
                RenderUtil.drawRect(x, sy, 1.0f, ey - sy, color);
                RenderUtil.drawRect(x2, sy, 1.0f, ey - sy, color);
            }
        }
    }

    public boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= renderX && mouseX <= renderX + width && mouseY >= renderY && mouseY <= renderY + height;
    }
}
