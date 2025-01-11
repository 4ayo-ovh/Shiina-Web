package dev.osunolimits.plugins.events;

import dev.osunolimits.plugins.ShiinaEventListener;
import lombok.Getter;

@Getter
public class OnAddDonorEvent extends ShiinaEvent {

    private int userId;
    private String duration;
    private int adminId;

    public OnAddDonorEvent(String duration, int userId) {
        this.duration = duration;
        this.userId = userId;
    }

    public OnAddDonorEvent(String duration, int userId, int adminId) {
        this.duration = duration;
        this.userId = userId;
        this.adminId = adminId;
    }

    @Override
    public void call(ShiinaEventListener listener) {
        listener.onAddDonorEvent(this);
    }
    
}
