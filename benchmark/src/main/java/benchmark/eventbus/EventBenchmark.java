package benchmark.eventbus;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import com.ricardojlrufino.eventbus.EventBus;

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State( Scope.Benchmark )
public class EventBenchmark {

    @Benchmark
    @Fork(value = 2)
    @Measurement(iterations = 10, time = 1)
    @Warmup(iterations = 5, time = 1)
    public void testMethodHandleSpeed(Blackhole blackhole) {
        EventBus.notify(new UIEvents.EventBenchmark(blackhole));
    }

    
//    /**
//     * Runner options that runs all benchmarks in this test class
//     * namely benchmark oldWay and newWay.
//     * @return
//     */
//    private Options initBench() {
//        return new OptionsBuilder() //
//                .include(SampleBenchmarkTest.class.getSimpleName() + ".*") //
//                .mode(Mode.AverageTime) //
//                .verbosity(VerboseMode.EXTRA) //
//                .timeUnit(TimeUnit.MILLISECONDS) //
//                .warmupTime(TimeValue.seconds(1)) //
//                .measurementTime(TimeValue.milliseconds(1)) //
//                .measurementIterations(2) //
//                .threads(4) //
//                .warmupIterations(2) //
//                .shouldFailOnError(true) //
//                .shouldDoGC(true) //
//                .forks(1) //
//                .build();
//    }
    
    @Setup
    public void setup( Blackhole blackhole ) {
        EventBus.register(UIEvents.BOARD_CHANGE, event -> {
            event.getBlackhole().consume(event);
        });
    }

}