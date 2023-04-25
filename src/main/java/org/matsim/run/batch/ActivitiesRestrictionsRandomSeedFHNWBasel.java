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
import org.matsim.episim.policy.FixedPolicy;
import org.matsim.run.modules.BaselFHNWScenario25pct2020;

/**
 * @author stefanopenazzi, mesaricr
 *
 */
public class ActivitiesRestrictionsRandomSeedFHNWBasel implements BatchRun<ActivitiesRestrictionsRandomSeedFHNWBasel.Params>  {
	
	@Override
	public Config prepareConfig(int id, Params params) {
		
		Config config = new BaselFHNWScenario25pct2020().config();
		EpisimConfigGroup episimConfig = ConfigUtils.addOrGetModule(config, EpisimConfigGroup.class);
		config.global().setRandomSeed(params.seed);
		
        episimConfig.setInitialInfections(100);
		
        com.typesafe.config.Config policyConf = new BaselFHNWScenario25pct2020.BasePolicyBuilder(episimConfig).buildFixed()
				.restrict("2020-03-16", params.stillGoingToWork, "work")
				.restrict("2020-03-16", params.stillGoingToShopping, "shop","services")
				.restrict("2020-03-16", params.stillDoingLeisureActivities, "leisure")
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
		
		@GenerateSeeds(15)
		long seed;
		
		@Parameter({1.0,0.5,0.0})
		double stillGoingToWork;

		@Parameter({1.0,0.5,0.0})
		double stillGoingToShopping;

		@Parameter({1.0,0.5,0.0})
		double stillDoingLeisureActivities;

//		@Parameter({1.0,0.5,0.0})
//		double stillDoingOtherActivities;

	}


}