package com.southcentralpositronics.reign_gdx.game.sim;

import java.util.ArrayList;
import java.util.List;

/** Collects the events that systems raise during ticks, until the simulation's owner reads them. */
public final class EventLog {
    private final List<SimulationEvent> events = new ArrayList<>();

    /** Records an event. */
    public void raise(SimulationEvent.Type type, EntityKind kind, double x, double y) {
        events.add(new SimulationEvent(type, kind, x, y));
    }

    /** Returns the events raised since the last call, in order, and forgets them. */
    public List<SimulationEvent> drain() {
        List<SimulationEvent> drained = List.copyOf(events);
        events.clear();
        return drained;
    }
}
