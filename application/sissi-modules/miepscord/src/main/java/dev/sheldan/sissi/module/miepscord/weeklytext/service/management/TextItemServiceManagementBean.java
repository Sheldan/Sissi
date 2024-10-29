package dev.sheldan.sissi.module.miepscord.weeklytext.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.sissi.module.miepscord.weeklytext.model.database.TextItem;
import dev.sheldan.sissi.module.miepscord.weeklytext.repository.TextItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Component
public class TextItemServiceManagementBean {

    @Autowired
    private TextItemRepository textItemRepository;

    @Autowired
    private SecureRandom secureRandom;

    public TextItem createTextItem(String text, AUserInAServer aUserInAServer) {
        TextItem textItem = TextItem
                .builder()
                .text(text)
                .creator(aUserInAServer)
                .done(false)
                .build();
        return textItemRepository.save(textItem);
    }

    public Optional<TextItem> getNextTextItem() {
        List<TextItem> allItems = textItemRepository.findByDoneFalseOrderByCreated();
        if(allItems.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(allItems.get(secureRandom.nextInt(allItems.size())));
        }
    }

    public Optional<TextItem> findTextItemById(Long id) {
        return textItemRepository.findById(id);
    }

    public void deleteById(Long id) {
        textItemRepository.deleteById(id);
    }

    public List<TextItem> getAllTextItems() {
        return textItemRepository.findAll();
    }

    public List<TextItem> getAllTextItemsWithDoneFlag(boolean done) {
        return textItemRepository.findByDone(done);
    }
}
