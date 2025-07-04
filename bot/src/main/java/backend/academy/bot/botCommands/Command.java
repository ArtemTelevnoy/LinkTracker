package backend.academy.bot.botCommands;

import backend.academy.bot.clients.ScrapperClient;
import backend.academy.bot.stateMachine.StateMachine;

public interface Command {
    String work(String message, long id, StateMachine stateMachine, ScrapperClient client);

    String name();

    String hint();
}
