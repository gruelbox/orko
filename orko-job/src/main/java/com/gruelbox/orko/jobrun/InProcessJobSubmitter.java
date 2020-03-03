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
package com.gruelbox.orko.jobrun;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.gruelbox.orko.jobrun.spi.Job;
import com.gruelbox.orko.jobrun.spi.JobControl;
import com.gruelbox.orko.jobrun.spi.JobProcessor;
import com.gruelbox.orko.jobrun.spi.Validatable;
import java.util.UUID;

/** Implementation of {@link JobSubmitter} which runs the job within the same process. */
public class InProcessJobSubmitter implements JobSubmitter {

  private final JobRunner jobRunner;
  private final Injector injector;

  @Inject
  InProcessJobSubmitter(JobRunner jobRunner, Injector injector) {
    this.jobRunner = jobRunner;
    this.injector = injector;
  }

  @Override
  public Job submitNew(Job job) throws Exception {
    if (isEmpty(job.id())) {
      job = job.toBuilder().id(UUID.randomUUID().toString()).build();
    }
    jobRunner.submitNew(job, () -> {}, () -> {});
    return job;
  }

  @Override
  public void validate(Job job, JobControl jobControl) {
    JobProcessor<Job> processor = JobProcessor.createProcessor(job, jobControl, injector);
    if (processor instanceof Validatable) {
      ((Validatable) processor).validate();
    }
  }

  public static final class JobNotUniqueException extends Exception {
    private static final long serialVersionUID = 7113718773155036498L;

    JobNotUniqueException() {
      super("Job cannot be locked. Already exists or UUID re-used");
    }
  }
}
