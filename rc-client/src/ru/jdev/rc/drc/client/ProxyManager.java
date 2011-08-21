package ru.jdev.rc.drc.client;

import ru.jdev.rc.drc.server.Competitor;
import ru.jdev.rc.drc.server.RobocodeServer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * User: jdev
 * Date: 21.08.11
 */
public class ProxyManager {

    private final List<RobocodeServerProxy> availableProxies = new ArrayList<>();

    private final ExecutorService executorService;
    private final List<Future<RobocodeServerProxy>> pendingProxies;
    private final Set<Bot> bots;

    public ProxyManager(ExecutorService executorService, Set<Bot> bots) throws IOException {
        this.executorService = executorService;
        this.bots = bots;

        pendingProxies = getProxiesFutures(executorService);
    }

    public RobocodeServerProxy getFreeProxy() {
        while (true) {
            checkPendingProxies();

            for (RobocodeServerProxy proxy : availableProxies) {
                if (proxy.ready()) {
                    return proxy;
                }
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public List<RobocodeServerProxy> getAvailableProxies() {
        return availableProxies;
    }

    private void checkPendingProxies() {
        for (Iterator<Future<RobocodeServerProxy>> iter = pendingProxies.iterator(); iter.hasNext(); ) {
            final Future<RobocodeServerProxy> future = iter.next();
            try {
                if (future.isDone()) {
                    final RobocodeServerProxy robocodeServerProxy = future.get();
                    if (robocodeServerProxy != null) {
                        loadCompetitors(robocodeServerProxy.getServerPort());
                        availableProxies.add(robocodeServerProxy);
                        iter.remove();
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        if (pendingProxies.size() == 0 && availableProxies.size() == 0) {
            throw new IllegalStateException("No available servers");
        }
    }

    private void loadCompetitors(final RobocodeServer serverPort) {
        for (Bot bot : bots) {
            registerCompetitor(bot.getCompetitor(), serverPort, bot.getCode());
        }
    }

    private void registerCompetitor(Competitor competitor, RobocodeServer serverPort, byte[] code) {
        if (!serverPort.hasCompetitor(competitor)) {
            System.out.printf("Registering competitor %s %s\n", competitor.getName(), competitor.getVersion());
            serverPort.registerCode(competitor, code);
        }
    }

    private static List<Future<RobocodeServerProxy>> getProxiesFutures(final ExecutorService service) throws IOException {
        final List<Future<RobocodeServerProxy>> futures = new ArrayList<>();
        try (
                BufferedReader reader = new BufferedReader(new FileReader("./config/servers.cfg"))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                final String[] serverCfg = line.split(";");
                futures.add(service.submit(new RSProxyFactory(serverCfg[2], serverCfg[0], Integer.parseInt(serverCfg[1]))));
            }
        }

        return futures;
    }

}
