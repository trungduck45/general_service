package scms_be.general_service.event.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import scms_be.general_service.exception.RpcException;
import scms_be.general_service.model.ErrorResponse;
import scms_be.general_service.model.event.ManufactureLineEvent;
import scms_be.general_service.service.ManufactureLineService;

@Service
public class ManufactureLineListener {

    @Autowired
    private ManufactureLineService lineService;

    @RabbitListener(queues = "general_queue")
    public Object handleManufactureLineEvents(ManufactureLineEvent event) { // Spring tự convert JSON -> DTO
        try {
            switch (event.getPattern()) {
                case "manufacture_line.create":
                    return lineService.createLine(event.getData());
                case "manufacture_line.get_all_in_plant":
                    return lineService.getAllLinesInPlant(event.getData());
                case "manufacture_line.get_by_id":
                    return lineService.getLineById(event.getData());
                case "manufacture_line.update":
                    return lineService.updateLine(event.getData());
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