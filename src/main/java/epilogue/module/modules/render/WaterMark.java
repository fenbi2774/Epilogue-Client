package epilogue.module.modules.render;

import epilogue.Epilogue;
import net.minecraft.client.Minecraft;
import epilogue.module.Module;
import epilogue.value.values.ModeValue;

public class WaterMark extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();
    public final ModeValue mode = new ModeValue("Mode", 0, new String[]{"Exhibition"});

    private float lastWidth = 0.0f;
    private float lastHeight = 0.0f;

    public WaterMark() {
        super("WaterMark", false);
    }

    public void render(float x, float y) {
        Interface interfaceModule = (Interface) Epilogue.moduleManager.getModule("Interface");
        int fps = Minecraft.getDebugFPS();

        int nColor = interfaceModule != null ? interfaceModule.color(0) : 0xFFFFFF;
        int whiteColor = 0xFFFFFF;
        int grayColor = 0xAAAAAA;

        mc.fontRendererObj.drawStringWithShadow("E", x, y, nColor);
        float nWidth = mc.fontRendererObj.getStringWidth("E");

        mc.fontRendererObj.drawStringWithShadow("pilogue ", x + nWidth, y, whiteColor);
        float nightSkyWidth = mc.fontRendererObj.getStringWidth("Epilogue ");

        mc.fontRendererObj.drawStringWithShadow("[", x + nightSkyWidth, y, grayColor);
        float bracketWidth = mc.fontRendererObj.getStringWidth("[");

        String fpsText = fps + " FPS";
        mc.fontRendererObj.drawStringWithShadow(fpsText, x + nightSkyWidth + bracketWidth, y, whiteColor);
        float fpsWidth = mc.fontRendererObj.getStringWidth(fpsText);

        mc.fontRendererObj.drawStringWithShadow("]", x + nightSkyWidth + bracketWidth + fpsWidth, y, grayColor);

        lastWidth = nightSkyWidth + bracketWidth + fpsWidth + mc.fontRendererObj.getStringWidth("]");
        lastHeight = mc.fontRendererObj.FONT_HEIGHT;
    }

    public float getLastWidth() {
        return lastWidth <= 0 ? 140f : lastWidth;
    }

    public float getLastHeight() {
        return lastHeight <= 0 ? 12f : lastHeight;
    }
}