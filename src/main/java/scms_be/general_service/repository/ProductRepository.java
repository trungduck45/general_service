package scms_be.general_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import scms_be.general_service.model.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
  
  List<Product> findByItemId(Long itemId);
  
  boolean existsBySerialNumber(String serialNumber);
  
  boolean existsByQrCode(String qrCode);
  
}
