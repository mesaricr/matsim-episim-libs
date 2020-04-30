package org.matsim.episim.model;

import org.junit.Before;
import org.junit.Test;
import org.matsim.episim.*;

import java.util.SplittableRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class DefaultProgressionModelTest {

	private EpisimReporting reporting;
	private ProgressionModel model;
	private TracingConfigGroup tracingConfig;

	@Before
	public void setup() {
		reporting = mock(EpisimReporting.class);
		tracingConfig = new TracingConfigGroup();
		model = new DefaultProgressionModel(new SplittableRandom(1), new EpisimConfigGroup(), tracingConfig);
	}

	@Test
	public void tracing() {

		// this test depends a bit on the random seed
		tracingConfig.setPutTraceablePersonsInQuarantineAfterDay(0);
		tracingConfig.setTracingDelay(0);

		EpisimPerson p = EpisimTestUtils.createPerson(reporting);
		p.setDiseaseStatus(0, EpisimPerson.DiseaseStatus.infectedButNotContagious);
		for (int day = 0; day <= 5; day++) {
			model.updateState(p, day);
		}

		p.addTraceableContactPerson(EpisimTestUtils.createPerson(reporting), 5 * 24 * 3600);

		model.updateState(p, 6);
		assertThat(p.getTraceableContactPersons(0)).allMatch(t -> t.getQuarantineStatus() == EpisimPerson.QuarantineStatus.atHome);
	}

	@Test
	public void tracingDelay() {

		// test with delay
		tracingConfig.setPutTraceablePersonsInQuarantineAfterDay(0);
		tracingConfig.setTracingDelay(2);

		EpisimPerson p = EpisimTestUtils.createPerson(reporting);
		p.setDiseaseStatus(0, EpisimPerson.DiseaseStatus.infectedButNotContagious);
		for (int day = 0; day <= 5; day++) {
			model.updateState(p, day);
		}

		p.addTraceableContactPerson(EpisimTestUtils.createPerson(reporting), 5 * 24 * 3600);

		model.updateState(p, 6);
		assertThat(p.getTraceableContactPersons(0)).allMatch(t -> t.getQuarantineStatus() == EpisimPerson.QuarantineStatus.no);


		model.updateState(p, 7);
		assertThat(p.getTraceableContactPersons(0)).allMatch(t -> t.getQuarantineStatus() == EpisimPerson.QuarantineStatus.no);


		model.updateState(p, 8);
		assertThat(p.getTraceableContactPersons(0)).allMatch(t -> t.getQuarantineStatus() == EpisimPerson.QuarantineStatus.atHome);

	}
}
