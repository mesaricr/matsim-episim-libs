package org.matsim.episim.model.input;

import org.matsim.episim.policy.ShutdownPolicy;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Interface for providing activity participation data.
 */
public interface ActivityParticipation {

	/**
	 * Sets the input for this file.
	 */
	ActivityParticipation setInput(Path input);

	/**
	 * Provide policy with activity reduction.
	 */
	ShutdownPolicy.ConfigBuilder<?> createPolicy() throws IOException;
}
