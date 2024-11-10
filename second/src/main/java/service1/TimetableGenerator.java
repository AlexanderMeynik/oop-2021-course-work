package service1;

import model.Ship;

import java.util.LinkedList;
import java.util.Random;

public class TimetableGenerator {
    private static final long SIMULATION_SPAN = 30 * 1000 * 60 * 60 * 24L;
    private static final int MAX_SHIPS = 100;
    private static final int MAX_WEIGHT = 1000;
    public static final int NAME_LENGTH = 5;

    private static final Random random = new Random();

    public static Ship generateShip(long time) {
        return new Ship(time + Math.floorMod(random.nextLong(), SIMULATION_SPAN), getRandomShipName(NAME_LENGTH)
                , getRandomShipType(), random.nextInt(MAX_WEIGHT));
    }

    public static LinkedList<Ship> generate(long time) {
        final LinkedList<Ship> timetable = new LinkedList<>();
        final int shipCount = random.nextInt(MAX_SHIPS);
        for (int i = 0; i < shipCount; i++) {
            timetable.add(generateShip(time));
        }
        //Collections.sort(timetable);
        return timetable;
    }

    public static LinkedList<Ship> generate(long time, long n) {
        final LinkedList<Ship> timetable = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            timetable.add(generateShip(time));
        }
        //Collections.sort(timetable);
        return timetable;
    }


    private static Ship.CargoType getRandomShipType() {
        return Ship.CargoType.values()[random.nextInt(Ship.CargoType.values().length)];
    }

    public static String getRandomShipName(int size) {
        StringBuilder sb = new StringBuilder();
        while (sb.length() < size) {
            sb.append(Integer.toHexString(random.nextInt()));
        }
        return sb.substring(0, size);
    }
}
