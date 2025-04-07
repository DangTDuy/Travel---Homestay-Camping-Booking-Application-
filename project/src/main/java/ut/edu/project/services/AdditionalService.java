package ut.edu.project.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import ut.edu.project.models.Additional;
import ut.edu.project.repositories.AdditionalRepository;
import ut.edu.project.dtos.AdditionalDTO;
import java.util.List;
import java.util.stream.Collectors;
import ut.edu.project.models.Homestay;

@Service
public class AdditionalService {
    @Autowired
    private AdditionalRepository additionalRepository;

    public List<AdditionalDTO> getAllAdditionals() {
        return additionalRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public AdditionalDTO getAdditionalById(Long id) {
        return additionalRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Additional not found"));
    }

    public AdditionalDTO createAdditional(AdditionalDTO dto) {
        Additional additional = convertToEntity(dto);
        return convertToDTO(additionalRepository.save(additional));
    }

    public AdditionalDTO updateAdditional(Long id, AdditionalDTO dto) {
        Additional existing = additionalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Additional not found"));
        updateEntityFromDTO(existing, dto);
        return convertToDTO(additionalRepository.save(existing));
    }

    public void deleteAdditional(Long id) {
        additionalRepository.deleteById(id);
    }

    public List<Additional> getByHomestay(Homestay homestay) {
        return additionalRepository.findByHomestay(homestay);
    }

    public List<Additional> getByIds(List<Long> ids) {
        return additionalRepository.findAllById(ids);
    }

    private AdditionalDTO convertToDTO(Additional entity) {
        AdditionalDTO dto = new AdditionalDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setPrice(entity.getPrice());
        dto.setTimeSlot(entity.getTimeSlot());
        dto.setCategory(entity.getCategory());
        dto.setHomestayId(entity.getHomestay() != null ? entity.getHomestay().getId() : null);
        dto.setCampingId(entity.getCamping() != null ? entity.getCamping().getId() : null);
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setActive(entity.isActive());
        dto.setImageUrl(entity.getImageUrl());
        return dto;
    }

    private Additional convertToEntity(AdditionalDTO dto) {
        Additional entity = new Additional();
        updateEntityFromDTO(entity, dto);
        return entity;
    }

    private void updateEntityFromDTO(Additional entity, AdditionalDTO dto) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setTimeSlot(dto.getTimeSlot());
        entity.setCategory(dto.getCategory());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setActive(dto.isActive());
        entity.setImageUrl(dto.getImageUrl());
    }
} 