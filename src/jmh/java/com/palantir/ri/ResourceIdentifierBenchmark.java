/*
 * (c) Copyright 2021 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.ri;

import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 3, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(4)
@SuppressWarnings({"checkstyle:hideutilityclassconstructor", "VisibilityModifier", "DesignForExtension"})
public class ResourceIdentifierBenchmark {

    public enum Input {
        NO_INSTANCE("service", "", "type", "8c51aa27-32f6-4d25-a3a5-966196e57be3"),
        UUID("service", "instance", "type", "8c51aa27-32f6-4d25-a3a5-966196e57be3");

        private final String string;
        private final String service;
        private final String instance;
        private final String type;
        private final String locator;

        Input(String service, String instance, String type, String locator) {
            this.string = "ri." + service + "." + instance + "." + type + "." + locator;
            this.service = service;
            this.instance = instance;
            this.type = type;
            this.locator = locator;
        }
    }

    @Param
    Input input;

    @Benchmark
    public ResourceIdentifier fromString() {
        return ResourceIdentifier.of(input.string);
    }

    @Benchmark
    public ResourceIdentifier fromComponents() {
        return ResourceIdentifier.of(input.service, input.instance, input.type, input.locator);
    }

    public static void main(String[] _args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(ResourceIdentifierBenchmark.class.getSimpleName())
                .addProfiler(GCProfiler.class)
                .forks(1)
                .threads(4)
                .warmupIterations(3)
                .warmupTime(TimeValue.seconds(3))
                .measurementIterations(4)
                .measurementTime(TimeValue.seconds(3))
                .build();
        new Runner(opt).run();
    }
}
