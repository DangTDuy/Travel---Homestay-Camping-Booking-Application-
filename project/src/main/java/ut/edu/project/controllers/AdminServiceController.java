package ut.edu.project.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ut.edu.project.dtos.AdditionalDTO;
import ut.edu.project.models.Additional;
import ut.edu.project.repositories.AdditionalServiceRepository;
import ut.edu.project.repositories.CategoryRepository;
import ut.edu.project.repositories.TimeSlotRepository;
import ut.edu.project.services.AdditionalService;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/services")
@RequiredArgsConstructor
@Slf4j
public class AdminServiceController {

    private final AdditionalService additionalService;
    private final CategoryRepository categoryRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final AdditionalServiceRepository additionalServiceRepository;

    @GetMapping
    public String servicesPage(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long timeSlotId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String serviceType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Model model) {

        log.info("Fetching services with filters - categoryId: {}, timeSlotId: {}, search: {}, serviceType: {}",
                categoryId, timeSlotId, search, serviceType);

        // Lấy danh sách dịch vụ bổ sung với bộ lọc
        List<AdditionalDTO> services;

        // Xử lý lọc theo loại dịch vụ (homestay, camping, travel)
        Long homestayId = null;
        Long campingId = null;
        Long travelId = null;

        if ("homestay".equalsIgnoreCase(serviceType)) {
            // Lọc theo homestay (homestayId = -1 để chỉ lấy dịch vụ của homestay)
            homestayId = -1L;
        } else if ("camping".equalsIgnoreCase(serviceType)) {
            // Lọc theo camping (campingId = -1 để chỉ lấy dịch vụ của camping)
            campingId = -1L;
        } else if ("travel".equalsIgnoreCase(serviceType)) {
            // Lọc theo travel (travelId = -1 để chỉ lấy dịch vụ của travel)
            travelId = -1L;
        } else if ("all".equalsIgnoreCase(serviceType)) {
            // Lọc theo dịch vụ áp dụng cho tất cả (serviceType = ALL)
            // Không cần set homestayId, campingId, travelId
        }

        if (categoryId != null || timeSlotId != null || search != null || serviceType != null) {
            // Nếu có bộ lọc, sử dụng phương thức tìm kiếm
            Pageable pageable = PageRequest.of(page, size);
            Page<Additional> filteredServices;

            if ("homestay".equalsIgnoreCase(serviceType)) {
                // Lọc theo homestay
                filteredServices = additionalServiceRepository.findByFilters(
                        categoryId, timeSlotId, homestayId, null, null, search, pageable);
            } else if ("camping".equalsIgnoreCase(serviceType)) {
                // Lọc theo camping
                filteredServices = additionalServiceRepository.findByFilters(
                        categoryId, timeSlotId, null, campingId, null, search, pageable);
            } else if ("travel".equalsIgnoreCase(serviceType)) {
                // Lọc theo travel
                filteredServices = additionalServiceRepository.findByFilters(
                        categoryId, timeSlotId, null, null, travelId, search, pageable);
            } else if ("all".equalsIgnoreCase(serviceType)) {
                // Lọc theo dịch vụ áp dụng cho tất cả (serviceType = ALL)
                // Tìm kiếm dịch vụ có serviceType = ALL
                filteredServices = additionalServiceRepository.findByFilters(
                        categoryId, timeSlotId, null, null, null, search, pageable);
                // TODO: Cần bổ sung logic để lọc theo serviceType = ALL
            } else {
                // Lọc theo các tiêu chí khác
                filteredServices = additionalServiceRepository.findByFilters(
                        categoryId, timeSlotId, null, null, null, search, pageable);
            }

            services = filteredServices.getContent().stream()
                    .map(additionalService::convertToDTO)
                    .collect(Collectors.toList());

            log.info("Found {} services matching filters", services.size());
        } else {
            // Nếu không có bộ lọc, lấy tất cả dịch vụ (cả hoạt động và không hoạt động)
            services = additionalService.getAllAdditionals();
            log.info("Fetched all {} services", services.size());
        }

        model.addAttribute("services", services);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("timeSlots", timeSlotRepository.findAll());
        model.addAttribute("currentPage", "admin/services");

        // Add filter parameters for pagination links and pre-select filters
        if (categoryId != null) model.addAttribute("categoryId", categoryId);
        if (timeSlotId != null) model.addAttribute("timeSlotId", timeSlotId);
        if (search != null) model.addAttribute("search", search);
        if (serviceType != null) model.addAttribute("serviceType", serviceType);

        return "admin/services";
    }
}
