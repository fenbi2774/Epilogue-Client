package epilogue.hud.widget.impl;

import epilogue.Epilogue;
import epilogue.hud.widget.Widget;
import epilogue.hud.widget.WidgetAlign;
import epilogue.module.Module;
import epilogue.module.modules.render.Scoreboard;
import net.minecraft.client.gui.ScaledResolution;

public class ScoreboardWidget extends Widget {
    public ScoreboardWidget() {
        super("Scoreboard", WidgetAlign.RIGHT | WidgetAlign.TOP);
        this.x = 0.99f;
        this.y = 0.40f;
        this.width = 140f;
        this.height = 160f;
    }

    @Override
    protected float getHoverOffsetY() {
        return -10.0f;
    }

    @Override
    public boolean shouldRender() {
        Module m = Epilogue.moduleManager.getModule("Scoreboard");
        return m != null && m.isEnabled() && m instanceof Scoreboard && !((Scoreboard) m).hide.getValue();
    }

    @Override
    public void render(float partialTicks) {
        Module m = Epilogue.moduleManager.getModule("Scoreboard");
        if (!(m instanceof Scoreboard)) return;
        Scoreboard sb = (Scoreboard) m;

        float anchorRightX = renderX + width;
        float anchorTopY = renderY;

        sb.renderAt(anchorRightX, anchorTopY);

        float newW = sb.getLastWidth();
        float newH = sb.getLastHeight();
        this.width = newW;
        this.height = newH;

        if ((align & WidgetAlign.RIGHT) != 0) {
            this.renderX = anchorRightX - this.width;
        }
    }
}
