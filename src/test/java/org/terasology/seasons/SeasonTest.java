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


import org.junit.jupiter.api.Test;
import org.terasology.specificationLanguage.SpecificationLanguage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.terasology.specificationLanguage.SpecificationLanguage.assuming;
import static org.terasology.specificationLanguage.SpecificationLanguage.assumingForAll;
import static org.terasology.specificationLanguage.SpecificationLanguage.test;
import static org.terasology.specificationLanguage.SpecificationLanguage.testForAll;

/**
 * Test of Season
 *
 * @author DizzyDragon
 */
public class SeasonTest {
    private static final SpecificationLanguage.UniversalRule THERE_ARE_EXACTLY_4_SEASONS = new SpecificationLanguage.UniversalRule() {
        @Override
        public void test() {
            assertEquals(4, Season.values().length, "Number of seasons");
        }
    };

    // domains ///////////////////////////////////////////////////////////////
    private static final SpecificationLanguage.UniversalRule DAYS_IN_A_YEAR_IS_THE_SUM_OF_THE_DAYS_OF_ALL_SEASONS = new SpecificationLanguage.UniversalRule() {
        @Override
        public void test() {
            int dayCount = 0;

            for (Season season : Season.values()) {
                dayCount += season.lengthInDays();
            }

            assertEquals(
                    dayCount,
                    Season.YEAR_LENGTH_IN_DAYS,
                    "Year length in days"
            );
        }
    };
    private static final SpecificationLanguage.UniversalRule DISPLAY_NAMES_ARE_UNIQUE = new SpecificationLanguage.UniversalRule() {
        @Override
        public void test() {
            Set<String> names = new HashSet<>();

            for (Season season : Season.values()) {
                names.add(season.displayName());
            }

            assertEquals(names.size(), Season.values().length, "Number of unique names");
        }
    };
    private static final SpecificationLanguage.InstanceRule LENGTH_IS_CONSISTENT_WITH_FIRST_AND_LAST_DAY = new SpecificationLanguage.InstanceRule<Season>() {
        @Override
        public void test(Season season) {
            int computedLength = Math.abs(season.lastDay() - season.firstDay()) + 1;

            assertEquals(season.lengthInDays(), computedLength, String.format("%s length in days", season.toString()));
        }
    };
    private static final SpecificationLanguage.InstanceRule FIRST_AND_LAST_DAY_NUMBER_ARE_POSITIVE = new SpecificationLanguage.InstanceRule<Season>() {
        @Override
        public void test(Season season) {
            assertTrue(season.firstDay() >= 0, String.format("First day of %s is positive number", season.toString()));
            assertTrue(season.lastDay() >= 0, String.format("Last day of %s is positive number", season.toString()));
        }
    };
    private static final SpecificationLanguage.TwoInstanceRule DAY_OF_CYCLE_IS_INDEPENDENT_OF_YEAR = new SpecificationLanguage.TwoInstanceRule<Integer, Integer>() {
        @Override
        public void test(Integer day, Integer year) {
            int absoluteDay = day + year * Season.YEAR_LENGTH_IN_DAYS;
            int expected = ((absoluteDay % Season.YEAR_LENGTH_IN_DAYS) + Season.YEAR_LENGTH_IN_DAYS) % Season.YEAR_LENGTH_IN_DAYS;
            double absoluteDayD = absoluteDay + random.nextDouble();
            assertEquals(expected, Season.dayOfCycle(absoluteDay),"Day of year");
            assertEquals(expected, Season.dayOfYear(absoluteDayD),"Day of year");
        }
    };

