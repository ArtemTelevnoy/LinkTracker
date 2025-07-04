package backend.academy.scrapper.link;

import java.time.Instant;

public record LinkBody(long id, String url, Instant lastUpdated, LinkType type) {}
