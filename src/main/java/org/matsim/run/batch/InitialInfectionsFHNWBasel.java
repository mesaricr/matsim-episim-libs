/**
 * 
 */
package org.matsim.run.batch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.episim.BatchRun;
import org.matsim.episim.EpisimConfigGroup;
import org.matsim.run.modules.BaselFHNWScenario25pct2020;

/**
 * @author stefanopenazzi, mesaricr
 *
 */
public class InitialInfectionsFHNWBasel implements BatchRun<InitialInfectionsFHNWBasel.Params>  {
	
	@Override
	public Config prepareConfig(int id, Params params) {
		
		Config config = null; //new BaselFHNWScenario25pct2020().config();
		EpisimConfigGroup episimConfig = ConfigUtils.addOrGetModule(config, EpisimConfigGroup.class);
		episimConfig.setInitialInfections(params.initialInfections);
		Map<LocalDate, Integer> infectionsPerDay0 = new HashMap<LocalDate, Integer>();
		infectionsPerDay0.put(episimConfig.getStartDate().minusDays(2),params.initialInfections);
		episimConfig.setInfections_pers_per_day(infectionsPerDay0);

		return config;
	}

	@Override
	public void writeAuxiliaryFiles(Path directory, Config config) throws IOException {
		EpisimConfigGroup episimConfig = ConfigUtils.addOrGetModule(config, EpisimConfigGroup.class);
		Files.writeString(directory.resolve(episimConfig.getPolicyConfig()), episimConfig.getPolicy().root().render());
	}

	public static final class Params {
		
//		@IntParameter({20,40,60,80,100,120,140,160,180,200,220,240,260,280,300,320,340,360,380,400,420,440,460,480,500,520,540,560,580,600,620,640,660,680,700,720,740,760,780,800,
//			820,840,860,880,900,920,940,960,980,1000})
//		int initialInfections;
		
		@IntParameter({100,200,400})
		int initialInfections;

	}


}
