package group2d.promo_graud.modules.voucher.component.distribute;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@StepScope // Chỉ khởi tạo đối tượng khi step chạy
@RequiredArgsConstructor
@Slf4j
public class DistributeVoucherReader implements ItemReader<Integer> {
    private final StringRedisTemplate redisTemplate;

    @Value("#{jobParameters['requestId']}")
    private String requestId;

    // Lấy chunkSize từ file cấu hình
    @Value("${voucher.batch.chunk-size:50}")
    private int chunkSize;

    // KHO ĐỆM TRONG RAM (Chứa danh sách ID kéo từ Redis về)
    private final Queue<Integer> buffer = new LinkedList<>();

    // TỪ KHÓA QUAN TRỌNG NHẤT: synchronized
    // Vì bạn đang chạy Đa luồng (4 luồng cùng lao vào lấy ID), phải có synchronized để xếp hàng,
    // chống giật lag và tránh trường hợp 2 luồng rút trùng ID của nhau.
    @Override
    public synchronized Integer read() {
        // Nếu kho đệm trống (Lần đầu chạy hoặc vừa phát hết mẻ trước)
        if (buffer.isEmpty()) {
            String listKey = "batch_users:" + requestId;

            // HỐT HÀNG LOẠT (BULK POP): Lấy một lúc N phần tử từ Redis
            // Lệnh leftPop(key, count) sẽ bốc N cái ra khỏi Redis chỉ bằng 1 network request
            List<String> poppedIds = redisTemplate.opsForList().leftPop(listKey, chunkSize);

            // Nếu Redis trả về rỗng (Đã phát xong toàn bộ 9000 người)
            if (poppedIds == null || poppedIds.isEmpty()) {
                return null; // Spring Batch nhận null sẽ tự động Dừng Step
            }

            // Nạp toàn bộ danh sách vừa lấy vào Kho đệm RAM
            for (String idStr : poppedIds) {
                buffer.add(Integer.valueOf(idStr));
            }

            log.info("Kéo thành công mẻ {} users từ Redis về Kho đệm RAM", poppedIds.size());
        }

        // Bốc 1 phần tử từ KHO ĐỆM RAM (chạy cực nhanh) trả về cho Spring Batch
        return buffer.poll();
    }
    // viết kiểu này chạy for => lâu
    //  @Override
    //  public Integer read() {
    //
    //    String listKey = "batch_users:" + requestId;
    //    // Rút ID từ đầu hàng đợi (Left Pop). Nếu hàng đợi rỗng, nó trả về null.
    //    String userIdStr = redisTemplate.opsForList().leftPop(listKey);
    //
    //    // Khi trả về null, Spring Batch sẽ hiểu là "Đã đọc hết danh sách" và tự động dừng Step
    //    if (userIdStr != null) {
    //      return Integer.valueOf(userIdStr);
    //    }
    //
    //    return null;
    //  }

}
