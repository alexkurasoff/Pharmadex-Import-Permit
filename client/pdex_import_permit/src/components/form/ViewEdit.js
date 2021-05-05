import React , {Component} from 'react'
import PropTypes from 'prop-types'
import FieldInput from './FieldInput'
import FieldDisplay from'./FieldDisplay'

/**
 * View or edit text, textarea or numeric
 * Free hand input or display - text, textarea, numbers along with label above
 * Require from the caller component this.state.data[attribute] and this.state.labels[attribute] as a valid label
 * Require FormFieldDTO object for this.state.data[attribute]
 * @example
 * <ViewEdit mode='text' attribute='firstName' component={this} edit /> will be editable
 * <ViewEdit mode='text' attribute='firstName' component={this} />      will not be editable
 */
class ViewEdit extends Component{

    constructor(props){
        super(props)
    }



    render(){
        if(this.props.edit){
            return <FieldInput mode={this.props.mode} attribute={this.props.attribute} component={this.props.component} rows={this.props.rows} />
        }else{
            return <FieldDisplay mode={this.props.mode} attribute={this.props.attribute} component={this.props.component} hideEmpty={this.props.hideEmpty}/>
        }
    }

}
export default ViewEdit
ViewEdit.propTypes={
    mode: PropTypes.oneOf(['text','textarea','number','boolean']).isRequired,
    attribute  :PropTypes.string.isRequired,                        //should be component.state.labels[attribute] and component.state.data[attribute]
    component   :PropTypes.object.isRequired,                        //caller component
    edit:   PropTypes.bool,                                           //view or edit
    hideEmpty : PropTypes.bool,                                       //hide empty in display mode
    rows:PropTypes.string                                             //rows in textarea                            
}
