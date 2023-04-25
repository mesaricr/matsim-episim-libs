/**
 *
 */
package org.matsim.run.modules;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import javax.inject.Singleton;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.episim.EpisimConfigGroup;
import org.matsim.episim.EpisimUtils;
import org.matsim.episim.TracingConfigGroup;
import org.matsim.core.config.groups.VspExperimentalConfigGroup;
import org.matsim.core.controler.ControlerUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.episim.*;
import org.matsim.episim.TracingConfigGroup.CapacityType;
import org.matsim.episim.model.*;
import org.matsim.episim.model.AgeDependentInfectionModelWithSeasonality;
import org.matsim.episim.model.ContactModel;
import org.matsim.episim.model.FaceMask;
import org.matsim.episim.model.InfectionModel;
import org.matsim.episim.model.InitialInfectionHandler;
import org.matsim.episim.model.ProgressionModel;
import org.matsim.episim.model.RandomInitialInfectionsBaselPerCanton;
import org.matsim.episim.model.SymmetricContactModel;
import org.matsim.episim.model.Transition;
import org.matsim.episim.model.VaccinationByAgeBasel;
import org.matsim.episim.model.VaccinationModel;
import org.matsim.episim.model.VirusStrain;

import org.matsim.episim.model.activity.ActivityParticipationModel;
import org.matsim.episim.model.activity.DefaultParticipationModel;
import org.matsim.episim.model.activity.LocationBasedParticipationModel;
import org.matsim.episim.model.input.CreateAdjustedRestrictionsFromCSV;
import org.matsim.episim.model.input.CreateRestrictionsFromCSV;
import org.matsim.episim.model.input.RestrictionInput;
import org.matsim.episim.model.progression.AgeDependentDiseaseStatusTransitionModel;
import org.matsim.episim.model.progression.AgeAndICUDependentDiseaseStatusTransitionModel;
import org.matsim.episim.model.progression.DiseaseStatusTransitionModel;
import org.matsim.episim.policy.*;
import org.matsim.episim.policy.Restriction;
import org.matsim.episim.policy.FixedPolicy;
import org.matsim.episim.policy.FixedPolicy.ConfigBuilder;
import org.matsim.episim.policy.ShutdownPolicy;
import org.matsim.episim.policy.DiseaseImportClass;
import org.matsim.run.modules.SnzBerlinProductionScenario.Tracing;
import org.matsim.vehicles.VehicleType;
import com.google.inject.Provides;

/**
 * @author stefanopenazzi
 *
 */
public final class BaselFHNWScenario25pct2020 extends AbstractFHNWBaselScenario {



	public static class Builder {
		private int importOffset = 0;
		private int sample = 25;
		private DiseaseImport diseaseImport = DiseaseImport.yes;
		private Restrictions restrictions = Restrictions.yes;
		private AdjustRestrictions adjustRestrictions = AdjustRestrictions.no;
		private Masks masks = Masks.yes;
		private Tracing tracing = Tracing.yes;
		private Vaccinations vaccinations = Vaccinations.yes;
		private ChristmasModel christmasModel = ChristmasModel.restrictive;
		private EasterModel easterModel = EasterModel.no;
		private WeatherModel weatherModel = WeatherModel.midpoints_175_250;
		private Snapshot snapshot = Snapshot.no;
		private EpisimConfigGroup.ActivityHandling activityHandling = EpisimConfigGroup.ActivityHandling.startOfDay;
		private Class<? extends InfectionModel> infectionModel = AgeAndProgressionDependentInfectionModelWithSeasonality.class;
		private Class<? extends VaccinationModel> vaccinationModel = VaccinationByAgeBasel.class;

		/* To be excluded */
		private double imprtFctMult = 1.;
		private double importFactorBeforeJune = 4.;
		private double importFactorAfterJune = 0.5;
		/* */

		private EpisimConfigGroup.DistrictLevelRestrictions locationBasedRestrictions = EpisimConfigGroup.DistrictLevelRestrictions.yesForActivityLocation;
		private LocationBasedContactIntensity locationBasedContactIntensity = LocationBasedContactIntensity.no;
		private AdaptiveRestrictions adaptiveRestrictions = AdaptiveRestrictions.no;

		/* To be excluded */
		public Builder setImportFactorBeforeJune(double importFactorBeforeJune) {
			this.importFactorBeforeJune = importFactorBeforeJune;
			return this;
		}

		public Builder setImportFactorAfterJune(double importFactorAfterJune) {
			this.importFactorAfterJune = importFactorAfterJune;
			return this;
		}
		/* */

		public Builder setSample( int sample ) {
			this.sample = sample;
			return this;
		}
		public Builder setDiseaseImport( DiseaseImport diseaseImport ) {
			this.diseaseImport = diseaseImport;
			return this;
		}
		public Builder setRestrictions( Restrictions restrictions ) {
			this.restrictions = restrictions;
			return this;
		}
		public Builder setAdjustRestrictions( AdjustRestrictions adjustRestrictions ) {
			this.adjustRestrictions = adjustRestrictions;
			return this;
		}
		public Builder setMasks( Masks masks ) {
			this.masks = masks;
			return this;
		}
		public Builder setTracing( Tracing tracing ) {
			this.tracing = tracing;
			return this;
		}
		public Builder setVaccinations( Vaccinations vaccinations ) {
			this.vaccinations = vaccinations;
			return this;
		}

		public Builder setChristmasModel( ChristmasModel christmasModel ) {
			this.christmasModel = christmasModel;
			return this;
		}

		public Builder setEasterModel( EasterModel easterModel ) {
			this.easterModel = easterModel;
			return this;
		}

