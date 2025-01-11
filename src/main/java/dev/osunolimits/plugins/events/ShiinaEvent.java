package dev.osunolimits.plugins.events;

import dev.osunolimits.plugins.ShiinaEventListener;
import dev.osunolimits.plugins.ShiinaRegistry;

public class ShiinaEvent {

    public void call(ShiinaEventListener listener) { }
    
    public void callListeners() {
        for(ShiinaEventListener listener : ShiinaRegistry.getListeners()) {
            call(listener);
        }
    }
}
