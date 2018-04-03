import { put, get } from './fetchUtil';

class JobService {
  
  async submitJob(job, token) {
    return await put('jobs', token, JSON.stringify(job));
  }

  async fetchJobs(token) {
    return await get('jobs', token);
  }

}

export default new JobService();