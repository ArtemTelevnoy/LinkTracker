package backend.academy.scrapper.clients;

import backend.academy.dto.links.LinkUpdate;

public interface BotClient {
    void sendUpdatesWithControl(LinkUpdate linkUpdate);

    void sendUpdatesNoControl(LinkUpdate linkUpdate);
}
