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
package org.matsim.episim.policy;

import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.episim.EpisimReporting;
import org.matsim.run.modules.BaselFHNWScenario25pct2020;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;


/**
 * Abstract base class for policies which are supposed to modify {@link Restriction}s at the end of each day.
 */
public abstract class DiseaseImportClass {

    /**
     * Magic number to indicate this entry should be replaced based on hospital numbers.
     */
    private static final Logger log = LogManager.getLogger(DiseaseImportClass.class);

    protected final Config config;

    public Config getConfig() {
        return config;
    }

    /**
     * Constructor from config.
     */
    protected DiseaseImportClass(Config config) {
        this.config = config;
        //log.info("Using policy {} with config: {}", getClass(), config.root().render(ConfigRenderOptions.concise().setJson(false)));
    }


    /**
     * Initialized the policies at start of simulation.
     * @param start simulation start date
     * @param importMap map of imported cases from airport
     */
    public abstract void init(LocalDate start, ImmutableMap<String, Integer> importMap);

    /**
     * Called when the policy is restored from snapshot.
     * @param start restored date
     * @param importMap importMap from snapshot
     */
    public void restore(LocalDate start, ImmutableMap<String, Integer> importMap) {}

    /**
     * Update the restrictions at the start of the day based on the report.
     * The map is immutable, use setters of {@link Restriction}.
     *
     * @param report       infections statistics of the day
     * @param importMap imported cases during the day
     */
    public abstract void updateImportMap(EpisimReporting.InfectionReport report, ImmutableMap<String, Integer> importMap);


    /**
     * Helper base class for config builders.
     */
    public static class ConfigBuilder<T> {

        /**
         * Maps activities to config objects.
         */
        protected Map<String, T> params = new HashMap<>();

        /**
         * Public inheritance is forbidden.
         */
        ConfigBuilder() {
        }

        public Config build() {
            return ConfigFactory.parseMap(params);
        }

    }
}



    /*public BaselFHNWScenario25pct2020.DiseaseImport.ConfigBuilder<?> build() {
        BaselFHNWScenario25pct2020.DiseaseImport.ConfigBuilder<?> configBuilder;
        try {
            configBuilder = DiseaseImportFunction(importMap, disImportPath, importFactorAfterJune);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return configBuilder;
    }*/



/*    private static BaselFHNWScenario25pct2020.DiseaseImport.ConfigBuilder<?> DiseaseImportFunction(BaselFHNWScenario25pct2020.DiseaseImport diseaseImport, double importFactorAfterJune) throws IOException {

        // note that there is already a builder around this
        FixedPolicy.ConfigBuilder importMap; // = ShutdownPolicy.config();

        if (diseaseImport != BaselFHNWScenario25pct2020.DiseaseImport.no) {
            //episimConfig.setInitialInfectionDistrict(null);
            Map<LocalDate, Integer> importMap = new HashMap<>();

			*//*interpolateImport(importMap, imprtFctMult * importFactorBeforeJune, LocalDate.parse("2020-06-26").plusDays(importOffset),
				LocalDate.parse("2020-07-26").plusDays(importOffset), 0.1, 5);
			interpolateImport(importMap, imprtFctMult * importFactorBeforeJune, LocalDate.parse("2020-07-13").plusDays(importOffset),
					LocalDate.parse("2020-08-10").plusDays(importOffset), 5, 18);
			interpolateImport(importMap, imprtFctMult * importFactorBeforeJune, LocalDate.parse("2020-08-10").plusDays(importOffset),
					LocalDate.parse("2020-09-07").plusDays(importOffset), 18, 12);
			interpolateImport(importMap, imprtFctMult * importFactorBeforeJune, LocalDate.parse("2020-09-07").plusDays(importOffset),
					LocalDate.parse("2020-11-01").plusDays(importOffset), 12, 6);
			episimConfig.setInfections_pers_per_day(VirusStrain.SARS_CoV_2,importMap);*//*

            if (diseaseImport == BaselFHNWScenario25pct2020.DiseaseImport.yes) {
				*//*interpolateImport(importMap, imprtFctMult * importFactorAfterJune, LocalDate.parse("2020-06-08").plusDays(importOffset),
						LocalDate.parse("2020-07-13").plusDays(importOffset), 0.1, 2.7);
				interpolateImport(importMap, imprtFctMult * importFactorAfterJune, LocalDate.parse("2020-07-13").plusDays(importOffset),
						LocalDate.parse("2020-08-10").plusDays(importOffset), 2.7, 17.9);
				interpolateImport(importMap, imprtFctMult * importFactorAfterJune, LocalDate.parse("2020-08-10").plusDays(importOffset),
						LocalDate.parse("2020-09-07").plusDays(importOffset), 17.9, 6.1);
				interpolateImport(importMap, imprtFctMult * importFactorAfterJune, LocalDate.parse("2020-10-26").plusDays(importOffset),
						LocalDate.parse("2020-12-21").plusDays(importOffset), 6.1, 1.1);*//*

                String untilDate_disImport = "20201201";
                Path disImportPath = INPUT.resolve("diseaseImport/" + "Disease_import_daily_until_" + untilDate_disImport + ".csv");

                diseaseImportFunction(importMap, disImportPath, importFactorAfterJune);

            }

        }

        return importMap;
    }*/
