package dev.sheldan.sissi.module.custom.imagegeneration.service;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.service.HttpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class ImageGenerationService {

    @Value("${abstracto.feature.imagegeneration.doge.orangeSun.url}")
    private String dogeOrangeSunUrl;

    @Autowired
    private HttpService httpService;

    public File getOrangeSunDogeImage(String inputText) {
        try {
            return httpService.downloadFileToTempFile(dogeOrangeSunUrl.replace("{1}", inputText));
        } catch (IOException e) {
            throw new AbstractoRunTimeException(String.format("Failed to download orange doge image for url %s with error %s", inputText, e.getMessage()));
        }
    }

}
