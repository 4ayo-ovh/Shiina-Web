package dev.osunolimits.plugins.events;

import dev.osunolimits.plugins.ShiinaEventListener;
import lombok.Getter;

@Getter
public class OnRegisterEvent extends ShiinaEvent {

    private int userId;
    private String email;
    private String country;
    private String name;
    private String safeName;
    private long curUnixTime;

    public OnRegisterEvent(int userId, String email, String country, String name, String safeName, long curUnixTime) {
        this.userId = userId;
        this.email = email;
        this.country = country;
        this.name = name;
        this.safeName = safeName;
        this.curUnixTime = curUnixTime;
    }

    @Override
    public void call(ShiinaEventListener listener) {
        listener.onRegisterEvent(this);
    }
    
}
