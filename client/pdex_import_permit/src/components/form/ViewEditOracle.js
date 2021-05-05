import React , {Component} from 'react'
import PropTypes from 'prop-types'
import FieldOracle from './FieldOracle'
import FieldDisplay from'./FieldDisplay'

/**
 * View or edit data for oracle fields, i.e. choices from pre-defined set of values
 * @example
 * <ViewEditOracle attribute='country' api='/common/countries' component={this} edit /> will be editable
 * <ViewEditOracle attribute='country' api='/common/countries' component={this} hideEmpty/>      will not be editable and hide when empty
 */
class ViewEditOracle extends Component{

    constructor(props){
        super(props)
    }

    render(){
        if(this.props.edit){
            return <FieldOracle attribute={this.props.attribute} api={this.props.api} component={this.props.component} addlabel="" selectlabel=""/>
        }else{
            return <FieldDisplay mode='text' attribute={this.props.attribute} component={this.props.component} hideEmpty={this.props.hideEmpty}/>
        }
    }

}
export default ViewEditOracle
ViewEditOracle.propTypes={
    attribute  :PropTypes.string.isRequired,                        //should be component.state.labels[attribute] and component.state.data[attribute]
    component   :PropTypes.object.isRequired,                       //caller component
    api        :PropTypes.string.isRequired,                       //api call for a list
    edit:   PropTypes.bool,                                          //view or edit
    hideEmpty: PropTypes.bool                                       //hide empty fields in display mode
}
