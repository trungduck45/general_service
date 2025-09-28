package scms_be.general_service.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import scms_be.general_service.exception.RpcException;
import scms_be.general_service.model.dto.ManufactureLineDto;
import scms_be.general_service.model.entity.ManufactureLine;
import scms_be.general_service.model.request.ManufactureLineRequest;

@Service
public class ManufactureLineService {

    // Simulate database với in-memory storage  
    private final Map<Long, ManufactureLine> lineStorage = new ConcurrentHashMap<>();
    private final AtomicLong lineIdGenerator = new AtomicLong(1);

    public ManufactureLineDto createLine(ManufactureLineRequest request) {
        Long plantId = request.getPlantId();
        ManufactureLineRequest.LineData lineData = request.getLine();

        String lineCode = lineData.getLineCode();
        if (lineCode != null && isLineCodeExists(lineCode)) {
            throw new RpcException(400, "Mã dây chuyền đã được sử dụng!");
        }

        ManufactureLine line = new ManufactureLine();
        line.setLineId(lineIdGenerator.getAndIncrement());
        // Trong microservice, chỉ lưu plantId thay vì entity relationship
        // line.setPlantId(plantId); // Cần thêm field này vào entity
        line.setLineCode(lineCode != null ? lineCode : generateLineCode(plantId));
        line.setLineName(lineData.getLineName());
        
        Double capacity = lineData.getCapacity();
        if (capacity != null) {
            line.setCapacity(capacity);
        }
        
        line.setDescription(lineData.getDescription());
        
        lineStorage.put(line.getLineId(), line);
        return convertToDto(line, plantId);
    }

    public List<ManufactureLineDto> getAllLinesInPlant(ManufactureLineRequest request) {
        Long plantId = request.getPlantId();
        
        // Trong thực tế sẽ filter theo plantId từ entity
        List<ManufactureLine> lines = lineStorage.values().stream()
            .collect(Collectors.toList());
            
        return lines.stream()
            .map(line -> convertToDto(line, plantId))
            .collect(Collectors.toList());
    }

    public ManufactureLineDto getLineById(ManufactureLineRequest request) {
        Long lineId = request.getLineId();
        
        ManufactureLine line = lineStorage.get(lineId);
        if (line == null) {
            throw new RpcException(404, "Không tìm thấy dây chuyền sản xuất!");
        }
        
        // Trong thực tế cần lấy plantId từ line entity
        Long plantId = request.getPlantId() != null ? request.getPlantId() : 1L; // Use from request or mock
        return convertToDto(line, plantId);
    }

    public ManufactureLineDto updateLine(ManufactureLineRequest request) {
        Long lineId = request.getLineId();
        ManufactureLineRequest.LineData lineData = request.getLine();
        
        ManufactureLine existingLine = lineStorage.get(lineId);
        if (existingLine == null) {
            throw new RpcException(404, "Không tìm thấy dây chuyền sản xuất!");
        }

        String newLineCode = lineData.getLineCode();
        if (newLineCode != null && !existingLine.getLineCode().equals(newLineCode)) {
            if (isLineCodeExists(newLineCode)) {
                throw new RpcException(400, "Mã dây chuyền đã được sử dụng!");
            }
            existingLine.setLineCode(newLineCode);
        }

        if (lineData.getLineName() != null) {
            existingLine.setLineName(lineData.getLineName());
        }
        if (lineData.getCapacity() != null) {
            existingLine.setCapacity(lineData.getCapacity());
        }
        if (lineData.getDescription() != null) {
            existingLine.setDescription(lineData.getDescription());
        }

        lineStorage.put(lineId, existingLine);
        Long plantId = request.getPlantId() != null ? request.getPlantId() : 1L; // Use from request or mock
        return convertToDto(existingLine, plantId);
    }

    public Map<String, Object> deleteLine(ManufactureLineRequest request) {
        Long lineId = request.getLineId();
        
        ManufactureLine line = lineStorage.remove(lineId);
        boolean success = line != null;
        
        return Map.of(
            "success", success,
            "message", success ? "Xóa dây chuyền thành công!" : "Không tìm thấy dây chuyền!"
        );
    }

    private String generateLineCode(Long plantId) {
        String prefix = "ML" + String.format("%04d", plantId);
        long count = lineStorage.values().stream()
            .filter(line -> line.getLineCode().startsWith(prefix))
            .count();
        return prefix + String.format("%03d", count + 1);
    }

    private boolean isLineCodeExists(String lineCode) {
        return lineStorage.values().stream()
            .anyMatch(line -> line.getLineCode().equals(lineCode));
    }

    private ManufactureLineDto convertToDto(ManufactureLine line, Long plantId) {
        ManufactureLineDto dto = new ManufactureLineDto();
        dto.setLineId(line.getLineId());
        dto.setLineCode(line.getLineCode());
        dto.setLineName(line.getLineName());
        dto.setCapacity(line.getCapacity());
        dto.setDescription(line.getDescription());
        
        // Mock data cho plant info
        dto.setPlantId(plantId);
        dto.setPlantName("Plant " + plantId);
        dto.setCompanyId(1L);
        
        return dto;
    }
}