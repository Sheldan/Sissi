package dev.sheldan.sissi.module.debra.job;

import dev.sheldan.sissi.module.debra.service.DonationService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Slf4j
@DisallowConcurrentExecution
@Component
@PersistJobDataAfterExecution
public class DonationFetchJob extends QuartzJobBean {

    @Autowired
    private DonationService donationService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            log.info("Checking for new donations.");
            donationService.checkForNewDonations();
        } catch (Exception e) {
            log.error("Failed to check for new donations.", e);
        }
    }
}
