import React, { Component } from 'react';
import { Card, Message, Button, List } from 'semantic-ui-react'
import { get, del } from '../context/fetchUtil';
import { Subscribe  } from 'unstated';
import Job from './Job'
import AuthContainer from '../context/AuthContainer';

export default class Jobs extends Component {

  constructor(props) {
    super(props);
    this.state = {
      failed: {}
    }
  }

  componentWillUnmount() {
    clearInterval(this.interval);
  }

  onKill = (job, auth) => {
    return del('jobs/' + job.id, auth.getUserName(), auth.getPassword())
      .catch(error => {
        console.log("Failed to delete job ", job.id);
        this.setState(oldState => ({
          failed: {
            ...oldState.failed,
            [job.id]: true
          }
        }));
        setTimeout(() =>
          this.setState(oldState => ({
            failed: {
              ...oldState.failed,
              [job.id]: true
            }
          })),
          5000
        );
      });
  };

  fetch = (auth) => {
    return get('jobs', auth.getUserName(), auth.getPassword())
      .then(response => response.json())
      .then(json => {
        this.setState({
          jobs: json
        });
      })
      .catch(error => {
        console.log("Failed to fetch jobs");
      });
  }

  render() {
    return (
      <Subscribe to={[AuthContainer]}>
        {(auth) => {
          if (!this.interval) {
            this.fetch(auth);
            this.interval = setInterval(() => this.fetch(auth), 5000);
          }
          
          if (!this.state.jobs || this.state.jobs.length === 0) {

            return (
              <Message>
                <Message.Header>
                  No active jobs
                </Message.Header>
                <p>No active jobs found on the server</p>
              </Message>
            );

          } else {

            return (
              <Card.Group>
                {this.state.jobs.map(job =>(
                  <Card raised>
                    <Card.Content>
                      <Card.Header>Job</Card.Header>
                      <Card.Meta>{job.id}</Card.Meta>
                    </Card.Content>
                    <Card.Content>
                      <Card.Description>
                        <List>
                          <Job job={job} />
                        </List>
                      </Card.Description>
                    </Card.Content>
                    <Card.Content extra>
                      <Button basic color='red' onClick={() => this.onKill(job, auth)}>Kill</Button>
                      {this.props.error && 
                        <Message>
                          <Message.Header>
                            Failed
                          </Message.Header>
                          <p>An error was encountered deleting the job.</p>
                        </Message>
                      }
                    </Card.Content>
                  </Card>
                ))}
              </Card.Group>
            );

          }
        }}
      </Subscribe>
    );
  }
}