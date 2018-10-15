import React from 'react';
import Para from './Para';
import { Icon } from "semantic-ui-react"

const Loading = ({fitted, p}) => (
  fitted
  ? <Icon fitted name="spinner" loading/>
  : <Para p={p}><Icon name="spinner" loading/></Para>
)

export default Loading;