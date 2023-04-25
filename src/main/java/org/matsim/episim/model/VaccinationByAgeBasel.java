/**
 * 
 */
package org.matsim.episim.model;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.episim.EpisimConfigGroup;
import org.matsim.episim.EpisimPerson;
import org.matsim.episim.EpisimUtils;
import org.matsim.episim.VaccinationConfigGroup;

import com.google.inject.Inject;

/**
 * @author stefanopenazzi, mesaricr
 *
 */
public class VaccinationByAgeBasel implements VaccinationModel {

	private final SplittableRandom rnd;
	private final VaccinationConfigGroup vaccinationConfig;

	private final static int MAX_AGE = 130;
	private final static int MINIMUM_AGE_FOR_VACCINATIONS = 0;

	@Inject
	public VaccinationByAgeBasel(SplittableRandom rnd,VaccinationConfigGroup vaccinationConfig) {
		this.rnd = rnd;
		this.vaccinationConfig = vaccinationConfig;
	
	}


	//vaccination start Basel: 2020-12-28
	//vaccination start France: 2020-12-26 (https://www.haut-rhin.gouv.fr/Actualites/Actualites-du-Prefet-et-des-Sous-Prefets/Coronavirus-COVID-19/La-vaccination-dans-le-Haut-Rhin)
	//vaccination start Germany: 2020-12-27 (https://sozialministerium.baden-wuerttemberg.de/de/service/presse/pressemitteilung/pid/impfstart-in-baden-wuerttemberg/)

	//Number of daily available vaccinations is currently constant at 9000 and has to be changed manually in the InfectionEventHandler

	@Override
	public int handleVaccination(Map<Id<Person>, EpisimPerson> persons, boolean reVaccination, int availableVaccinations, LocalDate date, int iteration, double now) {

		if(date.compareTo(LocalDate.parse("2020-12-26")) > 0) {

			if (availableVaccinations == 0)
				return 0;

			Map<VaccinationType, Double> prob = vaccinationConfig.getVaccinationTypeProb(date);

			// perAge is an ArrayList where we have for each age (in years) an
			// ArrayList of Persons that are qualified for a vaccination
			final List<EpisimPerson>[] perAge = new List[MAX_AGE];


			for (int i = 0; i < MAX_AGE; i++)
				perAge[i] = new ArrayList<>();

			for (EpisimPerson p : persons.values()) {
				if (p.isVaccinable() &&
				p.getDiseaseStatus() == EpisimPerson.DiseaseStatus.susceptible &&
				(p.getVaccinationStatus() == (reVaccination ? EpisimPerson.VaccinationStatus.yes : EpisimPerson.VaccinationStatus.no) ) &&
				p.getReVaccinationStatus() == EpisimPerson.VaccinationStatus.no) {

					perAge[p.getAge()].add(p);
				}
			}



			int age = MAX_AGE - 1;
			int vaccinationsLeft = availableVaccinations;

			while (vaccinationsLeft > 0 && age > MINIMUM_AGE_FOR_VACCINATIONS) {

				List<EpisimPerson> candidates = perAge[age];

				// list is shuffled to avoid eventual bias
				if (candidates.size() > vaccinationsLeft)
					Collections.shuffle(perAge[age], new Random(EpisimUtils.getSeed(rnd)));

				for (int i = 0; i < Math.min(candidates.size(), vaccinationsLeft); i++) {
					EpisimPerson person = candidates.get(i);
					vaccinate(person, iteration, reVaccination ? null : VaccinationModel.chooseVaccinationType(prob, rnd), reVaccination);
					vaccinationsLeft--;
				}

				age--;
			}

			return availableVaccinations - vaccinationsLeft;

		}

		else {
			return 0;
		}
		
	}

}
