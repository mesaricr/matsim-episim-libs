package org.matsim.episim;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.VspExperimentalConfigGroup;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.SplittableRandom;

/**
 * Provides the default bindings needed for Episim.
 */
public class EpisimModule extends AbstractModule {

	@Provides
	@Singleton
	public Scenario scenario(Config config) {

		if (config.getModules().size() == 0)
			throw new IllegalArgumentException("Please provide a config module or binding.");

		config.vspExperimental().setVspDefaultsCheckingLevel(VspExperimentalConfigGroup.VspDefaultsCheckingLevel.warn);

		// save some time for not needed inputs
		config.facilities().setInputFile(null);
		config.vehicles().setVehiclesFile(null);

		return ScenarioUtils.loadScenario(config);
	}

	@Provides
	@Singleton
	public EpisimConfigGroup episimConfigGroup(Config config) {
		return ConfigUtils.addOrGetModule(config, EpisimConfigGroup.class);
	}

	@Provides
	@Singleton
	public EpisimReporting episimReporting(Config config) {
		return new EpisimReporting(config);
	}

	@Provides
	@Singleton
	public EventsManager eventsManager() {
		return EventsUtils.createEventsManager();
	}

	@Provides
	@Singleton
	public SplittableRandom splittableRandom(Config config) {
		return new SplittableRandom(config.global().getRandomSeed());
	}

}
