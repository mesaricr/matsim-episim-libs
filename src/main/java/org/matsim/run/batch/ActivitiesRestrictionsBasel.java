/**
 * 
 */
package org.matsim.run.batch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.episim.BatchRun;
import org.matsim.episim.EpisimConfigGroup;
import org.matsim.episim.BatchRun.IntParameter;
import org.matsim.episim.BatchRun.Parameter;
import org.matsim.episim.policy.FixedPolicy;
import org.matsim.run.modules.BaselFHNWScenario25pct2020;
//import org.matsim.run.modules.BaselScenario;

/**
 * @author stefanopenazzi, mesaricr
 *
 */
public class ActivitiesRestrictionsBasel implements BatchRun<ActivitiesRestrictionsBasel.Params> {
	
	@Override
	public Config prepareConfig(int id, ActivitiesRestrictionsBasel.Params params) {
		
		String[] DEFAULT_ACTIVITIES = {
		       "transport", "work", "leisure", "education", "shop", "other","outside","services"
		};

		Config config = ConfigUtils.createConfig(new EpisimConfigGroup());
		EpisimConfigGroup episimConfig = ConfigUtils.addOrGetModule(config, EpisimConfigGroup.class);

		//episimConfig.setInputEventsFile("/home/stefanopenazzi/projects/covid19/data/covid_events.xml.gz");
		//episimConfig.setInputEventsFile("/cluster/work/ivt_vpl/mesaricr/Covid19/Basel25pct/data/covid_events.xml.gz");
		episimConfig.setInputEventsFile("/Users/ma1158221/mesaricr_euler/Covid19/Basel25pct/data/covid_events.xml.gz");
		episimConfig.setFacilitiesHandling(EpisimConfigGroup.FacilitiesHandling.bln);
		episimConfig.setSampleSize(0.1);
		episimConfig.setCalibrationParameter(0.000003);
		episimConfig.setInitialInfections(params.initialInfections);
		
		//BaselScenario.addParams(episimConfig);
		BaselFHNWScenario25pct2020.addParams(episimConfig);

		com.typesafe.config.Config policyConf = FixedPolicy.config()
				.restrict(0 + params.offset, params.stillDoingLeisureActivities, "leisure")
				.restrict(0 + params.offset, params.stillGoingToWork, "work")
				.restrict(0 + params.offset, params.stillGoingToShopping, "shop","services")
				.restrict(0 + params.offset, params.stillDoingOtherActivities, "transport","education","other","outside")
				.open(0 + params.offset + 30, DEFAULT_ACTIVITIES)
				.build();

		String policyFileName = "input/policy" + id + ".conf";
		episimConfig.setOverwritePolicyLocation(policyFileName);
		episimConfig.setPolicy(FixedPolicy.class, policyConf);
		return config;
	}

	@Override
	public void writeAuxiliaryFiles(Path directory, Config config) throws IOException {
		EpisimConfigGroup episimConfig = ConfigUtils.addOrGetModule(config, EpisimConfigGroup.class);
		Files.writeString(directory.resolve(episimConfig.getPolicyConfig()), episimConfig.getPolicy().root().render());
	}

	
	public static final class Params {
		
		@IntParameter({30,60,90})
		int initialInfections;
		
		@IntParameter({30})
		int offset;
		
		@Parameter({1.0})
		double stillGoingToWork;

		@Parameter({1.0})
		double stillGoingToShopping;

		@Parameter({1.0})
		double stillDoingLeisureActivities;

		@Parameter({1.0})
		double stillDoingOtherActivities;

	}

}