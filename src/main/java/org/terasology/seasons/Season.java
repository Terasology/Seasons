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

import org.terasology.world.time.WorldTime;
import org.terasology.world.time.WorldTimeImpl;

/**
 * Enumeration of the seasons.
 *
 * @author DizzyDragon.
 */
public enum Season {
    SPRING("spring", 6),
    SUMMER("summer", 6),
    FALL("fall", 6),
    WINTER("winter", 6);

    /**
     * The amount of days of all seasons combined.
     */
    public static final int YEAR_LENGTH_IN_DAYS;

    private final String displayName;
    private final int lengthInDays;
    private int firstDay;

    private Season(String name, int lengthInDays) {
        if (lengthInDays < 0) {
            throw new IllegalArgumentException("Length of a season must be >= 0");
        }

        displayName = name;
        this.lengthInDays = lengthInDays;
    }


    static {
        Season[] season = values();
        season[0].firstDay = 0;

        for (int i = 1; i < season.length; i++) {
            season[i].firstDay = season[i - 1].firstDay + season[i - 1].lengthInDays;
        }
    }

    /**
     * The length of the season in days
     *
     * @return The length of the season in days
     */
    public int lengthInDays() {
        return lengthInDays;
    }

    /**
     * The name of the season
     *
     * @return The name of the season
     */
    public String displayName() {
        return displayName;
    }

    /**
     * Returns the season that follows the season on which the function is called.
     *
     * @return The following season
     */
    public Season next() {
        Season[] seasons = Season.values();
        return seasons[(this.ordinal() + 1) % seasons.length];
    }

    /**
     * Returns the season that precedes the season on which the function is called.
     *
     * @return The preceding season
     */
    public Season previous() {
        Season[] seasons = Season.values();
        return seasons[(this.ordinal() + seasons.length - 1) % seasons.length];
    }

    /**
     * Returns the season on a particular day
     *
     * @param day absolute day (+ time)
     * @return The season on that particular day
     */
    public static Season onDay(double day) {
        return onDay((int) Math.floor(day));
    }

    /**
     * Returns the season on a particular day
     *
     * @param day absolute day
     * @return The season on that particular day
     */
    public static Season onDay(int day) {
        int dayWithinYear = ((day % YEAR_LENGTH_IN_DAYS) + YEAR_LENGTH_IN_DAYS) % YEAR_LENGTH_IN_DAYS;

        for (Season season : values()) {
            if (season.firstDay() <= dayWithinYear && dayWithinYear <= season.lastDay()) {
                return season;
            }
        }

        return null;  //unreachable code;
    }

    /**
     * Converts an absolute day to day of the year
     *
     * @param day absolute day (+ time)
     * @return The corresponding day of the year
     */
    public static int dayOfYear(double day) {
        return dayOfCycle((int) Math.floor(day));
    }

    /**
     * Converts an absolute day to day of the year
     *
     * @param day absolute day (+ time)
     * @return The corresponding day of the year
     */
    public static int dayOfCycle(int day) {
        return ((day % YEAR_LENGTH_IN_DAYS) + YEAR_LENGTH_IN_DAYS) % YEAR_LENGTH_IN_DAYS;
    }

    /**
     * Returns how many days you are into a season at a particular day (of the cycle).
     *
     * @param day The day (+ time) (since the start of the first cycle)
     * @return How many days you are into a season at the given day.
     */
    public static int dayOfSeason(double day) {
        return dayOfSeason((int) Math.floor(day));
    }

    /**
     * Returns how many days you are into a season at a particular day (of the cycle).
     *
     * @param day The day (since the start of the first cycle)
     * @return How many days you are into a season at the given day.
     */
    public static int dayOfSeason(int day) {
        int dayWithinSeason = dayOfCycle(day);
        return dayWithinSeason - onDay(dayWithinSeason).firstDay();
    }

    /**
     * Returns the day of the cycle that marks the first day of the season.
     *
     * @return The day of the cycle that marks the first day of the season.
     */
    public int firstDay() {
        return firstDay;
    }

    /**
     * Returns the day of the cycle that marks the last day of the season.
     *
     * @return The day of the cycle that marks the last day of the season.
     */
    public int lastDay() {
        return firstDay + lengthInDays - 1;
    }

    static {
        Season[] seasons = values();
        Season lastSeason = seasons[seasons.length - 1];
        YEAR_LENGTH_IN_DAYS = lastSeason.lastDay() + 1;
    }

}
