package dev.sheldan.sissi.module.rssnews.service;

import dev.sheldan.sissi.module.rssnews.service.management.NewsFeedSourceManagementServiceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NewsFeedSourceServiceBean {

    @Autowired
    private NewsFeedSourceManagementServiceBean newsFeedSourceManagementServiceBean;


}
