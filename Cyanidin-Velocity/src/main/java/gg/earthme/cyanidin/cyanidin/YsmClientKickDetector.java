package gg.earthme.cyanidin.cyanidin;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.scheduler.ScheduledTask;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class YsmClientKickDetector implements Runnable{
    private final Map<Player, Long> lastNotDetected = new ConcurrentHashMap<>();
    private final long timeOut;
    private volatile boolean scheduleNext = true;
    private volatile ScheduledTask lastScheduled = null;

    public YsmClientKickDetector() {
        this.timeOut = CyanidinConfig.ysmDetectionTimeout * 1000L * 1000L;
    }

    public void onPlayerJoin(Player player) {
        this.lastNotDetected.put(player, System.nanoTime());
    }

    public void onPlayerLeft(Player player) {
        this.lastNotDetected.remove(player);
    }

    public void signalStop() {
        this.scheduleNext = false;

        if (this.lastScheduled != null) {
            this.lastScheduled.cancel();
        }
    }

    public void bootstrap() {
        this.lastScheduled = Cyanidin.PROXY_SERVER.getScheduler().buildTask(Cyanidin.INSTANCE, this).delay(50, TimeUnit.MILLISECONDS).schedule();
    }

    @Override
    public void run() {
        try {
            final Set<Player> toKickOrCleanUp = new HashSet<>();

            for (Map.Entry<Player, Long> entry : this.lastNotDetected.entrySet()) {
                final long joinTimeNanos = entry.getValue();
                final Player target = entry.getKey();

                if (!target.isActive()) {
                    toKickOrCleanUp.add(target);
                    continue;
                }

                if (System.nanoTime() - joinTimeNanos > this.timeOut) {
                    if (!CyanidinConfig.kickIfYsmNotInstalled || Cyanidin.mapperManager.isPlayerInstalledYsm(target)) {
                        continue;
                    }

                    toKickOrCleanUp.add(target);
                }
            }

            for (Player target : toKickOrCleanUp) {
                this.lastNotDetected.remove(target);

                if (target.isActive()) {
                    target.disconnect(Cyanidin.languageManager.i18n("cyanidin.mod_handshake_time_outed", List.of(), List.of()));
                }
            }
        }finally {
            if (this.scheduleNext) {
                this.lastScheduled = Cyanidin.PROXY_SERVER.getScheduler().buildTask(Cyanidin.INSTANCE, this).delay(50, TimeUnit.MILLISECONDS).schedule();
            }
        }
    }
}