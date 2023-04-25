/**
 * 
 */
package org.matsim.episim.model;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.Hash;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.data.DataUtilities;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.episim.EpisimConfigGroup;
import org.matsim.episim.EpisimPerson;
import org.matsim.episim.EpisimUtils;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * @author stefanopenazzi, mesaricr
 *
 */
public class RandomInitialInfectionsBaselPerCanton implements InitialInfectionHandler {

	private static final Logger log = LogManager.getLogger(RandomInitialInfections.class);

	private final EpisimConfigGroup episimConfig;
	private final SplittableRandom rnd;

	private boolean pass;
	
	
	private final Map<String,Integer> initInfectPerCanton = new HashMap<>(); 

	@Inject
	public RandomInitialInfectionsBaselPerCanton(Config config, SplittableRandom rnd) {
		this.episimConfig = ConfigUtils.addOrGetModule(config, EpisimConfigGroup.class);
		
		this.rnd = rnd;
		double popShare = episimConfig.getSampleSize();


		//10%

		initInfectPerCanton.put("Aargau",(int)Math.ceil(2*popShare)); 			   // 5, 28, AG v1: 5 v2: 3 v4:  1 v5:   2 v6:  2
		initInfectPerCanton.put("Bern",(int)Math.ceil(62*popShare)); 			   //62, 16, BE v1:62 v2:62 v4: 62 v5:  62 v6: 62
		initInfectPerCanton.put("Solothurn",(int)Math.ceil(1*popShare)); 		   // 2,   , SO v1: 2 v2: 2 v4:  1 v5:   1 v6:  1
		initInfectPerCanton.put("Basel-Landschaft",(int)Math.ceil(1*popShare));    //40,  8, BL v1:20 v2: 5 v4:  2 v5:   2 v6:  2
		initInfectPerCanton.put("Jura",(int)Math.ceil(3*popShare)); 			   //12,  4, JU v1: 6 v2: 2 v4:  2 v5:   3 v6:  3
		initInfectPerCanton.put("Basel-Stadt",(int)Math.ceil(1*popShare));         //60, 12, BS v1:20 v2: 5 v4:  2 v5:   2 v6:  2
		initInfectPerCanton.put("Haut-Rhin",(int)Math.ceil(900*popShare)); 	       //60, 12, HR v1:60 v2:60 v4:600 v5:1000 v6:600
		initInfectPerCanton.put("Baden-Württemberg",(int)Math.ceil(1200*popShare)); //60, 12, BW v1:60 v2:60 v4:600 v5:3000 v6:600

		//initInfectPerCanton.put("Basel",(int)Math.ceil(2*popShare)); 			   // 5, 28, AG v1: 5 v2: 3 v4:  1 v5:   2 v6:  2
		//initInfectPerCanton.put("Riehen",(int)Math.ceil(2*popShare)); 			   //62, 16, BE v1:62 v2:62 v4: 62 v5:  62 v6: 62
		//initInfectPerCanton.put("Bettingen",(int)Math.ceil(2*popShare));           // 5, 28, AG v1: 5 v2: 3 v4:  1 v5:   2 v6:  2
		//initInfectPerCanton.put("Arlesheim",(int)Math.ceil(2*popShare)); 		   //62, 16, BE v1:62 v2:62 v4: 62 v5:  62 v6: 62
		//initInfectPerCanton.put("Laufen",(int)Math.ceil(2*popShare)); 			   // 5, 28, AG v1: 5 v2: 3 v4:  1 v5:   2 v6:  2
		//initInfectPerCanton.put("Liestal",(int)Math.ceil(2*popShare)); 			   //62, 16, BE v1:62 v2:62 v4: 62 v5:  62 v6: 62
		//initInfectPerCanton.put("Sissach",(int)Math.ceil(2*popShare)); 			   // 5, 28, AG v1: 5 v2: 3 v4:  1 v5:   2 v6:  2
		//initInfectPerCanton.put("Waldenburg",(int)Math.ceil(2*popShare)); 		   //62, 16, BE v1:62 v2:62 v4: 62 v5:  62 v6: 62
		//initInfectPerCanton.put("Emmendingen",(int)Math.ceil(2*popShare)); 		   // 5, 28, AG v1: 5 v2: 3 v4:  1 v5:   2 v6:  2
		//initInfectPerCanton.put("Lörrach",(int)Math.ceil(20*popShare)); 		   //62, 16, BE v1:62 v2:62 v4: 62 v5:  62 v6: 62
		//initInfectPerCanton.put("Breisgau-Hochschwarzwald",(int)Math.ceil(20*popShare)); 			   // 5, 28, AG v1: 5 v2: 3 v4:  1 v5:   2 v6:  2
		//initInfectPerCanton.put("Waldshut",(int)Math.ceil(20*popShare)); 		   //62, 16, BE v1:62 v2:62 v4: 62 v5:  62 v6: 62
		//initInfectPerCanton.put("Freiburg im Breisgau",(int)Math.ceil(20*popShare)); 			   // 5, 28, AG v1: 5 v2: 3 v4:  1 v5:   2 v6:  2

//		initInfectPerCanton.put("Schwyz",(int)Math.ceil(2*popShare)); 			   // 5, 28, AG v1: 5 v2: 3 v4:  1 v5:   2 v6:  2
//		initInfectPerCanton.put("Thurgau",(int)Math.ceil(2*popShare)); 			   // 5, 28, AG v1: 5 v2: 3 v4:  1 v5:   2 v6:  2
//		initInfectPerCanton.put("Vaud",(int)Math.ceil(2*popShare)); 			   // 5, 28, AG v1: 5 v2: 3 v4:  1 v5:   2 v6:  2
//		initInfectPerCanton.put("Schaffhausen",(int)Math.ceil(2*popShare)); 	   // 5, 28, AG v1: 5 v2: 3 v4:  1 v5:   2 v6:  2
//		initInfectPerCanton.put("Graubünden",(int)Math.ceil(2*popShare)); 		   // 5, 28, AG v1: 5 v2: 3 v4:  1 v5:   2 v6:  2
//		initInfectPerCanton.put("Nidwalden",(int)Math.ceil(2*popShare)); 		   // 5, 28, AG v1: 5 v2: 3 v4:  1 v5:   2 v6:  2
//		initInfectPerCanton.put("Fribourg",(int)Math.ceil(2*popShare)); 		   // 5, 28, AG v1: 5 v2: 3 v4:  1 v5:   2 v6:  2
//		initInfectPerCanton.put("Obwalden",(int)Math.ceil(2*popShare)); 		   // 5, 28, AG v1: 5 v2: 3 v4:  1 v5:   2 v6:  2
//		initInfectPerCanton.put("Uri",(int)Math.ceil(2*popShare)); 			       // 5, 28, AG v1: 5 v2: 3 v4:  1 v5:   2 v6:  2
//		initInfectPerCanton.put("Valais",(int)Math.ceil(2*popShare)); 			   // 5, 28, AG v1: 5 v2: 3 v4:  1 v5:   2 v6:  2
//		initInfectPerCanton.put("Zürich",(int)Math.ceil(2*popShare)); 			   // 5, 28, AG v1: 5 v2: 3 v4:  1 v5:   2 v6:  2
//		initInfectPerCanton.put("Luzern",(int)Math.ceil(2*popShare)); 			   // 5, 28, AG v1: 5 v2: 3 v4:  1 v5:   2 v6:  2
//		initInfectPerCanton.put("Zug",(int)Math.ceil(2*popShare)); 			       // 5, 28, AG v1: 5 v2: 3 v4:  1 v5:   2 v6:  2
//		initInfectPerCanton.put("St. Gallen",(int)Math.ceil(2*popShare)); 		   // 5, 28, AG v1: 5 v2: 3 v4:  1 v5:   2 v6:  2
//		initInfectPerCanton.put("Genève",(int)Math.ceil(2*popShare)); 			   // 5, 28, AG v1: 5 v2: 3 v4:  1 v5:   2 v6:  2
//		initInfectPerCanton.put("Ticino",(int)Math.ceil(2*popShare)); 			   // 5, 28, AG v1: 5 v2: 3 v4:  1 v5:   2 v6:  2
//		initInfectPerCanton.put("Neuchâtel",(int)Math.ceil(2*popShare)); 		   // 5, 28, AG v1: 5 v2: 3 v4:  1 v5:   2 v6:  2
//		initInfectPerCanton.put("Glarus",(int)Math.ceil(2*popShare)); 			   // 5, 28, AG v1: 5 v2: 3 v4:  1 v5:   2 v6:  2
//		initInfectPerCanton.put("Appenzell Ausserrhoden",(int)Math.ceil(2*popShare)); 			   // 5, 28, AG v1: 5 v2: 3 v4:  1 v5:   2 v6:  2
//		initInfectPerCanton.put("Appenzell Innerrhoden",(int)Math.ceil(2*popShare)); 			   // 5, 28, AG v1: 5 v2: 3 v4:  1 v5:   2 v6:  2

		//25%

//		initInfectPerCanton.put("Bern",(int)Math.ceil(35*popShare)); //16, BE
//		initInfectPerCanton.put("Solothurn",(int)Math.ceil(2*popShare)); //SO
//		initInfectPerCanton.put("Basel-Landschaft",(int)Math.ceil(50*popShare)); //8, BL
//		initInfectPerCanton.put("Jura",(int)Math.ceil(22*popShare)); //4, JU
//		initInfectPerCanton.put("Aargau",(int)Math.ceil(0*popShare)); //28, AG
//		initInfectPerCanton.put("Basel-Stadt",(int)Math.ceil(50*popShare)); //12, BS
//		initInfectPerCanton.put("Haut-Rhin",(int)Math.ceil(50*popShare)); //12, HR
//		initInfectPerCanton.put("Baden-Württemberg",(int)Math.ceil(50*popShare)); //12, BW
		
		pass = true;
		
	}

