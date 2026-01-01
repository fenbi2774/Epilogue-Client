package epilogue.hud.widget.impl;

import epilogue.Epilogue;
import epilogue.hud.widget.Widget;
import epilogue.hud.widget.WidgetAlign;
import epilogue.module.Module;
import epilogue.module.modules.render.TargetHUD;
import net.minecraft.client.gui.ScaledResolution;

public class TargetHUDWidget extends Widget {
    public TargetHUDWidget() {
        super("TargetHUD", WidgetAlign.LEFT | WidgetAlign.BOTTOM);
        this.x = 0.02f;
        this.y = 0.75f;
        this.width = 160f;
        this.height = 60f;
    }

    @Override
    public boolean shouldRender() {
        Module m = Epilogue.moduleManager.getModule("TargetHUD");
        return m != null && m.isEnabled();
    }

    @Override
    public void render(float partialTicks) {
        Module m = Epilogue.moduleManager.getModule("TargetHUD");
        if (!(m instanceof TargetHUD)) return;
        TargetHUD th = (TargetHUD) m;

        float anchorX = renderX;
        float anchorY = renderY;

        th.renderAt(anchorX, anchorY);

        this.width = th.getLastWidth();
        this.height = th.getLastHeight();

        if ((align & WidgetAlign.BOTTOM) != 0) {
            this.renderY = anchorY - this.height;
        }
    }
}
