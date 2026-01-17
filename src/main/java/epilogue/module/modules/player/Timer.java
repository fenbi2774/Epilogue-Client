package epilogue.module.modules.player;

import epilogue.event.EventTarget;
import epilogue.events.UpdateEvent;
import epilogue.module.Module;
import epilogue.value.values.FloatValue;
import epiloguemixinbridge.IAccessorMinecraft;

import static epilogue.util.MinecraftInstance.mc;

public class Timer extends Module {

    private final FloatValue timer = new FloatValue("Timer", 1.0F, 0.0F, 10.0F);

    public Timer() {
        super("Timer", false);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (!this.isEnabled()) return;
        ((IAccessorMinecraft) mc).getTimer().timerSpeed = timer.getValue();
    }

    @Override
    public void onDisabled() {
        ((IAccessorMinecraft) mc).getTimer().timerSpeed = 1.0F;
    }
}