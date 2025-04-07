package ut.edu.project.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Service
public class QRCodeService {
    
    public String generateQRCode(String content, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Error generating QR code", e);
        }
    }
    
    public String generateBookingQRCode(Long bookingId, String serviceType) {
        String content = String.format("Booking ID: %d, Service: %s", bookingId, serviceType);
        return generateQRCode(content, 350, 350);
    }
    
    public String generateServiceQRCode(Long serviceId, String serviceType) {
        String content = String.format("Service ID: %d, Type: %s", serviceId, serviceType);
        return generateQRCode(content, 350, 350);
    }
} 