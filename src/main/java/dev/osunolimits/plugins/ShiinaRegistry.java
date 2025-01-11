package dev.osunolimits.plugins;

import java.util.ArrayList;
import java.util.List;

public class ShiinaRegistry {

    private static List<ShiinaEventListener> listeners = new ArrayList<>();

    public static void registerListener(ShiinaEventListener listener) {
        listeners.add(listener);
    }

    public static void unregisterListener(ShiinaEventListener listener) {
        listeners.remove(listener);
    }

    public static List<ShiinaEventListener> getListeners() {
        return listeners;
    }

    
}
