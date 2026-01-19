package epilogue.ui.widget.impl;

import epilogue.Epilogue;
import epilogue.ui.widget.Widget;
import epilogue.ui.widget.WidgetAlign;
import epilogue.module.Module;
import epilogue.module.modules.render.WaterMark;

public class WaterMarkWidget extends Widget {
    public WaterMarkWidget() {
        super("WaterMark", WidgetAlign.LEFT | WidgetAlign.TOP);
        this.x = 0.01f;
        this.y = 0.01f;
        this.width = 120f;
        this.height = 12f;
    }

    @Override
    public boolean shouldRender() {
        Module m = Epilogue.moduleManager.getModule("WaterMark");
        return m != null && m.isEnabled();
    }

    @Override
    public void render(float partialTicks) {
        Module m = Epilogue.moduleManager.getModule("WaterMark");
        if (!(m instanceof WaterMark)) return;
        WaterMark wm = (WaterMark) m;
        wm.render(renderX, renderY, partialTicks);
        this.width = wm.getLastWidth();
        this.height = wm.getLastHeight();
    }
}
