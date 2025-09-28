package scms_be.general_service.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import scms_be.general_service.exception.RpcException;
import scms_be.general_service.model.dto.ProductDto;
import scms_be.general_service.model.entity.Product;
import scms_be.general_service.model.request.ProductRequest;

@Service
public class ProductService {

    // Simulate database với in-memory storage
    private final Map<Long, Product> productStorage = new ConcurrentHashMap<>();
    private final AtomicLong productIdGenerator = new AtomicLong(1);

    public ProductDto createProduct(ProductRequest request) {
        Long itemId = request.getItemId();
        ProductRequest.ProductData productData = request.getProduct();

        Product product = new Product();
        product.setProductId(productIdGenerator.getAndIncrement());
        // Trong microservice chỉ lưu itemId thay vì entity relationship
        // product.setItemId(itemId); // Cần thêm field này vào entity
        product.setSerialNumber(UUID.randomUUID().toString().substring(0, 8));
        
        Long batchNo = productData.getBatchNo();
        if (batchNo != null) {
            product.setBatchNo(batchNo);
        }
        
        product.setQrCode(productData.getQrCode());
        product.setCurrentCompanyId(1L); // Mock company ID
        
        productStorage.put(product.getProductId(), product);
        return convertToDto(product, itemId);
    }

    public List<ProductDto> getAllProductsByItem(ProductRequest request) {
        Long itemId = request.getItemId();
        
        // Trong thực tế sẽ filter theo itemId từ entity
        List<Product> products = productStorage.values().stream()
            .collect(Collectors.toList());
            
        return products.stream()
            .map(product -> convertToDto(product, itemId))
            .collect(Collectors.toList());
    }

    public ProductDto getProductById(ProductRequest request) {
        Long productId = request.getProductId();
        
        Product product = productStorage.get(productId);
        if (product == null) {
            throw new RpcException(404, "Không tìm thấy sản phẩm!");
        }
        
        // Mock itemId or use from request
        Long itemId = request.getItemId() != null ? request.getItemId() : 1L;
        return convertToDto(product, itemId);
    }

    public ProductDto updateProduct(ProductRequest request) {
        Long productId = request.getProductId();
        ProductRequest.ProductData productData = request.getProduct();
        
        Product existingProduct = productStorage.get(productId);
        if (existingProduct == null) {
            throw new RpcException(404, "Không tìm thấy sản phẩm!");
        }

        if (productData.getBatchNo() != null) {
            existingProduct.setBatchNo(productData.getBatchNo());
        }
        if (productData.getQrCode() != null) {
            existingProduct.setQrCode(productData.getQrCode());
        }

        productStorage.put(productId, existingProduct);
        Long itemId = request.getItemId() != null ? request.getItemId() : 1L; // Use from request or mock
        return convertToDto(existingProduct, itemId);
    }

    public Map<String, Object> deleteProduct(ProductRequest request) {
        Long productId = request.getProductId();
        
        Product product = productStorage.remove(productId);
        boolean success = product != null;
        
        return Map.of(
            "success", success,
            "message", success ? "Xóa sản phẩm thành công!" : "Không tìm thấy sản phẩm!"
        );
    }

    private ProductDto convertToDto(Product product, Long itemId) {
        ProductDto dto = new ProductDto();
        dto.setProductId(product.getProductId());
        dto.setSerialNumber(product.getSerialNumber());
        dto.setBatchNo(product.getBatchNo());
        dto.setQrCode(product.getQrCode());
        dto.setCurrentCompanyId(product.getCurrentCompanyId());
        
        // Mock item data
        dto.setItemId(itemId);
        dto.setItemName("Item " + itemId);
        dto.setTechnicalSpecifications("Mock specifications");
        
        return dto;
    }
}