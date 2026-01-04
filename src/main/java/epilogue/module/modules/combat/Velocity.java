package epilogue.module.modules.combat;

import com.google.common.base.CaseFormat;
import epilogue.Epilogue;
import epilogue.enums.DelayModules;
import epilogue.value.values.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.world.World;
import epilogue.event.EventTarget;
import epilogue.event.types.EventType;
import epilogue.events.*;
import epilogue.management.RotationState;
import epilogue.module.Module;

public class Velocity extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    private int chanceCounter = 0;
    private boolean pendingExplosion = false;
    private boolean allowNext = true;
    private int rotateTickCounter = 0;
    private float[] targetRotation = null;
    private double knockbackX = 0.0;
    private double knockbackZ = 0.0;
    private int advancedTimeWindowTicks = 0;
    private int delayTicksLeft = 0;
    private int airDelayTicksLeft = 0;
    private boolean delayedVelocityActive = false;
    private int attackReduceTicksLeft = 0;
    private boolean attackReduceApplied = false;

    public final ModeValue mode = new ModeValue("mode", 0, new String[]{"Vanilla", "JumpReset", "Mix"});

    // Vanilla
    public final PercentValue horizontal = new PercentValue("Horizontal", 100, () -> this.mode.getValue() == 0);
    public final PercentValue vertical = new PercentValue("Vertical", 100, () -> this.mode.getValue() == 0);
    public final PercentValue explosionHorizontal = new PercentValue("Explosions Horizontal", 100, () -> this.mode.getValue() == 0);
    public final PercentValue explosionVertical = new PercentValue("Explosions Vertical", 100, () -> this.mode.getValue() == 0);
    public final PercentValue chance = new PercentValue("Change", 100);
    public final BooleanValue fakeCheck = new BooleanValue("Check Fake", true);

    // JumpReset
    public final BooleanValue airDelay = new BooleanValue("Air Delay", false, () -> this.mode.getValue() == 1);
    public final IntValue airDelayTicks = new IntValue("Air Delay Ticks", 3, 1, 20, () -> this.mode.getValue() == 1 && this.airDelay.getValue());

    // Mix
    public final BooleanValue mixDelay = new BooleanValue("Delay", true, () -> this.mode.getValue() == 2);
    public final IntValue mixDelayTicks = new IntValue("Delay Ticks", 1, 1, 20, () -> this.mode.getValue() == 2 && this.mixDelay.getValue());
    public final BooleanValue mixDelayOnlyInGround = new BooleanValue("Delay Only In Ground", true, () -> this.mode.getValue() == 2 && this.mixDelay.getValue());
    public final BooleanValue mixJumpReset = new BooleanValue("Jump Reset", true, () -> this.mode.getValue() == 2);
    public final BooleanValue mixRotate = new BooleanValue("Rotate", false, () -> this.mode.getValue() == 2 && this.mixJumpReset.getValue());
    public final BooleanValue mixRotateOnlyInAir = new BooleanValue("Rotate Only In Air", false, () -> this.mode.getValue() == 2 && this.mixJumpReset.getValue() && this.mixRotate.getValue() && !this.mixRotateOnlyInGround.getValue());
    public final BooleanValue mixRotateOnlyInGround = new BooleanValue("Rotate Only In Ground", true, () -> this.mode.getValue() == 2 && this.mixJumpReset.getValue() && this.mixRotate.getValue() && !this.mixRotateOnlyInAir.getValue());
    public final BooleanValue mixAutoMove = new BooleanValue("Auto Move", true, () -> this.mode.getValue() == 2 && this.mixJumpReset.getValue() && this.mixRotate.getValue());
    public final IntValue mixRotateTicks = new IntValue("Rotate Ticks", 3, 1, 20, () -> this.mode.getValue() == 2 && this.mixJumpReset.getValue() && this.mixRotate.getValue());

    public Velocity() {
        super("Velocity", false);
    }

    private boolean isMix() { return this.mode.getValue() == 2; }

    private void startRotate(double knockbackX, double knockbackZ) {
        endRotate();
        this.knockbackX = knockbackX;
        this.knockbackZ = knockbackZ;
        if (Math.abs(this.knockbackX) > 0.01 || Math.abs(this.knockbackZ) > 0.01) {
            this.rotateTickCounter = 1;
            this.targetRotation = null;
        }
    }

    private void endRotate() {
        this.rotateTickCounter = 0;
        this.targetRotation = null;
        this.knockbackX = 0.0;
        this.knockbackZ = 0.0;
    }

    private void startDelayedVelocity(int ticks) {
        this.delayedVelocityActive = true;
        this.delayTicksLeft = Math.max(1, ticks);
    }

    private void queueDelayedVelocity(PacketEvent event, S12PacketEntityVelocity packet, int ticks) {
        Epilogue.delayManager.setDelayState(true, DelayModules.VELOCITY);
        Epilogue.delayManager.delayedPacket.offer(packet);
        event.setCancelled(true);
        this.startDelayedVelocity(ticks);
    }

    @EventTarget
    public void onKnockback(KnockbackEvent event) {
        if (!this.isEnabled() || event.isCancelled() || mc.thePlayer == null) {
            this.pendingExplosion = false;
            this.allowNext = true;
            this.endRotate();
            return;
        }

        if (!this.allowNext || !this.fakeCheck.getValue()) {
            this.allowNext = true;
            if (this.pendingExplosion) {
                this.pendingExplosion = false;
                this.handleExplosion(event);
            } else {
                this.chanceCounter = this.chanceCounter % 100 + this.chance.getValue();
                if (this.chanceCounter >= 100) {
                    boolean doJumpReset = (this.mode.getValue() == 1) || (this.isMix() && this.mixJumpReset.getValue());
                    boolean canDoJumpReset = doJumpReset && event.getY() > 0.0;

                    if (this.isMix() && this.mixJumpReset.getValue() && this.mixRotate.getValue() && canDoJumpReset) {
                        boolean shouldRotate = this.mixRotateOnlyInAir.getValue() ? !mc.thePlayer.onGround :
                                !this.mixRotateOnlyInGround.getValue() || mc.thePlayer.onGround;
                        if (shouldRotate) {
                            this.startRotate(event.getX(), event.getZ());
                        }
                    }

                    this.applyVanilla(event);
                    this.chanceCounter = 0;
                }
            }
        }
    }

    private void applyVanilla(KnockbackEvent event) {
        if (this.horizontal.getValue() > 0) {
            event.setX(event.getX() * this.horizontal.getValue() / 100.0);
            event.setZ(event.getZ() * this.horizontal.getValue() / 100.0);
        } else {
            event.setX(mc.thePlayer.motionX);
            event.setZ(mc.thePlayer.motionZ);
        }
        if (this.vertical.getValue() > 0) {
            event.setY(event.getY() * this.vertical.getValue() / 100.0);
        } else {
            event.setY(mc.thePlayer.motionY);
        }
    }

    private void handleExplosion(KnockbackEvent event) {
        if (this.explosionHorizontal.getValue() > 0) {
            event.setX(event.getX() * this.explosionHorizontal.getValue() / 100.0);
            event.setZ(event.getZ() * this.explosionHorizontal.getValue() / 100.0);
        } else {
            event.setX(mc.thePlayer.motionX);
            event.setZ(mc.thePlayer.motionZ);
        }
        if (this.explosionVertical.getValue() > 0) {
            event.setY(event.getY() * this.explosionVertical.getValue() / 100.0);
        } else {
            event.setY(mc.thePlayer.motionY);
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (!this.isEnabled() || mc.thePlayer == null) return;
        if (event.getType() != EventType.RECEIVE || event.isCancelled()) return;

        Packet<?> packet = event.getPacket();

        if (packet instanceof S12PacketEntityVelocity) {
            S12PacketEntityVelocity vel = (S12PacketEntityVelocity) packet;
            if (vel.getEntityID() != mc.thePlayer.getEntityId()) return;

            if (this.isMix()) {
                if (this.mixDelay.getValue()) {
                    if (!this.mixDelayOnlyInGround.getValue() || mc.thePlayer.onGround) {
                        this.queueDelayedVelocity(event, vel, this.mixDelayTicks.getValue());
                        return;
                    }
                }

                return;
            }

            if (this.mode.getValue() == 1 && this.airDelay.getValue() && !mc.thePlayer.onGround) {
                Epilogue.delayManager.setDelayState(true, DelayModules.VELOCITY);
                Epilogue.delayManager.delayedPacket.offer(vel);
                event.setCancelled(true);
                this.startDelayedVelocity(airDelayTicks.getValue());
                return;
            }
        }

        if (packet instanceof S19PacketEntityStatus) {
            S19PacketEntityStatus p = (S19PacketEntityStatus) packet;
            World world = mc.theWorld;
            if (world != null) {
                Entity entity = p.getEntity(world);
                if (entity != null && entity.equals(mc.thePlayer) && p.getOpCode() == 2) {
                    this.allowNext = false;
                }
            }
        }

        if (packet instanceof S27PacketExplosion) {
            S27PacketExplosion p = (S27PacketExplosion) packet;
            if (p.func_149149_c() != 0.0F || p.func_149144_d() != 0.0F || p.func_149147_e() != 0.0F) {
                this.pendingExplosion = true;
                if (this.explosionHorizontal.getValue() == 0 || this.explosionVertical.getValue() == 0) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (!this.isEnabled() || mc.thePlayer == null) return;

        if (event.getType() == EventType.POST) {
            if (this.attackReduceTicksLeft > 0) {
                this.attackReduceTicksLeft--;
                if (this.attackReduceTicksLeft <= 0) {
                    if (this.attackReduceApplied) {
                        Aura.attackBlocked = false;
                        Aura.swingBlocked = false;
                    }
                    this.attackReduceApplied = false;
                }
            }
        }

        if (this.isMix() && event.getType() == EventType.PRE) {
            if (this.advancedTimeWindowTicks > 0) {
                this.advancedTimeWindowTicks--;
            }
            if (mc.thePlayer.onGround && mc.gameSettings != null && mc.gameSettings.keyBindJump != null && mc.gameSettings.keyBindJump.isKeyDown()) {
                this.advancedTimeWindowTicks = 12;
            }

            int maxTick = this.mixRotateTicks.getValue();
            if (this.rotateTickCounter > 0 && this.rotateTickCounter <= maxTick) {
                if (this.rotateTickCounter == 1) {
                    double deltaX = -this.knockbackX;
                    double deltaZ = -this.knockbackZ;
                    this.targetRotation = epilogue.util.RotationUtil.getRotationsTo(deltaX, 0.0, deltaZ, event.getYaw(), event.getPitch());
                }
                if (this.targetRotation != null) {
                    event.setRotation(this.targetRotation[0], this.targetRotation[1], 2);
                    event.setPervRotation(this.targetRotation[0], 2);
                }
            }
        }

        if (this.isMix() && event.getType() == EventType.POST) {
            int maxTick = this.mixRotateTicks.getValue();
            if (this.rotateTickCounter > 0 && this.rotateTickCounter <= maxTick) {
                this.rotateTickCounter++;
                if (this.rotateTickCounter > maxTick) {
                    this.endRotate();
                }
            }

            if (this.delayedVelocityActive) {
                if (this.airDelayTicksLeft > 0) {
                    this.airDelayTicksLeft--;
                    if (this.airDelayTicksLeft <= 0) {
                        Epilogue.delayManager.setDelayState(false, DelayModules.VELOCITY);
                        this.delayedVelocityActive = false;
                    }
                } else if (this.delayTicksLeft > 0) {
                    this.delayTicksLeft--;
                    if (this.delayTicksLeft <= 0) {
                        Epilogue.delayManager.setDelayState(false, DelayModules.VELOCITY);
                        this.delayedVelocityActive = false;
                    }
                } else {
                    this.delayedVelocityActive = false;
                }
            }
        }
    }

    @EventTarget
    public void onMoveInput(MoveInputEvent event) {
        if (!this.isEnabled() || mc.thePlayer == null) return;

        if (this.isMix()) {
            int maxTick = this.mixRotateTicks.getValue();
            if (this.rotateTickCounter > 0 && this.rotateTickCounter <= maxTick) {
                if (this.mixAutoMove.getValue()) {
                    mc.thePlayer.movementInput.moveForward = 1.0F;
                }
                if (this.targetRotation != null && RotationState.isActived() && RotationState.getPriority() == 2.0F && epilogue.util.MoveUtil.isForwardPressed()) {
                    Aura aura = (Aura) Epilogue.moduleManager.modules.get(Aura.class);
                    if (aura != null && aura.isEnabled() && aura.moveFix.getValue() == 2 && aura.rotations.getValue() != 3) {
                        epilogue.util.MoveUtil.fixStrafe(RotationState.getSmoothedYaw());
                    }
                }
            }
        }
    }

    @Override
    public void onEnabled() {
        this.pendingExplosion = false;
        this.allowNext = true;
        this.chanceCounter = 0;
        this.rotateTickCounter = 0;
        this.targetRotation = null;
        this.knockbackX = 0.0;
        this.knockbackZ = 0.0;
        this.advancedTimeWindowTicks = 0;
        this.delayTicksLeft = 0;
        this.airDelayTicksLeft = 0;
        this.delayedVelocityActive = false;
        this.attackReduceTicksLeft = 0;
        this.attackReduceApplied = false;
        this.endRotate();
    }

    @Override
    public void onDisabled() {
        this.pendingExplosion = false;
        this.allowNext = true;
        this.chanceCounter = 0;
        this.rotateTickCounter = 0;
        this.targetRotation = null;
        this.knockbackX = 0.0;
        this.knockbackZ = 0.0;
        this.advancedTimeWindowTicks = 0;
        this.delayTicksLeft = 0;
        this.airDelayTicksLeft = 0;
        this.delayedVelocityActive = false;

        if (this.attackReduceTicksLeft > 0) {
            if (this.attackReduceApplied) {
                Aura.attackBlocked = false;
                Aura.swingBlocked = false;
            }
        }
        this.attackReduceTicksLeft = 0;
        this.attackReduceApplied = false;
        this.endRotate();
        if (Epilogue.delayManager.getDelayModule() == DelayModules.VELOCITY) {
            Epilogue.delayManager.setDelayState(false, DelayModules.VELOCITY);
        }
        Epilogue.delayManager.delayedPacket.clear();
    }

    @Override
    public String[] getSuffix() {
        String modeName = this.mode.getModeString();
        return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, modeName)};
    }
}