import React , {Component} from 'react'
import PropTypes from 'prop-types'
import FieldDate from './FieldDate'
import FieldDisplay from'./FieldDisplay'

/**
 * View or edit date
 * Require from the caller component this.state.data[attribute] and this.state.labels[attribute] as a valid label
 * Require FormFieldDTO object for this.state.data[attribute]
 * @example
 * <ViewEditDate attribute='firstName' component={this} edit /> will be editable
 * <ViewEdit attribute='firstName' component={this} />      will not be editable
 */
class ViewEditDate extends Component{

    constructor(props){
        super(props)
    }

    render(){
        if(this.props.edit){
            return <FieldDate attribute={this.props.attribute} component={this.props.component} />
        }else{
            return <FieldDisplay mode='date' attribute={this.props.attribute} component={this.props.component}/>
        }
    }

}
export default ViewEditDate
ViewEditDate.propTypes={
    attribute  :PropTypes.string.isRequired,                        //should be component.state.labels[attribute] and component.state.data[attribute]
    component   :PropTypes.object.isRequired,                       //caller component
    edit:   PropTypes.bool                                          //view or edit
}
