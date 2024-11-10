package service3;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import model.Crane;
import model.Ship;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class PortModel {
    public static RestTemplate restTemplate = new RestTemplate();
    public static String url = "http://localhost:8082/main/";
    private static String statistics;
    public static final int CRANES_COST = 30000;
    private final LinkedList<Ship> timetable = new LinkedList<>();
    private final AtomicLong currentTime;
    private long[] minimalPenalties = {Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE};
    private int[] minCranesCount;
    private long avgQueryLength = 0;
    private long avgQueryDuration = 0;
    private int avgDelay = 0;
    private int maxDelay = Integer.MIN_VALUE;
    private int minAvgDelay = 0;
    private int minMaxDelay = 0;
    private long minTime = 0;
    private ArrayList<Ship> lastDoneList = new ArrayList<>();
    private final Calendar calendar = Calendar.getInstance();
    private static final Random random = new Random();
    private static final int MAX_WEIGHT_DELAY = 1440;
    private static final long MAX_TIME_DELAY = 7 * 1000 * 60 * 60 * 24L;

    public PortModel(long startTime, String JsonName) throws FileNotFoundException {

        JsonParser jsonParser = new JsonParser();
        JsonElement obj = jsonParser.parse(new Scanner(new File(JsonName)).nextLine());
        System.out.println("Timetable data");
        for (JsonElement elem : obj.getAsJsonArray()) {
            Ship sh = new Gson().fromJson(elem.toString(), Ship.class);
            this.timetable.add(sh);
            System.out.println(sh);
        }

        currentTime = new AtomicLong(startTime);
        Collections.sort(timetable);
        calendar.setTimeInMillis(startTime);
    }

    public PortModel(long startTime, List<Ship> shipList) {
        timetable.addAll(shipList);
        currentTime = new AtomicLong(startTime);
        Collections.sort(timetable);
        calendar.setTimeInMillis(startTime);
    }

    public String runSimulation() {

        int[] cranesCount = new int[]{1, 1, 1};

        simulate(cranesCount, new boolean[]{true, true, true}, 0);
        //System.exit(0);
        return statistics;
    }

    private static void setDelays(LinkedList<Ship> timetable, long time) {
        timetable.forEach(ship -> {
            int d = random.nextInt(MAX_WEIGHT_DELAY);
            ship.setDelay(d);
            final long delay = -MAX_TIME_DELAY + random.nextLong() % (MAX_TIME_DELAY * 2);
            if (ship.getArriveTime() + delay < time) {
                ship.updateArriveTime(-delay);
            } else {
                ship.updateArriveTime(delay);
            }
        });
        Collections.sort(timetable);
    }

    private void simulate(int[] ammountOfCranes, boolean[] runStatus, int recEnd) {
        final Ship.CargoType[] types = Ship.CargoType.values();
        if (recEnd == 2) {
            List<String> result = new ArrayList<>();
            System.out.println("Simulation finished:");

            SimpleDateFormat format = new SimpleDateFormat("dd MMMMMMM yyyy hh:mm", Locale.ENGLISH);

            result.add("Unloading start time " + String.format("%s\nUnloading end time %s", format.format(calendar.getTime()), format.format(minTime)));
            result.add("Unloaded ships count: " + timetable.size());
            result.add("Average unloading queue length: " + avgQueryLength + " ships");
            result.add("Average waiting time: " + (avgQueryDuration / 1000 / 60) + " minutes");
            result.add("Maximal unload latency: " + minMaxDelay + " minutes");
            result.add("Average unload latency: " + (minAvgDelay / ((this.timetable.size() == 0) ? 1 : this.timetable.size())) + " minutes");
            result.add("Amount of the fine : " + Arrays.stream(minimalPenalties).reduce(Long::sum).getAsLong() + " c. u.");
            result.add("Used cranes:");


            System.out.println("Unloading start time " + String.format("%s\nUnloading end time %s", format.format(calendar.getTime()), format.format(minTime)));
            System.out.println("Unloaded ships count: " + timetable.size());
            System.out.println("Average unloading queue length: " + avgQueryLength + " ships");
            System.out.println("Average waiting time: " + (avgQueryDuration / 1000 / 60) + " minutes");
            System.out.println("Maximal unload latency: " + minMaxDelay + " minutes");
            System.out.println("Average unload latency: " + (minAvgDelay / ((this.timetable.size() == 0) ? 1 : this.timetable.size())) + " minutes");
            System.out.println("Amount of the fine : " + Arrays.stream(minimalPenalties).reduce(Long::sum).getAsLong() + " c. u.");
            System.out.println("Used cranes:");

            for (Ship.CargoType value : types) {
                result.add(value.getTitle() + ": " + minCranesCount[value.ordinal()]);
                System.out.println(value.getTitle() + ": " + minCranesCount[value.ordinal()]);
            }
            result.add("Ship information");
            System.out.println("Ship information");
            for (Ship ship : lastDoneList) {
                result.add(ship.toString());
                System.out.println(ship);
            }
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url + "statistic/", result, String.class);//TODO Post request
            statistics = responseEntity.getBody();
            System.out.println("Statistic save to " + responseEntity.getBody());
            return;
        }

        AtomicLong currentTime = new AtomicLong(this.currentTime.get());
        LinkedList<Ship> timetable = new LinkedList<>();
        this.timetable.forEach(ship ->
                timetable.add(ship.clone()));
        setDelays(timetable, currentTime.get());
        avgDelay = 0;
        maxDelay = Integer.MIN_VALUE;
        timetable.forEach(ship -> {
            if (ship.getDelay().get() > maxDelay) {
                maxDelay = ship.getDelay().get();
            }
            avgDelay += ship.getDelay().get();
        });
        final ConcurrentHashMap<Ship.CargoType, Queue<Ship>> waitingShip = new ConcurrentHashMap<>();
        final ConcurrentHashMap<Ship.CargoType, Queue<Ship>> performShip = new ConcurrentHashMap<>();
        final HashMap<Ship.CargoType, ArrayList<Crane>> cranes = new HashMap<>();
        System.out.println(Arrays.toString(ammountOfCranes));

        for (Ship.CargoType value : types) {
            final ArrayList<Crane> list = new ArrayList<>();
            for (int i = 0; i < ammountOfCranes[value.ordinal()]; i++) {
                list.add(new Crane());
            }
            cranes.put(value, list);
            waitingShip.put(value, new ConcurrentLinkedQueue<>());
            performShip.put(value, new ConcurrentLinkedQueue<>());
        }
        //System.out.println(Thread.activeCount());
        long queryLength = 0L;
        long queryCount = 0L;
        AtomicLong queryDuration = new AtomicLong();
        long count = timetable.size();
        AtomicLong processed = new AtomicLong(0);
        Vector<Long> currentPenalties = new Vector<>();

        currentPenalties.add(0L);
        currentPenalties.add(0L);
        currentPenalties.add(0L);


        final ArrayList<Ship> doneList = new ArrayList<>();
        while (true) {
            currentTime.addAndGet(1000 * 60);
            AtomicReference<Iterator<Ship>> iterator = new AtomicReference<>(timetable.iterator());
            while (iterator.get().hasNext()) {
                Ship ship = iterator.get().next();
                if (ship.getArriveTime() <= currentTime.get()) {
                    waitingShip.get(ship.getType()).add(ship);
                    iterator.get().remove();
                } else {
                    break;
                }
            }
            performShip.forEach((type, queue) -> {
                iterator.set(queue.iterator());
                while (iterator.get().hasNext()) {
                    Ship ship = iterator.get().next();
                    if (ship.getAmmountOfGoods().get() == 0) {
                        if (ship.getDelay().get() == 0) {
                            currentPenalties.set(type.ordinal(), currentPenalties.get(type.ordinal()) + ship.getPenalty(currentTime.get()));
                            processed.incrementAndGet();
                            ship.setWorkLengthTime(currentTime.get() - ship.getWorkStartTime());
                            doneList.add(ship);
                            iterator.get().remove();
                        } else {
                            ship.getDelay().decrementAndGet();
                        }
                    }
                }
            });
            cranes.forEach((type, craneList) -> craneList.forEach(crane -> {
                if (crane.isEnded()) {
                    performShip.get(type).forEach(ship -> {
                        if (crane.isEnded() && ship.getWorkingCranes().get() == 1) {
                            ship.getWorkingCranes().incrementAndGet();
                            crane.setShip(ship);
                        }
                    });
                    if (crane.isEnded()) {
                        final Ship ship = waitingShip.get(type).poll();
                        if (ship != null) {
                            ship.getWorkingCranes().incrementAndGet();
                            crane.setShip(ship);
                            ship.setWaitingTime(currentTime.get() - ship.getArriveTime());
                            ship.setWorkStartTime(currentTime.get());
                            performShip.get(type).add(ship);
                            queryDuration.addAndGet(currentTime.get() - ship.getArriveTime());

                        }
                    }
                }
                crane.startWork();
            }));
            for (Map.Entry<Ship.CargoType, Queue<Ship>> entry : waitingShip.entrySet()) {
                Ship.CargoType key = entry.getKey();
                Queue<Ship> ships = entry.getValue();
                if (ships.size() > 0) {
                    queryLength += ships.size();
                    queryCount++;
                }
            }
            if (processed.get() == count) {
                cranes.forEach((type, craneList) -> craneList.forEach(Crane::terminate));
                break;
            }
        }
        for (int i = 0; i < 3; i++) {
            currentPenalties.set(i, currentPenalties.get(i) / 60 * 100);
        }
       /* for (long lat : minimalPenalties) {
            System.out.print(lat + " ");
        }
        System.out.println();

        for (Long lat : currentPenalties) {
            System.out.print((lat) + " ");
        }
        System.out.println();*/
        System.out.println("Min penalty: " + Arrays.stream(minimalPenalties).reduce(Long::sum).getAsLong()
                + ", current penalty: " + currentPenalties.stream().reduce(Long::sum).get() + "\nRun Status :");
        for (Ship.CargoType t : types) {

            System.out.println(t.getTitle() + " crane optimal count "
                    + ((runStatus[t.ordinal()]) ? "is not found" : String.format("is reached and equals %d!", ammountOfCranes[t.ordinal()])));
        }
        System.out.println("\n---");

        for (Ship.CargoType t : types) {
            if (runStatus[t.ordinal()]) {
                if (minimalPenalties[t.ordinal()] - currentPenalties.get(t.ordinal()) < CRANES_COST) {
                    ammountOfCranes[t.ordinal()]--;
                    runStatus[t.ordinal()] = false;
                } else {
                    ammountOfCranes[t.ordinal()]++;
                    minimalPenalties[t.ordinal()] = currentPenalties.get(t.ordinal());

                    if (queryCount > 0) {
                        avgQueryLength = queryLength / queryCount;
                        avgQueryDuration = queryDuration.get() / count;
                    }
                    minTime = currentTime.get();
                    minCranesCount = ammountOfCranes;
                    lastDoneList.clear();
                    lastDoneList.addAll(doneList);
                    minAvgDelay = avgDelay;
                    minMaxDelay = maxDelay;

                }

            }
        }
        if (!(runStatus[0] || runStatus[1] || runStatus[2])) {
            recEnd++;
            simulate(ammountOfCranes, runStatus, recEnd);
        }
        if (recEnd == 0)
            simulate(ammountOfCranes, runStatus, recEnd);
        else {
            if (queryCount/*queryCount.get()*/ > 0) {
                avgQueryLength = queryLength / queryCount;
                avgQueryDuration = queryDuration.get() / count;
            }
            for (Ship.CargoType t : types) {
                minimalPenalties[t.ordinal()] = currentPenalties.get(t.ordinal());
            }
            minCranesCount = ammountOfCranes;
            lastDoneList.clear();
            lastDoneList.addAll(doneList);
            minAvgDelay = avgDelay;
            minMaxDelay = maxDelay;
            recEnd++;
        }
    }
}
