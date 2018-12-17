import React from "react"
import Href from "./primitives/Href"
import { Icon } from "semantic-ui-react"
import ReactTable from "react-table"
import "prismjs/components/prism-clike"
import "prismjs/components/prism-javascript"

const textStyle = {
  textAlign: "left"
}

const Scripts = ({ scripts, onDelete, onSelect }) => (
  <ReactTable
    data={scripts}
    columns={[
      {
        id: "close",
        Header: null,
        Cell: ({ original }) => (
          <Href title="Remove script" onClick={() => onDelete(original)}>
            <Icon fitted name="close" />
          </Href>
        ),
        headerStyle: textStyle,
        style: textStyle,
        width: 32,
        sortable: false,
        resizable: false
      },
      {
        id: "name",
        Header: "name",
        Cell: ({ original }) => (
          <Href
            data-orko={original.id + "/select"}
            title={"Select " + original.name}
            onClick={() => onSelect(original)}
          >
            {original.name}
          </Href>
        ),
        headerStyle: textStyle,
        style: textStyle,
        resizable: true,
        minWidth: 50
      }
    ]}
    showPagination={false}
    resizable={false}
    className="-striped"
    minRows={0}
    noDataText="No scripts"
  />
)

export default Scripts
