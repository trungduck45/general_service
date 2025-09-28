package scms_be.general_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import scms_be.general_service.model.entity.ManufactureLine;

@Repository
public interface ManufactureLineRepository extends JpaRepository<ManufactureLine, Long> {
  
  List<ManufactureLine> findByPlantId(Long plantId);

  boolean existsByLineCode(String lineCode);

  @Query("SELECT COUNT(l) FROM ManufactureLine l WHERE l.lineCode LIKE :prefix%")
  int countByLineCodeStartingWith(@Param("prefix") String prefix);
  
}
