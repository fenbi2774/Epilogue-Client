package epilogue.util.render;

import epilogue.util.shader.ShaderUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class RenderUtil {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static ShaderUtils roundedRectShader;
    private static final String ROUNDED_RECT_FRAG =
            "#version 120\n" +
            "uniform vec2 rectSize;\n" +
            "uniform vec4 color;\n" +
            "uniform float radius;\n" +
            "float roundSDF(vec2 p, vec2 b, float r) {\n" +
            "    return length(max(abs(p) - b, 0.0)) - r;\n" +
            "}\n" +
            "void main() {\n" +
            "    vec2 rectHalf = rectSize * 0.5;\n" +
            "    vec2 p = rectHalf - (gl_TexCoord[0].st * rectSize);\n" +
            "    float a = (1.0 - smoothstep(0.0, 1.0, roundSDF(p, rectHalf - radius - 1.0, radius))) * color.a;\n" +
            "    gl_FragColor = vec4(color.rgb, a);\n" +
            "}";

    private static ShaderUtils getRoundedRectShader() {
        if (roundedRectShader == null) {
            roundedRectShader = new ShaderUtils(ROUNDED_RECT_FRAG, true);
        }
        return roundedRectShader;
    }

    public static void drawQuads() {
        ScaledResolution sr = new ScaledResolution(mc);
        float w = (float) sr.getScaledWidth_double();
        float h = (float) sr.getScaledHeight_double();

        drawQuads(0f, 0f, w, h);
    }

    public static void drawQuads(float x, float y, float width, float height) {
        glBegin(GL_QUADS);
        glTexCoord2f(0f, 0f);
        glVertex2f(x, y);
        glTexCoord2f(0f, 1f);
        glVertex2f(x, y + height);
        glTexCoord2f(1f, 1f);
        glVertex2f(x + width, y + height);
        glTexCoord2f(1f, 0f);
        glVertex2f(x + width, y);
        glEnd();

    }

    public static void drawRect(float left, float top, float width, float height, int color) {
        Gui.drawRect((int)left, (int)top, (int)(left + width), (int)(top + height), color);
    }

    public static void drawRect(float left, float top, float width, float height, Color color) {
        drawRect(left, top, width, height, color.getRGB());
    }

    public static void drawBorderedRect(float x, float y, float width, float height, float borderWidth, int inside, int border) {
        drawRect(x, y, width, height, border);
        drawRect(x + borderWidth, y + borderWidth, width - borderWidth * 2.0f, height - borderWidth * 2.0f, inside);
    }

    public static void drawHorizontalGradientSideways(float x, float y, float width, float height, int leftColor, int rightColor) {
        setup2DRendering(() -> {
            GL11.glShadeModel(GL11.GL_SMOOTH);
            GL11.glBegin(GL11.GL_QUADS);

            glColor(leftColor);
            GL11.glVertex2f(x, y);
            GL11.glVertex2f(x, y + height);

            glColor(rightColor);
            GL11.glVertex2f(x + width, y + height);
            GL11.glVertex2f(x + width, y);

            GL11.glEnd();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    public static void renderItemStack(ItemStack stack, float x, float y, float scale, boolean overlay, float overlayScale) {
        if (stack == null) return;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0.0f);
        GlStateManager.scale(scale, scale, 1.0f);

        RenderHelper.enableGUIStandardItemLighting();
        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, 0, 0);
        if (overlay) {
            mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, stack, 0, 0);
        }
        RenderHelper.disableStandardItemLighting();

        GlStateManager.popMatrix();
    }

    public static void renderPlayer2D(EntityPlayer target, float x, float y, float size, float unused, int color) {
        if (target == null) return;

        ResourceLocation skin = mc.getNetHandler().getPlayerInfo(target.getName()) != null
                ? mc.getNetHandler().getPlayerInfo(target.getName()).getLocationSkin()
                : null;
        if (skin == null) return;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        if (color != -1) {
            glColor(color);
        } else {
            GlStateManager.color(1, 1, 1, 1);
        }
        mc.getTextureManager().bindTexture(skin);
        Gui.drawScaledCustomSizeModalRect((int) x, (int) y, 8.0F, 8.0F, 8, 8, (int) size, (int) size, 64.0F, 64.0F);
        Gui.drawScaledCustomSizeModalRect((int) x, (int) y, 40.0F, 8.0F, 8, 8, (int) size, (int) size, 64.0F, 64.0F);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void scaleStart(float x, float y, float scale) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(scale, scale, 1);
        GlStateManager.translate(-x, -y, 0);
    }

    public static void scaleEnd() {
        GlStateManager.popMatrix();
    }

    public static void bindTexture(int texture) {
        GlStateManager.bindTexture(texture);
    }

    public static void glColor(int hex) {
        float alpha = (hex >> 24 & 0xFF) / 255.0F;
        float red = (hex >> 16 & 0xFF) / 255.0F;
        float green = (hex >> 8 & 0xFF) / 255.0F;
        float blue = (hex & 0xFF) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
    }

    public static void drawTexturedRect(float x, float y, float width, float height) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0).tex(0.0, 1.0).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0).tex(1.0, 1.0).endVertex();
        worldrenderer.pos(x + width, y, 0.0).tex(1.0, 0.0).endVertex();
        worldrenderer.pos(x, y, 0.0).tex(0.0, 0.0).endVertex();
        tessellator.draw();
    }

    public static void setup2DRendering(Runnable f) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_TEXTURE_2D);
        f.run();
        glEnable(GL_TEXTURE_2D);
        GlStateManager.disableBlend();
    }

    public static void setColor(int color) {
        GL11.glColor4ub((byte) (color >> 16 & 0xFF), (byte) (color >> 8 & 0xFF), (byte) (color & 0xFF), (byte) (color >> 24 & 0xFF));
    }

    public static void drawRoundedRect(float x, double y, float width, double height, float radius, int color) {
        if (width <= 0.0f || height <= 0.0) return;

        float h = (float) height;
        float r = Math.max(0.0f, Math.min(radius, Math.min(width, h) / 2.0f));
        if (r <= 0.0f) {
            drawRect(x, (float) y, width, (float) height, color);
            return;
        }

        float a = (color >> 24 & 0xFF) / 255.0f;
        float cr = (color >> 16 & 0xFF) / 255.0f;
        float cg = (color >> 8 & 0xFF) / 255.0f;
        float cb = (color & 0xFF) / 255.0f;

        boolean blend = glIsEnabled(GL_BLEND);
        boolean tex = glIsEnabled(GL_TEXTURE_2D);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);

        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        ShaderUtils shader = getRoundedRectShader();
        shader.init();
        shader.setUniformf("rectSize", width, h);
        shader.setUniformf("color", cr, cg, cb, a);
        shader.setUniformf("radius", r);

        drawQuads(x, (float) y, width, h);

        shader.unload();

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        if (!blend) GlStateManager.disableBlend();
        if (!tex) GlStateManager.disableTexture2D();
    }

    public static void drawRoundedRect(float x, float y, float width, float height, float radius, Color color) {
        drawRoundedRect(x, y, width, height, radius, color.getRGB());
    }

    public static void scissorStart(float x, float y, float width, float height) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        net.minecraft.client.gui.ScaledResolution sr = new net.minecraft.client.gui.ScaledResolution(mc);
        int factor = sr.getScaleFactor();
        if (width < 0) width = 0;
        if (height < 0) height = 0;
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(
                (int) (x * factor),
                (int) ((sr.getScaledHeight() - (y + height)) * factor),
                (int) (width * factor),
                (int) (height * factor)
        );
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
    }
    
    public static void scissorEnd() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GlStateManager.colorMask(true, true, true, true);
    }
}