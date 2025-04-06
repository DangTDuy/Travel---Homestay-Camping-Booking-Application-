package ut.edu.project.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ut.edu.project.models.*;
import ut.edu.project.repositories.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AIRecommendationService {

    @Autowired
    private HomestayRepository homestayRepository;

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Phân tích sở thích của người dùng dựa trên lịch sử đặt phòng và đánh giá
     */
    public UserPreference analyzeUserPreferences(User user) {
        UserPreference preference = userPreferenceRepository.findByUser(user)
                .orElse(new UserPreference());
        preference.setUser(user);

        // Phân tích lịch sử đặt chỗ
        List<Homestay> bookedHomestays = bookingRepository.findHomestaysByUser(user);
        preference.setTotalBookings(bookedHomestays.size());

        // Phân tích đánh giá
        List<Review> userReviews = reviewRepository.findByUserId(user.getId());
        double avgRating = userReviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
        preference.setAverageRating(avgRating);

        // Phân tích địa điểm ưa thích
        Set<String> locations = bookedHomestays.stream()
                .map(Homestay::getLocation)
                .collect(Collectors.toSet());
        try {
            preference.setLastVisitedLocations(objectMapper.writeValueAsString(new ArrayList<>(locations)));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // Phân tích khoảng giá ưa thích
        if (!bookedHomestays.isEmpty()) {
            double avgPrice = bookedHomestays.stream()
                    .mapToDouble(Homestay::getPrice)
                    .average()
                    .orElse(0.0);
            preference.setPreferredPriceRange(avgPrice);
        } else {
            preference.setPreferredPriceRange(0.0); // Giá trị mặc định
        }

        // Phân tích tiện nghi ưa thích
        Set<String> amenities = new HashSet<>();
        bookedHomestays.forEach(homestay -> {
            if (homestay.getAmenities() != null) {
                amenities.addAll(homestay.getAmenities());
            }
        });
        try {
            preference.setPreferredAmenities(objectMapper.writeValueAsString(new ArrayList<>(amenities)));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // Phân tích tags ưa thích
        Set<String> tags = new HashSet<>();
        bookedHomestays.forEach(homestay -> {
            if (homestay.getTags() != null) {
                try {
                    List<String> homestayTags = objectMapper.readValue(
                            homestay.getTags(),
                            new TypeReference<List<String>>() {}
                    );
                    tags.addAll(homestayTags);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        });
        try {
            preference.setPreferredTags(objectMapper.writeValueAsString(new ArrayList<>(tags)));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // Phân tích mùa ưa thích
        Set<String> seasons = new HashSet<>();
        bookedHomestays.forEach(homestay -> {
            if (homestay.getSeasons() != null) {
                try {
                    List<String> homestaySeasons = objectMapper.readValue(
                            homestay.getSeasons(),
                            new TypeReference<List<String>>() {}
                    );
                    seasons.addAll(homestaySeasons);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        });
        try {
            preference.setPreferredSeasons(objectMapper.writeValueAsString(new ArrayList<>(seasons)));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // Phân tích các yếu tố khác
        preference.setPrefersQuiet(hasPreference(bookedHomestays, "quiet"));
        preference.setPrefersFamily(hasPreference(bookedHomestays, "family"));
        preference.setPrefersLuxury(hasPreference(bookedHomestays, "luxury"));
        preference.setPrefersBudget(hasPreference(bookedHomestays, "budget"));

        return userPreferenceRepository.save(preference);
    }

    /**
     * Kiểm tra xem người dùng có sở thích cụ thể không
     */
    private boolean hasPreference(List<Homestay> homestays, String preference) {
        return homestays.stream().anyMatch(homestay -> {
            if (homestay.getTags() == null) {
                return false;
            }
            try {
                List<String> tags = objectMapper.readValue(
                        homestay.getTags(),
                        new TypeReference<List<String>>() {}
                );
                return tags.contains(preference);
            } catch (JsonProcessingException e) {
                return false;
            }
        });
    }

    /**
     * Tạo gợi ý homestay dựa trên sở thích người dùng và loại gợi ý
     */
    public List<Homestay> generateRecommendations(User user, int limit, String type) {
        switch (type != null ? type.toLowerCase() : "") {
            case "trending":
                return getTrendingHomestays(limit);
            case "deals":
                return getDealHomestays(limit);
            default:
                return generatePersonalizedRecommendations(user, limit);
        }
    }

    /**
     * Tạo gợi ý cá nhân hóa
     */
    private List<Homestay> generatePersonalizedRecommendations(User user, int limit) {
        UserPreference preference = analyzeUserPreferences(user);
        List<Homestay> allHomestays = homestayRepository.findAll();
        
        // Tính điểm cho mỗi homestay
        Map<Homestay, Double> scores = new HashMap<>();
        for (Homestay homestay : allHomestays) {
            double score = 0.0;
            
            // Điểm theo địa điểm
            if (preference.getLastVisitedLocations() != null) {
                try {
                    List<String> visitedLocations = objectMapper.readValue(
                            preference.getLastVisitedLocations(),
                            new TypeReference<List<String>>() {}
                    );
                    if (visitedLocations.contains(homestay.getLocation())) {
                        score += 2.0;
                    }
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
            
            // Điểm theo giá
            Double preferredPrice = preference.getPreferredPriceRange();
            if (preferredPrice != null && preferredPrice > 0) {
                double priceDiff = Math.abs(homestay.getPrice() - preferredPrice);
                score += 1.0 - (priceDiff / preferredPrice);
            }
            
            // Điểm theo tiện nghi
            if (preference.getPreferredAmenities() != null) {
                try {
                    List<String> preferredAmenities = objectMapper.readValue(
                            preference.getPreferredAmenities(),
                            new TypeReference<List<String>>() {}
                    );
                    if (homestay.getAmenities() != null) {
                        long matchingAmenities = homestay.getAmenities().stream()
                                .filter(preferredAmenities::contains)
                                .count();
                        score += matchingAmenities * 0.5;
                    }
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
            
            // Điểm theo tags
            if (preference.getPreferredTags() != null && homestay.getTags() != null) {
                try {
                    List<String> preferredTags = objectMapper.readValue(
                            preference.getPreferredTags(),
                            new TypeReference<List<String>>() {}
                    );
                    List<String> homestayTags = objectMapper.readValue(
                            homestay.getTags(),
                            new TypeReference<List<String>>() {}
                    );
                    long matchingTags = homestayTags.stream()
                            .filter(preferredTags::contains)
                            .count();
                    score += matchingTags * 0.3;
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
            
            scores.put(homestay, score);
        }
        
        // Sắp xếp theo điểm số và trả về số lượng homestay được yêu cầu
        return scores.entrySet().stream()
                .sorted(Map.Entry.<Homestay, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách homestay theo bộ lọc
     */
    public List<Homestay> getFilteredRecommendations(
            User user, Double minPrice, Double maxPrice, 
            String location, List<String> amenities, int limit) {
        List<Homestay> allHomestays = homestayRepository.findAll();
        
        return allHomestays.stream()
                .filter(homestay -> {
                    // Lọc theo giá
                    if (minPrice != null && homestay.getPrice() < minPrice) return false;
                    if (maxPrice != null && homestay.getPrice() > maxPrice) return false;
                    
                    // Lọc theo địa điểm
                    if (location != null && !homestay.getLocation().toLowerCase()
                            .contains(location.toLowerCase())) return false;
                    
                    // Lọc theo tiện nghi
                    if (amenities != null && !amenities.isEmpty()) {
                        if (homestay.getAmenities() == null) return false;
                        return homestay.getAmenities().containsAll(amenities);
                    }
                    
                    return true;
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách homestay đang xu hướng
     */
    public List<Homestay> getTrendingHomestays(int limit) {
        return homestayRepository.findAll().stream()
                .sorted((h1, h2) -> {
                    // Sắp xếp theo số lượt đặt và đánh giá
                    int bookingCompare = h2.getBooking_count().compareTo(h1.getBooking_count());
                    if (bookingCompare != 0) return bookingCompare;
                    return h2.getRating().compareTo(h1.getRating());
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách homestay có ưu đãi
     */
    public List<Homestay> getDealHomestays(int limit) {
        // Tính giá trung bình của tất cả homestay
        double avgPrice = homestayRepository.findAll().stream()
                .mapToDouble(Homestay::getPrice)
                .average()
                .orElse(0.0);
        
        return homestayRepository.findAll().stream()
                .filter(homestay -> homestay.getPrice() < avgPrice * 0.8) // Giá thấp hơn 20% so với trung bình
                .sorted((h1, h2) -> h1.getPrice().compareTo(h2.getPrice())) // Sắp xếp theo giá tăng dần
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Cập nhật sở thích người dùng
     */
    public UserPreference updateUserPreferences(User user, Map<String, Object> preferences) {
        UserPreference userPreference = userPreferenceRepository.findByUser(user)
                .orElse(new UserPreference());
        userPreference.setUser(user);

        // Cập nhật các sở thích từ request
        if (preferences.containsKey("preferredLocation")) {
            userPreference.setPreferredLocation((String) preferences.get("preferredLocation"));
        }
        if (preferences.containsKey("preferredPriceRange")) {
            userPreference.setPreferredPriceRange((Double) preferences.get("preferredPriceRange"));
        }
        if (preferences.containsKey("preferredAmenities")) {
            try {
                userPreference.setPreferredAmenities(
                    objectMapper.writeValueAsString(preferences.get("preferredAmenities")));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        if (preferences.containsKey("preferredTags")) {
            try {
                userPreference.setPreferredTags(
                    objectMapper.writeValueAsString(preferences.get("preferredTags")));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        if (preferences.containsKey("prefersQuiet")) {
            userPreference.setPrefersQuiet((Boolean) preferences.get("prefersQuiet"));
        }
        if (preferences.containsKey("prefersFamily")) {
            userPreference.setPrefersFamily((Boolean) preferences.get("prefersFamily"));
        }
        if (preferences.containsKey("prefersLuxury")) {
            userPreference.setPrefersLuxury((Boolean) preferences.get("prefersLuxury"));
        }
        if (preferences.containsKey("prefersBudget")) {
            userPreference.setPrefersBudget((Boolean) preferences.get("prefersBudget"));
        }

        return userPreferenceRepository.save(userPreference);
    }
}