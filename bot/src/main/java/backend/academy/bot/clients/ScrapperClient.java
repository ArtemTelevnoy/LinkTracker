package backend.academy.bot.clients;

import backend.academy.dto.links.LinkResponse;
import backend.academy.dto.links.ListLinksResponse;
import backend.academy.dto.tags.LinksByTagResponse;
import backend.academy.dto.tags.RemoveLinksByTagResponse;
import backend.academy.dto.tags.TagsResponse;

public interface ScrapperClient {
    LinkResponse getUntrackResponse(long id, String url);

    LinkResponse getTrackResponse(long id, String url, String[] tags, String[] filters);

    String getStartResponse(long id);

    ListLinksResponse getListResponse(long id);

    TagsResponse getTagsResponse(long id);

    LinksByTagResponse getByTagResponse(long id, String tag);

    RemoveLinksByTagResponse removeByTagResponse(long id, String tag);

    String getChangeTimeResponse(long id, short hours, short minutes);

    String getChangeTimeResponse(long id);
}
