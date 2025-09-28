package scms_be.general_service.event.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import scms_be.general_service.exception.RpcException;
import scms_be.general_service.model.ErrorResponse;
import scms_be.general_service.model.event.ItemEvent;
import scms_be.general_service.service.ItemService;

@Service
public class ItemListener {

    @Autowired
    private ItemService itemService;

    @RabbitListener(queues = "general_queue")
    public Object handleItemEvents(ItemEvent event) { // Spring tự convert JSON -> DTO
        try {
            switch (event.getPattern()) {
                case "item.create":
                    return itemService.createItem(event.getData());
                case "item.get_all_in_company":
                    return itemService.getAllItemsInCompany(event.getData());
                case "item.get_by_id":
                    return itemService.getItemById(event.getData());
                case "item.update":
                    return itemService.updateItem(event.getData());
                case "item.delete":
                    return itemService.deleteItem(event.getData());
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