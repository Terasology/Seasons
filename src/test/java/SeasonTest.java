import org.junit.Test;
import org.terasology.seasons.Season;

import java.util.*;

import static specificationLanguage.SpecificationLanguage.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

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

/**
 * Created with IntelliJ IDEA.
 * User: Linus
 * Date: 1/19/14
 * Time: 1:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class SeasonTest {
    private static Random random = new Random(768179104);

    // domains ///////////////////////////////////////////////////////////////

    private static EnumDomain
            seasons = new EnumDomain(Season.class);

    private static Domain
            integers = new Domain(Integer.class) {
                @Override
                public Iterable<Integer> generateUniversalSamples() {
                    Integer[] a = {0, 1, -1, random.nextInt(90000) + 10000, -(random.nextInt(90000)) - 10000};

                    return Arrays.asList(a);
                }
            },
            years = integers,
            daysInYear = new Domain(Integer.class) {
                @Override
                public Iterable<Integer> generateUniversalSamples() {
                    return new Iterable<Integer>() {
                        @Override
                        public Iterator<Integer> iterator() {
                            return new Iterator() {
                                private int counter = 0;

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
                                    //counter++;
                                }
                            };
                        };
                    };
                }
            };

    private static Domain<Integer> daysIn(final Season season) {
        return new Domain<Integer>(Integer.class) {
            @Override
            public Iterable<Integer> generateUniversalSamples() {
                ArrayList<Integer> days = new ArrayList<>();
                for(int i = season.firstDay(); i <= season.lastDay(); i++) {
                    days.add(i);
                }

                return days;
            }
        };
    }

    // test cases ////////////////////////////////////////////////////////////

    @Test
    public void testNumberOfSeasons() {
        test(thereAreExactly4Seasons);
    }

    @Test
    public void testUniqueDisplayNames() {
        test(displayNamesAreUnique);
    }

    @Test
    public void testOrderOfSeasons() {
        assuming(thereAreExactly4Seasons);

        test(Season.SPRING, isFollowedBy, Season.SUMMER);
        test(Season.SUMMER, isFollowedBy, Season.FALL);
        test(Season.FALL,   isFollowedBy, Season.WINTER);
        test(Season.WINTER, isFollowedBy, Season.SPRING);
    }

    @Test
    public void testSeasonLengthConsistency() {
        testForAll(seasons, firstAndLastDayNumberArePositive);
        testForAll(seasons, lengthIsConsistentWithFirstAndLastDay);
    }

    @Test
    public void testLengthOfAYear() {
        assumingForAll(seasons, lengthIsConsistentWithFirstAndLastDay);

        test(daysInAYearIsTheSumOfTheDaysOfAllSeasons);
    }

    @Test
    public void conversionConsistency() {
        testForAll(daysInYear, years, dayOfCycleIsIndependentOfYear);

        testForAll(seasons, years, dayToSeasonIsConsistent);
        testForAll(seasons, years, dayOfSeasonIsConsistent);
    }

    /////////////////////////////////////////////////////////////////
    // specification rule definitions

    private final static UniversalRule
        thereAreExactly4Seasons = new UniversalRule() {
            @Override
            public void test() {
                assertEquals("Number of seasons",
                        4,
                        Season.values().length
                );
            }
        },

        daysInAYearIsTheSumOfTheDaysOfAllSeasons = new UniversalRule() {
            @Override
            public void test() {
                int dayCount = 0;

                for(Season season : Season.values()) {
                    dayCount += season.lengthInDays();
                }

                assertEquals("Year length in days",
                    dayCount,
                    Season.YEAR_LENGTH_IN_DAYS
                );
            }
        },

        displayNamesAreUnique = new UniversalRule() {
            @Override
            public void test() {
                HashSet<String> names = new HashSet<>();

                for(Season season: Season.values()) {
                    names.add(season.name());
                }

                assertEquals("Number of unique names", names.size(), Season.values().length);
            }
        };

    private final static InstanceRule

        lengthIsConsistentWithFirstAndLastDay = new InstanceRule<Season>() {
            @Override
            public void test(Season season) {
                int computedLength = Math.abs(season.lastDay() - season.firstDay()) + 1;

                assertEquals(
                        String.format("%s length in days", season.toString()),
                        season.lengthInDays(), computedLength
                );
            }
        },

        firstAndLastDayNumberArePositive = new InstanceRule<Season>() {
            @Override
            public void test(Season season) {
                int computedLength = Math.abs(season.lastDay() - season.firstDay()) + 1;

                assertTrue(
                        String.format("First day of %s is positive number", season.toString()),
                        season.firstDay() >= 0
                );

                assertTrue(
                        String.format("Last day of %s is positive number", season.toString()),
                        season.lastDay() >= 0
                );
            }
        };

    private final static TwoInstanceRule
        dayOfCycleIsIndependentOfYear = new TwoInstanceRule<Integer, Integer>() {
            @Override
            public void test(Integer day, Integer year) {
                int absoluteDay = day + year * Season.YEAR_LENGTH_IN_DAYS;
                int expected  = ((absoluteDay % Season.YEAR_LENGTH_IN_DAYS) + Season.YEAR_LENGTH_IN_DAYS) % Season.YEAR_LENGTH_IN_DAYS;
                assertEquals("Day of year", expected , Season.dayOfCycle(absoluteDay));
            }
        },

        dayToSeasonIsConsistent = new TwoInstanceRule<Season, Integer>() {
            @Override
            public void test(final Season season, final Integer year) {
                final int firstDayOfYear = year * (Season.YEAR_LENGTH_IN_DAYS);

                testForAll(daysIn(season), new InstanceRule<Integer>() {
                    @Override
                    public void test(Integer dayOfSeason) {
                        int    absoluteDayI     = firstDayOfYear + dayOfSeason;
                        double offset           = random.nextDouble();

                        String message = String.format("%s on day %d (+%f) of year %d (abs. %d)", season.toString(), dayOfSeason, offset, year, absoluteDayI);
                        assertSame(message + " (int)", season, Season.onDay(absoluteDayI));
                        assertSame(message + " (double)", season, Season.onDay(absoluteDayI + offset));
                    }
                });
            }
        },

        dayOfSeasonIsConsistent = new TwoInstanceRule<Season, Integer>() {
            @Override
            public void test(final Season season, final Integer year) {
                final int firstDayOfYear = year * Season.YEAR_LENGTH_IN_DAYS;

                testForAll(daysIn(season), new InstanceRule<Integer>() {
                    @Override
                    public void test(Integer dayOfYear) {
                        int absoluteDayInt = dayOfYear + firstDayOfYear;
                        int dayOfSeason    = dayOfYear - season.firstDay();
                        double absoluteDayDouble = absoluteDayInt + random.nextDouble(); // 0 <= random.nextDouble() < 1.0

                        String message = String.format("day %d of %s (year %d)", dayOfYear, season, year);
                        assertEquals(message, dayOfSeason, Season.dayOfSeason(absoluteDayInt));
                        assertEquals(message, dayOfSeason, Season.dayOfSeason(absoluteDayDouble));
                    }
                });
            }
        };

    private final static InfixRelationRule
        isFollowedBy = new InfixRelationRule<Season,Season>() {
            @Override
            public void test(Season leftOperand, Season rightOperand) {
                assertSame(
                        String.format("%s is followed by %s", leftOperand.toString(), rightOperand.toString()),
                        leftOperand.next(), rightOperand
                );

                assertSame(
                        String.format("%s is followed by %s", leftOperand.toString(), rightOperand.toString()),
                        leftOperand.next(), rightOperand
                );
            }
        };
}
