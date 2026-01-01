package epilogue.ui.widget.impl;

import epilogue.Epilogue;
import epilogue.ui.widget.Widget;
import epilogue.ui.widget.WidgetAlign;
import epilogue.module.Module;
import epilogue.module.modules.render.Session;

public class SessionWidget extends Widget {
    public SessionWidget() {
        super("Session", WidgetAlign.RIGHT | WidgetAlign.TOP);
        this.x = 0.98f;
        this.y = 0.02f;
        this.width = 200f;
        this.height = 110f;
    }

    @Override
    public boolean shouldRender() {
        Module m = Epilogue.moduleManager.getModule("Session");
        return m != null && m.isEnabled();
    }

    @Override
    public void render(float partialTicks) {
        Module m = Epilogue.moduleManager.getModule("Session");
        if (!(m instanceof Session)) return;
        Session s = (Session) m;
        s.renderAt(renderX, renderY);
        this.width = s.getLastWidth();
        this.height = s.getLastHeight();
    }
}
