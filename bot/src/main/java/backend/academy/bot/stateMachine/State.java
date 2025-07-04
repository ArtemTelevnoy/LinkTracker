package backend.academy.bot.stateMachine;

public enum State {
    DEFAULT,
    REGISTERED,
    ENTER_TRACK_LINK,
    ENTER_TAG_FOR_FINDING,
    ENTER_TAG_FOR_REMOVING,
    ENTER_TAGS,
    ENTER_FILTERS,
    ENTER_UNTRACK_LINK,
    ENTER_TIME
}
