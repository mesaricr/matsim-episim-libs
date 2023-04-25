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
import org.matsim.episim.policy.FixedPolicy;
import org.matsim.run.modules.BaselFHNWScenario25pct2020;

/**
 * @author stefanopenazzi, mesaricr
 *
 */
public class RandomSeedFHNWBasel implements BatchRun<RandomSeedFHNWBasel.Params>  {
	
	@Override
	public Config prepareConfig(int id, Params params) {
		
		Config config = new BaselFHNWScenario25pct2020().config();
		EpisimConfigGroup episimConfig = ConfigUtils.addOrGetModule(config, EpisimConfigGroup.class);
		com.typesafe.config.Config policyConf = new BaselFHNWScenario25pct2020.BasePolicyBuilder(episimConfig)
				.build()
				.build();
		config.global().setRandomSeed(params.seed);
		
		episimConfig.setInitialInfections(params.initialInfections);
		episimConfig.setCalibrationParameter(params.calibrationParameter);
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
		
		@GenerateSeeds(1)
		long seed;
		
		@IntParameter({200,400,800,2000})
		int initialInfections;
		
		@Parameter({0.0000014,0.0000015,0.0000016,0.0000017,0.0000018})
		double calibrationParameter;

	}

}
