/**
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gruelbox.orko.jobrun.spi;

/*-
 * ===============================================================================L
 * Orko Job
 * ================================================================================
 * Copyright (C) 2018 - 2019 Graham Crockford
 * ================================================================================
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
 * ===============================================================================E
 */

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

/**
 * A job may start multiple times, but only run exactly once to completion. It
 * can be serialized, and thus is persistent until finishing. Its behaviour
 * must be idempotent to avoid double-processing.
 *
 * <p>
 * Instances of this class are immutable, but can be copied and modified (using
 * {@link #toBuilder()}) and then saved, replacing the existing job.
 * </p>
 *
 * <p>
 * The processing code is kept separate from the data.  The applicatiom will
 * inject an instance of {@link #processorFactory()} to create the processor
 * necessary to run the job.
 * </p>
 *
 * <p>
 * In a multi instance application, only one instance can lock and run the job
 * at a time. In the event of a failure of the instance holding the lock, the
 * lock will time out and get picked up by another instance.
 * </p>
 *
 * <p>
 * Jobs are assumed to be asynchronous. They should return immediately on
 * startup and either subscribe to the necessary callbacks or spawn background
 * threads to achieve continuous processing.
 * </p>
 *
 * @author Graham Crockford
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CUSTOM, include=JsonTypeInfo.As.PROPERTY, property="jobType")
@JsonTypeIdResolver(JobTypeResolver.class)
public interface Job {

  /**
   * @return The job id.
   */
  String id();

  /**
   * Creates a duplicate copy of the job as a builder, allowing a modified
   * version to be created.
   *
   * <p>Subclasses should overload the return type of this method to match
   * the concrete job type.</p>
   *
   * @return A populated builder.
   */
  JobBuilder<? extends Job> toBuilder();

  /**
   * The class of the factory from which instances of the processor
   * class can be created.
   *
   * @return The processor factory class.
   */
  Class<? extends JobProcessor.Factory<? extends Job>> processorFactory();

}
