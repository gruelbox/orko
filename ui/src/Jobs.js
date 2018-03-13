import React, { Component } from 'react';
import { Card, Button, Message } from 'semantic-ui-react'
import { get, del } from './context/fetchUtil';
import { Subscribe  } from 'unstated';
import AuthContainer from './context/AuthContainer';

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
      .then(response => response.json())
      .then(json => {
        this.setState({
          jobs: json
        });
      })
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
                {this.state.jobs.map(job => {
                  return (
                    <Card>
                      <Card.Content>
                        <Card.Header>{job.id}</Card.Header>
                        <Card.Meta>{job.jobType}</Card.Meta>
                        <Card.Description>{JSON.stringify(job, null, 2)}</Card.Description>
                      </Card.Content>
                      <Card.Content extra>
                        <Button basic color='red' onClick={() => this.onKill(job, auth)}>Kill</Button>
                        {this.state.failed[job.id] && 
                          <Message>
                            <Message.Header>
                              No active jobs
                            </Message.Header>
                            <p>No active jobs found on the server</p>
                          </Message>
                        }
                      </Card.Content>
                    </Card>
                  );
                })}
              </Card.Group>
            );

          }
        }}
      </Subscribe>
    );
  }
}