		public Builder setWeatherModel( WeatherModel weatherModel ) {
			this.weatherModel = weatherModel;
			return this;
		}
		public Builder setSnapshot( Snapshot snapshot ) {
			this.snapshot = snapshot;
			return this;
		}
		public Builder setInfectionModel( Class<? extends InfectionModel> infectionModel ) {
			this.infectionModel = infectionModel;
			return this;
		}
		public Builder setVaccinationModel( Class<? extends VaccinationModel> vaccinationModel ) {
			this.vaccinationModel = vaccinationModel;
			return this;
		}
		public Builder setActivityHandling( EpisimConfigGroup.ActivityHandling activityHandling ) {
			this.activityHandling = activityHandling;
			return this;
		}
		public BaselFHNWScenario25pct2020 createBaselFHNWScenario25pct2020Scenario() {
			return new BaselFHNWScenario25pct2020( this );
		}
		public Builder setImportOffset( int importOffset ) {
			this.importOffset = importOffset;
			return this;
		}
		public Builder setImportFactor( double imprtFctMult ) {
			this.imprtFctMult = imprtFctMult;
			return this;
		}

		public Builder setLocationBasedRestrictions( EpisimConfigGroup.DistrictLevelRestrictions locationBasedRestrictions ) {
			this.locationBasedRestrictions = locationBasedRestrictions;
			return this;
		}

		public Builder setLocationBasedContactIntensity( LocationBasedContactIntensity localCI ) {
			this.locationBasedContactIntensity = localCI;
			return this;
		}

		public Builder setAdaptiveRestrictions( AdaptiveRestrictions adaptiveRestrictions ) {
			this.adaptiveRestrictions = adaptiveRestrictions;
			return this;
		}
	}

	public final static class DynamicProgressionModelFactors {

		private static int iteration = 0;

		//https://en.wikipedia.org/wiki/Generalised_logistic_function
		public static double getICUFactor() {
			double A  = 1;
			double K = 0.35;
			double C = 1;
			double Q = 1;
			double B = 0.15;
			double V = 0.9;
			double M = 40;
			//generalized logistic function
			double genlogHospitalFactor = A + ((K-A)/(Math.pow((C+Q*(Math.pow(Math.E,-B*(iteration-M)))),1/V)));
			iteration++ ;
			return genlogHospitalFactor;
		}


	}
//------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Path pointing to the input folder. Can be configured at runtime with EPISIM_INPUT variable.
	 */
	//public static Path INPUT = EpisimUtils.resolveInputPath("/cluster/work/ivt_vpl/spenazzi/Covid19/Switzerland25pct/data");
	public static Path INPUT = EpisimUtils.resolveInputPath("/cluster/work/ivt_vpl/mesaricr/Covid19/Basel25pct/data");

	public static enum DiseaseImport {yes, onlySpring, no}
	public static enum Restrictions {yes, no, onlyEdu, allExceptSchoolsAndDayCare, allExceptUniversities, allExceptEdu}
	public static enum Masks {yes, no}
	public static enum Tracing {yes, no}
	public static enum Vaccinations {yes, no}
	public static enum Snapshot {no, episim_snapshot_060_2020_04_24, episim_snapshot_120_2020_06_23}
	public static enum ChristmasModel {no, restrictive, permissive}
	public static enum WeatherModel {no, midpoints_175_175, midpoints_175_250, midpoints_200_250, midpoints_175_200, midpoints_200_200}
	public static enum AdjustRestrictions {yes, no}
	public static enum EasterModel {yes, no}
	public static enum LocationBasedContactIntensity {yes, no}
	public static enum AdaptiveRestrictions {yesGlobal, yesLocal, no}

	private final int sample;
	private final int importOffset;
	private final DiseaseImport diseaseImport;
	private final Restrictions restrictions;
	private final AdjustRestrictions adjustRestrictions;
	private final Masks masks;
	private final Tracing tracing;
	private final Snapshot snapshot;
	private final Vaccinations vaccinations;
	private final ChristmasModel christmasModel;
	private final EasterModel easterModel;
	private final WeatherModel weatherModel;
	private final Class<? extends InfectionModel> infectionModel;
	private final Class<? extends VaccinationModel> vaccinationModel;
	private final EpisimConfigGroup.ActivityHandling activityHandling;

	private final double imprtFctMult;
	private final double importFactorBeforeJune;
	private final double importFactorAfterJune;
	private final EpisimConfigGroup.DistrictLevelRestrictions locationBasedRestrictions;
	private final LocationBasedContactIntensity locationBasedContactIntensity;
	private final AdaptiveRestrictions adaptiveRestrictions;


	/**
	 * Empty constructor is needed for running scenario from command line.
	 */
	public BaselFHNWScenario25pct2020() {
		this(new Builder());
	}

	public BaselFHNWScenario25pct2020(Builder builder) {
		this.sample = builder.sample;
		this.diseaseImport = builder.diseaseImport;
		this.restrictions = builder.restrictions;
		this.adjustRestrictions = builder.adjustRestrictions;
		this.masks = builder.masks;
		this.tracing = builder.tracing;
		this.snapshot = builder.snapshot;
		this.activityHandling = builder.activityHandling;
		this.infectionModel = builder.infectionModel;
		this.importOffset = builder.importOffset;
		this.vaccinationModel = builder.vaccinationModel;

		this.vaccinations = builder.vaccinations;
		this.christmasModel = builder.christmasModel;
		this.weatherModel = builder.weatherModel;

		/* To be excluded */
		this.imprtFctMult = builder.imprtFctMult;
		this.importFactorBeforeJune = builder.importFactorBeforeJune;
		this.importFactorAfterJune = builder.importFactorAfterJune;
		/*  */

		this.easterModel = builder.easterModel;
		this.locationBasedRestrictions = builder.locationBasedRestrictions;
		this.locationBasedContactIntensity = builder.locationBasedContactIntensity;
		this.adaptiveRestrictions = builder.adaptiveRestrictions;
	}

