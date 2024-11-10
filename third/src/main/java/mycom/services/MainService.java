package mycom.services;


import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import exceptions.TimetableNotFoundException;
import com.google.gson.Gson;
import model.Ship;
import service3.PortModel;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.*;

@Service
public class MainService implements MainInterface {
    private RestTemplate restTemplate = new RestTemplate();
    private String url = "http://localhost:8081/timetable/generate/";
    private String url2="http://localhost:8081/timetable/get";

    @Override
    public String getTimetable(Integer count) throws TimetableNotFoundException {
        ResponseEntity<String> responseEntity;
        if (count == null) {
            responseEntity = restTemplate.getForEntity(url, String.class);
        } else {
            responseEntity = restTemplate.getForEntity(url + "?count=" + count, String.class);
        }
        String fileName = "timetable-" + getRandomShipName() + ".json";
        try (final FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(Objects.requireNonNull(responseEntity.getBody()));
            fileWriter.flush();
        } catch (Exception e) {
            throw new TimetableNotFoundException();
        }
        return fileName;
    }

    @Override
    public String getTimetableByFile(String fileName) throws TimetableNotFoundException {
            StringBuilder result = new StringBuilder();
            File file = new File(fileName);
            if (file.exists()) {
                try {
                    Scanner sc = new Scanner(file);
                    while (sc.hasNext()) {
                        result.append(sc.nextLine());
                    }
                    return result.toString();
                } catch (Exception e) {
                    throw new TimetableNotFoundException();
                }
            }
            throw new TimetableNotFoundException();
    }

    @Override
    public String simulate(String fileName) throws TimetableNotFoundException {
        PortModel portModel;
        if (fileName.length() == 0) {
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(url2, String.class);
            List<Ship> shipps = new LinkedList<>();
            JsonParser jsonParser = new JsonParser();
            JsonElement obj = jsonParser.parse(new Scanner(responseEntity.getBody()).nextLine());
            for (JsonElement elem : obj.getAsJsonArray()) {
                Ship sh = new Gson().fromJson(elem.toString(), Ship.class);
                shipps.add(sh);
            }
            if(shipps.isEmpty())
            {
                throw new TimetableNotFoundException();
            }

            portModel = new PortModel(System.currentTimeMillis(), shipps);
        } else {
            try {
                portModel = new PortModel(System.currentTimeMillis(), fileName);
            } catch (FileNotFoundException e) {
                throw new TimetableNotFoundException();
            }
        }
        return portModel.runSimulation();

    }

    @Override
    public String saveTimetableInJson() throws TimetableNotFoundException {
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url2, String.class);
        if(responseEntity.getBody().equals(""))
        {
            throw new TimetableNotFoundException();
        }
        String fileName = "timetable-" + getRandomShipName() + ".json";
        try (final FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(Objects.requireNonNull(responseEntity.getBody()));
            fileWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
            throw new TimetableNotFoundException();
        }
        System.out.println("Timetable saved in "+fileName+ " json file");
        return fileName;
    }

    @Override
    public String saveStatistic(List<String> statistic) throws TimetableNotFoundException {
        String fileName = "statistic-" + getRandomShipName() + ".json";
        try (final FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(new Gson().toJson(statistic));
            fileWriter.flush();
        } catch (Exception e) {
            throw new TimetableNotFoundException();
        }
        return fileName;
    }

    private static String getRandomShipName() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        while (sb.length() < 5) {
            sb.append(Integer.toHexString(random.nextInt()));
        }
        return sb.substring(0, 5);
    }
}
