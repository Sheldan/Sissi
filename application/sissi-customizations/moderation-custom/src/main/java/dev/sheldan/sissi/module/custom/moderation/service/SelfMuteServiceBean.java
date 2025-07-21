package dev.sheldan.sissi.module.custom.moderation.service;

import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class SelfMuteServiceBean {

    @Autowired
    private MemberService memberService;

    @Autowired
    private TemplateService templateService;

    private static final String SELF_MUTE_REASON_TEMPLATE = "self_mute_reason";

    public CompletableFuture<Void> selfMuteMember(Member member, Duration duration) {
        String reason = templateService.renderSimpleTemplate(SELF_MUTE_REASON_TEMPLATE, member.getGuild().getIdLong());
        log.info("Self muting user {} in server {}.", member.getIdLong(), member.getGuild().getIdLong());
        return memberService.timeoutUser(member, duration, reason);
    }
}
