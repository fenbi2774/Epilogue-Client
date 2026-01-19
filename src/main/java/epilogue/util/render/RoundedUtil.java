package epilogue.util.render;

import epilogue.util.shader.ShaderUtils;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class RoundedUtil {

    private static ShaderUtils gradientCustomRoundShader;
    private static final String GRADIENT_CUSTOM_ROUND_FRAG =
            "#version 120\n" +
            "uniform vec2 rectSize;\n" +
            "uniform vec4 color1;\n" +
            "uniform vec4 color2;\n" +
            "uniform float radiusTL;\n" +
            "uniform float radiusTR;\n" +
            "uniform float radiusBL;\n" +
            "uniform float radiusBR;\n" +
            "float sdRoundBox(vec2 p, vec2 b, float r) {\n" +
            "    return length(max(abs(p) - b, 0.0)) - r;\n" +
            "}\n" +
            "float roundedMask(vec2 uv) {\n" +
            "    vec2 s = rectSize;\n" +
            "    vec2 p = uv * s;\n" +
            "    float r = (p.x < s.x * 0.5) ? ((p.y < s.y * 0.5) ? radiusTL : radiusBL) : ((p.y < s.y * 0.5) ? radiusTR : radiusBR);\n" +
            "    vec2 q = p - s * 0.5;\n" +
            "    vec2 b = s * 0.5 - vec2(r);\n" +
            "    float dist = sdRoundBox(q, b, r);\n" +
            "    return 1.0 - smoothstep(0.0, 1.0, dist);\n" +
            "}\n" +
            "void main() {\n" +
            "    vec2 uv = gl_TexCoord[0].st;\n" +
            "    vec4 gradX1 = mix(color1, color2, 0.35);\n" +
            "    vec4 gradX2 = mix(color1, color2, 0.65);\n" +
            "    vec4 top = mix(gradX1, gradX2, uv.x);\n" +
            "    vec4 gradY1 = mix(color2, color1, 0.65);\n" +
            "    vec4 gradY2 = mix(color2, color1, 0.35);\n" +
            "    vec4 bottom = mix(gradY1, gradY2, uv.x);\n" +
            "    vec4 c = mix(top, bottom, uv.y);\n" +
            "    float a = roundedMask(uv) * c.a;\n" +
            "    gl_FragColor = vec4(c.rgb, a);\n" +
            "}";

    private static ShaderUtils solidCustomRoundShader;
    private static final String SOLID_CUSTOM_ROUND_FRAG =
            "#version 120\n" +
            "uniform vec2 rectSize;\n" +
            "uniform vec4 color;\n" +
            "uniform float radiusTL;\n" +
            "uniform float radiusTR;\n" +
            "uniform float radiusBL;\n" +
            "uniform float radiusBR;\n" +
            "float sdRoundBox(vec2 p, vec2 b, float r) {\n" +
            "    return length(max(abs(p) - b, 0.0)) - r;\n" +
            "}\n" +
            "float roundedDist(vec2 uv) {\n" +
            "    vec2 s = rectSize;\n" +
            "    vec2 p = uv * s;\n" +
            "    float r = (p.x < s.x * 0.5) ? ((p.y < s.y * 0.5) ? radiusTL : radiusBL) : ((p.y < s.y * 0.5) ? radiusTR : radiusBR);\n" +
            "    vec2 q = p - s * 0.5;\n" +
            "    vec2 b = s * 0.5 - vec2(r);\n" +
            "    return sdRoundBox(q, b, r);\n" +
            "}\n" +
            "void main() {\n" +
            "    vec2 uv = gl_TexCoord[0].st;\n" +
            "    float dist = roundedDist(uv);\n" +
            "    float aa = 1.0;\n" +
            "    float mask = 1.0 - smoothstep(-aa, aa, dist);\n" +
            "    gl_FragColor = vec4(color.rgb, color.a * mask);\n" +
            "}";

    private static ShaderUtils outlineCustomRoundShader;
    private static final String OUTLINE_CUSTOM_ROUND_FRAG =
            "#version 120\n" +
            "uniform vec2 rectSize;\n" +
            "uniform vec4 fillColor;\n" +
            "uniform vec4 outlineColor;\n" +
            "uniform float thickness;\n" +
            "uniform float radiusTL;\n" +
            "uniform float radiusTR;\n" +
            "uniform float radiusBL;\n" +
            "uniform float radiusBR;\n" +
            "float sdRoundBox(vec2 p, vec2 b, float r) {\n" +
            "    return length(max(abs(p) - b, 0.0)) - r;\n" +
            "}\n" +
            "float roundedDist(vec2 uv, float shrink) {\n" +
            "    vec2 s = rectSize;\n" +
            "    vec2 p = uv * s;\n" +
            "    float r = (p.x < s.x * 0.5) ? ((p.y < s.y * 0.5) ? radiusTL : radiusBL) : ((p.y < s.y * 0.5) ? radiusTR : radiusBR);\n" +
            "    r = max(r - shrink, 0.0);\n" +
            "    vec2 q = p - s * 0.5;\n" +
            "    vec2 b = s * 0.5 - vec2(r) - vec2(shrink);\n" +
            "    return sdRoundBox(q, b, r);\n" +
            "}\n" +
            "void main() {\n" +
            "    vec2 uv = gl_TexCoord[0].st;\n" +
            "    float aa = 1.0;\n" +
            "    float t = max(thickness, 0.0);\n" +
            "    float outer = 1.0 - smoothstep(-aa, aa, roundedDist(uv, 0.0));\n" +
            "    float inner = 1.0 - smoothstep(-aa, aa, roundedDist(uv, t));\n" +
            "    float outlineMask = clamp(outer - inner, 0.0, 1.0);\n" +
            "    float fillMask = inner;\n" +
            "    vec4 c = mix(fillColor, outlineColor, outlineMask);\n" +
            "    float a = (fillMask * fillColor.a) + (outlineMask * outlineColor.a);\n" +
            "    gl_FragColor = vec4(c.rgb, a);\n" +
            "}";

    private static ShaderUtils circleShader;
    private static final String CIRCLE_FRAG =
            "#version 120\n" +
            "uniform vec2 rectSize;\n" +
            "uniform vec4 color;\n" +
            "uniform float radius;\n" +
            "uniform float thickness;\n" +
            "uniform float progress;\n" +
            "float circleDist(vec2 uv) {\n" +
            "    vec2 p = uv * rectSize - rectSize * 0.5;\n" +
            "    return length(p) - radius;\n" +
            "}\n" +
            "float angleNorm(vec2 uv) {\n" +
            "    vec2 p = uv * rectSize - rectSize * 0.5;\n" +
            "    float ang = atan(p.y, p.x);\n" +
            "    float a = (ang + 3.14159265) / 6.2831853;\n" +
            "    return a;\n" +
            "}\n" +
            "void main() {\n" +
            "    vec2 uv = gl_TexCoord[0].st;\n" +
            "    float aa = 1.25 / max(min(rectSize.x, rectSize.y), 1.0);\n" +
            "    float d = abs(circleDist(uv));\n" +
            "    float ring = 1.0 - smoothstep(thickness * 0.5, thickness * 0.5 + aa, d);\n" +
            "    float ang = angleNorm(uv);\n" +
            "    float p = clamp(progress, 0.0, 1.0);\n" +
            "    float cut = step(p, ang);\n" +
            "    float m = ring * cut;\n" +
            "    gl_FragColor = vec4(color.rgb, color.a * m);\n" +
            "}";

    private static ShaderUtils getGradientCustomRoundShader() {
        if (gradientCustomRoundShader == null) {
            gradientCustomRoundShader = new ShaderUtils(GRADIENT_CUSTOM_ROUND_FRAG, true);
        }
        return gradientCustomRoundShader;
    }

    private static ShaderUtils getSolidCustomRoundShader() {
        if (solidCustomRoundShader == null) {
            solidCustomRoundShader = new ShaderUtils(SOLID_CUSTOM_ROUND_FRAG, true);
        }
        return solidCustomRoundShader;
    }

    private static ShaderUtils getOutlineCustomRoundShader() {
        if (outlineCustomRoundShader == null) {
            outlineCustomRoundShader = new ShaderUtils(OUTLINE_CUSTOM_ROUND_FRAG, true);
        }
        return outlineCustomRoundShader;
    }

    private static ShaderUtils getCircleShader() {
        if (circleShader == null) {
            circleShader = new ShaderUtils(CIRCLE_FRAG, true);
        }
        return circleShader;
    }

    private static void with2DShaderState(Runnable r) {
        boolean blend = glIsEnabled(GL_BLEND);
        boolean tex = glIsEnabled(GL_TEXTURE_2D);
        boolean depth = glIsEnabled(GL_DEPTH_TEST);
        boolean cull = glIsEnabled(GL_CULL_FACE);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        GlStateManager.disableDepth();
        glDisable(GL_CULL_FACE);
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        r.run();

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        if (!blend) GlStateManager.disableBlend();
        if (!tex) GlStateManager.disableTexture2D();
        if (depth) GlStateManager.enableDepth();
        if (cull) glEnable(GL_CULL_FACE);
    }

    private static void drawRoundMask(float x, float y, float width, float height, float radius) {
        if (width <= 0.0f || height <= 0.0f) return;
        float r = Math.max(0.0f, Math.min(radius, Math.min(width, height) / 2.0f));

        boolean tex = glIsEnabled(GL_TEXTURE_2D);
        boolean blend = glIsEnabled(GL_BLEND);
        boolean depth = glIsEnabled(GL_DEPTH_TEST);
        boolean cull = glIsEnabled(GL_CULL_FACE);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        GlStateManager.disableDepth();
        glDisable(GL_CULL_FACE);
        glDisable(GL_TEXTURE_2D);
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        float x2 = x + width;
        float y2 = y + height;

        glBegin(GL_QUADS);
        glVertex2f(x + r, y);
        glVertex2f(x2 - r, y);
        glVertex2f(x2 - r, y2);
        glVertex2f(x + r, y2);

        glVertex2f(x, y + r);
        glVertex2f(x + r, y + r);
        glVertex2f(x + r, y2 - r);
        glVertex2f(x, y2 - r);

        glVertex2f(x2 - r, y + r);
        glVertex2f(x2, y + r);
        glVertex2f(x2, y2 - r);
        glVertex2f(x2 - r, y2 - r);
        glEnd();

        int segments = 64;
        drawQuarterFan(x + r, y + r, r, 180.0, 270.0, segments);
        drawQuarterFan(x + r, y2 - r, r, 90.0, 180.0, segments);
        drawQuarterFan(x2 - r, y2 - r, r, 0.0, 90.0, segments);
        drawQuarterFan(x2 - r, y + r, r, 270.0, 360.0, segments);

        if (tex) glEnable(GL_TEXTURE_2D);
        if (depth) GlStateManager.enableDepth();
        if (cull) glEnable(GL_CULL_FACE);
        if (!blend) GlStateManager.disableBlend();
    }

    public static void drawRound(float x, float y, float width, float height, float radius, Color color) {
        drawRound(x, y, width, height, color, 24, radius, radius, radius, radius);
    }

    public static void drawRound(float x, float y, float width, float height, Color color, int segments, float radiusTL, float radiusTR, float radiusBR, float radiusBL) {
        if (width <= 0.0f || height <= 0.0f) return;
        float halfMin = Math.min(width, height) / 2.0f;
        float rtl = Math.max(0.0f, Math.min(radiusTL, halfMin));
        float rtr = Math.max(0.0f, Math.min(radiusTR, halfMin));
        float rbr = Math.max(0.0f, Math.min(radiusBR, halfMin));
        float rbl = Math.max(0.0f, Math.min(radiusBL, halfMin));

        float a = color.getAlpha() / 255.0f;
        float cr = color.getRed() / 255.0f;
        float cg = color.getGreen() / 255.0f;
        float cb = color.getBlue() / 255.0f;

        with2DShaderState(() -> {
            ShaderUtils shader = getSolidCustomRoundShader();
            shader.init();
            shader.setUniformf("rectSize", width, height);
            shader.setUniformf("color", cr, cg, cb, a);
            shader.setUniformf("radiusTL", rtl);
            shader.setUniformf("radiusTR", rtr);
            shader.setUniformf("radiusBL", rbl);
            shader.setUniformf("radiusBR", rbr);
            RenderUtil.drawQuads(x, y, width, height);
            shader.unload();
        });
    }

    private static void drawQuarterFan(float cx, float cy, float r, double startDeg, double endDeg, int segments) {
        if (r <= 0.0f) return;
        glBegin(GL_TRIANGLE_FAN);
        glVertex2f(cx, cy);
        for (int i = 0; i <= segments; i++) {
            double t = i / (double) segments;
            double ang = Math.toRadians(startDeg + (endDeg - startDeg) * t);
            glVertex2d(cx + Math.cos(ang) * r, cy + Math.sin(ang) * r);
        }
        glEnd();
    }

    public static void drawRoundOutline(float x, float y, float width, float height, float radius, float thickness, Color fill, Color outline) {
        if (width <= 0.0f || height <= 0.0f) return;
        float r = Math.max(0.0f, Math.min(radius, Math.min(width, height) / 2.0f));
        float t = Math.max(0.0f, thickness);

        float fa = fill.getAlpha() / 255.0f;
        float fr = fill.getRed() / 255.0f;
        float fg = fill.getGreen() / 255.0f;
        float fb = fill.getBlue() / 255.0f;

        float oa = outline.getAlpha() / 255.0f;
        float or = outline.getRed() / 255.0f;
        float og = outline.getGreen() / 255.0f;
        float ob = outline.getBlue() / 255.0f;

        with2DShaderState(() -> {
            ShaderUtils shader = getOutlineCustomRoundShader();
            shader.init();
            shader.setUniformf("rectSize", width, height);
            shader.setUniformf("fillColor", fr, fg, fb, fa);
            shader.setUniformf("outlineColor", or, og, ob, oa);
            shader.setUniformf("thickness", t);
            shader.setUniformf("radiusTL", r);
            shader.setUniformf("radiusTR", r);
            shader.setUniformf("radiusBL", r);
            shader.setUniformf("radiusBR", r);
            RenderUtil.drawQuads(x, y, width, height);
            shader.unload();
        });
    }

    private static void arcToLine(float cx, float cy, float r, double startDeg, double endDeg, int segments) {
        if (r <= 0.0f) {
            glVertex2f(cx, cy);
            return;
        }
        for (int i = 0; i <= segments; i++) {
            double t = i / (double) segments;
            double ang = Math.toRadians(startDeg + (endDeg - startDeg) * t);
            glVertex2d(cx + Math.cos(ang) * r, cy + Math.sin(ang) * r);
        }
    }

    public static void drawCircle(float x, float y, float radius, float progress, float thickness, Color color, int unused) {
        if (radius <= 0.0f) return;
        float p = Math.max(0.0f, Math.min(1.0f, progress));
        float t = Math.max(0.0f, thickness);

        float a = color.getAlpha() / 255.0f;
        float cr = color.getRed() / 255.0f;
        float cg = color.getGreen() / 255.0f;
        float cb = color.getBlue() / 255.0f;

        float size = (radius + t) * 2.0f + 4.0f;
        float sx = x - size / 2.0f;
        float sy = y - size / 2.0f;

        with2DShaderState(() -> {
            ShaderUtils shader = getCircleShader();
            shader.init();
            shader.setUniformf("rectSize", size, size);
            shader.setUniformf("color", cr, cg, cb, a);
            shader.setUniformf("radius", radius);
            shader.setUniformf("thickness", t);
            shader.setUniformf("progress", p);
            RenderUtil.drawQuads(sx, sy, size, size);
            shader.unload();
        });
    }

    public static void drawGradientHorizontal(float x, float y, float width, float height, float radius, Color left, Color right) {
        drawGradientCustomRound(x, y, width, height, left, right, 24, radius, radius, radius, radius);
    }

    public static void drawGradientVertical(float x, float y, float width, float height, float radius, Color top, Color bottom) {
        drawGradientCustomRound(x, y, width, height, top, bottom, 24, radius, radius, radius, radius);
    }

    public static void beginRoundClip(float x, float y, float width, float height, float radius) {
        epilogue.util.render.StencilUtil.write(false);
        drawRoundMask(x, y, width, height, radius);
        epilogue.util.render.StencilUtil.erase(true);
    }

    public static void endRoundClip() {
        epilogue.util.render.StencilUtil.dispose();
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void drawGradientRound(float x, float y, float width, float height, float radius, Color c1, Color c2, boolean vertical) {
        if (vertical) {
            drawGradientCustomRound(x, y, width, height, c1, c2, 24, radius, radius, radius, radius);
        } else {
            drawGradientCustomRound(x, y, width, height, c1, c2, 24, radius, radius, radius, radius);
        }
    }

    public static void drawGradientCustomRound(float x, float y, float width, float height, Color color1, Color color2, int segments, float radiusTopLeft, float radiusTopRight, float radiusButtomLeft, float radiusButtomRight) {
        if (width <= 0.0f || height <= 0.0f) return;
        float halfMin = Math.min(width, height) / 2.0f;
        float rtl = Math.max(0.0f, Math.min(radiusTopLeft, halfMin));
        float rtr = Math.max(0.0f, Math.min(radiusTopRight, halfMin));
        float rbl = Math.max(0.0f, Math.min(radiusButtomLeft, halfMin));
        float rbr = Math.max(0.0f, Math.min(radiusButtomRight, halfMin));

        float c1a = color1.getAlpha() / 255.0f;
        float c1r = color1.getRed() / 255.0f;
        float c1g = color1.getGreen() / 255.0f;
        float c1b = color1.getBlue() / 255.0f;

        float c2a = color2.getAlpha() / 255.0f;
        float c2r = color2.getRed() / 255.0f;
        float c2g = color2.getGreen() / 255.0f;
        float c2b = color2.getBlue() / 255.0f;

        boolean blend = glIsEnabled(GL_BLEND);
        boolean tex = glIsEnabled(GL_TEXTURE_2D);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        ShaderUtils shader = getGradientCustomRoundShader();
        shader.init();
        shader.setUniformf("rectSize", width, height);
        shader.setUniformf("color1", c1r, c1g, c1b, c1a);
        shader.setUniformf("color2", c2r, c2g, c2b, c2a);
        shader.setUniformf("radiusTL", rtl);
        shader.setUniformf("radiusTR", rtr);
        shader.setUniformf("radiusBL", rbl);
        shader.setUniformf("radiusBR", rbr);

        RenderUtil.drawQuads(x, y, width, height);

        shader.unload();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        if (!blend) GlStateManager.disableBlend();
        if (!tex) GlStateManager.disableTexture2D();
    }
}
