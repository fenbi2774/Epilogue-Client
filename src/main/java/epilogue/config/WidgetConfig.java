package epilogue.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import epilogue.Epilogue;
import epilogue.ui.widget.Widget;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class WidgetConfig {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final File file;

    public WidgetConfig(String name) {
        this.file = new File("./Epilogue/", name + ".json");
    }

    public void load() {
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            JsonObject obj = new JsonParser().parse(reader).getAsJsonObject();
            for (Widget widget : Epilogue.widgetManager.widgets) {
                if (!obj.has(widget.name)) continue;
                JsonObject w = obj.getAsJsonObject(widget.name);
                if (w.has("x")) widget.x = w.get("x").getAsFloat();
                if (w.has("y")) widget.y = w.get("y").getAsFloat();
            }
        } catch (Exception ignored) {
        }
    }

    public void save() {
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();

            JsonObject obj = new JsonObject();
            for (Widget widget : Epilogue.widgetManager.widgets) {
                JsonObject w = new JsonObject();
                w.addProperty("x", widget.x);
                w.addProperty("y", widget.y);
                obj.add(widget.name, w);
            }

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(gson.toJson(obj));
            }
        } catch (Exception ignored) {
        }
    }
}
