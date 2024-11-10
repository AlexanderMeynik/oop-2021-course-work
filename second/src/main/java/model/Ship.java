package model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Ship implements Cloneable, Comparable<Ship> {
    private long arriveTime;
    private final String name;
    private final CargoType type;
    private final int weight;
    private AtomicLong ammountOfGoods = new AtomicLong(0);
    private long stayTime;
    private AtomicInteger workingCranes = new AtomicInteger(0);
    private AtomicInteger delay = new AtomicInteger(0);
    private long waitingTime = 0;
    private long workStartTime = 0;
    private long workLengthTime = 0;

    public Ship() {
        this(0, "", CargoType.LIQUID, 0);
    }

    public Ship(long arriveTime, String name, CargoType type, int weight) {
        this.arriveTime = arriveTime;
        this.name = name;
        this.type = type;
        this.weight = weight;
        ammountOfGoods.set((long) (weight * WeightCoefficients.data[type.ordinal()]));
        stayTime = (ammountOfGoods.get() / 2) * 1000 * 60;
    }

    public void setDelay(int delay) {
        this.delay.addAndGet(delay);
    }

    public AtomicInteger getDelay() {
        return delay;
    }

    public long getPenalty(long currentTime) {
        return Math.max(0, currentTime - arriveTime - stayTime) / 1000 / 60;
    }

    public AtomicInteger getWorkingCranes() {
        return workingCranes;
    }

    public AtomicLong getAmmountOfGoods() {
        return ammountOfGoods;
    }

    public CargoType getType() {
        return type;
    }

    public void setWaitingTime(long waitingTime) {
        this.waitingTime = waitingTime;
    }

    public long getWorkStartTime() {
        return workStartTime;
    }

    public void setWorkStartTime(long workStartTime) {
        this.workStartTime = workStartTime;
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }

    public long getStayTime() {
        return stayTime;
    }

    public long getWaitingTime() {
        return waitingTime;
    }

    public long getWorkLengthTime() {
        return workLengthTime;
    }

    public void setWorkLengthTime(long workLengthTime) {
        this.workLengthTime = workLengthTime;
    }

    @Override
    public int compareTo(Ship anotherShip) {
        return Long.compare(arriveTime, anotherShip.arriveTime);
    }

    public enum CargoType {
        LOOSE("loose"),
        LIQUID("liquid"),
        CONTAINER("container");
        private final String title;

        CargoType(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }


    public long getArriveTime() {
        return arriveTime;
    }

    public void updateArriveTime(long delay) {
        arriveTime += delay;
    }

    private String getTime(long time) {
        final Calendar date = Calendar.getInstance();
        date.setTimeInMillis(time);
        SimpleDateFormat format = new SimpleDateFormat("dd MMMMMMM yyyy hh:mm", Locale.ENGLISH);
        return (format.format(date.getTime()));
    }

    private String getWaitingFormatted() {

        StringBuilder str = new StringBuilder();
        long minutes = waitingTime / 60000;
        long days = minutes / 60 / 24;
        str.append(days).append(":");
        long hours = (minutes - days * 60 * 24) / 60;
        str.append(hours).append(":");
        minutes = minutes - days * 60 * 24 - hours * 60;
        str.append(minutes);
        return str.toString();
    }

    private String getCounter() {
        if (type.ordinal() == 2) {
            return "containers";
        } else {
            return "tons";
        }
    }

    @Override
    public String toString() {
        if (waitingTime == 0) {
            return String.format("Ship %s arrived at %s, having %d %s of %s cargo, will unload within %d minutes and stay at port %d minutes longer",
                    this.name, this.getTime(this.arriveTime),/* this.getWeight()*/this.weight,
                    getCounter(), this.type.getTitle(),
                    this.stayTime / 1000 / 60, this.delay.get());
        } else {
            return String.format("Ship %s arrived at %s, waited in queue for %s, began to unload %s within %d minutes",
                    this.name, this.getTime(this.arriveTime), getWaitingFormatted(), this.getTime(this.workStartTime),
                    this.workLengthTime / 1000 / 60);
        }
    }

    @Override
    public Ship clone() {
        final Ship ship = new Ship(this.arriveTime, this.name, this.type, this.weight);
        ship.setDelay(delay.get());
        return ship;
    }
}
