package dev.sheldan.sissi.module.debra.service.management;

import dev.sheldan.sissi.module.debra.model.database.EndlessStream;
import dev.sheldan.sissi.module.debra.repository.EndlessStreamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EndlessStreamManagementServiceBean {

    @Autowired
    private EndlessStreamRepository endlessStreamRepository;

    public EndlessStream getEndlessStream(Long id) {
        return endlessStreamRepository.getReferenceById(id);
    }
}