	public static void interpolateImport(Map<LocalDate, Integer> importMap, double importFactor, LocalDate start, LocalDate end, double a, double b) {
		int days = end.getDayOfYear() - start.getDayOfYear();
		for (int i = 1; i <= days; i++) {
			double fraction = (double) i / days;
			importMap.put(start.plusDays(i), (int) Math.round(importFactor * (a + fraction * (b - a))));
		}
	}


	public static void diseaseImportFunction(Map<LocalDate, Integer> importMap, Path diseaseImportPath, double importFactor) throws IOException {
		try (BufferedReader in = Files.newBufferedReader(diseaseImportPath)) {

			CSVParser parser = CSVFormat.RFC4180.withFirstRecordAsHeader().withDelimiter('\t').parse(in);
			DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd");

			for (CSVRecord record : parser) {
				LocalDate date = LocalDate.parse(record.get(0), fmt);

				int value = Integer.parseInt(record.get(1));
				importMap.put(date, (int) Math.round(importFactor * value));
			}

		}

	}

	/**
	 * Resolve input for sample size. Smaller than 25pt samples are in a different subfolder.
	 */
	/*private static String inputForSample(String base, int sample) {
		Path folder = (sample == 100 | sample == 25) ? INPUT : INPUT.resolve("samples");
		return folder.resolve(String.format(base, sample)).toString();
	}*/

	@Override
	protected void configure() {
		bind(ContactModel.class).to(SymmetricContactModel.class).in(Singleton.class);
		bind(DiseaseStatusTransitionModel.class).to(AgeAndICUDependentDiseaseStatusTransitionModel.class).in(Singleton.class);
		bind(InfectionModel.class).to(infectionModel).in(Singleton.class);
		bind(VaccinationModel.class).to(vaccinationModel).in(Singleton.class);
		bind(InitialInfectionHandler.class).to(RandomInitialInfectionsBaselPerCanton.class).in(Singleton.class);

		if (adjustRestrictions == AdjustRestrictions.yes) {
			bind(ShutdownPolicy.class).to(AdjustedPolicy.class).in(Singleton.class);
			if (adaptiveRestrictions != AdaptiveRestrictions.no) {
				throw (new RuntimeException("adjust restrictions & adaptive restrictions cannot be turned on simultaneously"));
			}
		} else if (adaptiveRestrictions != AdaptiveRestrictions.no) {
			bind(ShutdownPolicy.class).to(AdaptivePolicy.class).in(Singleton.class);
		} else {
			bind(ShutdownPolicy.class).to(FixedPolicy.class).in(Singleton.class);
		}


		if (activityHandling == EpisimConfigGroup.ActivityHandling.startOfDay){
			bind(ActivityParticipationModel.class).to(LocationBasedParticipationModel.class);
		}
	}

