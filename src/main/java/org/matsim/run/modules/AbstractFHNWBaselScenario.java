/**
 *
 */
package org.matsim.run.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.episim.EpisimConfigGroup;
import org.matsim.episim.EpisimPerson;
import org.matsim.episim.model.ProgressionModel;
import org.matsim.episim.model.Transition;
import org.matsim.episim.model.progression.AgeDependentDiseaseStatusTransitionModel;
import org.matsim.episim.model.progression.DiseaseStatusTransitionModel;

import javax.inject.Singleton;

import static org.matsim.episim.model.Transition.to;

/**
 * @author mesaricr
 *
 */
public abstract class AbstractFHNWBaselScenario extends AbstractModule {

	public static final String[] DEFAULT_ACTIVITIES = {
			"pt","tr" ,"work", "leisure", "education_kiga", "education_primary", "education_secondary",
			"education_higher", "shop", "outside", "home", "quarantine_home","other","service",
			"lieferwagen","aussenverkehr_miv","aussenverkehr_oev"
	};

	public static void addParams(EpisimConfigGroup episimConfig) {

		int spaces = 20;		//available number of spaces per facility, used in AbstractContactModel

		//contact intensities
		episimConfig.getOrAddContainerParams("pt", "tr").setContactIntensity(10.0).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("work").setContactIntensity(1.47).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("service").setContactIntensity(1.47).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("leisure").setContactIntensity(9.24).setSpacesPerFacility(spaces).setSeasonal(true);
		episimConfig.getOrAddContainerParams("education_kiga").setContactIntensity(11.0).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("education_primary").setContactIntensity(11.0).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("education_secondary").setContactIntensity(11.0).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("education_higher").setContactIntensity(5.5).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("shop").setContactIntensity(0.88).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("outside").setContactIntensity(0.2).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("home").setContactIntensity(1.0).setSpacesPerFacility(1);
		episimConfig.getOrAddContainerParams("other").setContactIntensity(1.0).setSpacesPerFacility(spaces);
		episimConfig.getOrAddContainerParams("quarantine_home").setContactIntensity(1.0).setSpacesPerFacility(1);
		//episimConfig.getOrAddContainerParams("lieferwagen").setContactIntensity(1.0).setSpacesPerFacility(2);
		//episimConfig.getOrAddContainerParams("aussenverkehr_miv").setContactIntensity(1.0).setSpacesPerFacility(2);
		//episimConfig.getOrAddContainerParams("aussenverkehr_oev").setContactIntensity(10.0).setSpacesPerFacility(spaces);


//		if (locationBasedContactIntensity == LocationBasedContactIntensity.yes) {
//			episimConfig.getOrAddContainerParams("home_15").setContactIntensity(1.47).setSpacesPerFacility(1);
//			episimConfig.getOrAddContainerParams("home_25").setContactIntensity(0.88).setSpacesPerFacility(1);
//			episimConfig.getOrAddContainerParams("home_35").setContactIntensity(0.63).setSpacesPerFacility(1);
//			episimConfig.getOrAddContainerParams("home_45").setContactIntensity(0.49).setSpacesPerFacility(1);
//			episimConfig.getOrAddContainerParams("home_55").setContactIntensity(0.40).setSpacesPerFacility(1);
//			episimConfig.getOrAddContainerParams("home_65").setContactIntensity(0.34).setSpacesPerFacility(1);
//			episimConfig.getOrAddContainerParams("home_75").setContactIntensity(0.29).setSpacesPerFacility(1);
//
//			// weighted average of contact intensities (39 m2 per person)
//			episimConfig.getOrAddContainerParams("home").setContactIntensity(0.56).setSpacesPerFacility(1);
//
//		} else {
//			episimConfig.getOrAddContainerParams("home").setContactIntensity(1.0).setSpacesPerFacility(1); // 33/33
//		}


	}



