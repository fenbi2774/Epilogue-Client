package epilogue.events;

import epilogue.event.events.Event;

public class ChatGUIEvent implements Event {
    private final int mouseX;
    private final int mouseY;

    public ChatGUIEvent(int mouseX, int mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public int getMouseX() {
        return mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }
}