	/**
	 * The base policy based on actual restrictions in the past and mobility data
	 */
	private static ShutdownPolicy.ConfigBuilder<?> basePolicy(RestrictionInput activityParticipation, Map<String, Double> ciCorrections,
															  long introductionPeriod, Double maskCompliance, boolean restrictSchoolsAndDayCare,
															  boolean restrictUniversities) throws IOException {

		// note that there is already a builder around this
		ConfigBuilder restrictions; // = ShutdownPolicy.config();

		// adjusted restrictions must be created after policy was set, currently there is no nicer way to do this
		if (activityParticipation == null || activityParticipation instanceof CreateAdjustedRestrictionsFromCSV) {
			restrictions = FixedPolicy.config();
		} else
			restrictions = (ConfigBuilder) activityParticipation.createPolicy();


		for (Map.Entry<String, Double> e : ciCorrections.entrySet()) {
			String date = e.getKey();
			Double ciCorrection = e.getValue();
			restrictions.restrict(date, Restriction.ofCiCorrection(ciCorrection), DEFAULT_ACTIVITIES);
//			restrictions.restrict(date, Restriction.ofCiCorrection(ciCorrection), "pt");
//			restrictions.restrict(date, Restriction.ofCiCorrection(ciCorrection), "tr");
//			restrictions.restrict(date, Restriction.ofCiCorrection(ciCorrection), "work");
//			restrictions.restrict(date, Restriction.ofCiCorrection(ciCorrection), "shop");
//			restrictions.restrict(date, Restriction.ofCiCorrection(ciCorrection), "leisure");
//			restrictions.restrict(date, Restriction.ofCiCorrection(ciCorrection), "other");
		}

		restrictions.restrict(LocalDate.parse("2020-03-16") , 0.1, "education_secondary", "education_higher","education_primary", "education_kiga")
		    .restrict(LocalDate.parse("2020-03-18") , 0.72, "work")
		  	.restrict(LocalDate.parse("2020-03-18") , 0.59, "shop","leisure","other")
			.restrict(LocalDate.parse("2020-03-25") , 0.53, "work")
			.restrict(LocalDate.parse("2020-03-25") , 0.28, "shop","leisure","other")
			.restrict(LocalDate.parse("2020-04-01") , 0.52, "work")
			.restrict(LocalDate.parse("2020-04-01") , 0.24, "shop","leisure","other")
			.restrict(LocalDate.parse("2020-04-08") , 0.53, "work")
			.restrict(LocalDate.parse("2020-04-08") , 0.22, "shop","leisure","other")
			.restrict(LocalDate.parse("2020-04-15") , 0.43, "work")
			.restrict(LocalDate.parse("2020-04-15") , 0.22, "shop","leisure","other")
	        .restrict(LocalDate.parse("2020-04-22") , 0.58, "work")
			.restrict(LocalDate.parse("2020-04-22") , 0.27, "shop","leisure","other")
	        .restrict(LocalDate.parse("2020-04-29") , 0.61, "work")
			.restrict(LocalDate.parse("2020-04-29") , 0.32, "shop","leisure","other")
	        .restrict(LocalDate.parse("2020-05-06") , 0.62, "work")
			.restrict(LocalDate.parse("2020-05-06") , 0.37, "shop","leisure","other")
	        .restrict(LocalDate.parse("2020-05-11") , 0.7, "education_secondary", "education_higher","education_primary", "education_kiga")
			.restrict(LocalDate.parse("2020-05-13") , 0.72, "work")
			.restrict(LocalDate.parse("2020-05-13") , 0.52, "shop","leisure","other")
			.restrict(LocalDate.parse("2020-05-20") , 0.77, "work")
			.restrict(LocalDate.parse("2020-05-20") , 0.70, "shop","leisure","other")
			.restrict(LocalDate.parse("2020-05-27") , 0.66, "work")
			.restrict(LocalDate.parse("2020-05-27") , 0.72, "shop","leisure","other")
	        .restrict(LocalDate.parse("2020-06-03") , 0.75, "work")
			.restrict(LocalDate.parse("2020-06-03") , 0.76, "shop","leisure","other")
			.restrict(LocalDate.parse("2020-06-10") , 0.83, "work")
			.restrict(LocalDate.parse("2020-06-10") , 0.78, "shop","leisure","other")
			.restrict(LocalDate.parse("2020-06-17") , 0.81, "work")
			.restrict(LocalDate.parse("2020-06-17") , 0.81, "shop","leisure","other")
	        .restrict(LocalDate.parse("2020-06-24") , 0.84, "work")
			.restrict(LocalDate.parse("2020-06-24") , 0.86, "shop","leisure","other")
	        .restrict(LocalDate.parse("2020-07-01") , 0.85, "work")
			.restrict(LocalDate.parse("2020-07-01") , 0.90, "shop","leisure","other")
	        .restrict(LocalDate.parse("2020-07-08") , 0.83, "work")
			.restrict(LocalDate.parse("2020-07-08") , 0.92, "shop","leisure","other")
	        .restrict(LocalDate.parse("2020-07-15") , 0.79, "work")
			.restrict(LocalDate.parse("2020-07-15") , 0.90, "shop","leisure","other")
	        .restrict(LocalDate.parse("2020-07-22") , 0.76, "work")
			.restrict(LocalDate.parse("2020-07-22") , 0.89, "shop","leisure","other")
	        .restrict(LocalDate.parse("2020-07-29") , 0.72, "work")
			.restrict(LocalDate.parse("2020-07-29") , 0.90, "shop","leisure","other")
	        .restrict(LocalDate.parse("2020-08-05") , 0.66, "work")
			.restrict(LocalDate.parse("2020-08-05") , 0.83, "shop","leisure","other")
	        .restrict(LocalDate.parse("2020-08-12") , 0.75, "work")
			.restrict(LocalDate.parse("2020-08-12") , 0.88, "shop","leisure","other")
	        .restrict(LocalDate.parse("2020-08-19") , 0.79, "work")
			.restrict(LocalDate.parse("2020-08-19") , 0.89, "shop","leisure","other")
	        .restrict(LocalDate.parse("2020-08-26") , 0.82, "work")
			.restrict(LocalDate.parse("2020-08-26") , 0.90, "shop","leisure","other")
	        .restrict(LocalDate.parse("2020-09-02") , 0.81, "work")
			.restrict(LocalDate.parse("2020-09-02") , 0.87, "shop","leisure","other")
            .restrict(LocalDate.parse("2020-09-11") , 1.0, "education_secondary", "education_higher","education_primary", "education_kiga")
            .restrict(LocalDate.parse("2020-09-09") , 0.82, "work")
			.restrict(LocalDate.parse("2020-09-09") , 0.90, "shop","leisure","other")
            .restrict(LocalDate.parse("2020-09-16") , 0.84, "work")
			.restrict(LocalDate.parse("2020-09-16") , 0.91, "shop","leisure","other")
            .restrict(LocalDate.parse("2020-09-23") , 0.83, "work")
			.restrict(LocalDate.parse("2020-09-23") , 0.88, "shop","leisure","other")
            .restrict(LocalDate.parse("2020-09-30") , 0.83, "work")
			.restrict(LocalDate.parse("2020-09-30") , 0.87, "shop","leisure","other")
			.restrict(LocalDate.parse("2020-10-07") , 0.81, "work")
			.restrict(LocalDate.parse("2020-10-07") , 0.88, "shop","leisure","other")
            .restrict(LocalDate.parse("2020-10-14") , 0.81, "work")
			.restrict(LocalDate.parse("2020-10-14") , 0.85, "shop","leisure","other")
            .restrict(LocalDate.parse("2020-10-21") , 0.82, "work")
			.restrict(LocalDate.parse("2020-10-21") , 0.81, "shop","leisure","other")
            .restrict(LocalDate.parse("2020-10-28") , 0.82, "work")
			.restrict(LocalDate.parse("2020-10-28") , 0.79, "shop","leisure","other")
            .restrict(LocalDate.parse("2020-11-04") , 0.81, "work")
			.restrict(LocalDate.parse("2020-11-04") , 0.74, "shop","leisure","other")
            .restrict(LocalDate.parse("2020-11-11") , 0.79, "work")
			.restrict(LocalDate.parse("2020-11-11") , 0.69, "shop","leisure","other")
            .restrict(LocalDate.parse("2020-11-18") , 0.81, "work")
			.restrict(LocalDate.parse("2020-11-18") , 0.70, "shop","leisure","other")
            .restrict(LocalDate.parse("2020-11-25") , 0.81, "work")
			.restrict(LocalDate.parse("2020-11-25") , 0.70, "shop","leisure","other")
            .restrict(LocalDate.parse("2020-12-02") , 0.83, "work")
			.restrict(LocalDate.parse("2020-12-02") , 0.75, "shop","leisure","other")
            .restrict(LocalDate.parse("2020-12-09") , 0.81, "work")
			.restrict(LocalDate.parse("2020-12-09") , 0.71, "shop","leisure","other")
            .restrict(LocalDate.parse("2020-12-16") , 0.83, "work")
			.restrict(LocalDate.parse("2020-12-16") , 0.75, "shop","leisure","other")
            .restrict(LocalDate.parse("2020-12-23") , 0.75, "work")
			.restrict(LocalDate.parse("2020-12-23") , 0.81, "shop","leisure","other")
            .restrict(LocalDate.parse("2020-12-30") , 0.75, "work")
			.restrict(LocalDate.parse("2020-12-30") , 0.81, "shop","leisure","other")
            .restrict(LocalDate.parse("2021-01-06") , 0.55, "work")
			.restrict(LocalDate.parse("2021-01-06") , 0.46, "shop","leisure","other")
            .restrict(LocalDate.parse("2021-01-13") , 0.76, "work")
			.restrict(LocalDate.parse("2021-01-13") , 0.55, "shop","leisure","other")
            .restrict(LocalDate.parse("2021-01-20") , 0.74, "work")
			.restrict(LocalDate.parse("2021-01-20") , 0.49, "shop","leisure","other")
            .restrict(LocalDate.parse("2021-01-27") , 0.74, "work")
			.restrict(LocalDate.parse("2021-01-27") , 0.46, "shop","leisure","other")
            .restrict(LocalDate.parse("2021-02-03") , 0.74, "work")
			.restrict(LocalDate.parse("2021-02-03") , 0.46, "shop","leisure","other")
            .restrict(LocalDate.parse("2021-02-10") , 0.74, "work")
			.restrict(LocalDate.parse("2021-02-10") , 0.46, "shop","leisure","other")
            .restrict(LocalDate.parse("2021-02-17") , 0.73, "work")
			.restrict(LocalDate.parse("2021-02-17") , 0.50, "shop","leisure","other");


		//MASKS

		LocalDate masksFirstDay = LocalDate.of(2020, 3, 20);
		int introductionPeriodMask = 160;
		int midpointPt = 80;
		int midpointShop = 80;
		int midpointWork = 80;
		int midpointLeisure = 80;
		double maxvalueMaskPt = 0.95;
		double maxvalueMaskShop = 0.95;
		double maxvalueMaskWork = 0.55;
		double maxvalueMaskLeisure = 0.15;
		double logGrowthRatePt = 0.05;
		double logGrowthRateShop = 0.07;
		double logGrowthRateWork = 0.07;
		double logGrowthRateLeisure = 0.07;
		for (int ii = 0; ii <= introductionPeriodMask; ii++) {
			  LocalDate date = masksFirstDay.plusDays(ii);
			  double logResPt = maxvalueMaskPt/(1+Math.pow(Math.E,-logGrowthRatePt*(ii- midpointPt)));
			  double logResShop = maxvalueMaskShop/(1+Math.pow(Math.E,-logGrowthRateShop*(ii- midpointShop)));
			  double logResWork = maxvalueMaskWork/(1+Math.pow(Math.E,-logGrowthRateWork*(ii- midpointWork)));
			  double logResLeisure = maxvalueMaskLeisure/(1+Math.pow(Math.E,-logGrowthRateLeisure*(ii- midpointLeisure)));
			  restrictions.restrict(date, Restriction.ofMask(Map.of(FaceMask.CLOTH, logResPt * 0.9, FaceMask.SURGICAL, logResPt * 0.1)), "pt","tr");
			  restrictions.restrict(date, Restriction.ofMask(Map.of(FaceMask.CLOTH, logResShop * 0.9, FaceMask.SURGICAL, logResShop * 0.1)), "shop","service");
			  restrictions.restrict(date, Restriction.ofMask(Map.of(FaceMask.CLOTH, logResWork * 0.9, FaceMask.SURGICAL, logResWork * 0.1)), "work");
			  restrictions.restrict(date, Restriction.ofMask(Map.of(FaceMask.CLOTH, logResLeisure * 0.9, FaceMask.SURGICAL, logResLeisure * 0.1)), "leisure","other");
		}
		restrictions.restrict("2020-06-07", Restriction.ofMask(Map.of(FaceMask.CLOTH, 0.95 * 0.9, FaceMask.SURGICAL, 0.95 * 0.1)), "pt","tr");
		restrictions.restrict("2020-08-26", Restriction.ofMask(Map.of(FaceMask.CLOTH, 0.95 * 0.9, FaceMask.SURGICAL, 0.95 * 0.1)), "pt","tr" ,"shop","service");

		return restrictions;
	}



