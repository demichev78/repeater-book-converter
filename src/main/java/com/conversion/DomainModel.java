package com.conversion;

import java.util.List;

public class DomainModel {

    public boolean authenticated = false;
    public List<Repeater> repeaters = null;

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(final boolean authenticated) {
        this.authenticated = authenticated;
    }

    public List<Repeater> getRepeaters() {
        return repeaters;
    }

    public void setRepeaters(final List<Repeater> repeaters) {
        this.repeaters = repeaters;
    }
}
