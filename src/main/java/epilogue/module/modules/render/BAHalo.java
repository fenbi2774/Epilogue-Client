package epilogue.module.modules.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import epilogue.event.EventTarget;
import epilogue.events.Render3DEvent;
import epilogue.module.Module;
import epilogue.value.values.*;
import epilogue.util.render.halo.HaloRenderer;

public class BAHalo extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private final HaloRenderer haloRenderer = new HaloRenderer();

    public final ModeValue style = new ModeValue("style", 0, new String[]{"Sunaookami Shiroko", "Kuromi Serika", "Takanashi Hoshino", "Opai Logo"});
    public final BooleanValue inFirstPerson = new BooleanValue("first-person", false);
    public final BooleanValue followPitch = new BooleanValue("follow-pitch", false);
    public final FloatValue size = new FloatValue("size", 1.5f, 0.1f, 3.0f);
    public final FloatValue spacing = new FloatValue("spacing", 0.65f, 0.0f, 10.0f);
    public final FloatValue xRot = new FloatValue("x-rot", 0f, -90f, 90f);
    public final FloatValue yRot = new FloatValue("y-rot", 0f, -90f, 90f);

    public BAHalo() {
        super("BAHalo", false);
        this.initHalos();
    }

    private void initHalos() {
        this.haloRenderer.addHalo("Sunaookami Shiroko", new HaloRenderer.HaloData(0.08,
                "epilogue/texture/halo/shiroko/layer0.png", "epilogue/texture/halo/shiroko/layer1.png"));
        this.haloRenderer.addHalo("Kuromi Serika", new HaloRenderer.HaloData(0.08,
                "epilogue/texture/halo/serika/layer0.png", "epilogue/texture/halo/serika/layer1.png"));
        this.haloRenderer.addHalo("Takanashi Hoshino", new HaloRenderer.HaloData(0.06,
                "epilogue/texture/halo/hoshino/layer0.png", "epilogue/texture/halo/hoshino/layer1.png", "epilogue/texture/halo/hoshino/layer2.png"));
        this.haloRenderer.addHalo("Opai Logo", new HaloRenderer.HaloData("epilogue/texture/halo/logo.png"));
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (!this.isEnabled()) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (mc.gameSettings.thirdPersonView == 0 && !inFirstPerson.getValue()) return;

        this.haloRenderer.setCurrentHalo(this.haloRenderer.getHaloDataMap().get(style.getModeString()));

        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player.isInvisible() || player.isDead) continue;
            if (player != mc.thePlayer) continue;

            renderHalo(player, event.getPartialTicks());
        }
    }

    private void renderHalo(EntityPlayer player, float partialTicks) {
        double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks - mc.getRenderManager().viewerPosX;
        double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks - mc.getRenderManager().viewerPosY;
        double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks - mc.getRenderManager().viewerPosZ;

        float height = player.height + 0.3f;
        if (player.isSneaking()) height -= 0.2f;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + height, z);

        float yaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partialTicks;
        float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;

        GlStateManager.rotate(-yaw, 0, 1, 0);
        if (followPitch.getValue()) {
            GlStateManager.rotate(pitch, 1, 0, 0);
        }

        GlStateManager.rotate(xRot.getValue(), 1, 0, 0);
        GlStateManager.rotate(yRot.getValue(), 0, 0, 1);

        GlStateManager.translate(0, spacing.getValue(), 0);

        GlStateManager.rotate(90, 1, 0, 0);

        boolean cullFace = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        if (cullFace) {
            GlStateManager.disableCull();
        }

        this.haloRenderer.render(this);

        if (cullFace) {
            GlStateManager.enableCull();
        }

        GlStateManager.popMatrix();
    }
}