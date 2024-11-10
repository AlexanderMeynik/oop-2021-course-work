package service2;

import model.Ship;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import service1.TimetableGenerator;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class SecondService {
    private static RestTemplate restTemplate = new RestTemplate();
    private static String url = "http://localhost:8082/main/";
    private static String urlForShips = "http://localhost:8081/timetable/add/";
    static String timetableFile = null;

    public static void main(String[] args) {
        startProgram();
    }

    private static void startProgram() {
        Scanner sc = new Scanner(System.in);
        long time = System.currentTimeMillis();
        System.out.println("Generate new timetable (y/n)?");
        ResponseEntity<String> responseEntity;
        if (sc.nextLine().toLowerCase().trim().equals("y")) {
            responseEntity = restTemplate.getForEntity(url + "timetable/", String.class);
            timetableFile = responseEntity.getBody();
            if (timetableFile != null) {
                System.out.println("Add some ships (y/n)?");
                if (sc.next().toLowerCase().trim().equals("y")) {
                    shipAddition(sc, time);
                }
            }
            System.out.println("Timetable saved to " + timetableFile);
        } else {
            System.out.println("Enter file name\n");
            timetableFile = sc.next();
        }
        try {
            responseEntity = restTemplate.getForEntity(url + "simulate/" + timetableFile, String.class);
            System.out.println("Statistic saved to " + responseEntity.getBody());
        } catch (HttpClientErrorException e) {
            System.out.printf("ERROR: FILE %s NOT FOUND\n", timetableFile);
            startProgram();
        }
    }

    private static void shipAddition(Scanner sc, long time) {
        boolean runFlag = true;
        List<Ship> ships = new ArrayList<>();
        while (runFlag) {
            String command = sc.nextLine().toLowerCase().trim();

            Ship ship;
            switch (command) {
                case "add":
                    ship = addNewShip(sc);
                    break;
                case "generate":
                    ship = TimetableGenerator.generateShip(time);
                    break;
                case "exit": {
                    runFlag = false;
                    ship = null;
                    System.out.println("Addition of ships stopped!");
                    break;
                }
                default:
                    ship = null;
                    break;
            }

            if (runFlag) {
                if (ship != null) {
                    System.out.println(ship);
                    ships.add(ship);
                } else {
                    if (!command.equals("")) {
                        System.out.println(String.format("Invalid command %s!", command));
                    }
                }
                System.out.println("Add new ship/Generate new ship/Stop(add/generate/exit)?");
            }
        }
        restTemplate.postForEntity(urlForShips + "?fileName=" + timetableFile, ships, Boolean.class);
    }


    private static Ship addNewShip(Scanner sc) {
        System.out.println("Adding new ship");
        System.out.println("Enter arrival data in format dd.mm.yyyy hh:mm: ");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        String s = sc.nextLine();
        LocalDateTime date = LocalDateTime.parse(s, formatter);
        long mills = date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        System.out.println("Choose ship's cargo type (1. Loose, 2. Liquid, 3. Container):");
        int type = sc.nextInt();
        System.out.println("Enter weight: ");
        int weight = sc.nextInt();
        System.out.println("Enter latency: ");
        int delay = sc.nextInt();
        Ship ship = new Ship(mills, TimetableGenerator.getRandomShipName(TimetableGenerator.NAME_LENGTH), Ship.CargoType.values()[type - 1], weight);
        ship.setDelay(delay);
        return ship;
    }
}
