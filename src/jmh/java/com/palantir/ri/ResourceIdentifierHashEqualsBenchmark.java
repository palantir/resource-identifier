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
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
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
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(4)
@SuppressWarnings({"checkstyle:hideutilityclassconstructor", "VisibilityModifier", "DesignForExtension"})
public class ResourceIdentifierHashEqualsBenchmark {

    private static final ResourceIdentifier rid1 =
            ResourceIdentifier.of("ri.service.instance.type.8c51aa27-32f6-4d25-a3a5-966196e57be1");
    private static final ResourceIdentifier rid2 =
            ResourceIdentifier.of("ri.service.instance.type.8c51aa27-32f6-4d25-a3a5-966196e57be2");
    private static final ResourceIdentifier rid3 = ResourceIdentifier.of(rid1.toString());

    @Benchmark
    @OperationsPerInvocation(3)
    public int hash() {
        return rid1.hashCode() + rid2.hashCode() + rid3.hashCode();
    }

    @Benchmark
    @OperationsPerInvocation(2)
    public boolean equals() {
        return rid1.equals(rid3) && rid1.equals(rid2);
    }

    public static void main(String[] _args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(ResourceIdentifierHashEqualsBenchmark.class.getSimpleName())
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
