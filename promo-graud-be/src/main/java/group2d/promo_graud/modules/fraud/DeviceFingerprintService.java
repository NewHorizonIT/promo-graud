package group2d.promo_graud.modules.fraud;

import group2d.promo_graud.shared.exception.AppException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class DeviceFingerprintService {

  /** Trả fingerprint đã hash; ném VISITOR_ID_REQUIRED nếu thiếu visitorId. */
  public String fingerprint(HttpServletRequest req, String visitorId) {
    if (visitorId == null || visitorId.isBlank()) {
      throw new AppException(FraudErrorCode.VISITOR_ID_REQUIRED);
    }
    String raw =
      String.join(
        "|",
        nullSafe(req.getHeader("User-Agent")),
        nullSafe(req.getHeader("Accept-Language")),
        nullSafe(req.getHeader("Sec-CH-UA-Platform")),
        visitorId);
    return sha256(raw);
  }

  private static String nullSafe(String s) {
    return s == null ? "" : s;
  }

  private static String sha256(String input) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(md.digest(input.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
