package reactor.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
public class FluxTest {

    @Test
    public void fluxSubscriber(){
        Flux<String> fluxString =  Flux.just("Vinicius Von Ahn", "Web Flux", "Mono")
                .log();

        StepVerifier.create(fluxString)
                .expectNext("Vinicius Von Ahn", "Web Flux", "Mono")
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberNumbers(){
        Flux<Integer> fluxInteger =  Flux.range(1,5)
                .log();

        fluxInteger.subscribe(i -> log.info("Number {}", i));

        log.info("-----------------------------");
        StepVerifier.create(fluxInteger)
                .expectNext(1,2,3,4,5)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberFromList(){
        Flux<Integer> fluxInteger =  Flux.fromIterable(List.of(1,2,3,4,5))
                .log();

        fluxInteger.subscribe(i -> log.info("Number {}", i));

        log.info("-----------------------------");
        StepVerifier.create(fluxInteger)
                .expectNext(1,2,3,4,5)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberNumbersError(){
        Flux<Integer> fluxInteger =  Flux.range(1,5)
                .log()
                .map(i -> {
                    if(i == 4){
                        throw new IndexOutOfBoundsException("index error");
                    }
                    return i;
                });

        fluxInteger.subscribe(i -> log.info("Number {}", i), Throwable::printStackTrace,
                () -> log.info("DONE!"), subscription -> subscription.request(3));

        log.info("-----------------------------");
        StepVerifier.create(fluxInteger)
                .expectNext(1,2,3)
                .expectError(IndexOutOfBoundsException.class)
                .verify();
    }

    @Test
    public void fluxSubscriberNumbersUglyBackpressure(){
        Flux<Integer> fluxInteger =  Flux.range(1,10)
                .log();

        fluxInteger.subscribe(new Subscriber<>() {
            private int count = 0;
            private Subscription subscription;
            private final int requestCount = 2;
            @Override
            public void onSubscribe(Subscription subscription) {
                this.subscription = subscription;
                subscription.request(requestCount);
            }

            @Override
            public void onNext(Integer integer) {
                count++;
                if(count >= requestCount){
                    count = 0;
                    subscription.request(requestCount);
                }
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        });

        log.info("-----------------------------");
        StepVerifier.create(fluxInteger)
                .expectNext(1,2,3,4,5,6,7,8,9,10)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberNumbersNotSoUglyBackpressure(){
        Flux<Integer> fluxInteger =  Flux.range(1,10)
                .log();

        fluxInteger.subscribe(new BaseSubscriber<>() {
            private int count = 0;
            private final int requestCount = 2;

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                request(requestCount);
            }

            @Override
            protected void hookOnNext(Integer value) {
                count++;
                if(count >= requestCount){
                    count = 0;
                    request(requestCount);
                }
            }
        });

        log.info("-----------------------------");
        StepVerifier.create(fluxInteger)
                .expectNext(1,2,3,4,5,6,7,8,9,10)
                .verifyComplete();
    }
}
