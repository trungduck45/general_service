package scms_be.general_service.event.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import scms_be.general_service.exception.RpcException;
import scms_be.general_service.model.ErrorResponse;
import scms_be.general_service.model.event.ProductEvent;
import scms_be.general_service.service.ProductService;

@Service
public class ProductListener {

    @Autowired
    private ProductService productService;

    @RabbitListener(queues = "general_queue")
    public Object handleProductEvents(ProductEvent event) { 
        try {
            switch (event.getPattern()) {
                case "product.create":
                    return productService.createProduct(event.getData());
                case "product.get_all_by_item":
                    return productService.getAllProductsByItem(event.getData());
                case "product.get_by_id":
                    return productService.getProductById(event.getData());
                case "product.update":
                    return productService.updateProduct(event.getData());
                case "product.delete":
                    return productService.deleteProduct(event.getData());
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