package ut.edu.project.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import ut.edu.project.services.RevenueService;
import ut.edu.project.dtos.RevenueDTO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/revenue")
public class RevenueController {
    @Autowired
    private RevenueService revenueService;

    @GetMapping
    public String showRevenueReport(Model model) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        
        BigDecimal totalRevenue = revenueService.calculateTotalRevenue(startOfMonth, now);
        BigDecimal totalAdminFee = revenueService.calculateTotalAdminFee(startOfMonth, now);
        BigDecimal totalOwnerAmount = revenueService.calculateTotalOwnerAmount(startOfMonth, now);
        
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalAdminFee", totalAdminFee);
        model.addAttribute("totalOwnerAmount", totalOwnerAmount);
        
        List<RevenueDTO> revenues = revenueService.getRevenuesByDateRange(startOfMonth, now);
        model.addAttribute("revenues", revenues);
        
        return "revenue/report";
    }

    @GetMapping("/filter")
    public String filterRevenue(
            @RequestParam String startDate,
            @RequestParam String endDate,
            Model model) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime start = LocalDateTime.parse(startDate, formatter);
        LocalDateTime end = LocalDateTime.parse(endDate, formatter);
        
        BigDecimal totalRevenue = revenueService.calculateTotalRevenue(start, end);
        BigDecimal totalAdminFee = revenueService.calculateTotalAdminFee(start, end);
        BigDecimal totalOwnerAmount = revenueService.calculateTotalOwnerAmount(start, end);
        
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalAdminFee", totalAdminFee);
        model.addAttribute("totalOwnerAmount", totalOwnerAmount);
        
        List<RevenueDTO> revenues = revenueService.getRevenuesByDateRange(start, end);
        model.addAttribute("revenues", revenues);
        
        return "revenue/report";
    }
} 