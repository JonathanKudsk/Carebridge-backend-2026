package com.carebridge.controllers.impl;

import com.carebridge.controllers.IController;
import com.carebridge.dao.impl.CityDAO;
import com.carebridge.dao.impl.LocationDAO;
import com.carebridge.dtos.*;
import com.carebridge.entities.City;
import com.carebridge.entities.Location;
import com.carebridge.exceptions.ApiRuntimeException;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LocationController implements IController<Location, Long> {

    private static final Logger logger = LoggerFactory.getLogger(LocationController.class);
    private final LocationDAO locationDAO = LocationDAO.getInstance();
    private final CityDAO cityDAO = CityDAO.getInstance();

    @Override
    public void read(Context ctx) {
        try {
            Long id = parseId(ctx);
            Location entity = locationDAO.read(id);
            if (entity == null) {
                ctx.status(404).json("{\"msg\":\"Location not found\"}");
                return;
            }
            ctx.status(200);
            ctx.json(new LocationResponseDTO(entity));
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("read Location failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void readAll(Context ctx) {
        try {
            List<Location> entities = locationDAO.readAll();
            if (entities == null || entities.isEmpty()) {
                ctx.status(404).json("{\"msg\":\"no Locations not found\"}");
                return;
            }
            LocationResponseDTO[] responseDTO = entities.stream().map(LocationResponseDTO::new).toArray(LocationResponseDTO[]::new);
            ctx.status(200);
            ctx.json(responseDTO);
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("read Location failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void create(Context ctx) {
        try {
            CreateLocationRequestDTO dto = ctx.bodyAsClass(CreateLocationRequestDTO.class);

            City city = cityDAO.read(dto.getCity().getPostalCode(),dto.getCity().getCityName());
            if (city == null){
                city = cityDAO.create(new City(dto.getCity().getCityName(), dto.getCity().getPostalCode()));
            }
            Location location = new Location(dto.getLocationName(),dto.getAddress());
            location.setCity(city);
            location = locationDAO.create(location);

            ctx.status(201).json(new LocationResponseDTO(location));
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("create template failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void update(Context ctx) {
        try {
            CreateLocationRequestDTO dto = ctx.bodyAsClass(CreateLocationRequestDTO.class);
            Long id = parseId(ctx);

            City city = cityDAO.read(dto.getCity().getPostalCode(),dto.getCity().getCityName());
            if (city == null){
                city = cityDAO.create(new City(dto.getCity().getCityName(), dto.getCity().getPostalCode()));
            }
            Location location = new Location(dto.getLocationName(),dto.getAddress());
            location.setCity(city);
            location = locationDAO.update(id, location);

            ctx.status(201).json(new LocationResponseDTO(location));
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("Update Location failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void delete(Context ctx) {
        try {
            Long id = parseId(ctx);
            locationDAO.delete(id);
            ctx.status(200);
            ctx.json("Location successfully deleted");
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("Delete Location failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public boolean validatePrimaryKey(Long id) {
        return id != null && id > 0;
    }

    @Override
    public Location validateEntity(Context ctx) {
        return null;
    }

    private Long parseId(Context ctx) {
        try {
            return Long.parseLong(ctx.pathParam("id"));
        } catch (Exception ex) {
            throw new ApiRuntimeException(400, "Invalid id");
        }
    }
}