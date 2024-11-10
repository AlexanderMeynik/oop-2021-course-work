package mycom.services;

import java.util.List;

public interface MainInterface {
    String getTimetable(Integer count);

    String getTimetableByFile(String fileName);

    String simulate(String fileName);

    String saveTimetableInJson();

    String saveStatistic(List<String> statistic);


}
