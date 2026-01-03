package epilogue.ui.clickgui.menu.component.settings;

import epilogue.ui.clickgui.menu.Fonts;
import epilogue.ui.clickgui.menu.component.SettingComponent;
import epilogue.ui.clickgui.menu.render.DrawUtil;
import epilogue.util.render.ColorUtil;
import epilogue.util.render.RenderUtil;
import epilogue.value.values.TextValue;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

public class TextComponent extends SettingComponent<TextValue> {
    private boolean focused;
    private final GuiTextField field;

    public TextComponent(TextValue setting) {
        super(setting);
        this.height = 30;
        this.field = new GuiTextField(0, mc.fontRendererObj, 0, 0, 0, 0);
        this.field.setMaxStringLength(32767);
    }

    @Override
    public void updateBounds(float x, float y, float width) {
        this.x = x;
        this.y = y;
        this.width = width;
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        if (alpha <= 0.0f) return;

        Fonts.draw(Fonts.small(), setting.getName(), x + 10, y + 4, ColorUtil.applyOpacity(0xFFFFFFFF, 0.4f * alpha));

        float boxX = x + 10;
        float boxY = y + 16;
        float boxW = 145;
        float boxH = 12;

        int bg = ColorUtil.applyOpacity(0xFFFFFFFF, 0.10f * alpha);
        int border = ColorUtil.applyOpacity(0xFFFFFFFF, 0.18f * alpha);
        RenderUtil.drawRect(boxX, boxY, boxW, boxH, bg);
        RenderUtil.drawRect(boxX, boxY, boxW, 1, border);
        RenderUtil.drawRect(boxX, boxY + boxH - 1, boxW, 1, border);
        RenderUtil.drawRect(boxX, boxY, 1, boxH, border);
        RenderUtil.drawRect(boxX + boxW - 1, boxY, 1, boxH, border);

        String value = setting.getValue() == null ? "" : setting.getValue();
        if (!value.equals(field.getText())) {
            field.setText(value);
        }

        field.xPosition = (int) (boxX + 4);
        field.yPosition = (int) (boxY + 2);
        field.width = (int) (boxW - 8);
        field.height = (int) (boxH - 4);
        field.setEnableBackgroundDrawing(false);
        field.setFocused(focused);
        field.setTextColor(ColorUtil.applyOpacity(0xFFFFFFFF, alpha));
        field.setDisabledTextColour(ColorUtil.applyOpacity(0xFFFFFFFF, alpha));

        field.drawTextBox();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        float boxX = x + 10;
        float boxY = y + 16;
        float boxW = 145;
        float boxH = 12;

        if (button == 0) {
            focused = DrawUtil.isHovering(boxX, boxY, boxW, boxH, mouseX, mouseY);
            if (focused) {
                Keyboard.enableRepeatEvents(true);
                field.mouseClicked(mouseX, mouseY, button);
            } else {
                Keyboard.enableRepeatEvents(false);
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (!focused) return;

        if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_RETURN) {
            focused = false;
            field.setFocused(false);
            Keyboard.enableRepeatEvents(false);
            return;
        }

        field.textboxKeyTyped(typedChar, keyCode);
        setting.setValue(field.getText());
    }

    @Override
    public boolean blocksScroll() {
        return focused;
    }
}
