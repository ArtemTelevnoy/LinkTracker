package backend.academy.scrapper.service;

import backend.academy.dto.chats.TimeBody;
import backend.academy.dto.links.LinkUpdate;
import backend.academy.scrapper.clients.BotClient;
import backend.academy.scrapper.repositories.settings.SettingsRepository;
import backend.academy.scrapper.repositories.user.UserRepository;
import java.time.LocalTime;
import java.time.ZoneId;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@EnableScheduling
@AllArgsConstructor
public class TimeTableSendingService {
    private final BotClient botClient;
    private final UserUpdatesService userUpdatesService;
    private final UserRepository userRepository;
    private final SettingsRepository settingsRepository;

    @Scheduled(fixedRate = 60000)
    public void timetableChecking() {
        log.info("Starting checking users updates with timetable");
        final LocalTime now = LocalTime.now(ZoneId.systemDefault());

        for (long userId : userRepository.getAllUsers()) {
            final TimeBody timeBody = settingsRepository.get(userId);

            if (timeBody == null || (now.getHour() == timeBody.hours() && now.getMinute() == timeBody.minutes())) {
                final String updates = userUpdatesService.get(userId);
                if (updates == null) {
                    continue;
                }

                botClient.sendUpdatesWithControl(new LinkUpdate(updates, userId));
            }
        }

        log.info("Ending checking users updates with timetable");
    }
}
