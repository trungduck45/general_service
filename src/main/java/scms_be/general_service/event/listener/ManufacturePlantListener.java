package scms_be.general_service.event.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import scms_be.general_service.exception.RpcException;
import scms_be.general_service.model.ErrorResponse;
import scms_be.general_service.model.event.ManufacturePlantEvent;
import scms_be.general_service.service.ManufacturePlantService;

@Service
public class ManufacturePlantListener {

    @Autowired
    private ManufacturePlantService manufacturePlantService;

    @RabbitListener(queues = "general_queue")
    public Object handleManufacturePlantEvents(ManufacturePlantEvent event) { // Spring tự convert JSON -> DTO
        try {
            switch (event.getPattern()) {
                case "manufacture_plant.create":
                    return manufacturePlantService.createPlant(event.getData());
                case "manufacture_plant.get_all_in_company":
                    return manufacturePlantService.getAllPlantsInCompany(event.getData());
                case "manufacture_plant.get_by_id":
                    return manufacturePlantService.getPlantById(event.getData());
                case "manufacture_plant.update":
                    return manufacturePlantService.updatePlant(event.getData());
                default:
                    throw new RpcException(400, "Unknown event: " + event.getPattern());
            }
        } catch (RpcException ex) {
            return new ErrorResponse(ex.getStatusCode(), ex.getMessage());
        } catch (Exception ex) {
            return new ErrorResponse(500, "Internal error: " + ex.getMessage());
        }
    }
}