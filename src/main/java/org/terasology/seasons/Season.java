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

import org.terasology.world.time.WorldTime;
import org.terasology.world.time.WorldTimeImpl;

/**
 * @author DizzyDragon.
 * Enumeration of the seasons.
 * @version 1.0
 */
@SuppressWarnings("unused")
public enum Season {
    SPRING("spring", 6),
    SUMMER("summer", 6),
    FALL("fall", 6),
    WINTER("winter", 6);


    private Season(String name, int lengthInDays)
    {
        if(lengthInDays < 0)
            throw new IllegalArgumentException("Length of a season must be >= 0");

        DISPLAY_NAME = name;
        LENGTH_IN_DAYS = lengthInDays;
    }


    static
    {
        Season season[] = values();
        season[0].FIRST_DAY = 0;

        for(int i = 1; i < season.length; i++)
        {
            season[i].FIRST_DAY = season[i-1].FIRST_DAY + season[i-1].LENGTH_IN_DAYS;
        }
    }

    /**
     * The length of the season in days
     * @return The length of the season in days
     */
    public int lengthInDays()
    {
        return LENGTH_IN_DAYS;
    }

    /**
     * The name of the season
     * @return The name of the season
     */
    public String displayName()
    {
        return DISPLAY_NAME;
    }

    /**
     * Returns the season that follows the season on which the function is called.
     * @return The following season
     */
    public Season next()
    {
        Season[] seasons = Season.values();
        return seasons[(this.ordinal() + 1) % seasons.length];
    }

    /**
     * Returns the season that precedes the season on which the function is called.
     * @return The preceding season
     */
    public Season previous()
    {
        Season[] seasons = Season.values();
        return seasons[(this.ordinal() + seasons.length - 1) % seasons.length];
    }

    /**
     * Returns the season on a particular day
     * @param day absolute day (+ time)
     * @return The season on that particular day
     */
    public static Season onDay(double day)
    {
        return onDay((int) Math.floor(day));
    }

    /**
     * Returns the season on a particular day
     * @param day absolute day
     * @return The season on that particular day
     */
    public static Season onDay(int day)
    {
        day = ((day % YEAR_LENGTH_IN_DAYS) + YEAR_LENGTH_IN_DAYS) % YEAR_LENGTH_IN_DAYS;

        for(Season season: values())
        {
            if(season.firstDay() <= day && day <= season.lastDay() )
                return season;
        }

        return null;  //unreachable code;
    }

    /**
     * Converts an absolute day to day of the year
     * @param day absolute day (+ time)
     * @return The corresponding day of the year
     */
    public static int dayOfYear(double day)
    {
        return dayOfCycle((int)Math.floor(day));
    }

    /**
     * Converts an absolute day to day of the year
     * @param day absolute day (+ time)
     * @return The corresponding day of the year
     */
    public static int dayOfCycle(int day)
    {
        return ((day % YEAR_LENGTH_IN_DAYS) + YEAR_LENGTH_IN_DAYS) % YEAR_LENGTH_IN_DAYS;
    }

    /**
     * Returns how many days you are into a season at a particular day (of the cycle).
     * @param day The day (+ time) (since the start of the first cycle)
     * @return How many days you are into a season at the given day.
     */
    public static int dayOfSeason(double day)
    {
        return dayOfSeason((int)Math.floor(day));
    }

    /**
     * Returns how many days you are into a season at a particular day (of the cycle).
     * @param day The day (since the start of the first cycle)
     * @return How many days you are into a season at the given day.
     */
    public static int dayOfSeason(int day)
    {
        day = dayOfCycle(day);
        return day - onDay(day).firstDay();
    }

    /**
     * Returns the day of the cycle that marks the first day of the season.
     * @return The day of the cycle that marks the first day of the season.
     */
    public int firstDay()
    {
        return FIRST_DAY;
    }

    /**
     * Returns the day of the cycle that marks the last day of the season.
     * @return The day of the cycle that marks the last day of the season.
     */
    public int lastDay()
    {
       return FIRST_DAY + LENGTH_IN_DAYS - 1;
    }

    /**
     * The amount of days of all seasons combined.
     */
    public final static int YEAR_LENGTH_IN_DAYS;
    static
    {
        Season[] seasons = values();
        Season lastSeason = seasons[seasons.length - 1];
        YEAR_LENGTH_IN_DAYS = lastSeason.lastDay() + 1;
    }

    /**
     * Constant that needs to be added to the world time to get the season time.
     */
    public final static double WORLD_TIME_OFFSET = -(WorldTime.DAY_LENGTH - WorldTimeImpl.DUSK_TIME) * WorldTimeImpl.MS_TO_DAYS;

    private final String DISPLAY_NAME;
    private final int LENGTH_IN_DAYS;
    private int FIRST_DAY;
}
