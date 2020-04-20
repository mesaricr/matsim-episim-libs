/*-
 * #%L
 * MATSim Episim
 * %%
 * Copyright (C) 2020 matsim-org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.matsim.run;

import org.matsim.scenarioCreation.ConvertPersonAttributes;
import org.matsim.scenarioCreation.DownSampleScenario;
import org.matsim.scenarioCreation.FilterEvents;
import org.matsim.scenarioCreation.MergeEvents;
import picocli.AutoComplete;
import picocli.CommandLine;

@CommandLine.Command(
		name = "scenarioCreation",
		description = "Scenario creation tool for Episim offering various subcommands.",
		mixinStandardHelpOptions = true,
		usageHelpWidth = 120,
		subcommands = {CommandLine.HelpCommand.class, AutoComplete.GenerateCompletion.class,
				ConvertPersonAttributes.class, FilterEvents.class, MergeEvents.class, DownSampleScenario.class}
)
public class ScenarioCreation implements Runnable {

	@CommandLine.Spec
	CommandLine.Model.CommandSpec spec;

	public static void main(String[] args) {
		System.exit(new CommandLine(new ScenarioCreation()).execute(args));
	}

	@Override
	public void run() {
		throw new CommandLine.ParameterException(spec.commandLine(), "Missing required subcommand");
	}
}
