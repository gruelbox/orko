import { put, get, del } from './fetchUtil';

class JobService {
  
  async submitJob(job, token) {
    return await put('jobs', token, JSON.stringify(job));
  }

  async deleteJob(job, token) {
    return await del('jobs/' + job.id, token);
  }

  async fetchJobs(token) {
    return await get('jobs', token);
  }

}

export default new JobService();