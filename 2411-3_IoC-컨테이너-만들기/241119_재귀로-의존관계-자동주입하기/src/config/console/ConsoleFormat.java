package config.console;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ConsoleFormat {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static String toJson(Object object) {
        return gson.toJson(object);
    }
}