	@Provides
	@Singleton
	public Config config() {

		if (this.sample != 25) throw new RuntimeException("Sample size not calibrated! Currently only 25% is calibrated. Comment this line out to continue.");

		Config config = getBaseConfig();
		//config.vehicles().setVehiclesFile(INPUT.resolve("output_vehicles.xml.gz").toString());
		//config.global().setRandomSeed(5825L);

		//config.global().setRandomSeed(100L);
		//config.global().setRandomSeed(200L);
		//config.global().setRandomSeed(300L);
		//config.global().setRandomSeed(400L);
		//config.global().setRandomSeed(500L);
		//config.global().setRandomSeed(600L);
		//config.global().setRandomSeed(700L);
		config.global().setRandomSeed(800L);


		EpisimConfigGroup episimConfig = ConfigUtils.addOrGetModule(config, EpisimConfigGroup.class);
		episimConfig.setInputEventsFile(INPUT.resolve("covid_events.xml.gz").toString());
		config.plans().setInputFile(INPUT.resolve("output_plans.xml.gz").toString());

		//episimConfig.setInitialInfections(1800);
		//episimConfig.setInitialInfectionDistrict("Ticino");

		episimConfig.setSampleSize(0.25);
		episimConfig.setCalibrationParameter(2.5E-5);                 //Calibration parameter
		episimConfig.setMaxContacts(3);                               //Calibration parameter
		String startDate = "2020-02-20";
		episimConfig.setStartDate(startDate);
		episimConfig.setHospitalFactor(0.22);                         //Calibration parameter
		episimConfig.setProgressionConfig(baseProgressionConfig(Transition.config()).build());


		// todo: use the plain 1/v values (1./3.57, 1./33, ...) and multiply theta with 33.  kai, feb'21

		//restrictions and masks
		RestrictionInput activityParticipation;
		BaselFHNWScenario25pct2020.BasePolicyBuilder basePolicyBuilder = new BaselFHNWScenario25pct2020.BasePolicyBuilder(episimConfig);
		if (adjustRestrictions == AdjustRestrictions.yes) {
			activityParticipation = new CreateAdjustedRestrictionsFromCSV();
		} else {
			activityParticipation = new CreateRestrictionsFromCSV(episimConfig);
		}

		String untilDate = "20210926";
		activityParticipation.setInput(INPUT.resolve("perNeighborhood/" + "Act_restr_Switzerland_daily_until_" + untilDate + ".csv"));

		//location based restrictions
		episimConfig.setDistrictLevelRestrictions(locationBasedRestrictions);
		//episimConfig.setWriteEvents(EpisimConfigGroup.WriteEvents.all);

		config.facilities().setInputFile(INPUT.resolve("facility_test.xml.gz").toString());
		//config.plans().setInputFile(INPUT.resolve("be_2020-week_snz_entirePopulation_emptyPlans_withDistricts_andNeighborhood_%dpt_split.xml.gz").toString());
		List<String> subdistricts = Arrays.asList("Basel", "Riehen", "Bettingen",
				"Arlesheim", "Laufen", "Liestal", "Sissach", "Waldenburg",
				"Breisgau-Hochschwarzwald", "Emmendingen", "Freiburg im Breisgau", "Lörrach","Waldshut",
				"Aargau","Bern","Jura","Schaffhausen","Solothurn", "Haut-Rhin");

		if (locationBasedRestrictions != EpisimConfigGroup.DistrictLevelRestrictions.no) {
			episimConfig.setDistrictLevelRestrictionsAttribute("canton");

			if (activityParticipation instanceof CreateRestrictionsFromCSV) {

				Map<String, Path> subdistrictInputs = new HashMap<>();
				for (String canton : subdistricts) {
					subdistrictInputs.put(canton, INPUT.resolve("perNeighborhood/" + "Act_restr_" + canton + "_daily_until_" + untilDate + ".csv"));
				}

				((CreateRestrictionsFromCSV) activityParticipation).setDistrictInputs(subdistrictInputs);
			}
		}

		basePolicyBuilder.setActivityParticipation(activityParticipation);

		if (this.restrictions == Restrictions.no || this.restrictions == Restrictions.onlyEdu) {
			basePolicyBuilder.setActivityParticipation(null);
		}
		if (this.restrictions == Restrictions.no || this.restrictions == Restrictions.allExceptEdu || this.restrictions == Restrictions.allExceptSchoolsAndDayCare) {
			basePolicyBuilder.setRestrictSchoolsAndDayCare(false);
		}
		if (this.restrictions == Restrictions.no || this.restrictions == Restrictions.allExceptEdu || this.restrictions == Restrictions.allExceptUniversities) {
			basePolicyBuilder.setRestrictUniversities(false);
		}


		/*if (this.masks == Masks.no) basePolicyBuilder.setMaskCompliance(0);
		basePolicyBuilder.setCiCorrections(Map.of());
		FixedPolicy.ConfigBuilder builder = basePolicyBuilder.buildFixed();*/


		//tracing
		if (this.tracing == Tracing.yes) {
			TracingConfigGroup tracingConfig = ConfigUtils.addOrGetModule(config, TracingConfigGroup.class);
			int offset = (int) (ChronoUnit.DAYS.between(episimConfig.getStartDate(), LocalDate.parse("2020-04-01")) + 1);
			tracingConfig.setPutTraceablePersonsInQuarantineAfterDay(offset);
			tracingConfig.setTracingProbability(0.5);
			tracingConfig.setTracingPeriod_days(2);
			tracingConfig.setMinContactDuration_sec(15 * 60.);
			tracingConfig.setQuarantineHouseholdMembers(true);
			tracingConfig.setEquipmentRate(1.);
			tracingConfig.setTracingDelay_days(5);

			tracingConfig.setTraceSusceptible(true);
			tracingConfig.setCapacityType(CapacityType.PER_PERSON);

			int tracingCapacity = 60;
			tracingConfig.setTracingCapacity_pers_per_day(Map.of(
					LocalDate.of(2020, 4, 1), (int) (0.2 * tracingCapacity),
					LocalDate.of(2020, 6, 15), tracingCapacity
			));
		}


		//BasePolicyBuilder basePolicyBuilder = new BasePolicyBuilder(episimConfig);

		/*
		 * basePolicyBuilder.setCiCorrections(Map.of( "2020-06-07", 0.32, "2020-03-24",
		 * 0.40, "2020-06-20", 0.28, //0.3 "2020-10-07", 0.32)); //0.32
		 *
		 */
		/*
		 * basePolicyBuilder.setCiCorrections(Map.of( "2020-03-09", 0.33, "2020-03-24",
		 * 0.40, "2020-06-20", 0.28, //0.3 "2020-10-07", 0.32)); //0.32
		 */

		/*
		 * basePolicyBuilder.setCiCorrections(Map.of( "2020-03-14", 0.50, "2020-06-20",
		 * 0.28, //0.3 "2020-10-07", 0.32)); //0.32
		 */

		basePolicyBuilder.setCiCorrections(Map.of(
				"2020-03-09", 0.33,
				"2020-03-21",0.24,
				"2020-06-24", 0.345,
			    "2020-10-07", 0.315)); //0.32

		FixedPolicy.ConfigBuilder builder = basePolicyBuilder.buildFixed();
		episimConfig.setPolicy(FixedPolicy.class, builder.build());
		//config.controler().setOutputDirectory("//cluster/work/ivt_vpl/spenazzi/Covid19/Basel25pct/outputList/output_1");

		//config.controler().setOutputDirectory("//cluster/work/ivt_vpl/mesaricr/Covid19/Basel25pct/outputList/output-" + basePolicyBuilder.getActivityParticipation() +
		//		"-ciCorrections-" + basePolicyBuilder.getCiCorrections() + "-startDate-" + episimConfig.getStartDate() +
		//		"-hospitalFactor-" + episimConfig.getHospitalFactor() + "-calibrParam-" + episimConfig.getCalibrationParameter() + "-tracingProba-" + "0.5");


		//config.controler().setOutputDirectory("//cluster/work/ivt_vpl/mesaricr/Covid19/Basel25pct/outputList/output_100");
		//config.controler().setOutputDirectory("//cluster/work/ivt_vpl/mesaricr/Covid19/Basel25pct/outputList/output_200");
		//config.controler().setOutputDirectory("//cluster/work/ivt_vpl/mesaricr/Covid19/Basel25pct/outputList/output_300");
		//config.controler().setOutputDirectory("//cluster/work/ivt_vpl/mesaricr/Covid19/Basel25pct/outputList/output_400");
		//config.controler().setOutputDirectory("//cluster/work/ivt_vpl/mesaricr/Covid19/Basel25pct/outputList/output_500");
		//config.controler().setOutputDirectory("//cluster/work/ivt_vpl/mesaricr/Covid19/Basel25pct/outputList/output_600");
		//config.controler().setOutputDirectory("//cluster/work/ivt_vpl/mesaricr/Covid19/Basel25pct/outputList/output_700");
		//config.controler().setOutputDirectory("//cluster/work/ivt_vpl/mesaricr/Covid19/Basel25pct/outputList/output_800");
		//config.controler().setOutputDirectory("//cluster/work/ivt_vpl/mesaricr/Covid19/Basel25pct/outputList/output_vacc3000");
		config.controler().setOutputDirectory("//cluster/work/ivt_vpl/mesaricr/Covid19/Basel25pct/outputList/output_vacc6000");
		//config.controler().setOutputDirectory("//cluster/work/ivt_vpl/mesaricr/Covid19/Basel25pct/outputList/output_vacc9000");
		//config.controler().setOutputDirectory("//cluster/work/ivt_vpl/mesaricr/Covid19/Basel25pct/outputList/output_vacc12000");


		//episimConfig.setInitialInfections(Integer.MAX_VALUE);

		if (this.diseaseImport != DiseaseImport.no) {
			//episimConfig.setInitialInfectionDistrict(null);
			Map<LocalDate, Integer> importMap = new HashMap<>();

			/*interpolateImport(importMap, imprtFctMult * importFactorBeforeJune, LocalDate.parse("2020-06-26").plusDays(importOffset),
				LocalDate.parse("2020-07-26").plusDays(importOffset), 0.1, 5);
			interpolateImport(importMap, imprtFctMult * importFactorBeforeJune, LocalDate.parse("2020-07-13").plusDays(importOffset),
					LocalDate.parse("2020-08-10").plusDays(importOffset), 5, 18);
			interpolateImport(importMap, imprtFctMult * importFactorBeforeJune, LocalDate.parse("2020-08-10").plusDays(importOffset),
					LocalDate.parse("2020-09-07").plusDays(importOffset), 18, 12);
			interpolateImport(importMap, imprtFctMult * importFactorBeforeJune, LocalDate.parse("2020-09-07").plusDays(importOffset),
					LocalDate.parse("2020-11-01").plusDays(importOffset), 12, 6);
			episimConfig.setInfections_pers_per_day(VirusStrain.SARS_CoV_2,importMap);*/

			if (this.diseaseImport == DiseaseImport.yes) {
				/*interpolateImport(importMap, imprtFctMult * importFactorAfterJune, LocalDate.parse("2020-06-08").plusDays(importOffset),
						LocalDate.parse("2020-07-13").plusDays(importOffset), 0.1, 2.7);
				interpolateImport(importMap, imprtFctMult * importFactorAfterJune, LocalDate.parse("2020-07-13").plusDays(importOffset),
						LocalDate.parse("2020-08-10").plusDays(importOffset), 2.7, 17.9);
				interpolateImport(importMap, imprtFctMult * importFactorAfterJune, LocalDate.parse("2020-08-10").plusDays(importOffset),
						LocalDate.parse("2020-09-07").plusDays(importOffset), 17.9, 6.1);
				interpolateImport(importMap, imprtFctMult * importFactorAfterJune, LocalDate.parse("2020-10-26").plusDays(importOffset),
						LocalDate.parse("2020-12-21").plusDays(importOffset), 6.1, 1.1);*/

				String untilDate_disImport = "20201201";
				Path disImportPath = INPUT.resolve("diseaseImport/" + "Disease_import_daily_until_" + untilDate_disImport + ".csv");

				try {
					diseaseImportFunction(importMap, disImportPath, importFactorAfterJune);
				} catch(IOException e) {
					e.printStackTrace();
				}


			}

		}
		return config;
	}


