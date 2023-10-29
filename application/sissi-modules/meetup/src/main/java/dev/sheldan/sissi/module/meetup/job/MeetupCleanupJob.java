package dev.sheldan.sissi.module.meetup.job;

import dev.sheldan.sissi.module.meetup.service.MeetupServiceBean;
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
public class MeetupCleanupJob extends QuartzJobBean {

    @Autowired
    private MeetupServiceBean meetupServiceBean;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            log.info("Executing cleanup job for meetups.");
            meetupServiceBean.cleanupMeetups();
        } catch (Exception e) {
            log.error("Meetup cleanup job failed.", e);
        }
    }

}
