package dev.sheldan.sissi.module.miepscord.weeklytext.job;

import dev.sheldan.sissi.module.miepscord.weeklytext.service.TextItemServiceBean;
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
public class WeeklyTextJob extends QuartzJobBean {

    @Autowired
    private TextItemServiceBean textItemServiceBean;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            log.info("Executing text item job.");
            textItemServiceBean.sendTexItem();
        } catch (Exception e) {
            log.error("Text item job failed.", e);
        }
    }
}
