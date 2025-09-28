package scms_be.general_service.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import scms_be.general_service.exception.RpcException;
import scms_be.general_service.model.dto.ManufacturePlantDto;
import scms_be.general_service.model.entity.ManufacturePlant;
import scms_be.general_service.model.request.ManufacturePlantRequest;

@Service
public class ManufacturePlantService {

    // Simulate database với in-memory storage
    private final Map<Long, ManufacturePlant> plantStorage = new ConcurrentHashMap<>();
    private final AtomicLong plantIdGenerator = new AtomicLong(1);

    public ManufacturePlantDto createPlant(ManufacturePlantRequest request) {
        Long companyId = request.getCompanyId();
        ManufacturePlantRequest.PlantData plantData = request.getPlant();

        String plantCode = plantData.getPlantCode();
        if (plantCode != null && isPlantCodeExists(plantCode)) {
            throw new RpcException(400, "Mã xưởng đã được sử dụng!");
        }

        ManufacturePlant plant = new ManufacturePlant();
        plant.setPlantId(plantIdGenerator.getAndIncrement());
        plant.setCompanyId(companyId);
        plant.setPlantCode(plantCode != null ? plantCode : generatePlantCode(companyId));
        plant.setPlantName(plantData.getPlantName());
        plant.setDescription(plantData.getDescription());
        
        plantStorage.put(plant.getPlantId(), plant);
        return convertToDto(plant);
    }

    public List<ManufacturePlantDto> getAllPlantsInCompany(ManufacturePlantRequest request) {
        Long companyId = request.getCompanyId();
        
        List<ManufacturePlant> plants = plantStorage.values().stream()
            .filter(plant -> plant.getCompanyId().equals(companyId))
            .collect(Collectors.toList());
            
        return plants.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public ManufacturePlantDto getPlantById(ManufacturePlantRequest request) {
        Long plantId = request.getPlantId();
        
        ManufacturePlant plant = plantStorage.get(plantId);
        if (plant == null) {
            throw new RpcException(404, "Không tìm thấy xưởng sản xuất!");
        }
        return convertToDto(plant);
    }

    public ManufacturePlantDto updatePlant(ManufacturePlantRequest request) {
        Long plantId = request.getPlantId();
        ManufacturePlantRequest.PlantData plantData = request.getPlant();
        
        ManufacturePlant existingPlant = plantStorage.get(plantId);
        if (existingPlant == null) {
            throw new RpcException(404, "Không tìm thấy xưởng sản xuất!");
        }

        String newPlantCode = plantData.getPlantCode();
        if (newPlantCode != null && !existingPlant.getPlantCode().equals(newPlantCode)) {
            if (isPlantCodeExists(newPlantCode)) {
                throw new RpcException(400, "Mã xưởng đã được sử dụng!");
            }
            existingPlant.setPlantCode(newPlantCode);
        }

        if (plantData.getPlantName() != null) {
            existingPlant.setPlantName(plantData.getPlantName());
        }
        if (plantData.getDescription() != null) {
            existingPlant.setDescription(plantData.getDescription());
        }

        plantStorage.put(plantId, existingPlant);
        return convertToDto(existingPlant);
    }

    public Map<String, Object> deletePlant(ManufacturePlantRequest request) {
        Long plantId = request.getPlantId();
        
        ManufacturePlant plant = plantStorage.remove(plantId);
        boolean success = plant != null;
        
        return Map.of(
            "success", success,
            "message", success ? "Xóa xưởng sản xuất thành công!" : "Không tìm thấy xưởng sản xuất!"
        );
    }

    private String generatePlantCode(Long companyId) {
        String prefix = "MP" + String.format("%04d", companyId);
        long count = plantStorage.values().stream()
            .filter(plant -> plant.getPlantCode().startsWith(prefix))
            .count();
        return prefix + String.format("%03d", count + 1);
    }

    private boolean isPlantCodeExists(String plantCode) {
        return plantStorage.values().stream()
            .anyMatch(plant -> plant.getPlantCode().equals(plantCode));
    }

    private ManufacturePlantDto convertToDto(ManufacturePlant plant) {
        ManufacturePlantDto dto = new ManufacturePlantDto();
        dto.setPlantId(plant.getPlantId());
        dto.setPlantCode(plant.getPlantCode());
        dto.setPlantName(plant.getPlantName());
        dto.setDescription(plant.getDescription());
        dto.setCompanyId(plant.getCompanyId());
        return dto;
    }
}
