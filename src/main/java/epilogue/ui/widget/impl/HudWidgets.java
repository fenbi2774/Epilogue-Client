package epilogue.ui.widget.impl;

import epilogue.Epilogue;

public final class HudWidgets {
    private HudWidgets() {
    }

    public static void registerAll() {
        Epilogue.widgetManager.register(new ArrayListWidget());
        Epilogue.widgetManager.register(new NotificationWidget());
        Epilogue.widgetManager.register(new PotionEffectsWidget());
        Epilogue.widgetManager.register(new ScoreboardWidget());
        Epilogue.widgetManager.register(new SessionWidget());
        Epilogue.widgetManager.register(new TargetHUDWidget());
        Epilogue.widgetManager.register(new WaterMarkWidget());
    }
}
