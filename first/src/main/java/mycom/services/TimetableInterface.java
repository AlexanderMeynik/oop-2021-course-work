package mycom.services;


import model.Ship;

import java.util.LinkedList;
import java.util.List;


public interface TimetableInterface {
    LinkedList<Ship> generate(long time, Integer count);

    LinkedList<Ship> get();

    Boolean add(List<Ship> ship, String fileName);
}
