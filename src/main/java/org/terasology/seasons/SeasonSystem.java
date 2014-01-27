/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.seasons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.*;
import org.terasology.seasons.events.OnSeasonChangeEvent;
import org.terasology.world.WorldComponent;
import org.terasology.world.time.OnMidnightEvent;
import org.terasology.world.time.WorldTime;
import org.terasology.utilities.OrdinalIndicator;

@RegisterSystem(RegisterMode.AUTHORITY)
public class SeasonSystem implements ComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(SeasonSystem.class);

    @In
    private EntityManager entityManager;

    @In
    private org.terasology.world.WorldProvider world;

    private WorldTime worldTime;
    private double lastDay, currentDay;

	@Override
	public void initialise() {
        worldTime = world.getTime();
        lastDay = currentDay = worldTime.getDays();

        //UIWindow window = new UIWindow();
        //window.open();
	}

	@Override
	public void shutdown() {
        lastDay = currentDay = 0.0;
        worldTime = null;
	}

    @ReceiveEvent(components = WorldComponent.class)
    public void onMidnight(OnMidnightEvent event, EntityRef entity) {
        lastDay = currentDay;
        currentDay = worldTime.getDays();

        Season s = Season.onDay(currentDay);
        int d = Season.dayOfSeason(currentDay);

        logger.info(String.format("%s day of %s", OrdinalIndicator.addedTo(d), s.displayName()));

        if(seasonChanged())
            broadcastSeasonChangeEvent();
    }

    private boolean seasonChanged()
    {
        return Season.onDay(lastDay) != Season.onDay(currentDay);
    }

    private void broadcastSeasonChangeEvent()
    {
        OnSeasonChangeEvent event = new OnSeasonChangeEvent(Season.onDay(lastDay), Season.onDay(currentDay));
    }


}
