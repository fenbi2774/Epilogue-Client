package epilogue.module.modules.render;

import epilogue.Epilogue;
import epilogue.event.EventTarget;
import epilogue.event.types.EventType;
import epilogue.events.PacketEvent;
import epilogue.events.Render2DEvent;
import epilogue.module.Module;
import epilogue.module.modules.combat.Aura;
import epilogue.ui.chat.GuiChat;
import epilogue.util.RenderUtil;
import epilogue.util.TeamUtil;
import epilogue.util.TimerUtil;
import epilogue.value.values.BooleanValue;
import epilogue.value.values.ModeValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C02PacketUseEntity.Action;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TargetHUD extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    private final TimerUtil lastAttackTimer = new TimerUtil();
    private final TimerUtil animTimer = new TimerUtil();
    private EntityLivingBase lastTarget = null;
    private EntityLivingBase target = null;

    private int lastTargetEntityId = -1;

    public final ModeValue mode = new ModeValue("Mode", 0, new String[]{"Exhibition"});
    public final BooleanValue shadow = new BooleanValue("Text Shadow", true, () -> this.mode.getValue() == 1);
    public final BooleanValue kaOnly = new BooleanValue("Only KillAura", true);
    private static final float SCALE = 1.0F;
    private float anchorX = 0.0f;
    private float anchorY = 0.0f;
    private float lastWidth = 0.0f;
    private float lastHeight = 0.0f;

    public void renderAt(float x, float y) {
        this.anchorX = x;
        this.anchorY = y;
        render(new Render2DEvent(0.0f));
    }

    public float getLastWidth() {
        return lastWidth <= 0 ? 180f : lastWidth;
    }

    public float getLastHeight() {
        return lastHeight <= 0 ? 80f : lastHeight;
    }

    private void setLastSize(float w, float h) {
        this.lastWidth = w;
        this.lastHeight = h;
    }

    private EntityLivingBase resolveTarget() {
        Aura aura = (Aura) Epilogue.moduleManager.modules.get(Aura.class);
        if (aura.isEnabled() && aura.isAttackAllowed() && TeamUtil.isEntityLoaded(aura.getTarget())) {
            return aura.getTarget();
        } else if (!(java.lang.Boolean) this.kaOnly.getValue()
                && !this.lastAttackTimer.hasTimeElapsed(1500L)
                && TeamUtil.isEntityLoaded(this.lastTarget)) {
            return this.lastTarget;
        } else {
            return (mc.currentScreen instanceof net.minecraft.client.gui.GuiChat || mc.currentScreen instanceof GuiChat) ? mc.thePlayer : null;
        }
    }

    public TargetHUD() {
        super("TargetHUD", false, true);
    }

    @EventTarget
    public void render(Render2DEvent event) {
        if (!this.isEnabled() || mc.thePlayer == null) {
            return;
        }

        EntityLivingBase currentTarget = this.resolveTarget();
        if (currentTarget == null && (mc.currentScreen instanceof net.minecraft.client.gui.GuiChat)) {
            currentTarget = mc.thePlayer;
        }

        if (currentTarget == null) {
            this.target = null;
            return;
        }

        if (this.target != currentTarget) {
            this.target = currentTarget;
            this.animTimer.setTime();
            this.lastTargetEntityId = currentTarget.getEntityId();
        } else {
            updateHurtTrigger(currentTarget);
        }

        switch (this.mode.getValue()) {
            case 0:
                renderExhibitionMode();
                break;
        }
    }

    private void updateHurtTrigger(EntityLivingBase currentTarget) {
        if (currentTarget == null) return;
        if (currentTarget.getEntityId() != lastTargetEntityId) {
            lastTargetEntityId = currentTarget.getEntityId();
        }
    }

    private float[] computePos() {
        return new float[]{anchorX / SCALE, anchorY / SCALE};
    }

    private void renderExhibitionMode() {
        if (!(this.target instanceof EntityPlayer)) return;
        ScaledResolution resolution = new ScaledResolution(mc);

        double boxWidth = 40 + mc.fontRendererObj.getStringWidth(target.getName());
        double renderWidth = Math.max(boxWidth, 120);

        setLastSize((float) renderWidth, 40.0F);

        float[] pos = computePos();
        float posX = pos[0];
        float posY = pos[1];

        GlStateManager.pushMatrix();
        GlStateManager.translate(posX + (float) renderWidth / 2.0F, posY + 20.0F, 0);
        float finalScale = SCALE;
        GlStateManager.scale(finalScale, finalScale, 1.0F);
        GlStateManager.translate(-(float) renderWidth / 2.0F, -20.0F, 0);

        drawExhibitionBorderedRect(-2.5F, -2.5F, (float) renderWidth + 2.5F, 40 + 2.5F, 0.5F, getExhibitionColor(60), getExhibitionColor(10));
        drawExhibitionBorderedRect(-1.5F, -1.5F, (float) renderWidth + 1.5F, 40 + 1.5F, 1.5F, getExhibitionColor(60), getExhibitionColor(40));
        drawExhibitionBorderedRect(0, 0, (float) renderWidth, 40, 0.5F, getExhibitionColor(22), getExhibitionColor(60));
        drawExhibitionBorderedRect(2, 2, 38, 38, 0.5F, getExhibitionColor(0, 0), getExhibitionColor(10));
        drawExhibitionBorderedRect(2.5F, 2.5F, 38 - 0.5F, 38 - 0.5F, 0.5F, getExhibitionColor(17), getExhibitionColor(48));

        GL11.glScissor((int) ((posX + 3) * resolution.getScaleFactor()), (int) ((resolution.getScaledHeight() - (posY + 37)) * resolution.getScaleFactor()), (int) ((37 - 3) * resolution.getScaleFactor()), (int) ((37 - 3) * resolution.getScaleFactor()));
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        drawEntityOnScreen(target);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GlStateManager.translate(2, 0, 0);

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.8F, 0.8F, 0.8F);
        mc.fontRendererObj.drawString(target.getName(), 46, 4, -1, this.shadow.getValue());
        GlStateManager.popMatrix();

        float health = target.getHealth();
        float absorption = target.getAbsorptionAmount();
        float progress = health / (target.getMaxHealth() + absorption);
        float realHealthProgress = (health / target.getMaxHealth());

        Color customColor = health >= 0 ? blendColors(new float[]{0f, 0.5f, 1f}, new Color[]{Color.RED, Color.YELLOW, Color.GREEN}, realHealthProgress).brighter() : Color.RED;
        double width = Math.max(mc.fontRendererObj.getStringWidth(target.getName()), 60);

        width = getIncremental(width, 10);
        if (width < 60) {
            width = 60;
        }
        double healthLocation = width * progress;

        drawExhibitionBorderedRect(37, 12, 39 + (float) width, 16, 0.5F, getExhibitionColor(0, 0), getExhibitionColor(0));
        drawExhibitionRect(38 + (float) healthLocation + 0.5F, 12.5F, 38 + (float) width + 0.5F, 15.5F, getExhibitionColorOpacity(customColor.getRGB(), 35));
        drawExhibitionRect(37.5F, 12.5F, 38 + (float) healthLocation + 0.5F, 15.5F, customColor.getRGB());

        if (absorption > 0) {
            double absorptionDifferent = width * (absorption / (target.getMaxHealth() + absorption));
            drawExhibitionRect(38 + (float) healthLocation + 0.5F, 12.5F, 38 + (float) healthLocation + 0.5F + (float) absorptionDifferent, 15.5F, 0x80FFAA00);
        }

        for (int i = 1; i < 10; i++) {
            double dThing = (width / 10) * i;
            drawExhibitionRect(38 + (float) dThing, 12, 38 + (float) dThing + 0.5F, 16, getExhibitionColor(0));
        }

        String str = "HP: " + (int) health + " | Dist: " + (int) mc.thePlayer.getDistanceToEntity(target);
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.7F, 0.7F, 0.7F);
        mc.fontRendererObj.drawString(str, 53, 26, -1, this.shadow.getValue());
        GlStateManager.popMatrix();

        if (target instanceof EntityPlayer) {
            EntityPlayer targetPlayer = (EntityPlayer) target;
            GL11.glPushMatrix();
            final List<ItemStack> items = new ArrayList<ItemStack>();
            int split = 20;

            for (int index = 3; index >= 0; --index) {
                final ItemStack armor = targetPlayer.inventory.armorInventory[index];
                if (armor != null) {
                    items.add(armor);
                }
            }
            int yOffset = 23;
            if (targetPlayer.getCurrentEquippedItem() != null) {
                items.add(targetPlayer.getCurrentEquippedItem());
            }

            RenderHelper.enableGUIStandardItemLighting();
            for (final ItemStack itemStack : items) {
                if (mc.theWorld != null) {
                    split += 16;
                }
                GlStateManager.pushMatrix();
                GlStateManager.disableAlpha();
                GlStateManager.clear(256);
                mc.getRenderItem().zLevel = -150.0f;
                mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, split, yOffset);
                mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, itemStack, split, yOffset);
                mc.getRenderItem().zLevel = 0.0f;

                int renderY = yOffset;
                if (itemStack.getItem() instanceof ItemSword) {
                    int sLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, itemStack);
                    int fLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, itemStack);
                    if (sLevel > 0) {
                        drawEnchantTag("S" + getSharpnessColor(sLevel) + sLevel, split, renderY);
                        renderY += 4.5F;
                    }
                    if (fLevel > 0) {
                        drawEnchantTag("F" + getFireAspectColor(fLevel) + fLevel, split, renderY);
                    }
                } else if ((itemStack.getItem() instanceof ItemArmor)) {
                    int pLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, itemStack);
                    if (pLevel > 0) {
                        drawEnchantTag("P" + getProtectionColor(pLevel) + pLevel, split, renderY);
                    }
                }

                GlStateManager.disableBlend();
                GlStateManager.disableLighting();
                GlStateManager.enableAlpha();
                GlStateManager.popMatrix();
            }
            RenderHelper.disableStandardItemLighting();
            GL11.glPopMatrix();
        }

        GlStateManager.popMatrix();
    }

    private double getIncremental(double value, double increment) {
        return Math.ceil(value / increment) * increment;
    }

    private void drawEntityOnScreen(EntityLivingBase ent) {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate(20.0F, 36.0F, 50.0F);

        float largestSize = Math.max(ent.height, ent.width);
        float relativeScale = Math.max(largestSize / 1.8F, 1);

        GlStateManager.scale((float) -16 / relativeScale, (float) 16 / relativeScale, (float) 16 / relativeScale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-((float) Math.atan((float) 17 / 40.0F)) * 20.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        RenderManager renderManager = mc.getRenderManager();
        renderManager.setPlayerViewY(180.0F);
        renderManager.setRenderShadow(false);
        renderManager.renderEntityWithPosYaw(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        renderManager.setRenderShadow(true);
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    private Color blendColors(float[] fractions, Color[] colors, float progress) {
        if (fractions.length == colors.length) {
            int[] indicies = getFractionIndicies(fractions, progress);
            float[] range = new float[]{fractions[indicies[0]], fractions[indicies[1]]};
            Color[] colorRange = new Color[]{colors[indicies[0]], colors[indicies[1]]};
            float max = range[1] - range[0];
            float value = progress - range[0];
            float weight = value / max;
            return blend(colorRange[0], colorRange[1], 1.0F - weight);
        } else {
            return colors[0];
        }
    }

    private int[] getFractionIndicies(float[] fractions, float progress) {
        int[] range = new int[2];
        int startPoint = 0;
        while (startPoint < fractions.length && fractions[startPoint] <= progress) {
            ++startPoint;
        }
        if (startPoint >= fractions.length) {
            startPoint = fractions.length - 1;
        }
        range[0] = startPoint - 1;
        range[1] = startPoint;
        return range;
    }

    private Color blend(Color color1, Color color2, double ratio) {
        float r = (float) ratio;
        float ir = 1.0F - r;
        float[] rgb1 = color1.getColorComponents(new float[3]);
        float[] rgb2 = color2.getColorComponents(new float[3]);
        return new Color(rgb1[0] * r + rgb2[0] * ir, rgb1[1] * r + rgb2[1] * ir, rgb1[2] * r + rgb2[2] * ir);
    }

    private void drawEnchantTag(String text, int x, float y) {
        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        mc.fontRendererObj.drawString(text, x * 2, y * 2, -1, true);
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    private String getProtectionColor(int level) {
        switch (level) {
            case 1:
                return "§a";
            case 2:
                return "§9";
            case 3:
                return "§e";
            case 4:
                return "§c";
            default:
                return "§f";
        }
    }

    private String getSharpnessColor(int level) {
        switch (level) {
            case 1:
                return "§a";
            case 2:
                return "§9";
            case 3:
                return "§e";
            case 4:
                return "§6";
            case 5:
                return "§c";
            default:
                return "§f";
        }
    }

    private String getFireAspectColor(int level) {
        switch (level) {
            case 1:
                return "§6";
            case 2:
                return "§c";
            default:
                return "§f";
        }
    }

    private int getExhibitionColor(int brightness) {
        return getExhibitionColor(brightness, brightness, brightness, 255);
    }

    private int getExhibitionColor(int brightness, int alpha) {
        return getExhibitionColor(brightness, brightness, brightness, alpha);
    }

    private int getExhibitionColor(int red, int green, int blue, int alpha) {
        int color = 0;
        color |= Math.max(0, Math.min(255, alpha)) << 24;
        color |= Math.max(0, Math.min(255, red)) << 16;
        color |= Math.max(0, Math.min(255, green)) << 8;
        color |= Math.max(0, Math.min(255, blue));
        return color;
    }

    private int getExhibitionColorOpacity(int color, int alpha) {
        int red = (color >> 16 & 0xFF);
        int green = (color >> 8 & 0xFF);
        int blue = (color & 0xFF);
        return getExhibitionColor(red, green, blue, Math.max(0, Math.min(255, alpha)));
    }

    private void drawExhibitionRect(float x1, float y1, float x2, float y2, int color) {
        RenderUtil.enableRenderState();
        RenderUtil.drawRect(x1, y1, x2, y2, color);
        RenderUtil.disableRenderState();
    }

    private void drawExhibitionBorderedRect(float x1, float y1, float x2, float y2, float borderWidth, int fillColor, int borderColor) {
        RenderUtil.enableRenderState();
        RenderUtil.drawRect(x1, y1, x2, y2, borderColor);
        RenderUtil.drawRect(x1 + borderWidth, y1 + borderWidth, x2 - borderWidth, y2 - borderWidth, fillColor);
        RenderUtil.disableRenderState();
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.getType() == EventType.SEND && event.getPacket() instanceof C02PacketUseEntity) {
            C02PacketUseEntity packet = (C02PacketUseEntity) event.getPacket();
            if (packet.getAction() != Action.ATTACK) {
                return;
            }
            Entity entity = packet.getEntityFromWorld(mc.theWorld);
            if (entity instanceof EntityLivingBase) {
                if (entity instanceof EntityArmorStand) {
                    return;
                }
                this.lastAttackTimer.reset();
                this.lastTarget = (EntityLivingBase) entity;
            }
        }
    }
}