package ut.edu.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ut.edu.project.models.Camping;
import ut.edu.project.models.User;
import ut.edu.project.models.Booking;
import ut.edu.project.repositories.CampingRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CampingService {
    @Autowired
    private CampingRepository campingRepository; // Repository quản lý khu cắm trại

    // Tạo khu cắm trại mới
    @Transactional
    public Camping createCamping(Camping camping) {
        validateCamping(camping); // Kiểm tra tính hợp lệ của khu cắm trại
        return campingRepository.save(camping); // Lưu khu cắm trại vào cơ sở dữ liệu
    }

    // Cập nhật thông tin khu cắm trại
    @Transactional
    public Camping updateCamping(Long id, Camping camping) {
        Camping existingCamping = campingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khu cắm trại")); // Tìm khu cắm trại theo ID
        existingCamping.setName(camping.getName()); // Cập nhật tên
        existingCamping.setLocation(camping.getLocation()); // Cập nhật địa điểm
        existingCamping.setDescription(camping.getDescription()); // Cập nhật mô tả
        existingCamping.setPrice(camping.getPrice()); // Cập nhật giá
        existingCamping.setMaxPlaces(camping.getMaxPlaces()); // Cập nhật số chỗ tối đa
        existingCamping.setIsAvailable(camping.isAvailable()); // Cập nhật trạng thái sẵn sàng (sử dụng phương thức có sẵn)
        existingCamping.setImageUrls(camping.getImageUrls()); // Cập nhật danh sách URL ảnh
        existingCamping.setFacilities(camping.getFacilities()); // Cập nhật danh sách tiện ích
        existingCamping.setRules(camping.getRules()); // Cập nhật quy tắc
        existingCamping.setTerrain(camping.getTerrain()); // Cập nhật địa hình
        existingCamping.setWeather(camping.getWeather()); // Cập nhật thời tiết
        existingCamping.setEquipment(camping.getEquipment()); // Cập nhật thiết bị
        validateCamping(existingCamping); // Kiểm tra tính hợp lệ sau khi cập nhật
        return campingRepository.save(existingCamping); // Lưu thay đổi
    }

    // Lấy thông tin khu cắm trại theo ID
    public Optional<Camping> getCampingById(Long id) {
        return campingRepository.findById(id); // Trả về khu cắm trại nếu tồn tại
    }

    // Lấy tất cả khu cắm trại
    public List<Camping> getAllCampings() {
        return campingRepository.findAll(); // Trả về danh sách tất cả khu cắm trại
    }

    // Xóa khu cắm trại
    @Transactional
    public void deleteCamping(Long id) {
        Camping camping = campingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khu cắm trại")); // Tìm khu cắm trại
        if (!camping.getBookings().isEmpty()) {
            throw new RuntimeException("Không thể xóa khu cắm trại đã có đặt chỗ"); // Báo lỗi nếu có đặt chỗ
        }
        campingRepository.delete(camping); // Xóa khu cắm trại
    }

    // Kiểm tra tính hợp lệ của khu cắm trại
    private void validateCamping(Camping camping) {
        if (camping.getName() == null || camping.getName().trim().isEmpty()) {
            throw new RuntimeException("Tên khu cắm trại không được để trống");
        }
        if (camping.getLocation() == null || camping.getLocation().trim().isEmpty()) {
            throw new RuntimeException("Địa điểm không được để trống");
        }
        if (camping.getPrice() == null || camping.getPrice() <= 0) {
            throw new RuntimeException("Giá phải lớn hơn 0");
        }
        if (camping.getMaxPlaces() == null || camping.getMaxPlaces() <= 0) {
            throw new RuntimeException("Số chỗ phải lớn hơn 0");
        }
        if (camping.getOwner() == null) {
            throw new RuntimeException("Chủ sở hữu không được để trống");
        }
    }

    // Đặt chỗ khu cắm trại
    public boolean bookCamping(Long id, LocalDate startDate, LocalDate endDate, int numberOfPeople, String customerName) {
        return campingRepository.findById(id).map(camping -> {
            if (camping.isAvailable() && numberOfPeople <= camping.getMaxPlaces() && isAvailableForDates(camping, startDate, endDate)) {
                Booking booking = new Booking(); // Tạo đặt chỗ mới
                booking.setCheckIn(startDate.atStartOfDay()); // Ngày nhận chỗ
                booking.setCheckOut(endDate.atStartOfDay()); // Ngày trả chỗ
                booking.setGuests(numberOfPeople); // Số lượng khách
                booking.setStatus(Booking.BookingStatus.PENDING); // Trạng thái chờ xử lý
                camping.addBooking(booking); // Thêm đặt chỗ vào khu cắm trại
                campingRepository.save(camping); // Lưu thay đổi
                return true;
            }
            return false;
        }).orElse(false); // Trả về false nếu không tìm thấy khu cắm trại
    }

    // Hủy đặt chỗ khu cắm trại
    public boolean cancelBooking(Long id, int bookingIndex) {
        return campingRepository.findById(id).map(camping -> {
            List<Booking> bookings = camping.getBookings();
            if (bookingIndex >= 0 && bookingIndex < bookings.size()) {
                Booking booking = bookings.get(bookingIndex); // Lấy đặt chỗ theo chỉ số
                if (booking.getCheckIn().toLocalDate().isAfter(LocalDate.now().plusDays(1))) {
                    booking.setStatus(Booking.BookingStatus.CANCELLED); // Đặt trạng thái hủy
                    campingRepository.save(camping); // Lưu thay đổi
                    return true;
                }
            }
            return false;
        }).orElse(false); // Trả về false nếu không tìm thấy khu cắm trại
    }

    // Tìm kiếm khu cắm trại theo tên với phân trang
    public Page<Camping> searchCampingsByName(String name, int page, int size, String sort) {
        PageRequest pageRequest = buildPageRequest(page, size, sort); // Tạo yêu cầu phân trang
        return campingRepository.findByNameContainingIgnoreCase(name, pageRequest); // Trả về trang kết quả
    }

    // Lấy khu cắm trại theo số chỗ tối thiểu với phân trang
    public Page<Camping> getCampingsByMinPlaces(int minPlaces, int page, int size, String sort) {
        PageRequest pageRequest = buildPageRequest(page, size, sort); // Tạo yêu cầu phân trang
        return campingRepository.findByMaxPlacesGreaterThanEqual(minPlaces, pageRequest); // Trả về trang kết quả
    }

    // Lấy danh sách khu cắm trại với phân trang và lọc theo ngày
    public Page<Camping> getCampingsPaginated(int page, int size, String sort, LocalDate startDate, LocalDate endDate) {
        PageRequest pageRequest = buildPageRequest(page, size, sort); // Tạo yêu cầu phân trang
        Page<Camping> campingPage = campingRepository.findAll(pageRequest); // Lấy tất cả khu cắm trại
        if (startDate != null && endDate != null) {
            List<Camping> filteredList = campingPage.getContent().stream()
                    .filter(c -> isAvailableForDates(c, startDate, endDate)) // Lọc theo ngày sẵn sàng
                    .collect(Collectors.toList());
            return new PageImpl<>(filteredList, pageRequest, campingPage.getTotalElements()); // Trả về trang đã lọc
        }
        return campingPage; // Trả về trang gốc nếu không có lọc ngày
    }

    // Kiểm tra tính sẵn sàng của khu cắm trại trong khoảng thời gian
    private boolean isAvailableForDates(Camping camping, LocalDate start, LocalDate end) {
        for (Booking booking : camping.getBookings()) {
            if (booking.getStatus() != Booking.BookingStatus.CANCELLED &&
                    !(end.isBefore(booking.getCheckIn().toLocalDate()) ||
                            start.isAfter(booking.getCheckOut().toLocalDate()))) {
                return false; // Không sẵn sàng nếu có đặt chỗ trùng
            }
        }
        return true; // Sẵn sàng nếu không có trùng lặp
    }

    // Xây dựng yêu cầu phân trang và sắp xếp
    private PageRequest buildPageRequest(int page, int size, String sort) {
        if (sort == null || sort.isEmpty()) {
            return PageRequest.of(page, size); // Không sắp xếp nếu sort rỗng
        }
        switch (sort) {
            case "price_asc":
                return PageRequest.of(page, size, Sort.by("price").ascending()); // Sắp xếp giá tăng dần
            case "price_desc":
                return PageRequest.of(page, size, Sort.by("price").descending()); // Sắp xếp giá giảm dần
            case "maxPlaces_asc":
                return PageRequest.of(page, size, Sort.by("maxPlaces").ascending()); // Sắp xếp số chỗ tăng dần
            case "maxPlaces_desc":
                return PageRequest.of(page, size, Sort.by("maxPlaces").descending()); // Sắp xếp số chỗ giảm dần
            default:
                return PageRequest.of(page, size); // Mặc định không sắp xếp
        }
    }

    // Thêm đánh giá mới
    @Transactional
    public Camping addReview(Long id, Integer rating, String comment, String reply) {
        Camping camping = campingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khu cắm trại")); // Tìm khu cắm trại
        if (rating < 1 || rating > 5) {
            throw new RuntimeException("Điểm đánh giá phải từ 1 đến 5"); // Kiểm tra điểm hợp lệ
        }
        camping.addReview(comment, rating, reply); // Thêm đánh giá
        return campingRepository.save(camping); // Lưu thay đổi
    }

    // Phản hồi một đánh giá
    @Transactional
    public Camping replyToReview(Long id, int reviewIndex, String reply) {
        Camping camping = campingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khu cắm trại")); // Tìm khu cắm trại
        camping.replyToReview(reviewIndex, reply); // Thêm phản hồi
        return campingRepository.save(camping); // Lưu thay đổi
    }

    // Thêm thông báo mới
    @Transactional
    public Camping addNotification(Long id, String message) {
        Camping camping = campingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khu cắm trại")); // Tìm khu cắm trại
        camping.addNotification(message); // Thêm thông báo
        return campingRepository.save(camping); // Lưu thay đổi
    }

    // Lấy danh sách thông báo
    public List<String> getNotifications(Long id) {
        return campingRepository.findById(id)
                .map(camping -> camping.getRecentNotifications(5)) // Lấy 5 thông báo gần nhất
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khu cắm trại"));
    }

    // Thêm từ khóa tìm kiếm
    @Transactional
    public Camping addSearchTerm(Long id, String term) {
        Camping camping = campingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khu cắm trại")); // Tìm khu cắm trại
        camping.addSearchTerm(term); // Thêm từ khóa
        return campingRepository.save(camping); // Lưu thay đổi
    }

    // Lấy danh sách khu cắm trại gợi ý
    public List<Camping> getSuggestedCampings(String term) {
        return campingRepository.findBySearchTerm(term); // Tìm khu cắm trại theo từ khóa
    }

    // Xử lý thanh toán
    public String processPayment(Long id, Double amount) {
        Camping camping = campingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khu cắm trại")); // Tìm khu cắm trại
        if (amount < camping.getPrice()) {
            camping.setPaymentStatus("FAILED: Số tiền không đủ"); // Cập nhật trạng thái thất bại
            campingRepository.save(camping); // Lưu thay đổi
            return "Thanh toán thất bại: Số tiền nhỏ hơn giá";
        }
        camping.setPaymentStatus("SUCCESS"); // Cập nhật trạng thái thành công
        campingRepository.save(camping); // Lưu thay đổi
        return "Thanh toán thành công";
    }
}