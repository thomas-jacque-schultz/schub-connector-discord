package schultz.thomas.discord.bot.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import schultz.thomas.discord.bot.controllers.events.schedullers.GameServerPullScheduler;

/** Programme le pull périodique à partir de {@code core.pull-interval}. */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class PullScheduling implements SchedulingConfigurer {

    private final GameServerPullScheduler pullScheduler;
    private final CoreProperties properties;

    @Override
    public void configureTasks(ScheduledTaskRegistrar registrar) {
        log.info("Pull du cœur programmé toutes les {}s", properties.getPullInterval().toSeconds());
        registrar.addFixedRateTask(pullScheduler::pull, properties.getPullInterval());
    }
}
