package ut.edu.project.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    private String secret = "yourSecretKey"; // Thay bằng secret key mạnh
    private long expiration = 1000 * 60 * 60; // 1 giờ

    // Tạo token với username và role
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .claim("role", role) // Thêm claim "role"
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    // Kiểm tra token có hợp lệ không (bao gồm kiểm tra chữ ký và thời gian hết hạn)
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return !isTokenExpired(token); // Token hợp lệ nếu chưa hết hạn
        } catch (Exception e) {
            return false;
        }
    }

    // Trích xuất username từ token
    public String extractUsername(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    // Trích xuất role từ token
    public String extractRole(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
        return claims.get("role", String.class);
    }

    // Kiểm tra token có hết hạn không
    public boolean isTokenExpired(String token) {
        Date expirationDate = extractExpiration(token);
        return expirationDate.before(new Date());
    }

    // Trích xuất thời gian hết hạn từ token
    private Date extractExpiration(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration();
    }
}