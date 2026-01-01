package epilogue.module.modules.movement;

import epilogue.Epilogue;
import epilogue.enums.BlinkModules;
import net.minecraft.client.Minecraft;
import epilogue.event.EventTarget;
import epilogue.events.LivingUpdateEvent;
import epilogue.events.MoveInputEvent;
import epilogue.events.StrafeEvent;
import epilogue.events.UpdateEvent;
import epilogue.module.Module;

public class Stuck extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private double savedMotionX;
    private double savedMotionY;
    private double savedMotionZ;

    public Stuck() {
        super("Stuck", false);
    }

    @Override
    public void onEnabled() {
        if (mc.thePlayer != null) {
            savedMotionX = mc.thePlayer.motionX;
            savedMotionY = mc.thePlayer.motionY;
            savedMotionZ = mc.thePlayer.motionZ;
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (this.isEnabled()) {
            Epilogue.blinkManager.setBlinkState(true, BlinkModules.BLINK);
            mc.thePlayer.motionX = 0.0;
            mc.thePlayer.motionZ = 0.0;
            mc.thePlayer.motionY = 0.0;
        }
    }

    @EventTarget
    public void onMoveInput(MoveInputEvent event) {
        if (this.isEnabled()) {
            mc.thePlayer.movementInput.moveForward = 0.0f;
            mc.thePlayer.movementInput.moveStrafe = 0.0f;
            mc.thePlayer.movementInput.jump = false;
            mc.thePlayer.movementInput.sneak = false;
        }
    }

    @EventTarget
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (this.isEnabled()) {
            mc.thePlayer.motionX = 0.0;
            mc.thePlayer.motionY = 0.0;
            mc.thePlayer.motionZ = 0.0;
        }
    }

    @EventTarget
    public void onStrafe(StrafeEvent event) {
        if (this.isEnabled()) {
            event.setForward(0.0f);
            event.setStrafe(0.0f);
        }
    }

    @Override
    public void onDisabled() {
        if (mc.thePlayer != null) {
            Epilogue.blinkManager.setBlinkState(false, BlinkModules.BLINK);
            mc.thePlayer.motionX = savedMotionX;
            mc.thePlayer.motionZ = savedMotionZ;
            mc.thePlayer.motionY = savedMotionY;
        }
    }
}