	@Override
	public int handleInfections(Map<Id<Person>, EpisimPerson> persons, int iteration) {

		int infected = 0;


		
		//only for the initialization
		if(pass) {
			double now = EpisimUtils.getCorrectedTime(episimConfig.getStartOffset(), 0, iteration);
		
			int lowerAgeBoundaryForInitInfections = episimConfig.getLowerAgeBoundaryForInitInfections();
			int upperAgeBoundaryForInitInfections = episimConfig.getUpperAgeBoundaryForInitInfections();
		
			LocalDate date = episimConfig.getStartDate().plusDays(iteration - 1);

			for (HashMap.Entry<String,Integer> e : initInfectPerCanton.entrySet()) {

				int numInfections = e.getValue();

				/*List<EpisimPerson> testList = persons.values().stream()
						.collect(Collectors.toList());

				ArrayList<String> cantons = new ArrayList<>();
				for (int i = 0; i < testList.size(); i++){

					EpisimPerson p = testList.get(i);
					String canton = (String)p.getAttributes().getAttribute("canton");

					if(canton == null || canton.length() == 0){
						String cantonId = p.getPersonId().toString();
						cantons.add(cantonId);
					}
				}*/


				//System.out.println(e.getKey() + " " + cantons);
				List<EpisimPerson> candidates = persons.values().stream()
						.filter(p -> initInfectPerCanton.containsKey(p.getAttributes().getAttribute("canton")) && p.getAttributes().getAttribute("canton").toString().equals(e.getKey().toString()))
						.filter(p -> p.getAttributes().getAttribute("canton").toString().equals(e.getKey()))
						.filter(p -> lowerAgeBoundaryForInitInfections == -1 || (int) p.getAttributes().getAttribute("age") >= lowerAgeBoundaryForInitInfections)
						.filter(p -> upperAgeBoundaryForInitInfections == -1 || (int) p.getAttributes().getAttribute("age") <= upperAgeBoundaryForInitInfections)
						.filter(p -> p.getDiseaseStatus() == EpisimPerson.DiseaseStatus.susceptible)
						.collect(Collectors.toList());
				//System.out.println("Size" + candidates.size());
				//System.out.println("Candidates" + candidates);


				//.filter(p -> !p.getAttributes().getAttribute("canton").toString().equals("null"))
		
				if (candidates.size() < numInfections) {
					log.warn("Not enough persons match the initial infection requirement, using whole population...");
					candidates = Lists.newArrayList(persons.values());
				}
		
				while (numInfections > 0 && candidates.size() > 0) {
					EpisimPerson randomPerson = candidates.remove(rnd.nextInt(candidates.size()));
					if (randomPerson.getDiseaseStatus() == EpisimPerson.DiseaseStatus.susceptible) {
						randomPerson.setDiseaseStatus(now, EpisimPerson.DiseaseStatus.infectedButNotContagious);
						randomPerson.setVirusStrain(VirusStrain.SARS_CoV_2);
						log.warn("Person {} has initial infection with {}.", randomPerson.getPersonId(), e.getKey());
						numInfections--;
						infected++;
					}
				}
			}
			pass = false;
			return infected;
		}


		//for the imported cases
		double now = EpisimUtils.getCorrectedTime(episimConfig.getStartOffset(), 0, iteration);

		int lowerAgeBoundaryForInitInfections = episimConfig.getLowerAgeBoundaryForInitInfections();
		int upperAgeBoundaryForInitInfections = episimConfig.getUpperAgeBoundaryForInitInfections();

		LocalDate date = episimConfig.getStartDate().plusDays(iteration - 1);

		for (Map.Entry<VirusStrain, NavigableMap<LocalDate, Integer>> e : episimConfig.getInfections_pers_per_day().entrySet()) {

			int numInfections = EpisimUtils.findValidEntry(e.getValue(), 0, date);

			if (numInfections == 0) {
				return 0;
			}

			List<EpisimPerson> candidates = persons.values().stream()
					.filter(p -> lowerAgeBoundaryForInitInfections == -1 || (int) p.getAttributes().getAttribute("microm:modeled:age") >= lowerAgeBoundaryForInitInfections)
					.filter(p -> upperAgeBoundaryForInitInfections == -1 || (int) p.getAttributes().getAttribute("microm:modeled:age") <= upperAgeBoundaryForInitInfections)
					.filter(p -> p.getDiseaseStatus() == EpisimPerson.DiseaseStatus.susceptible)
					.collect(Collectors.toList());

			if (candidates.size() < numInfections) {
				log.warn("Not enough persons match the initial infection requirement, using whole population...");
				candidates = Lists.newArrayList(persons.values());
			}

			while (numInfections > 0 && candidates.size() > 0) {
				EpisimPerson randomPerson = candidates.remove(rnd.nextInt(candidates.size()));
				if (randomPerson.getDiseaseStatus() == EpisimPerson.DiseaseStatus.susceptible) {
					randomPerson.setDiseaseStatus(now, EpisimPerson.DiseaseStatus.infectedButNotContagious);
					randomPerson.setVirusStrain(e.getKey());
					log.warn("Person {} has initial infection with {}.", randomPerson.getPersonId(), e.getKey());
					numInfections--;
					infected++;
				}
			}
		}
		return 0;

	}

	@Override
	public int getInfectionsLeft() {
		return 0;
	}

	@Override
	public void setInfectionsLeft(int num) {
		
	}
	
}

