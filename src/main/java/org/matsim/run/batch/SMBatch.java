package org.matsim.run.batch;

import com.google.inject.AbstractModule;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.episim.BatchRun;
import org.matsim.episim.EpisimConfigGroup;
import org.matsim.episim.EpisimUtils;
import org.matsim.run.modules.SnzBerlinProductionScenario;

import javax.annotation.Nullable;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;



/**
 * Interventions for symmetric Berlin week model with different contact models
 */
public class SMBatch implements BatchRun<SMBatch.Params> {

	@Override
	public AbstractModule getBindings(int id, @Nullable Params params) {
		return new SnzBerlinProductionScenario.Builder().setSnapshot( SnzBerlinProductionScenario.Snapshot.episim_snapshot_120_2020_06_14 ).createSnzBerlinProductionScenario();
	}

	@Override
	public Metadata getMetadata() {
		return Metadata.of("berlin", "calibration");
	}

	@Override
	public Config prepareConfig(int id, Params params) {

		SnzBerlinProductionScenario module = new SnzBerlinProductionScenario.Builder().setSnapshot(
				SnzBerlinProductionScenario.Snapshot.episim_snapshot_120_2020_06_14 ).createSnzBerlinProductionScenario();
		Config config = module.config();
		config.global().setRandomSeed(params.seed);

		EpisimConfigGroup episimConfig = ConfigUtils.addOrGetModule(config, EpisimConfigGroup.class);
		episimConfig.setCalibrationParameter(episimConfig.getCalibrationParameter() * params.theta);
		episimConfig.setSnapshotSeed(EpisimConfigGroup.SnapshotSeed.reseed);
		
		episimConfig.setChildInfectivity(episimConfig.getChildInfectivity() * params.childInfectivitySusceptibility);
		episimConfig.setChildSusceptibility(episimConfig.getChildSusceptibility() * params.childInfectivitySusceptibility);

		if (params.winterEnd.equals("fromWeather")) {
			try {
				Map<LocalDate, Double> outdoorFractions = EpisimUtils.getOutdoorFractionsFromWeatherData("berlinWeather.csv", 2, null, null );
				episimConfig.setLeisureOutdoorFraction(outdoorFractions);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		else {
			Map<LocalDate, Double> leisureOutdoorFraction = new TreeMap<>(Map.of(
					LocalDate.parse("2020-01-15"), 0.1,
					LocalDate.parse("2020-04-15"), 0.8,
					LocalDate.parse(params.winterEnd), 0.8,
					LocalDate.parse("2020-11-15"), 0.1,
					LocalDate.parse("2021-02-15"), 0.1,
					LocalDate.parse("2021-04-15"), 0.8,
					LocalDate.parse("2021-09-15"), 0.8)
					);
			episimConfig.setLeisureOutdoorFraction(leisureOutdoorFraction);
		}


		return config;
	}

	public static final class Params {

		@GenerateSeeds(3)
		public long seed;
		
		@Parameter({0.9, 0.95, 1., 1.05, 1.1, 1.15, 1.2})
		double theta;
		
		@Parameter({1., 0.9, 0.8, 0.7, 0.5, 0.})
		double childInfectivitySusceptibility;
		
		@StringParameter({"2020-09-15", "2020-09-01", "2020-08-15", "fromWeather"})
		public String winterEnd;

	}


}

