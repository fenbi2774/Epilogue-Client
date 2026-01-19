package epilogue.module.modules.render;

import epilogue.ui.chat.GuiChat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.EnumChatFormatting;
import epilogue.module.Module;
import epilogue.value.values.BooleanValue;
import epilogue.value.values.IntValue;
import epilogue.util.render.RenderUtil;
import epilogue.util.render.PostProcessing;
import epilogue.util.render.RoundedUtil;

import java.util.Collection;
import java.util.List;
import java.awt.Color;

public class Scoreboard extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();
    
    public final BooleanValue hide = new BooleanValue("Hide", false);
    public final BooleanValue removeRedScore = new BooleanValue("Remove Red Score", false);
    public final IntValue round = new IntValue("Round", 0, 0, 12);
    
    public static Scoreboard instance;
    private float lastWidth;
    private float lastHeight;
    
    public Scoreboard() {
        super("Scoreboard", false);
        instance = this;
    }
    
    public boolean shouldHideOriginalScoreboard() {
        return this.isEnabled();
    }

    public void renderAt(float anchorRightX, float anchorTopY) {
        if (mc.theWorld == null || mc.gameSettings.showDebugInfo) return;
        if (hide.getValue()) return;

        FontRenderer fr = mc.fontRendererObj;
        int fontHeight = fr.FONT_HEIGHT;

        boolean preview = (mc.currentScreen instanceof net.minecraft.client.gui.GuiChat) || (mc.currentScreen instanceof GuiChat);

        ScoreObjective scoreobjective = mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(1);
        if (scoreobjective != null) {
            net.minecraft.scoreboard.Scoreboard scoreboard = scoreobjective.getScoreboard();
            Collection<Score> collection = scoreboard.getSortedScores(scoreobjective);
            List<Score> list = com.google.common.collect.Lists.newArrayList(com.google.common.collect.Iterables.filter(collection, new com.google.common.base.Predicate<Score>() {
                public boolean apply(Score score) {
                    return score.getPlayerName() != null && !score.getPlayerName().startsWith("#");
                }
            }));
            
            if (list.size() > 15) {
                collection = com.google.common.collect.Lists.newArrayList(com.google.common.collect.Iterables.skip(list, collection.size() - 15));
            } else {
                collection = list;
            }
            
            int maxWidth = fr.getStringWidth(scoreobjective.getDisplayName());
            
            for (Score score : collection) {
                ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(score.getPlayerName());
                String s1 = removeRedScore.getValue()
                        ? ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.getPlayerName())
                        : (ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.getPlayerName()) + ": " + EnumChatFormatting.RED + score.getScorePoints());
                maxWidth = Math.max(maxWidth, fr.getStringWidth(s1));
            }
            
            int listSize = collection.size();
            int height = listSize * fontHeight;
            int paddingX = 4;
            int maxWidthPadded = maxWidth + paddingX * 2;

            float drawW = (maxWidthPadded + 4);
            float drawH = (height + fontHeight + 2);

            float drawX = anchorRightX - drawW;
            float drawY = anchorTopY;
            float yOffset = -12.0f;
            drawY += yOffset;
            
            int backgroundColor = 0x90505050;

            Framebuffer bloomBuffer;
            int r = Math.max(0, round.getValue());
            float scaledR = r;
            bloomBuffer = PostProcessing.beginBloom();
            if (bloomBuffer != null) {
                int glowColor = epilogue.module.modules.render.PostProcessing.getBloomColor();
                if (r > 0) {
                    RoundedUtil.drawRound(drawX, drawY, drawW, drawH, scaledR, new Color(glowColor, true));
                } else {
                    RenderUtil.drawRect(drawX, drawY, drawW, drawH, glowColor);
                }
                mc.getFramebuffer().bindFramebuffer(false);
            }
            
            GlStateManager.enableBlend();
            if (r > 0) {
                float finalDrawY = drawY;
                PostProcessing.drawBlur(drawX, drawY, drawX + drawW, drawY + drawH, () -> () ->
                        RoundedUtil.drawRound(drawX, finalDrawY, drawW, drawH, scaledR, new Color(-1, true))
                );
            } else {
                PostProcessing.drawBlurRect(drawX, drawY, drawX + drawW, drawY + drawH);
            }
            if (r > 0) {
                RoundedUtil.drawRound(drawX, drawY, drawW, drawH, scaledR, new Color(backgroundColor, true));
            } else {
                Gui.drawRect((int) drawX, (int) drawY, (int) (drawX + drawW), (int) (drawY + drawH), backgroundColor);
            }
            
            PostProcessing.endBloom(bloomBuffer);
            
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            String titleText = scoreobjective.getDisplayName();

            float basePanelX = drawX;
            float basePanelY = drawY;
            float basePanelW = drawW;
            float bodyOffsetY = 12.0f;
            float titleX = basePanelX + basePanelW / 2f - fr.getStringWidth(titleText) / 2f;
            float titleY = basePanelY;
            fr.drawStringWithShadow(titleText, titleX, titleY, 0xFFFFFFFF);
            
            int index = 0;
            for (Score score : collection) {
                ++index;
                ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(score.getPlayerName());
                String playerName = ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.getPlayerName());
                String scoreValue = "" + EnumChatFormatting.RED + score.getScorePoints();
                float baseYPos = basePanelY + bodyOffsetY + (listSize - index) * fontHeight;
                float textX = basePanelX + paddingX;
                float yPos = baseYPos;
                fr.drawStringWithShadow(playerName, textX, yPos, 0xFFFFFFFF);
                if (!removeRedScore.getValue()) {
                    float sx = basePanelX + maxWidthPadded - paddingX - fr.getStringWidth(scoreValue);
                    fr.drawStringWithShadow(scoreValue, sx, yPos, 0xFFFFFFFF);
                }
            }
            GlStateManager.disableBlend();

            lastWidth = drawW;
            lastHeight = drawH;
        } else if (preview) {
            String title = "Test Server";
            String l1 = "PlayerOne: " + EnumChatFormatting.RED + "20";
            String l2 = "PlayerTwo: " + EnumChatFormatting.RED + "15";
            String l3 = "PlayerThree: " + EnumChatFormatting.RED + "7";

            int maxWidth = fr.getStringWidth(title);
            maxWidth = Math.max(maxWidth, fr.getStringWidth(l1));
            maxWidth = Math.max(maxWidth, fr.getStringWidth(l2));
            maxWidth = Math.max(maxWidth, fr.getStringWidth(l3));

            int listSize = 3;
            int height = listSize * fontHeight;
            int paddingX = 4;
            int maxWidthPadded = maxWidth + paddingX * 2;

            float drawW = (maxWidthPadded + 4);
            float drawH = (height + fontHeight + 2);
            float drawX = anchorRightX - drawW;
            float drawY = anchorTopY;
            float yOffset = -4.0f;
            drawY += yOffset;

            int backgroundColor = 0x90505050;
            int r = Math.max(0, round.getValue());
            float scaledR = r;

            Framebuffer bloomBuffer = PostProcessing.beginBloom();
            if (bloomBuffer != null) {
                int glowColor = epilogue.module.modules.render.PostProcessing.getBloomColor();
                if (r > 0) {
                    RoundedUtil.drawRound(drawX, drawY, drawW, drawH, scaledR, new Color(glowColor, true));
                } else {
                    RenderUtil.drawRect(drawX, drawY, drawW, drawH, glowColor);
                }
                mc.getFramebuffer().bindFramebuffer(false);
            }

            GlStateManager.enableBlend();
            if (r > 0) {
                float finalDrawY = drawY;
                PostProcessing.drawBlur(drawX, drawY, drawX + drawW, drawY + drawH, () -> () ->
                        RoundedUtil.drawRound(drawX, finalDrawY, drawW, drawH, scaledR, new Color(-1, true))
                );
            } else {
                PostProcessing.drawBlurRect(drawX, drawY, drawX + drawW, drawY + drawH);
            }
            if (r > 0) {
                RoundedUtil.drawRound(drawX, drawY, drawW, drawH, scaledR, new Color(backgroundColor, true));
            } else {
                Gui.drawRect((int) drawX, (int) drawY, (int) (drawX + drawW), (int) (drawY + drawH), backgroundColor);
            }

            PostProcessing.endBloom(bloomBuffer);

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            float basePanelX = drawX;
            float basePanelY = drawY;
            float basePanelW = drawW;
            float bodyOffsetY = 3.0f;

            float tx = basePanelX + basePanelW / 2f - fr.getStringWidth(title) / 2f;
            float ty = basePanelY;
            fr.drawStringWithShadow(title, tx, ty, 0xFFFFFFFF);

            float bx = basePanelX + paddingX;
            fr.drawStringWithShadow(l3, bx, basePanelY + bodyOffsetY + 0 * fontHeight, 0xFFFFFFFF);
            fr.drawStringWithShadow(l2, bx, basePanelY + bodyOffsetY + 1 * fontHeight, 0xFFFFFFFF);
            fr.drawStringWithShadow(l1, bx, basePanelY + bodyOffsetY + 2 * fontHeight, 0xFFFFFFFF);
            GlStateManager.disableBlend();

            lastWidth = drawW;
            lastHeight = drawH;
        }
    }

    public float getLastWidth() {
        return lastWidth <= 0 ? 140f : lastWidth;
    }

    public float getLastHeight() {
        return lastHeight <= 0 ? 160f : lastHeight;
    }
}