	@Provides
	@Singleton
	public Scenario scenario(Config config) {

		// guice will use no args constructor by default, we check if this config was initialized
		// this is only the case when no explicit binding are required
		if (config.getModules().size() == 0)
			throw new IllegalArgumentException("Please provide a config module or binding.");

		config.vspExperimental().setVspDefaultsCheckingLevel(VspExperimentalConfigGroup.VspDefaultsCheckingLevel.warn);

		// save some time for not needed inputs (facilities are needed for location based restrictions)
		//		if (locationBasedRestrictions == EpisimConfigGroup.DistrictLevelRestrictions.no) {
		//			config.facilities().setInputFile(null);
		//		}

		ControlerUtils.checkConfigConsistencyAndWriteToLog(config, "before loading scenario");

		final Scenario scenario = ScenarioUtils.loadScenario(config);

		double capFactor = 1.3;

		for (VehicleType vehicleType : scenario.getVehicles().getVehicleTypes().values()) {
			switch (vehicleType.getId().toString()) {
				case "BUS":
					vehicleType.getCapacity().setSeats((int) (70 * capFactor));
					vehicleType.getCapacity().setStandingRoom((int) (40 * capFactor));
					// https://de.wikipedia.org/wiki/Stadtbus_(Fahrzeug)#Stehpl%C3%A4tze
					break;
//				case "metro":
//					vehicleType.getCapacity().setSeats((int) (200 * capFactor));
//					vehicleType.getCapacity().setStandingRoom((int) (550 * capFactor));
//					// https://mein.berlin.de/ideas/2019-04585/#:~:text=Ein%20Vollzug%20der%20Baureihe%20H,mehr%20Stehpl%C3%A4tze%20zur%20Verf%C3%BCgung%20stehen.
//					break;
//				case "plane":
//					vehicleType.getCapacity().setSeats((int) (200 * capFactor));
//					vehicleType.getCapacity().setStandingRoom((int) (0 * capFactor));
//					break;
//				case "pt":
//					vehicleType.getCapacity().setSeats((int) (70 * capFactor));
//					vehicleType.getCapacity().setStandingRoom((int) (70 * capFactor));
//					break;
//				case "ship":
//					vehicleType.getCapacity().setSeats((int) (150 * capFactor));
//					vehicleType.getCapacity().setStandingRoom((int) (150 * capFactor));
//					// https://www.berlin.de/tourismus/dampferfahrten/faehren/1824948-1824660-faehre-f10-wannsee-altkladow.html
//					break;
				case "ZUG":
					vehicleType.getCapacity().setSeats((int) (400 * capFactor));
					vehicleType.getCapacity().setStandingRoom((int) (0 * capFactor));
					// https://de.wikipedia.org/wiki/Stadler_KISS#Technische_Daten_der_Varianten , mehr als ICE (https://inside.bahn.de/ice-baureihen/)
					break;
//				case "tram":
//					vehicleType.getCapacity().setSeats((int) (84 * capFactor));
//					vehicleType.getCapacity().setStandingRoom((int) (216 * capFactor));
//					// https://mein.berlin.de/ideas/2019-04585/#:~:text=Ein%20Vollzug%20der%20Baureihe%20H,mehr%20Stehpl%C3%A4tze%20zur%20Verf%C3%BCgung%20stehen.
//					break;
				default:
					throw new IllegalStateException("Unexpected value=|" + vehicleType.getId().toString() + "|");
			}
		}

		return scenario;
	}


