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

        logger.info(String.format("%s day of %s", OrdinalIndicator.addTo(d), s.displayName()));

        if(seasonChanged())
            broadcastSeasonChangeEvent();
    }

    private boolean seasonChanged()
    {
        return Season.onDay(lastDay) != Season.onDay(currentDay);
    }

    private void broadcastSeasonChangeEvent()
    {
        logger.info(String.format("Season changed from %s to %s.", Season.onDay(lastDay).displayName(), Season.onDay(currentDay).displayName()));
        OnSeasonChangeEvent event = new OnSeasonChangeEvent(Season.onDay(lastDay), Season.onDay(currentDay));
    }
}
