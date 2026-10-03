package group2d.promo_graud.modules.voucher.component;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemStreamReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope //Chỉ khởi tạo đối tượng khi step chạy
public class VoucherSequenceReader implements ItemStreamReader<Integer> {

  @Value("#{jobParameters['quantity']}")
  private Long quantity;

  private int current;
    // Đc gọi liên tục theo vòng lặp
  @Override public Integer read() {
    return current < quantity ? ++current : null;
  }
  //Chạy 1 lần duy nhất trước khi bắt đầu đọc
  @Override public void open(ExecutionContext ctx) {
    current = ctx.getInt("current", 0);
  }
  // Đc gọi khi hoàn thành mỗi chunk
  @Override public void update(ExecutionContext ctx) {
    ctx.putInt("current", current);
  }
  @Override public void close() {}
}
