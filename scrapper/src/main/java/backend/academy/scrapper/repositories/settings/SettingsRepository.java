package backend.academy.scrapper.repositories.settings;

import backend.academy.dto.chats.TimeBody;

public interface SettingsRepository {
    void add(long id, TimeBody time);

    TimeBody get(long id);

    void delete(long id);
}
