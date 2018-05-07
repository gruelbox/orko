import React from 'react';
import Para from './Para';
import { Icon } from "semantic-ui-react"

const Loading = ({fitted}) => (
  fitted
  ? <Icon fitted name="spinner" loading/>
  : <Para p={2}><Icon name="spinner" loading/></Para>
)

export default Loading;