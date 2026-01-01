package epilogue.util.render.halo;

import epilogue.module.modules.render.BAHalo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class HaloRenderer {

    private final Map<String, HaloData> haloDataMap = new HashMap<>();
    private final List<String> haloNames = new ArrayList<>();
    private HaloData currentHalo = null;

    final Animation floatingAnimation = new Animation(Easing.EASE_IN_OUT_CUBIC, 2000000000L);
    boolean animationBackwards = false;

    public void render(BAHalo module) {
        double animationHeight = 0.1;
        floatingAnimation.run(animationBackwards ? 0 : animationHeight);

        if (floatingAnimation.isFinished()) {
            animationBackwards = !animationBackwards;
        }

        if (this.currentHalo == null) {
            if (this.haloDataMap.isEmpty())
                return;
            this.currentHalo = this.haloDataMap.get(this.haloNames.get(0));
        }

        GlStateManager.pushMatrix();

        GlStateManager.rotate(-90, 1, 0, 0);
        GlStateManager.translate(0, (float) floatingAnimation.getValue(), 0);
        GlStateManager.rotate(90, 1, 0, 0);

        float imgWidth = module.size.getValue().floatValue();
        float imgHeight = module.size.getValue().floatValue();

        boolean maskEnabled = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);

        if (maskEnabled)
            GlStateManager.depthMask(false);

        currentHalo.render(module, imgWidth, imgHeight);

        if (maskEnabled)
            GlStateManager.depthMask(true);

        GlStateManager.popMatrix();
    }

    public void addHalo(String name, HaloData data) {
        haloDataMap.put(name, data);
        this.haloNames.add(name);
    }

    public Map<String, HaloData> getHaloDataMap() {
        return haloDataMap;
    }

    public List<String> getHaloNames() {
        return haloNames;
    }

    public void setCurrentHalo(HaloData currentHalo) {
        this.currentHalo = currentHalo;
    }

    public static class HaloData {
        public final boolean layered;
        private final List<ResourceLocation> textures = new ArrayList<>();
        public double spacing = 0.0;

        public HaloData(String textureLocation) {
            this.textures.add(new ResourceLocation(textureLocation));
            this.layered = false;
        }

        public HaloData(double spacing, String... textureLocations) {
            this.spacing = spacing;
            this.layered = true;
            for (String location : textureLocations) {
                this.textures.add(new ResourceLocation(location));
            }
        }

        private void renderImage(float x, float y, float width, float height) {
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(0, 1);
            GL11.glVertex2f(x, y + height);
            GL11.glTexCoord2f(1, 1);
            GL11.glVertex2f(x + width, y + height);
            GL11.glTexCoord2f(1, 0);
            GL11.glVertex2f(x + width, y);
            GL11.glTexCoord2f(0, 0);
            GL11.glVertex2f(x, y);
            GL11.glEnd();
        }

        public void render(BAHalo module, float imgWidth, float imgHeight) {
            GlStateManager.enableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
            GlStateManager.color(1, 1, 1, 1);

            for (ResourceLocation texture : this.textures) {
                Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
                this.renderImage(-imgWidth * 0.5f, -imgHeight * 0.5f, imgWidth, imgHeight);

                GlStateManager.rotate(-90, 1, 0, 0);
                GlStateManager.translate(0, (float) (this.spacing * (module.size.getValue() / 1.5)), 0);
                GlStateManager.rotate(90, 1, 0, 0);
            }
        }
    }
}