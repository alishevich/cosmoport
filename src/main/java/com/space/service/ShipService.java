package com.space.service;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;

@Service
public class ShipService {

    private ShipRepository shipRepository;

    @Autowired
    public void setShipRepository(ShipRepository shipRepository) {
        this.shipRepository = shipRepository;
    }

    public Ship createShip(Ship ship) {
      return  shipRepository.save(ship);
    }

    public Ship getShip(long id) {
            return shipRepository.findById(id).orElse(null);
    }

    public boolean deleteShip(long id) {
        Ship ship = shipRepository.findById(id).orElse(null);

        if (ship == null) {
            return false;
        } else {
            shipRepository.delete(ship);
            return true;
        }
    }

    public Page<Ship> getAllShips(String name,
                                  String planet,
                                  ShipType shipType,
                                  Long after,
                                  Long before,
                                  Boolean isUsed,
                                  Double minSpeed,
                                  Double maxSpeed,
                                  Integer minCrewSize,
                                  Integer maxCrewSize,
                                  Double minRating,
                                  Double maxRating,
                                  Pageable pageable) {
        return shipRepository.findAll(Specification.where(criteriaByName(name)
                .and(criteriaByPlanet(planet))
                .and(criteriaByShipType(shipType))
                .and(criteriaByDate(after, before))
                .and(criteriaByUsed(isUsed))
                .and(criteriaBySpeed(minSpeed, maxSpeed))
                .and(criteriaByCrewSize(minCrewSize, maxCrewSize))
                .and(criteriaByRating(minRating, maxRating))), pageable);
    }

    public long getShipsCount(String name,
                                 String planet,
                                 ShipType shipType,
                                 Long after,
                                 Long before,
                                 Boolean isUsed,
                                 Double minSpeed,
                                 Double maxSpeed,
                                 Integer minCrewSize,
                                 Integer maxCrewSize,
                                 Double minRating,
                                 Double maxRating) {
        return shipRepository.count(Specification.where(criteriaByName(name)
                .and(criteriaByPlanet(planet))
                .and(criteriaByShipType(shipType))
                .and(criteriaByDate(after, before))
                .and(criteriaByUsed(isUsed))
                .and(criteriaBySpeed(minSpeed, maxSpeed))
                .and(criteriaByCrewSize(minCrewSize, maxCrewSize))
                .and(criteriaByRating(minRating, maxRating))));
    }

    public Ship updateShip(Ship newShip, long id) throws IllegalAccessException {
        Ship oldShip = shipRepository.findById(id).orElse(null);

        if (oldShip == null) {
            return null;
        }

        String name = newShip.getName();
        if (name != null) {
            if (isStringValid(name)) {
                oldShip.setName(name);
            } else {
                throw new IllegalAccessException();
            }
        }

        String planet = newShip.getPlanet();
        if (planet != null) {
            if (isStringValid(planet)) {
                oldShip.setPlanet(newShip.getPlanet());
            } else {
                throw new IllegalAccessException();
            }
        }

        ShipType shipType = newShip.getShipType();
        if (shipType != null) {
            oldShip.setShipType(shipType);
        }

        Date prodDate = newShip.getProdDate();
        if (prodDate != null) {
            if (isDateValid(prodDate)) {
                oldShip.setProdDate(prodDate);
            } else {
                throw new IllegalAccessException();
            }
        }

        Boolean isUsed = newShip.getUsed();
        if (isUsed != null) {
            oldShip.setUsed(isUsed);
        }

        Double speed = newShip.getSpeed();
        if (speed != null) {
            if (isSpeedValid(speed)) {
                oldShip.setSpeed(speed);
            } else {
                throw new IllegalAccessException();
            }
        }

        Integer crewSize = newShip.getCrewSize();
        if (crewSize != null) {
            if (isCrewSizeValid(crewSize)) {
                oldShip.setCrewSize(crewSize);
            } else {
                throw new IllegalAccessException();
            }
        }

        oldShip.setRating(ratingShip(oldShip));
        shipRepository.save(oldShip);

        return oldShip;
    }

    public boolean isIdValid(Long id) {
        return id != null && id % 1 == 0 && id > 0;
    }