	/**
	 * Adds base progression config to the given builder.
	 */
	public static Transition.Builder baseProgressionConfig(Transition.Builder builder) {
		return builder

				// Inkubationszeit: Die Inkubationszeit [ ... ] liegt im Mittel (Median) bei 5–6 Tagen (Spannweite 1 bis 14 Tage)
				.from(EpisimPerson.DiseaseStatus.infectedButNotContagious,
						to(EpisimPerson.DiseaseStatus.contagious, Transition.logNormalWithMedianAndStd(3.5, 3.5)))
						//to(EpisimPerson.DiseaseStatus.contagious, Transition.logNormalWithMedianAndStd(4., 4.))) // ETH scenario


				// Dauer Infektiosität:: 	Es wurde geschätzt, dass eine relevante Infektiosität bereits zwei Tage vor Symptombeginn vorhanden ist
				// 							und die höchste Infektiosität am Tag vor dem Symptombeginn liegt
				// Dauer Infektiosität: 	Abstrichproben vom Rachen enthielten vermehrungsfähige Viren bis zum vierten, aus dem Sputum bis zum
				// 							achten Tag nach Symptombeginn
				.from(EpisimPerson.DiseaseStatus.contagious,
						to(EpisimPerson.DiseaseStatus.showingSymptoms, Transition.logNormalWithMedianAndStd(2., 2.)),    //80%
						to(EpisimPerson.DiseaseStatus.recovered, Transition.logNormalWithMedianAndStd(4., 4.)))            //20%


				// Erkankungsbeginn -> Hospitalisierung: Eine Studie aus Deutschland zu 50 Patienten mit eher schwereren Verläufen berichtete für
				// 		alle Patienten eine mittlere (Median) Dauer von vier Tagen (IQR: 1–8 Tage)
				.from(EpisimPerson.DiseaseStatus.showingSymptoms,
						to(EpisimPerson.DiseaseStatus.seriouslySick, Transition.logNormalWithMedianAndStd(4., 4.)),
						//to(EpisimPerson.DiseaseStatus.seriouslySick, Transition.logNormalWithMedianAndStd(5., 5.)), // ETH scenario
						to(EpisimPerson.DiseaseStatus.recovered, Transition.logNormalWithMedianAndStd(8., 8.)))


				// Hospitalisierung -> ITS: In einer chinesischen Fallserie betrug diese Zeitspanne im Mittel (Median) einen Tag (IQR: 0–3 Tage)
				.from(EpisimPerson.DiseaseStatus.seriouslySick,
						to(EpisimPerson.DiseaseStatus.critical, Transition.logNormalWithMedianAndStd(1., 1.)),
						to(EpisimPerson.DiseaseStatus.recovered, Transition.logNormalWithMedianAndStd(14., 14.)))


				// Dauer des Krankenhausaufenthalts: „WHO-China Joint Mission on Coronavirus Disease 2019“ wird berichtet, dass milde Fälle im
				// 		Mittel (Median) einen Krankheitsverlauf von zwei Wochen haben und schwere Fälle von 3–6 Wochen
				.from(EpisimPerson.DiseaseStatus.critical,
						to(EpisimPerson.DiseaseStatus.seriouslySickAfterCritical, Transition.logNormalWithMedianAndStd(21., 21.)))

				.from(EpisimPerson.DiseaseStatus.seriouslySickAfterCritical,
						to(EpisimPerson.DiseaseStatus.recovered, Transition.logNormalWithMedianAndStd(7., 7.)))

				.from(EpisimPerson.DiseaseStatus.recovered,
						to(EpisimPerson.DiseaseStatus.susceptible, Transition.logNormalWithMean(360, 15)))

				;

		// yyyy Quellen für alle Aussagen oben??  "Es" oder "Eine Studie aus ..." ist mir eigentlich nicht genug.  kai, aug'20
		// yyyy Der obige Code existiert nochmals in ConfigurableProgressionModel.  Können wir in konsolidieren?  kai, oct'20

	}

	/*
	@Override
	protected void configure() {

		// Use age dependent progression model
		bind(DiseaseStatusTransitionModel.class).to(AgeDependentDiseaseStatusTransitionModel.class).in(Singleton.class);
	}*/

	/**
	 * Provider method that needs to be overwritten to generate fully configured scenario.
	 * Needs to be annotated with {@link Provides} and {@link Singleton}
	 */
	public abstract Config config();

	/**
	 * Creates a config with the default settings for all Basel scenarios.
	 */
	protected static Config getBaseConfig() {

		Config config = ConfigUtils.createConfig(new EpisimConfigGroup());
		EpisimConfigGroup episimConfig = ConfigUtils.addOrGetModule(config, EpisimConfigGroup.class);
		episimConfig.setFacilitiesHandling(EpisimConfigGroup.FacilitiesHandling.bln);
		addParams(episimConfig);
		return config;
	}

}
