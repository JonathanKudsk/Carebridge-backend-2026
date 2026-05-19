package populator;

import com.carebridge.dao.impl.CityDAO;
import com.carebridge.dao.impl.LocationDAO;
import com.carebridge.entities.City;
import com.carebridge.entities.Location;

import java.util.ArrayList;
import java.util.List;

public class LocationPopulator {

    // store created test data
    private static final List<Location> data = new ArrayList<>();

    public static void populate() {

        LocationDAO locationDAO = LocationDAO.getInstance();
        CityDAO cityDAO = CityDAO.getInstance();

        data.clear(); // avoid duplicates between tests

        City city1 = City.builder()
                .cityname("By1")
                .postalcode(1111)
                .build();
        City city2 = City.builder()
                .cityname("By2")
                .postalcode(2222)
                .build();

        Location location1 = Location.builder()
                .locationName("location1")
                .address("somewhere 1")
                .build();

        Location location2 = Location.builder()
                .locationName("location2")
                .address("nowhere 2")
                .build();

        Location location3 = Location.builder()
                .locationName("location3")
                .address("here 3")
                .build();

        cityDAO.create(city1);
        cityDAO.create(city2);

        location1.setCity(city1);
        location2.setCity(city2);
        location3.setCity(city1);

        locationDAO.create(location1);
        locationDAO.create(location2);
        locationDAO.create(location3);

        // store references AFTER persist (IDs assigned)
        data.add(location1);
        data.add(location2);
        data.add(location3);
    }

    public static List<Location> fetch() {
        return data;
    }
}