    public boolean isShipValid(Ship ship) {
        return (ship != null  && ship.getShipType() != null &&
                isStringValid(ship.getName()) &&
                isStringValid(ship.getPlanet()) &&
                isSpeedValid(ship.getSpeed()) &&
                isCrewSizeValid(ship.getCrewSize()) &&
                isDateValid(ship.getProdDate()));
    }

    public boolean isStringValid(String string) {
        return  string != null && !string.isEmpty() && string.length() <= 50;
    }

    public boolean isSpeedValid(Double speed) {
        return speed != null && speed >= 0.01 && speed <= 0.99;
    }

    public boolean isCrewSizeValid(Integer size) {
        return size != null && size >= 1 && size <= 9999;
    }

    public boolean isDateValid(Date date) {
       final Date MIN_YEAR = getDateFromYear(2800);
       final Date MAX_YEAR = getDateFromYear(3019);

       return date != null && date.after(MIN_YEAR) && date.before(MAX_YEAR);
    }

    public Date getDateFromYear(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        return calendar.getTime();
    }

    public double ratingShip(Ship ship){
        final int CURRENT_YEAR = 3019;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ship.getProdDate());

       return BigDecimal.valueOf(80 * ship.getSpeed() * (ship.getUsed() ? 0.5 : 1) / (CURRENT_YEAR - calendar.get(Calendar.YEAR) + 1))
                .setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public Long convertIdToLong(String path) {
        if (path == null) {
            return null;
        } else {
            try {
                return Long.parseLong(path);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    public Specification<Ship> criteriaByName(String name) {
        return (root, query, criteriaBuilder) -> name == null ? null : criteriaBuilder.like(root.get("name"), "%" + name + "%");
    }

    public Specification<Ship> criteriaByPlanet(String planet) {
        return (root, query, criteriaBuilder) -> planet == null ? null : criteriaBuilder.like(root.get("planet"), "%" + planet + "%");
    }

    public Specification<Ship> criteriaByShipType(ShipType shipType) {
        return (root, query, criteriaBuilder) -> shipType == null ? null : criteriaBuilder.equal(root.get("shipType"), shipType);
    }

    public Specification<Ship> criteriaByDate(Long after, Long before) {
        return (root, query, criteriaBuilder) -> {
            if (after == null && before == null) {
                return null;
            }
            if (after == null && before != null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("prodDate"),new Date(before));
            }
            if (after!= null && before == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("prodDate"), new Date(after));
            }
            return criteriaBuilder.between(root.get("prodDate"), new Date(after), new Date(before));
        };
    }

    public Specification<Ship> criteriaByUsed(Boolean isUsed) {
        return (root, query, criteriaBuilder) -> isUsed == null ? null : criteriaBuilder.equal(root.get("isUsed"), isUsed);
    }

    public Specification<Ship> criteriaBySpeed(Double minSpeed, Double maxSpeed) {
        return (root, query, criteriaBuilder) -> {
            if (minSpeed == null && maxSpeed == null) {
                return null;
            }
            if (minSpeed == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("speed"), maxSpeed);
            }
            if (maxSpeed == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("speed"), minSpeed);
            }
            return criteriaBuilder.between(root.get("speed"), minSpeed, maxSpeed);
        };
    }

    public Specification<Ship> criteriaByCrewSize(Integer minCrewSize, Integer maxCrewSize) {
        return (root, query, criteriaBuilder) -> {
            if (minCrewSize == null && maxCrewSize == null) {
                return null;
            }
            if (minCrewSize == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("crewSize"), maxCrewSize);
            }
            if (maxCrewSize == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("crewSize"), minCrewSize);
            }
            return criteriaBuilder.between(root.get("crewSize"), minCrewSize, maxCrewSize);
        };
    }

    public Specification<Ship> criteriaByRating(Double minRating, Double maxRating) {
        return (root, query, criteriaBuilder) -> {
            if (minRating == null && maxRating == null) {
                return null;
            }
            if (minRating == null && maxRating != null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("rating"), maxRating);
            }
            if (minRating != null && maxRating == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("rating"), minRating);
            }
            return criteriaBuilder.between(root.get("rating"), minRating, maxRating);
        };
    }
}





