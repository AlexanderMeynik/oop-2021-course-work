package mycom.web;

import mycom.services.MainInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/main")
public class MainRequests {

    private MainInterface mainService;

    @GetMapping("/timetable")
    public ResponseEntity<String> getTimetable(@RequestParam(required = false) Integer count) {
        return new ResponseEntity<>(mainService.getTimetable(count), HttpStatus.OK);
    }

    @GetMapping("/timetable/{fileName}")
    public ResponseEntity<String> getTimetableByFile(@PathVariable("fileName") String fileName) {
        return new ResponseEntity<>(mainService.getTimetableByFile(fileName), HttpStatus.OK);
    }
    @GetMapping("/simulate/{fileName}")
    public ResponseEntity<String> simulate(@PathVariable("fileName") String fileName) {
        return new ResponseEntity<>(mainService.simulate(fileName), HttpStatus.OK);
    }

    @GetMapping("/simulate")
    public ResponseEntity<String> simulate() {
        return new ResponseEntity<>(mainService.simulate(""), HttpStatus.OK);
    }

    @GetMapping("/save")
    public ResponseEntity<String> save() {
        return new ResponseEntity<>(mainService.saveTimetableInJson(), HttpStatus.OK);
    }

    @PostMapping(value = "/statistic")
    public ResponseEntity<String> saveStatistic(@RequestBody List<String> statistic) {
        return new ResponseEntity<>(mainService.saveStatistic(statistic), HttpStatus.OK);
    }

    @Autowired
    public void setApplicationService(MainInterface mainService) {
        this.mainService = mainService;
    }
}
