// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.seasons;

import com.google.common.base.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.climateConditions.ClimateConditionsSystem;
import org.terasology.climateConditions.ConditionModifier;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.engine.utilities.OrdinalIndicator;
import org.terasology.engine.world.WorldComponent;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.sun.OnMidnightEvent;
import org.terasology.engine.world.time.WorldTime;
import org.terasology.math.TeraMath;
import org.terasology.seasons.events.OnSeasonChangeEvent;

/**
 * Handles the passing of seasons.
 *
 * @author DizzyDragon.
 */
@RegisterSystem
@Share(value = SeasonSystem.class)
public class SeasonSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(SeasonSystem.class);
    private static final float TIME_SHIFT = 0.5f * WorldTime.DAY_LENGTH;
    private final float yearlyTemperatureAmplitude = 15;
    private final float yearlyHumidityAmplitude = 0.4f;
    private final Function<Float, Float> yearlyTemperatureModifier =
            new Function<Float, Float>() {
                @Override
                public Float apply(Float timeInYear) {
                    // Temperature peaks in the middle of summer, bottoms in the middle of winter
                    double x = Math.PI * (timeInYear * 2 - 0.25f);
                    return yearlyTemperatureAmplitude / 2f * (float) Math.sin(x);
                }
            };
    private final Function<Float, Float> yearlyHumidityModifier =
            new Function<Float, Float>() {
                @Override
                public Float apply(Float timeInYear) {
                    // Humidity peaks in the middle of spring and fall, bottoms in the middle of summer and winter
                    double x = Math.PI * (2 * timeInYear * 2);
                    return yearlyHumidityAmplitude / 2f * (float) Math.sin(x);
                }
            };
    @In
    private EntityManager entityManager;
    @In
    private ClimateConditionsSystem climateConditionsSystem;
    @In
    private WorldProvider world;
    private WorldTime worldTime;
    private double lastDay;
    private double currentDay;

    @Override
    public void initialise() {
        worldTime = world.getTime();
        lastDay = worldTime.getDays();
        currentDay = worldTime.getDays();
        if (logger.isInfoEnabled()) {
            logger.info("Initializing SeasonSystem - {} {} {}", worldTime, lastDay, currentDay);
        }
    }

    @Override
    public void preBegin() {
        // These have to be registered only on authority
        if (climateConditionsSystem != null) {
            climateConditionsSystem.addHumidityModifier(
                    Integer.MIN_VALUE,
                    new ConditionModifier() {
                        @Override
                        public float getCondition(float value, float x, float y, float z) {
                            return getHumidity(value);
                        }
                    });
            climateConditionsSystem.addTemperatureModifier(
                    Integer.MIN_VALUE,
                    new ConditionModifier() {
                        @Override
                        public float getCondition(float value, float x, float y, float z) {
                            return getTemperature(value);
                        }
                    });
        }
    }

    @Override
    public void shutdown() {
        lastDay = 0.0;
        currentDay = 0.0;
        worldTime = null;
    }

    @ReceiveEvent(components = WorldComponent.class)
    public void onMidnight(OnMidnightEvent event, EntityRef entity) {
        lastDay = currentDay;
        currentDay = worldTime.getDays();

        Season s = Season.onDay(currentDay);
        int d = Season.dayOfSeason(currentDay);

        if (logger.isInfoEnabled()) {
            logger.info(String.format("%s day of %s", OrdinalIndicator.addedTo(d), s.displayName()));
        }

        if (seasonChanged()) {
            broadcastSeasonChangeEvent();
        }
    }

    public String getSeasonDayDescription() {
        float days = worldTime.getDays() + TIME_SHIFT;
        Season s = Season.onDay(days);
        int d = Season.dayOfSeason(days);

        return String.format("%s day of %s", OrdinalIndicator.addedTo(d + 1), s.displayName());
    }

    private float getTemperature(float baseValue) {
        float days = worldTime.getDays() + TIME_SHIFT;
        float years = days / Season.YEAR_LENGTH_IN_DAYS;
        return baseValue + yearlyTemperatureModifier.apply(years);
    }

    private float getHumidity(float baseValue) {
        float days = worldTime.getDays() + TIME_SHIFT;
        float years = days / Season.YEAR_LENGTH_IN_DAYS;
        return TeraMath.clamp(baseValue + yearlyHumidityModifier.apply(years), 0, 1);
    }

    private boolean seasonChanged() {
        return Season.onDay(lastDay) != Season.onDay(currentDay);
    }

    private void broadcastSeasonChangeEvent() {
        OnSeasonChangeEvent event = new OnSeasonChangeEvent(Season.onDay(lastDay), Season.onDay(currentDay));
        getWorldEntity().send(event);
    }

    private EntityRef getWorldEntity() {
        return entityManager.getEntitiesWith(WorldComponent.class).iterator().next();
    }
}
