package epilogue.module.modules.render;

import net.minecraft.client.Minecraft;
import epilogue.module.Module;
import epilogue.value.values.IntValue;

import epilogue.event.EventTarget;
import epilogue.events.Render2DEvent;

public class WorldTime extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    public final IntValue time = new IntValue("Time", 0, 0, 24000);

    private long lastApplied = -1;

    public WorldTime() {
        super("WorldTime", false);
    }

    @Override
    public void onEnabled() {
        lastApplied = -1;
        applyIfPossible();
    }

    @Override
    public void onDisabled() {
        lastApplied = -1;
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        applyIfPossible();
    }

    private void applyIfPossible() {
        if (mc.theWorld == null) return;

        long target = normalizeTime(time.getValue());
        if (lastApplied == target) {
            return;
        }

        try {
            mc.theWorld.setWorldTime(target);
            lastApplied = target;
        } catch (Throwable ignored) {
        }
    }

    private long normalizeTime(int raw) {
        int t = raw;
        if (t < 0) t = 0;
        if (t > 24000) t = 24000;
        return t;
    }
}
