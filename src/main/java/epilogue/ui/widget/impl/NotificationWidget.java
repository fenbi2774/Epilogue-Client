package epilogue.ui.widget.impl;

import epilogue.Epilogue;
import epilogue.ui.widget.Widget;
import epilogue.ui.widget.WidgetAlign;
import epilogue.module.Module;
import epilogue.module.modules.render.Notification;
import net.minecraft.client.gui.ScaledResolution;

public class NotificationWidget extends Widget {
    public NotificationWidget() {
        super("Notification", WidgetAlign.RIGHT | WidgetAlign.BOTTOM);
        this.x = 0.99f;
        this.y = 0.99f;
        this.width = 180f;
        this.height = 160f;
    }

    @Override
    protected float getHoverOffsetX() {
        ScaledResolution sr = new ScaledResolution(mc);
        float screenMid = sr.getScaledWidth() / 2.0f;
        float centerX = renderX + width / 2.0f;
        boolean renderRight = centerX >= screenMid;

        return renderRight ? -10.0f : 12.0f;
    }

    @Override
    protected float getHoverOffsetY() {
        ScaledResolution sr = new ScaledResolution(mc);
        float screenMid = sr.getScaledWidth() / 2.0f;
        float centerX = renderX + width / 2.0f;
        boolean renderRight = centerX >= screenMid;

        return renderRight ? -10.0f : -12.0f;
    }

    @Override
    public boolean shouldRender() {
        Module m = Epilogue.moduleManager.getModule("Notification");
        return m != null && m.isEnabled();
    }

    @Override
    public void render(float partialTicks) {
        Module m = Epilogue.moduleManager.getModule("Notification");
        if (!(m instanceof Notification)) return;

        Notification n = (Notification) m;

        ScaledResolution sr = new ScaledResolution(mc);
        float screenMid = sr.getScaledWidth() / 2.0f;
        float anchorY = renderY + height;
        float centerX = renderX + width / 2.0f;

        boolean renderRight = centerX >= screenMid;
        float anchorX = renderRight ? (renderX + width) : renderX;

        n.renderAt(anchorX, anchorY);

        float newW = n.getLastWidth();
        float newH = n.getLastHeight();
        this.width = newW;
        this.height = newH;

        if (renderRight) {
            this.renderX = anchorX - this.width;
        } else {
            this.renderX = anchorX;
        }
        if ((align & WidgetAlign.BOTTOM) != 0) {
            this.renderY = anchorY - this.height;
        }
    }
}
