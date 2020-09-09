// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.seasons.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.seasons.Season;

/**
 * Event to trigger when the season changes.
 *
 * @author DizzyDragon.
 */
public class OnSeasonChangeEvent implements Event {

    public final Season from;
    public final Season to;

    public OnSeasonChangeEvent(Season from, Season to) {
        this.from = from;
        this.to = to;
    }
}
