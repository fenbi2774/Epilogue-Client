package epilogue.hud.widget.impl;

import epilogue.Epilogue;
import epilogue.hud.widget.Widget;
import epilogue.hud.widget.WidgetAlign;
import epilogue.module.Module;
import epilogue.module.modules.render.PotionEffects;

public class PotionEffectsWidget extends Widget {
    public PotionEffectsWidget() {
        super("PotionEffects", WidgetAlign.RIGHT | WidgetAlign.TOP);
        this.x = 0.98f;
        this.y = 0.20f;
        this.width = 140f;
        this.height = 120f;
    }

    @Override
    protected float getHoverExtraWidth() {
        return 12.0f;
    }

    @Override
    public boolean shouldRender() {
        Module m = Epilogue.moduleManager.getModule("PotionEffects");
        return m != null && m.isEnabled();
    }

    @Override
    public void render(float partialTicks) {
        Module m = Epilogue.moduleManager.getModule("PotionEffects");
        if (!(m instanceof PotionEffects)) return;
        PotionEffects p = (PotionEffects) m;

        float anchorRightX = renderX + width;
        float anchorTopY = renderY;

        float currentW = p.getCurrentWidth();
        p.renderAt(anchorRightX - currentW, anchorTopY);

        float newW = p.getCurrentWidth();
        float newH = p.getCurrentHeight();
        this.width = newW;
        this.height = newH;

        if ((align & WidgetAlign.RIGHT) != 0) {
            this.renderX = anchorRightX - this.width;
        }
    }
}
