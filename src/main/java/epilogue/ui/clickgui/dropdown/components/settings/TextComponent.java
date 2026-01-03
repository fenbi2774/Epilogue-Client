package epilogue.ui.clickgui.dropdown.components.settings;

import epilogue.ui.clickgui.dropdown.components.SettingComponent;
import epilogue.ui.clickgui.dropdown.utils.DropdownFontRenderer;
import epilogue.util.render.ColorUtil;
import epilogue.value.values.TextValue;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class TextComponent extends SettingComponent<TextValue> {
    private boolean focused;
    private final GuiTextField field;

    public TextComponent(TextValue value) {
        super(value);
        this.field = new GuiTextField(0, mc.fontRendererObj, 0, 0, 0, 0);
        this.field.setMaxStringLength(32767);
    }

    @Override
    public void initGui() {
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (!focused) return;

        typing = true;

        if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_RETURN) {
            focused = false;
            typing = false;
            field.setFocused(false);
            Keyboard.enableRepeatEvents(false);
            return;
        }

        field.textboxKeyTyped(typedChar, keyCode);
        applyValueChange(field.getText());
    }

    @Override
    public void updateCountSize() {
        countSize = 1.2f;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        String value = getSetting().getValue() == null ? "" : getSetting().getValue();

        Color nameColor = ColorUtil.applyOpacity(textColor, alpha * 0.7f);
        DropdownFontRenderer.drawString(getSetting().getName(), x + 5, y + 2, nameColor.getRGB(), 18);

        float boxX = x + 5;
        float boxY = y + 10;
        float boxW = width - 10;
        float boxH = 9;

        Color outline = focused ? ColorUtil.applyOpacity(Color.WHITE, alpha * 0.9f)
                : ColorUtil.applyOpacity(settingRectColor.brighter(), alpha * 0.8f);
        Color fill = ColorUtil.applyOpacity(settingRectColor, alpha * 0.6f);

        net.minecraft.client.gui.Gui.drawRect((int) boxX, (int) boxY, (int) (boxX + boxW), (int) (boxY + boxH), outline.getRGB());
        net.minecraft.client.gui.Gui.drawRect((int) (boxX + 0.5f), (int) (boxY + 0.5f), (int) (boxX + boxW - 0.5f), (int) (boxY + boxH - 0.5f), fill.getRGB());

        if (!value.equals(field.getText())) {
            field.setText(value);
        }

        field.xPosition = (int) (boxX + 3);
        field.yPosition = (int) (boxY + 1);
        field.width = (int) (boxW - 6);
        field.height = (int) (boxH - 2);
        field.setEnableBackgroundDrawing(false);
        field.setFocused(focused);
        field.setTextColor(ColorUtil.applyOpacity(textColor.getRGB(), alpha));
        field.setDisabledTextColour(ColorUtil.applyOpacity(textColor.getRGB(), alpha));

        field.drawTextBox();

        typing = focused;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (button != 0) return;

        float boxX = x + 5;
        float boxY = y + 10;
        float boxW = width - 10;
        float boxH = 9;

        focused = isClickable(boxY + boxH) && mouseX >= boxX && mouseX <= boxX + boxW && mouseY >= boxY && mouseY <= boxY + boxH;
        typing = focused;
        Keyboard.enableRepeatEvents(focused);
        if (focused) {
            field.mouseClicked(mouseX, mouseY, button);
        } else {
            field.setFocused(false);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
    }

    @Override
    public void onGuiClosed() {
        focused = false;
        typing = false;
        field.setFocused(false);
        Keyboard.enableRepeatEvents(false);
    }
}
