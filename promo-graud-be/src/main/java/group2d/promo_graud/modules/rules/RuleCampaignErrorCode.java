package group2d.promo_graud.modules.rules;


import group2d.promo_graud.shared.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum RuleCampaignErrorCode implements BaseErrorCode {
  RULE_NOT_EXIST(1501, "Rule campaign doesn't exist", HttpStatus.BAD_REQUEST);
  private final int code;
  private final String message;
  private final HttpStatusCode statusCode;

  RuleCampaignErrorCode(int code, String message, HttpStatusCode statusCode) {
    this.code = code;
    this.message = message;
    this.statusCode = statusCode;
  }
}
