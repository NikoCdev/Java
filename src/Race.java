import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Race {
    public static AtomicLong startRaceTime = new AtomicLong();

    public static void main(String[] args) throws InterruptedException {
        final int numberOfCars = 2;
        CountDownLatch latch = new CountDownLatch(numberOfCars);

        List<RaceCarRunnable> cars = new ArrayList<>();
        cars.add(new RaceCarRunnable("Car 1", 120, 1000, latch));
        cars.add(new RaceCarRunnable("Car 2", 150, 1000, latch));

        List<Thread> threads = new ArrayList<>();
        for (RaceCarRunnable car : cars) {
            threads.add(new Thread(car));
        }

        startRace(threads);
        latch.await(); 

        RaceCarRunnable winner = cars.get(0);
        for (RaceCarRunnable car : cars) {
            System.out.println(car.getName() + " FINISHED! Time: " + convertToTime(car.getFinishTime()));
            if (car.getFinishTime() < winner.getFinishTime()) {
                winner = car;
            }
        }

        System.out.println("Winner is " + winner.getName() + " with time: " + convertToTime(winner.getFinishTime()));
    }

    public static String convertToTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss.SSS");
        return sdf.format(new Date(time));
    }

    public static void startRace(List<Thread> cars) {
        new Thread(() -> {
            try {
                Thread.sleep(500);
                System.out.println("3...");
                Thread.sleep(500);
                System.out.println("2...");
                Thread.sleep(500);
                System.out.println("1...");
                Thread.sleep(500);
                System.out.println("GO!!!");
                startRaceTime.set(System.currentTimeMillis());
                cars.forEach(Thread::start);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    public static class Car {
        protected String name;
        protected int maxSpeed;

        public Car(String name, int maxSpeed) {
            this.name = name;
            this.maxSpeed = maxSpeed;
        }

        public String getName() {
            return name;
        }

        public int getMaxSpeed() {
            return maxSpeed;
        }
    }

    public static class RaceCarRunnable extends Car implements Runnable {
        private int passed = 0;
        private final int distance;
        private boolean isFinish = false;
        private final CountDownLatch latch;
        private long finishTime;

        public RaceCarRunnable(String name, int maxSpeed, int distance, CountDownLatch latch) {
            super(name, maxSpeed);
            this.distance = distance;
            this.latch = latch;
        }

        private int getRandomSpeed() {
            return ThreadLocalRandom.current().nextInt(maxSpeed / 2, maxSpeed + 1);
        }

        @Override
        public void run() {
            while (!isFinish) {
                try {
                    Thread.sleep(1000);
                    int speed = getRandomSpeed();
                    passed += speed;
                    System.out.println(name + " => speed: " + speed + "; progress: " + passed + "/" + distance);
                    if (passed >= distance) {
                        isFinish = true;
                        finishTime = System.currentTimeMillis() - startRaceTime.get();
                        latch.countDown();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        public long getFinishTime() {
            return finishTime;
        }
    }
}
