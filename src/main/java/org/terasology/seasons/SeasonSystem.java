package org.terasology.seasons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.world.time.WorldTime;
import org.terasology.seasons.components.DayChangeComponent;
import org.terasology.seasons.events.DayChangedEvent;
import org.terasology.seasons.events.SeasonChangedEvent;

@RegisterSystem(RegisterMode.AUTHORITY)
public class SeasonSystem implements UpdateSubscriberSystem {

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
	}

	@Override
	public void shutdown() {
        worldTime = null;
        lastDay = currentDay = 0.0;
	}

	@Override
	public void update(float delta) {
        lastDay = currentDay;
        currentDay = worldTime.getDays();

        if(dayChanged())
            broadcastDayChangeEvent();

        if(seasonChanged())
            broadcastSeasonChangeEvent();

        if(dayChanged())
        {
            Season season = Season.onDay(currentDay);
            int day = Season.dayOfSeason(currentDay) + 1;

            String dayOrd = OrdinalIndicator.addTo(day);

            logger.info(String.format("%s day of %s", dayOrd, season.displayName()));
        }

        if(seasonChanged())
        {
            logger.info(String.format("Season changed from %s to %s.", Season.onDay(lastDay).displayName(), Season.onDay(currentDay).displayName()));
        }
	}

    private boolean dayChanged()
    {
        return (int)Math.floor(currentDay) != (int)Math.floor(lastDay);
    }

    private boolean seasonChanged()
    {
        return Season.onDay(lastDay) != Season.onDay(currentDay);
    }

    private void broadcastDayChangeEvent()
    {
        DayChangedEvent event = new DayChangedEvent(lastDay, currentDay);

        for(EntityRef entity : entityManager.getEntitiesWith(DayChangeComponent.class))
        {
            entity.send(event);
        }
    }

    private void broadcastSeasonChangeEvent()
    {
        SeasonChangedEvent event = new SeasonChangedEvent(Season.onDay(lastDay), Season.onDay(currentDay));
    }

}
