package group2d.promo_graud.modules.voucher.component;

import javax.sql.DataSource;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.transaction.PlatformTransactionManager;

import group2d.promo_graud.modules.voucher.Voucher;

@Configuration
public class GenerateVoucherJobConfig {
    @Value("${voucher.batch.unique-threshold:50}")
    private int chunkSize;

    @Bean
    public JdbcBatchItemWriter<Voucher> voucherWriter(DataSource ds) {
        return new JdbcBatchItemWriterBuilder<Voucher>()
                .dataSource(ds)
                .sql(
                        """
                    INSERT INTO voucher
                      (code, type, rule_id, quantity, quantity_remain,
                       limit_client, distribution_channel, status)
                    VALUES
                      (:code, :type, :ruleId, :quantity, :quantityRemain,
                       :limitClient, :channel, :status)
                    ON CONFLICT (code) DO NOTHING
                    """) // MySQL: "INSERT IGNORE INTO voucher ..." và bỏ dòng ON CONFLICT
                .itemSqlParameterSourceProvider(
                        v ->
                                new MapSqlParameterSource()
                                        .addValue("code", v.getCode())
                                        .addValue("type", v.getType().name())
                                        .addValue("ruleId", v.getRuleCampaign().getId())
                                        .addValue("quantity", v.getQuantity())
                                        .addValue("quantityRemain", v.getQuantityRemain())
                                        .addValue("limitClient", v.getLimitClient())
                                        .addValue("channel", v.getDistributionChannel().name())
                                        .addValue("status", v.getStatus().name()))
                .assertUpdates(false)
                .build();
    }

    @Bean
    public Step generateVoucherStep(
            JobRepository repo,
            PlatformTransactionManager tx,
            VoucherSequenceReader reader,
            VoucherCodeProcessor processor,
            JdbcBatchItemWriter<Voucher> voucherWriter) {
        return new StepBuilder("generateVoucherStep", repo)
                .<Integer, Voucher>chunk(chunkSize) // Gom chunk
                .transactionManager(
                        tx) // nếu trong quá trình lưu bị fail thì all voucher sẽ bị thu hồi
                .reader(reader)
                .processor(processor)
                .writer(voucherWriter)
                .faultTolerant()
                .retryLimit(3)
                .retry(TransientDataAccessException.class)
                .build();
    }

    @Bean
    public Job generateVoucherJob(
            JobRepository repo, Step generateVoucherStep, VoucherJobListener listener) {
        return new JobBuilder("generateVoucherJob", repo)
                .listener(listener)
                .start(generateVoucherStep)
                .build();
    }
}
