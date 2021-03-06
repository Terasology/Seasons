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
package org.terasology.specificationLanguage;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author DizzyDragon
 * Simple, lightweight specification language embedded in JUnit.
 */
public final class SpecificationLanguage {

    private SpecificationLanguage() {
        // Empty private constructor for utility class
    }

    public static void test(UniversalRule rule) {
        rule.test();
    }

    public static <T> void test(T instance, InstanceRule<T> rule) {
        rule.test(instance);
    }

    //////////////////////////////////////
    // testing of rules

    public static <A, B> void test(A leftOperand, InfixRelationRule<A, B> rule, B rightOperand) {
        rule.test(leftOperand, rightOperand);
    }

    public static void assuming(UniversalRule rule) {
        assertDoesNotThrow(() -> {
            test(rule);
        });
    }

    public static <T> void assuming(T instance, InstanceRule<T> rule) {
        assertDoesNotThrow(() -> {
            test(instance, rule);
        });
    }

    public static <A, B> void assuming(A leftOperand, InfixRelationRule<A, B> rule, B rightOperand) {
        assertDoesNotThrow(() -> {
            test(leftOperand, rule, rightOperand);
        });
    }

    public static <T> void testForAll(Domain<T> domain, InstanceRule<T> rule) {
        for (T instance : domain.generateUniversalSamples()) {
            rule.test(instance);
        }
    }

    public static <A, B> void testForAll(Domain<A> domainA, Domain<B> domainB, TwoInstanceRule<A, B> rule) {
        for (A a : domainA.generateUniversalSamples()) {
            for (B b : domainB.generateUniversalSamples()) {
                rule.test(a, b);
            }
        }
    }

    // with quantifiers

    public static <A, B, C> void testForAll(Domain<A> domainA, Domain<B> domainB, Domain<C> domainC, ThreeInstanceRule<A, B, C> rule) {
        for (A a : domainA.generateUniversalSamples()) {
            for (B b : domainB.generateUniversalSamples()) {
                for (C c : domainC.generateUniversalSamples()) {
                    rule.test(a, b, c);
                }
            }
        }
    }

    public static <T extends Enum<T>> void testExists(EnumDomain<T> domain, InstanceRule<T> rule) {

        for (T instance : domain.generateUniversalSamples()) {
            try {
                rule.test(instance);
            } catch (AssertionError e) {
                // on failure try next instance
                continue;
            }
            // on success return
            return;
        }
        //failed on all instances
        fail();
    }

    public static <T> void assumingForAll(Domain<T> domain, InstanceRule<T> rule) {
        assertDoesNotThrow(() -> {
            testForAll(domain, rule);
        });
    }

    public static <T extends Enum<T>> void assumingExists(EnumDomain<T> domain, InstanceRule<T> rule) {
        assertDoesNotThrow(() -> {
            testExists(domain, rule);
        });
    }

    // rule without arguments
    public interface UniversalRule {
        void test();
    }

    public interface InstanceRule<T> {
        void test(T instance);
    }

    //////////////////////////////////////
    // Rule types

    public interface TwoInstanceRule<A, B> {
        void test(A instanceA, B instanceB);
    }

    public interface ThreeInstanceRule<A, B, C> {
        void test(A instanceA, B instanceB, C instanceC);
    }

    public interface InfixRelationRule<A, B> extends TwoInstanceRule<A, B> {
    }

    //////////////////////////////////////
    // Domain types
    public abstract static class Domain<T> {
        protected final Class<T> underlyingClass;

        public Domain(Class<T> underlyingClass) {
            if (underlyingClass == null) {
                throw new NullPointerException();
            }

            this.underlyingClass = underlyingClass;
        }

        public abstract Iterable<T> generateUniversalSamples();
    }

    public static final class EnumDomain<T extends Enum<T>> extends Domain<T> {
        public EnumDomain(Class<T> underlyingEnumType) {
            super(underlyingEnumType);
        }

        public Iterable<T> generateUniversalSamples() {
            return Arrays.asList((T[]) underlyingClass.getEnumConstants());
        }
    }
}
