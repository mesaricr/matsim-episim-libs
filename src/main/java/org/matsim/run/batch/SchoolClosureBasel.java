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
import org.matsim.episim.BatchRun.Parameter;
import org.matsim.episim.policy.FixedPolicy;
//import org.matsim.run.modules.SwitzerlandScenario;

/**
 * @author stefanopenazzi, mesaricr
 *
 */
public class SchoolClosureBasel implements BatchRun<SchoolClosureBasel.Params>  {
	
	@Override
	public Config prepareConfig(int id, SchoolClosureBasel.Params params) {
		
		String[] DEFAULT_ACTIVITIES = {
			       "transport", "work", "leisure", "shop", "other","outside","services"
			};

		Config config = ConfigUtils.createConfig(new EpisimConfigGroup());
		EpisimConfigGroup episimConfig = ConfigUtils.addOrGetModule(config, EpisimConfigGroup.class);

		//episimConfig.setInputEventsFile(inputPath+"/covid_events.xml.gz");
		episimConfig.setFacilitiesHandling(EpisimConfigGroup.FacilitiesHandling.snz);

		episimConfig.setSampleSize(0.1);
		episimConfig.setCalibrationParameter(0.000003);
		//episimConfig.setInitialInfections(4);

		//SwitzerlandScenario.addParams(episimConfig);
		//SwitzerlandScenario.setContactIntensities(episimConfig);

		com.typesafe.config.Config policyConf = FixedPolicy.config()
				.restrict(11 , 0.2, "leisure","outside")
				.restrict(11 , 0.1, "education_primary", "education_kiga")
				.restrict(11 , 0.0, "education_secondary", "education_higher")
				.restrict(11 , 0.6, "work")
				.restrict(11 , 0.2, "shop","services")
				.restrict(11 , 0.5, "other")
				.restrict(62 , 0.4 , "shop","services")
				.restrict(62 , 1.0 , "outside")
				.restrict(62 , 0.8, "work")
				.restrict(76 , params.remainingFractionKiga, "education_kiga")
				.restrict(76 , params.remainingFractionPrima, "education_primary")
				.restrict(76 , params.remainingFractionSecon, "education_secondary")
				.restrict(76 , 1.0, "shop","services")
				.restrict(76 , 0.6, "leisure")
				.restrict(104 , params.remainingFractionUni, "education_higher")
				.open(104, DEFAULT_ACTIVITIES)
				.build();

		String policyFileName = "input/policy" + id + ".conf";
		episimConfig.setOverwritePolicyLocation(policyFileName);
		episimConfig.setPolicy(FixedPolicy.class, policyConf);

		//config.plans().setInputFile(inputPath+"/output_plans.xml.gz");

		return config;
	}

	@Override
	public void writeAuxiliaryFiles(Path directory, Config config) throws IOException {
		EpisimConfigGroup episimConfig = ConfigUtils.addOrGetModule(config, EpisimConfigGroup.class);
		Files.writeString(directory.resolve(episimConfig.getPolicyConfig()), episimConfig.getPolicy().root().render());
	}

	public static final class Params {

//		@IntParameter({-5, 5})
//		int offset;

		@Parameter({1.0,0.5 ,0.1})
		double remainingFractionKiga;

		@Parameter({1.0,0.5 ,0.1})
		double remainingFractionPrima;

		@Parameter({1.0,0.5 ,0.0})
		double remainingFractionSecon;
		
		@Parameter({1.0,0.5 ,0.0})
		double remainingFractionUni;

//		@Parameter({0.4, 0.2, 0})
//		double remainingFractionLeisure;

//		@Parameter({0.8, 0.4, 0.2})
//		double remainingFractionWork;

//		@Parameter({0.4, 0.2})
//		double remainingFractionShoppingBusinessErrands;

	}

}

