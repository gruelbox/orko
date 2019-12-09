/*
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
import { JobType, Job, OcoJob } from "modules/server"

export const isAlert = (job: Job) =>
  job.jobType === JobType.OCO &&
  ((job: OcoJob) =>
    (job.high && job.high.job.jobType === JobType.ALERT) ||
    (job.low && job.low.job.jobType === JobType.ALERT))(job as OcoJob)

export const isStop = (job: Job) =>
  job.jobType === JobType.OCO &&
  ((job: OcoJob) =>
    (!job.low && job.high && job.high.job.jobType === JobType.LIMIT_ORDER) ||
    (!job.high && job.low && job.low.job.jobType === JobType.LIMIT_ORDER))(job as OcoJob)
