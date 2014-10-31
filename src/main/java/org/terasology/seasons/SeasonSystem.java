/*
 * Copyright 2014 MovingBlocks
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

import com.google.common.base.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.climateConditions.ClimateConditionsSystem;
import org.terasology.climateConditions.ConditionModifier;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.math.TeraMath;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.seasons.events.OnSeasonChangeEvent;
import org.terasology.world.WorldComponent;
import org.terasology.world.WorldProvider;
import org.terasology.world.time.OnMidnightEvent;
import org.terasology.world.time.WorldTime;
import org.terasology.utilities.OrdinalIndicator;
import org.terasology.world.time.WorldTimeImpl;

/**
 * Handles the passing of seasons.
 *
 * @author DizzyDragon.
 */
@RegisterSystem
@Share(value = SeasonSystem.class)
public class SeasonSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(SeasonSystem.class);
    private static final float TIME_SHIFT = WorldTime.MIDDAY_TIME * WorldTime.DAY_LENGTH;

    @In
    private EntityManager entityManager;

    @In
    private ClimateConditionsSystem climateConditionsSystem;

    @In
    private WorldProvider world;

    private WorldTime worldTime;
    private double lastDay;
    private double currentDay;

    private float yearlyTemperatureAmplitude = 15;
    private float yearlyHumidityAmplitude = 0.4f;

    private Function<Float, Float> yearlyTemperatureModifier =
            new Function<Float, Float>() {
                @Override
                public Float apply(Float timeInYear) {
                    // Temperature peaks in the middle of summer, bottoms in the middle of winter
                    double x = Math.PI * (timeInYear * 2 - 0.25f);
                    return yearlyTemperatureAmplitude / 2f * (float) Math.sin(x);
                }
            };

    private Function<Float, Float> yearlyHumidityModifier =
            new Function<Float, Float>() {
                @Override
                public Float apply(Float timeInYear) {
                    // Humidity peaks in the middle of spring and fall, bottoms in the middle of summer and winter
                    double x = Math.PI * (2 * timeInYear * 2);
                    return yearlyHumidityAmplitude / 2f * (float) Math.sin(x);
                }
            };

    @Override
    public void initialise() {
        worldTime = world.getTime();
        lastDay = worldTime.getDays();
        currentDay = worldTime.getDays();
        logger.info("Initializing SeasonSystem - {} {} {}", worldTime, lastDay, currentDay);
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

        logger.info(String.format("%s day of %s", OrdinalIndicator.addedTo(d), s.displayName()));

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
