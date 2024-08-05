package org.study.pixelbattleback;

import org.junit.jupiter.api.Test;
import org.study.pixelbattleback.dto.PixelRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class MapServiceTest {

    /**
     * Минутный тест, постоянно обращающийся к методу окраски пикселя.
     * Каждую секунду выводит в консоль количество успешных на данный момент вызовов,
     * благодаря такому подходу можно наглядно увидеть прирост производительности после оптимизации -
     * за одно и то же время количество обработанных вызовов увеличится.
     */
    @Test
    void benchmark() throws InterruptedException {
        AtomicLong handled = new AtomicLong();
        MapService mapService = new MapService();

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.printf("%f%n", handled.get() / (double) 1_000_00);
            }
        }, 0, 1000);

        List<Producer> producers = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            producers.add(new Producer(mapService, handled));
        }

        producers.forEach(Thread::start);
        Thread.sleep(60_000);
        producers.forEach(Producer::stopProducer);
        for (Producer producer: producers) {
            producer.join();
        }
    }

    private static class Producer extends Thread {

        private final MapService mapService;
        private final AtomicLong handled;

        public Producer(MapService mapService, AtomicLong handled) {
            this.mapService = mapService;
            this.handled = handled;
        }

        private boolean isActive = true;

        @Override
        public void run() {
            int threadColor = ThreadLocalRandom.current().nextInt(1, 255 << 17);
            while (isActive) {
                PixelRequest pixelRequest = new PixelRequest();
                int x = ThreadLocalRandom.current().nextInt(0, 100);
                int y = ThreadLocalRandom.current().nextInt(0, 100);
                pixelRequest.setColor(threadColor);
                pixelRequest.setX(x);
                pixelRequest.setY(y);
                if (mapService.draw(pixelRequest)) {
                    handled.incrementAndGet();
                }
            }

        }

        public void stopProducer() {
            isActive = false;
        }
    }
}
