package scms_be.general_service.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "manufacture_line")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManufactureLine {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long lineId;

  // For microservice - store plantId directly
  @Column(name = "plant_id", nullable = false)
  private Long plantId;

  @Column(unique = true, nullable = false)
  private String lineCode;

  @Column(nullable = false)
  private String lineName;

  @Column(nullable = false)
  private double capacity;

  private String description;
}
