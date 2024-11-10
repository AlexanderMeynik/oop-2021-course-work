package mycom.web;

import mycom.services.TimetableInterface;
import model.Ship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/timetable")
public class TimetableRequests {

    private TimetableInterface timetableInterface;

    @GetMapping("/generate")
    public ResponseEntity<List<Ship>> generateTimetable(@RequestParam(required = false) Integer count) {
        return new ResponseEntity<>(timetableInterface.generate(System.currentTimeMillis(), count), HttpStatus.OK);
    }

    @GetMapping("/get")
    public ResponseEntity<List<Ship>> getTimetable() {
        return new ResponseEntity<>(timetableInterface.get(), HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<Boolean> addShip(@RequestBody List<Ship> ship, @RequestParam(required = false) String fileName) {
        return new ResponseEntity<>(timetableInterface.add(ship, fileName), HttpStatus.OK);
    }

    @Autowired
    public void setApplicationService(TimetableInterface timetableInterface) {
        this.timetableInterface = timetableInterface;
    }
}
