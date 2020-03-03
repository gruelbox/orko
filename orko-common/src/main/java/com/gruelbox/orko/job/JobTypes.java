/**
 * Orko - Copyright © 2018-2019 Graham Crockford
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.gruelbox.orko.job;

import com.google.common.collect.ImmutableList;
import com.gruelbox.orko.job.script.ScriptJob;
import com.gruelbox.orko.jobrun.spi.Job;
import com.gruelbox.orko.jobrun.spi.JobTypeContribution;
import java.util.ServiceLoader;

/**
 * Orko jobs (registered by {@link ServiceLoader}).
 *
 * @author Graham Crockford
 */
public class JobTypes implements JobTypeContribution {
  @Override
  public Iterable<Class<? extends Job>> jobTypes() {
    return ImmutableList.of(
        AutoValue_Alert.class,
        AutoValue_LimitOrderJob.class,
        OneCancelsOther.class,
        SoftTrailingStop.class,
        StatusUpdateJob.class,
        ScriptJob.class);
  }
}