	public static class BasePolicyBuilder {
		private final EpisimConfigGroup episimConfig;

		/*
		 *  alpha = 1 -> ci=0.323
		 *  alpha = 1.2 -> ci=0.360
		 *  alpha = 1.4 -> ci=0.437
		 */
		//?
		private Map<String, Double> ciCorrections = Map.of("2020-03-12", 0.26); //0.34
		private long introductionPeriod = 14;
		private double maskCompliance = 0.95;

		/* To be excluded */
		private boolean restrictSchoolsAndDayCare = true;
		private boolean restrictUniversities = true;
		/* */

		private RestrictionInput activityParticipation;

		public BasePolicyBuilder(EpisimConfigGroup episimConfig) {
			this.episimConfig = episimConfig;
			this.activityParticipation = new CreateRestrictionsFromCSV(episimConfig);
			this.activityParticipation.setInput(INPUT.resolve("perNeighborhood/" + "Act_restr_Switzerland_daily_until_" + "20210926" + ".csv"));
		}

		public void setIntroductionPeriod(long introductionPeriod) {
			this.introductionPeriod = introductionPeriod;
		}

		public void setMaskCompliance(double maskCompliance) {
			this.maskCompliance = maskCompliance;
		}

		public void setActivityParticipation(RestrictionInput activityParticipation) {
			this.activityParticipation = activityParticipation;
		}

