package reactor.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

@Slf4j
public class MonoTest {

    @Test
    public void monoSubscriber(){
        String name = "Vinicius Von Ahn";
        Mono<String> mono = Mono.just(name)
                .log();

        mono.subscribe();
        log.info("-------------------------");
        StepVerifier.create(mono)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumer(){
        String name = "Vinicius Von Ahn";
        Mono<String> mono = Mono.just(name)
                .log();

        mono.subscribe(s -> log.info("Value {}", s));
        log.info("-------------------------");
        StepVerifier.create(mono)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerError(){
        String name = "Vinicius Von Ahn";
        Mono<String> mono = Mono.just(name)
                .map(s -> {
                    throw new RuntimeException("Testing mono with error");
                });

        mono.subscribe(s -> log.info("Name {}", s), s -> log.error("Something bad happened"));
        mono.subscribe(s -> log.info("Name {}", s), Throwable::printStackTrace);

        log.info("-------------------------");

        StepVerifier.create(mono)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    public void monoSubscriberConsumerComplete(){
        String name = "Vinicius Von Ahn";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase);

        mono.subscribe(s -> log.info("Value {}", s), Throwable::printStackTrace,
                () -> log.info("FINISHED!"));
        log.info("-------------------------");
        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerSubscription(){
        String name = "Vinicius Von Ahn";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase);

        mono.subscribe(s -> log.info("Value {}", s), Throwable::printStackTrace,
                () -> log.info("FINISHED!"),
                subscription -> subscription.request(5));

        log.info("-------------------------");
        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoDoOnMethods(){
        String name = "Vinicius Von Ahn";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase)
                .doOnSubscribe(subscription -> log.info("Subscribed {}"))
                .doOnRequest(longNumber -> log.info("Request Received, starting doing something..."))
                .doOnNext(s -> log.info("Value is here. Executing doOnNext {}", s))
                .doOnSuccess(s -> log.info("doOnSuccess executed"));

        mono.subscribe(s -> log.info("Value {}", s), Throwable::printStackTrace,
                () -> log.info("FINISHED!"));
        log.info("-------------------------");
    }

    @Test
    public void monoDoOnError(){
        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal argument message error!"))
                .doOnError(e -> MonoTest.log.error("Error message: {}", e.getMessage()))
                .log();

        StepVerifier.create(error)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    public void monoDoOnErrorResume(){
        String name = "Vinicius Von Ahn";
        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal argument message error!"))
                .doOnError(e -> MonoTest.log.error("Error message: {}", e.getMessage()))
                .onErrorResume(e -> {
                    log.info("Inside On Error Resume");
                    return Mono.just(name);
                })
                .log();

        StepVerifier.create(error)
                .expectNext(name)
                .verifyComplete();
    }
}