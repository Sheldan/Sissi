package dev.sheldan.sissi.module.rssnews.orf.job;

import dev.sheldan.sissi.module.rssnews.orf.service.OrfNewsService;
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
public class NewsPostGatherJob extends QuartzJobBean {

    @Autowired
    private OrfNewsService orfNewsRFDService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            log.info("Executing news retrieval job.");
            orfNewsRFDService.checkNewsPosts();
        } catch (Exception e) {
            log.error("News retrieval job failed.", e);
        }
    }
}