		public RestrictionInput getActivityParticipation() {
			return activityParticipation;
		}

		public void setCiCorrections(Map<String, Double> ciCorrections) {
			this.ciCorrections = ciCorrections;
		}

		public Map<String, Double> getCiCorrections() {
			return ciCorrections;
		}

		public boolean getRestrictSchoolsAndDayCare() {
			return restrictSchoolsAndDayCare;
		}

		public void setRestrictSchoolsAndDayCare(boolean restrictSchoolsAndDayCare) {
			this.restrictSchoolsAndDayCare = restrictSchoolsAndDayCare;
		}
		public boolean getRestrictUniversities() {
			return restrictUniversities;
		}

		public void setRestrictUniversities(boolean restrictUniversities) {
			this.restrictUniversities = restrictUniversities;
		}

		public ConfigBuilder buildFixed() {
			ConfigBuilder configBuilder;
			try {
				configBuilder = (ConfigBuilder) basePolicy(activityParticipation, ciCorrections,introductionPeriod,
						maskCompliance, restrictSchoolsAndDayCare, restrictUniversities);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			return configBuilder;
		}

		public ShutdownPolicy.ConfigBuilder<?> build() {
			ShutdownPolicy.ConfigBuilder<?> configBuilder;
			try {
				configBuilder = basePolicy(activityParticipation, ciCorrections,introductionPeriod,
						maskCompliance, restrictSchoolsAndDayCare, restrictUniversities);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			return configBuilder;
		}
	}
}
