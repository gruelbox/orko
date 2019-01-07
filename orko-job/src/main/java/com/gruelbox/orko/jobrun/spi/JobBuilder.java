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


/**
 * Builder for {@link Job}s.
 *
 * @author Graham Crockford
 * @param <T> The job type.
 */
public interface JobBuilder<T extends Job> {

  /**
   * Sets the job id.
   *
   * @param id The job id.
   * @return this, for method chaining.
   */
  public JobBuilder<T> id(String id);

  /**
   * Builds the completed job.
   *
   * @return The job.
   */
  public T build();

}
