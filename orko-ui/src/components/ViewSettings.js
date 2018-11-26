import React, { Component } from "react"

import Section from "./primitives/Section"
import Modal from "./primitives/Modal"
import Href from "./primitives/Href"
import { Icon } from "semantic-ui-react"
import Form from "./primitives/Form"
import Button from "./primitives/Button"
import FormButtonBar from "./primitives/FormButtonBar"
import Checkbox from "./primitives/Checkbox"
import Immutable from "seamless-immutable"

export default class ViewSettings extends Component {
  render() {
    return (
      <Modal>
        <Section
          id="viewSettings"
          heading={"View settings"}
          buttons={() => (
            <Href title="Close" onClick={() => this.props.onClose()}>
              <Icon fitted name="close" />
            </Href>
          )}
        >
          <Form>
            {this.props.panels.map(panel => (
              <Checkbox
                key={panel.key}
                id={"panel" + panel.key}
                label={panel.title}
                type="checkbox"
                checked={panel.visible}
                onChange={() =>
                  this.props.onChangePanels(
                    Immutable([{ key: panel.key, visible: !panel.visible }])
                  )
                }
              />
            ))}
            <FormButtonBar>
              <Button onClick={this.props.onReset}>Reset</Button>
              <Button onClick={this.props.onClose}>OK</Button>
            </FormButtonBar>
          </Form>
        </Section>
      </Modal>
    )
  }
}