    // test cases ////////////////////////////////////////////////////////////
    private static final SpecificationLanguage.TwoInstanceRule DAY_TO_SEASON_IS_CONSISTENT = new SpecificationLanguage.TwoInstanceRule<Season, Integer>() {
        @Override
        public void test(final Season season, final Integer year) {
            final int firstDayOfYear = year * (Season.YEAR_LENGTH_IN_DAYS);

            testForAll(daysIn(season), new SpecificationLanguage.InstanceRule<Integer>() {
                @Override
                public void test(Integer dayOfSeason) {
                    int absoluteDayI = firstDayOfYear + dayOfSeason;
                    double offset = random.nextDouble();

                    String message = String.format("%s on day %d (+%f) of year %d (abs. %d)", season.toString(), dayOfSeason, offset, year, absoluteDayI);

                    assertSame(season, Season.onDay(absoluteDayI), message + " (int)");
                    assertSame(season, Season.onDay(absoluteDayI + offset),message + " (double)");
                }
            });
        }
    };
    private static final SpecificationLanguage.TwoInstanceRule DAY_OF_SEASON_IS_CONSISTENT = new SpecificationLanguage.TwoInstanceRule<Season, Integer>() {
        @Override
        public void test(final Season season, final Integer year) {
            final int firstDayOfYear = year * Season.YEAR_LENGTH_IN_DAYS;

            testForAll(daysIn(season), new SpecificationLanguage.InstanceRule<Integer>() {
                @Override
                public void test(Integer dayOfYear) {
                    int absoluteDayInt = dayOfYear + firstDayOfYear;
                    int dayOfSeason = dayOfYear - season.firstDay();
                    double absoluteDayDouble = absoluteDayInt + random.nextDouble(); // 0 <= random.nextDouble() < 1.0

                    String message = String.format("day %d of %s (year %d)", dayOfYear, season, year);
                    assertEquals(dayOfSeason, Season.dayOfSeason(absoluteDayInt), message);
                    assertEquals(dayOfSeason, Season.dayOfSeason(absoluteDayDouble), message);
                }
            });
        }
    };
    private static final SpecificationLanguage.InfixRelationRule IS_FOLLOWED_BY = new SpecificationLanguage.InfixRelationRule<Season, Season>() {
        @Override
        public void test(Season leftOperand, Season rightOperand) {
            assertSame(leftOperand.next(), rightOperand,
                    String.format("%s is followed by %s", leftOperand.toString(), rightOperand.toString())
            );

            assertSame(rightOperand.previous(), leftOperand,
                    String.format("%s is preceded by %s", rightOperand.toString(), leftOperand.toString())
            );
        }
    };
    private static Random random = new Random(768179104);
    private static SpecificationLanguage.EnumDomain seasons = new SpecificationLanguage.EnumDomain<>(Season.class);
    private static SpecificationLanguage.Domain integers = new SpecificationLanguage.Domain<Integer>(Integer.class) {
        @Override
        public Iterable<Integer> generateUniversalSamples() {
            Integer[] a = {0, 1, -1, random.nextInt(90000) + 10000, -(random.nextInt(90000)) - 10000};

            return Arrays.asList(a);
        }
    };

    /////////////////////////////////////////////////////////////////
    // specification rule definitions
    private static SpecificationLanguage.Domain years = integers;
    private static SpecificationLanguage.Domain daysInYear = new SpecificationLanguage.Domain<Integer>(Integer.class) {
        @Override
        public Iterable<Integer> generateUniversalSamples() {
            return new Iterable<Integer>() {
                @Override
                public Iterator<Integer> iterator() {
                    return new Iterator<Integer>() {
                        private int counter;

                        @Override
                        public boolean hasNext() {
                            return counter < Season.YEAR_LENGTH_IN_DAYS;
                        }

                        @Override
                        public Integer next() {
                            counter++;
                            return counter;
                        }

                        @Override
                        public void remove() {
                            throw new RuntimeException("Not implemented");
                        }
                    };
                }
            };
        }
    };

    private static SpecificationLanguage.Domain<Integer> daysIn(final Season season) {
        return new SpecificationLanguage.Domain<Integer>(Integer.class) {
            @Override
            public Iterable<Integer> generateUniversalSamples() {
                List<Integer> days = new ArrayList<>();
                for (int i = season.firstDay(); i <= season.lastDay(); i++) {
                    days.add(i);
                }

                return days;
            }
        };
    }

    @Test
    public void testNumberOfSeasons() {
        test(THERE_ARE_EXACTLY_4_SEASONS);
    }

    @Test
    public void testUniqueDisplayNames() {
        test(DISPLAY_NAMES_ARE_UNIQUE);
    }

    @Test
    public void testOrderOfSeasons() {
        assuming(THERE_ARE_EXACTLY_4_SEASONS);

        test(Season.SPRING, IS_FOLLOWED_BY, Season.SUMMER);
        test(Season.SUMMER, IS_FOLLOWED_BY, Season.FALL);
        test(Season.FALL, IS_FOLLOWED_BY, Season.WINTER);
        test(Season.WINTER, IS_FOLLOWED_BY, Season.SPRING);
    }

    @Test
    public void testSeasonLengthConsistency() {
        testForAll(seasons, FIRST_AND_LAST_DAY_NUMBER_ARE_POSITIVE);
        testForAll(seasons, LENGTH_IS_CONSISTENT_WITH_FIRST_AND_LAST_DAY);
    }

    @Test
    public void testLengthOfAYear() {
        assumingForAll(seasons, LENGTH_IS_CONSISTENT_WITH_FIRST_AND_LAST_DAY);

        test(DAYS_IN_A_YEAR_IS_THE_SUM_OF_THE_DAYS_OF_ALL_SEASONS);
    }

    @Test
    public void conversionConsistency() {
        testForAll(daysInYear, years, DAY_OF_CYCLE_IS_INDEPENDENT_OF_YEAR);

        testForAll(seasons, years, DAY_TO_SEASON_IS_CONSISTENT);
        testForAll(seasons, years, DAY_OF_SEASON_IS_CONSISTENT);
    }
}
