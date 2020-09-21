package com.space.controller;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
@RequestMapping(value = "/rest")
public class ShipController {

    private ShipService shipService;

    @Autowired
    public void setShipService(ShipService shipService) {
        this.shipService = shipService;
    }

    @PostMapping(value = "/ships")
    public ResponseEntity<?> createShip(@RequestBody Ship ship) {
        if (!shipService.isShipValid(ship)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (ship.getUsed() == null) ship.setUsed(false);

        final double rating = shipService.ratingShip(ship);
        ship.setRating(rating);
        final Ship savedShip = shipService.createShip(ship);

        return new ResponseEntity<>(savedShip, HttpStatus.OK);
    }

    @GetMapping(value = "/ships/{id}")
    public ResponseEntity<Ship> getShip(@PathVariable(name = "id") String path) {
        final Long id = shipService.convertIdToLong(path);
        if (!shipService.isIdValid(id)) {
           return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        final Ship ship = shipService.getShip(id);

        return ship == null ?
               new ResponseEntity<>(HttpStatus.NOT_FOUND) :
               new ResponseEntity<>(ship, HttpStatus.OK);
    }

    @PostMapping(value = "/ships/{id}")
    public ResponseEntity<Ship> updateShip(@PathVariable(name = "id") String path, @RequestBody Ship ship) {
        Long id = shipService.convertIdToLong(path);

        if (!shipService.isIdValid(id)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        final Ship shipUpdate;
        try {
            shipUpdate = shipService.updateShip(ship, id);
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return shipUpdate == null ?
                new ResponseEntity<>(HttpStatus.NOT_FOUND) :
                new ResponseEntity<>(shipUpdate, HttpStatus.OK);
    }

    @DeleteMapping(value = "/ships/{id}")
    public ResponseEntity<?> deleteShip(@PathVariable(name = "id") String path) {
        Long id = shipService.convertIdToLong(path);
        if (!shipService.isIdValid(id)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        final boolean isShipDelete = shipService.deleteShip(id);

        return !isShipDelete ?
                new ResponseEntity<>(HttpStatus.NOT_FOUND) :
                new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/ships")
    public ResponseEntity<?> getAllShip(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "planet", required = false) String planet,
            @RequestParam(value = "shipType", required = false) ShipType shipType,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "isUsed", required = false) Boolean isUsed,
            @RequestParam(value = "minSpeed", required = false) Double minSpeed,
            @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
            @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
            @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
            @RequestParam(value = "minRating", required = false) Double minRating,
            @RequestParam(value = "maxRating", required = false) Double maxRating,
            @RequestParam(value = "order", required = false, defaultValue = "ID" ) ShipOrder order,
            @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(value = "pageSize", required = false, defaultValue = "3") Integer pageSize) {

        final Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(order.getFieldName()));
        final List<Ship> allShips = shipService.getAllShips(name, planet, shipType, after, before, isUsed, minSpeed,
                maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating, pageable).getContent();

        return new ResponseEntity<>(allShips, HttpStatus.OK);
    }

    @GetMapping(value = "/ships/count")
    public ResponseEntity<?> getShipsCount(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "planet", required = false) String planet,
            @RequestParam(value = "shipType", required = false) ShipType shipType,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "isUsed", required = false) Boolean isUsed,
            @RequestParam(value = "minSpeed", required = false) Double minSpeed,
            @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
            @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
            @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
            @RequestParam(value = "minRating", required = false) Double minRating,
            @RequestParam(value = "maxRating", required = false) Double maxRating) {

        int shipsCount = (int) shipService.getShipsCount(name, planet, shipType, after, before, isUsed, minSpeed,
                maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating);

        return new ResponseEntity<>(shipsCount, HttpStatus.OK);
    }
}
