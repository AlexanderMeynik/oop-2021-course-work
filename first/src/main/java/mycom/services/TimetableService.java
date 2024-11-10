package mycom.services;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import exceptions.TimetableNotFoundException;

import model.Ship;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.*;
import service1.TimetableGenerator;

@Service
public class TimetableService implements TimetableInterface {

    private LinkedList<Ship> ships = new LinkedList<>();

    @Override
    public LinkedList<Ship> generate(long time, Integer count) {
        ships.clear();
        if (count != null) {
            ships.addAll(TimetableGenerator.generate(time, count));
        } else {
            ships.addAll(TimetableGenerator.generate(time));
        }
        System.out.println(Arrays.toString(ships.toArray(new Ship[0])));
        return get();
    }

    @Override
    public LinkedList<Ship> get() {
        return ships;
    }

    @Override
    public Boolean add(List<Ship> ship, String fileName) throws TimetableNotFoundException {
        if(fileName!=null) {
            JsonParser jsonParser = new JsonParser();
            JsonElement obj;
            LinkedList<Ship> timetable = new LinkedList<>();
            try {
                obj = jsonParser.parse(new Scanner(new File(fileName)).nextLine());
                for (JsonElement elem : obj.getAsJsonArray()) {
                    Ship sh = new Gson().fromJson(elem.toString(), Ship.class);
                    timetable.add(sh);
                }
                timetable.addAll(ship);
                Collections.sort(timetable);
                try (final FileWriter fileWriter = new FileWriter(fileName)) {
                    fileWriter.write(new Gson().toJson(timetable));
                    fileWriter.flush();
                } catch (Exception e) {
                    throw new TimetableNotFoundException();
                }
            } catch (FileNotFoundException e) {
                throw new TimetableNotFoundException();
            }
            return true;
        }
        else
        {
            ships.addAll(ship);
            Collections.sort(ships);
            return true;
        }
    }



}
