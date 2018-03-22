import React, { Component } from 'react';
import TradingViewWidget from 'react-tradingview-widget';
import { Sidebar, Segment, Menu, Icon, Input, Tab, Label, Grid, Header, Select, Button, Form, Table, List } from 'semantic-ui-react'
import './App.css';

export default class Mockup extends Component {
  render() {
    return (
      <div>
        <Sidebar.Pushable as={Segment}>
          <Sidebar as={Menu} animation='overlay' direction='top' visible={true} inverted>
            <Menu.Item key='hide'><Icon name="hide"/></Menu.Item>
            <Menu.Item key='binance/BTC/VEN'><Icon name="bitcoin"/>Binance<br/>VEN-BTC<Label>2 active</Label></Menu.  Item>
            <Menu.Item key='gdax/EUR/BTC'><Icon name="bitcoin"/>GDAX<br/>BTC-EUR<Label color='green'>+10%</Label><Label>1 active</Label></Menu.Item>
            <Menu.Item key='kucoin/NEO/DRGN'><Icon name="bitcoin"/>KuCoin<br/>DRGN-NEO<Label color='red'>-5%</Label></Menu.Item>
            <Menu.Item name='new-ticker'><Icon name='add' />Add ticker</Menu.Item>
          </Sidebar>
          <Sidebar.Pusher>
            <Segment.Group horizontal>
              <Segment basic>
                <div style={{paddingTop: 50, height: 500, width: '100%', paddingBottom: 10}}>
                  <TradingViewWidget 
                    symbol="VENBTC"
                    hide_side_toolbar={false}
                    autosize
                    interval="240"
                    allow_symbol_change={false}
                    studies={['RSI@tv-basicstudies']}
                  />
                </div>
                <Tab renderActiveOnly={true} panes={[
                  {
                    menuItem: { key: 'simple', content: 'Instant orders' },
                    render: () => <Tab.Pane>
                      <Grid columns={2} divided>
                        <Grid.Row>
                          <Grid.Column width="200">
                            <Header as='h2'>
                              <Icon color="green" name='plus' />
                              <Header.Content>
                                Limit Buy
                              </Header.Content>
                            </Header>
                            <Form>
                              <Form.Field>
                                <label>Price</label>
                                <Input type='text' placeholder='Enter price...' action>
                                  <input />
                                  <Button color="green" type='submit'>Market</Button>
                                </Input>
                              </Form.Field>
                              <Form.Field>
                                <label>Amount</label>
                                <Input type='text' placeholder='Enter amount...' action>
                                  <input />
                                  <Select compact options={[
                                    { key: 'VEN', text: 'VEN / 23.1232', value: 'VEN' }, 
                                    { key: 'BTC', text: 'BTC / 0.0245234', value: 'BTC' }
                                  ]} defaultValue='VEN' />
                                  <Button color="green" type='submit'>MAX</Button>
                                </Input>
                              </Form.Field>
                              <Button color="green" type='submit'>Submit</Button>
                            </Form>
                          </Grid.Column>
                          <Grid.Column width="200">
                            <Header as='h2'>
                              <Icon color="red" name='minus' />
                              <Header.Content>
                                Limit Sell
                              </Header.Content>
                            </Header>
                            <Form>
                              <Form.Field>
                                <label>Price</label>
                                <Input type='text' placeholder='Enter price...' action>
                                  <input />
                                  <Button color="red" type='submit'>Market</Button>
                                </Input>
                              </Form.Field>
                              <Form.Field>
                                <label>Amount</label>
                                <Input type='text' placeholder='Enter amount...' action>
                                  <input />
                                  <Select compact options={[
                                    { key: 'VEN', text: 'VEN / 23.1232', value: 'VEN' }, 
                                    { key: 'BTC', text: 'BTC / 0.0245234', value: 'BTC' }
                                  ]} defaultValue='VEN' />
                                  <Button color="red" type='submit'>MAX</Button>
                                </Input>
                              </Form.Field>
                              <Button color="red" type='submit'>Submit</Button>
                            </Form>
                          </Grid.Column>
                        </Grid.Row>
                      </Grid>
                    </Tab.Pane>
                  },
                  {
                    menuItem: { key: 'trailing', content: 'Stop orders' },
                    render: () => <Tab.Pane loading>

                    </Tab.Pane>,
                  },
                  {
                    menuItem: { key: 'trailing', content: 'Trailing stops' },
                    render: () => <Tab.Pane>
                      <Grid columns={2} divided>
                        <Grid.Row>
                          <Grid.Column width="200">
                            <Header as='h2'>
                              <Icon color="green" name='plus' />
                              <Header.Content>
                                Buy on reversal
                              </Header.Content>
                            </Header>
                            <Form>
                              <Form.Field>
                                <label>Initial stop price</label>
                                <Input type='text' placeholder='Enter price...' action>
                                  <input />
                                  <Button color="green" type='submit'>Bid</Button>
                                </Input>
                              </Form.Field>
                              <Form.Field>
                                <label>Limit price</label>
                                <Input type='text' placeholder='Enter price...' action>
                                  <input />
                                  <Button color="green" type='submit'>Market</Button>
                                </Input>
                              </Form.Field>
                              <Form.Field>
                                <label>Amount</label>
                                <Input type='text' placeholder='Enter amount...' action>
                                  <input />
                                  <Select color="green" compact options={[
                                    { key: 'VEN', text: 'VEN / 23.1232', value: 'VEN' }, 
                                    { key: 'BTC', text: 'BTC / 0.0245234', value: 'BTC' }
                                  ]} defaultValue='VEN' />
                                  <Button color="green" type='submit'>MAX</Button>
                                </Input>
                              </Form.Field>
                              <Button color="green" type='submit'>Submit</Button>
                            </Form>
                          </Grid.Column>
                          <Grid.Column width="200">
                            <Header as='h2'>
                              <Icon color="red" name='minus' />
                              <Header.Content>
                                Sell on reversal
                              </Header.Content>
                            </Header>
                            <Form>
                              <Form.Field>
                                <label>Initial stop price</label>
                                <Input type='text' placeholder='Enter price...' action>
                                  <input />
                                  <Button color="red" type='submit'>Bid</Button>
                                </Input>
                              </Form.Field>
                              <Form.Field>
                                <label>Limit price</label>
                                <Input type='text' placeholder='Enter price...' action>
                                  <input />
                                  <Button color="red" type='submit'>Market</Button>
                                </Input>
                              </Form.Field>
                              <Form.Field>
                                <label>Amount</label>
                                <Input type='text' placeholder='Enter amount...' action>
                                  <input />
                                  <Select color="red" compact options={[
                                    { key: 'VEN', text: 'VEN / 23.1232', value: 'VEN' }, 
                                    { key: 'BTC', text: 'BTC / 0.0245234', value: 'BTC' }
                                  ]} defaultValue='VEN' />
                                  <Button color="red" type='submit'>MAX</Button>
                                </Input>
                              </Form.Field>
                              <Button color="red" type='submit'>Submit</Button>
                            </Form>
                          </Grid.Column>
                        </Grid.Row>
                      </Grid>
                    </Tab.Pane>,
                  },
                  {
                    menuItem: { key: 'complex', content: 'Complex order' },
                    render: () => <Tab.Pane loading>

                    </Tab.Pane>,
                  },
                ]} />
              </Segment>
              <Segment compact>
                <div style={{paddingTop: 50}}>
                  <Header as='h2'>
                    <Icon name='hourglass half' />
                    <Header.Content>Active</Header.Content>
                  </Header>
                  <Table celled>
                    <Table.Header>
                      <Table.Row>
                        <Table.HeaderCell>#</Table.HeaderCell>
                        <Table.HeaderCell>Description</Table.HeaderCell>
                        <Table.HeaderCell>Kill</Table.HeaderCell>
                      </Table.Row>
                    </Table.Header>
                    <Table.Body>
                      <Table.Row>
                        <Table.Cell>200485293DGCDSFDS</Table.Cell>
                        <Table.Cell>Trailing stop on binance/BTC/VEN at 0.0023623</Table.Cell>
                        <Table.Cell><Button type='submit'><Icon name="x"/></Button></Table.Cell>
                      </Table.Row>
                    </Table.Body>
                  </Table>
                  <Segment>
                    <List divided relaxed>
                      <List.Item>
                        <List.Icon name='lightbulb' size='large' verticalAlign='middle' />
                        <List.Content>
                          <List.Header as='a'>2018-03-03 00:17:12</List.Header>
                          <List.Description as='a'>Active. Stop not reached</List.Description>
                        </List.Content>
                      </List.Item>
                      <List.Item>
                        <List.Icon name='lightbulb' size='large' verticalAlign='middle' />
                        <List.Content>
                          <List.Header as='a'>2018-03-03 00:17:12</List.Header>
                          <List.Description as='a'>Active. Stop not reached</List.Description>
                        </List.Content>
                      </List.Item>
                      <List.Item>
                        <List.Icon name='lightbulb' size='large' verticalAlign='middle' />
                        <List.Content>
                          <List.Header as='a'>2018-03-03 00:17:12</List.Header>
                          <List.Description as='a'>Active. Stop not reached</List.Description>
                        </List.Content>
                      </List.Item>
                    </List>
                  </Segment>
                </div>
              </Segment>
            </Segment.Group>
          </Sidebar.Pusher>
        </Sidebar.Pushable>
      </div>
    )
  }
}