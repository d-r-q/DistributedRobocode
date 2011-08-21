package ru.jdev.rc.drc.client;

import ru.jdev.rc.drc.server.BfSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: jdev
 * Date: 21.08.11
 */
public class BattleRequestManager {

    private final List<BattleRequest> pendingRequests = new ArrayList<>();
    private final List<BattleRequest> executingRequests = new ArrayList<>();
    private final List<BattleRequest> executedRequests = new ArrayList<>();

    public BattleRequestManager(Challenge challenge, int seasons) {
        final BfSpec battlefieldSpecification = new BfSpec();
        battlefieldSpecification.setBfWidth(800);
        battlefieldSpecification.setBfHeight(600);

        int idSeq = 0;
        for (int i = 0; i < seasons; i++) {
            for (BotsGroup botsGroup : challenge.getBotGroups()) {
                for (Bot bot : botsGroup.getBots()) {
                    final BattleRequest battleRequest = new BattleRequest(Arrays.asList(challenge.getChallenger().getCompetitor(), bot.getCompetitor()), battlefieldSpecification, challenge.getRounds());
                    battleRequest.localId = idSeq++;
                    pendingRequests.add(battleRequest);
                }
            }
        }
    }

    public BattleRequest getBattleRequest() {
        if (pendingRequests.size() > 0) {
            final BattleRequest request = pendingRequests.remove(0);
            executingRequests.add(request);
            return request;
        } else if (executingRequests.size() > 0) {
            final BattleRequest battleRequest = executingRequests.remove(0);
            executingRequests.add(battleRequest);
            return battleRequest;
        }

        return null;
    }

    public void battleRequestExecuted(BattleRequest battleRequest) {
        executedRequests.add(battleRequest);
        executingRequests.remove(battleRequest);
    }

    @Override
    public String toString() {
        return "pendingRequests: " + pendingRequests + "\n" +
                "executingRequests: " + executingRequests + "\n" +
                "executedRequests: " + executedRequests;
    }

    public boolean hasNotExecutedRequests() {
        return pendingRequests.size() > 0 || executingRequests.size() > 0;
    }
}
