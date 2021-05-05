import React , {Component} from 'react'
import {FormGroup,Label} from 'reactstrap'
import PropTypes from 'prop-types'
import Option from './Option'
import { isUndefined } from 'util'

/**
 * Option FormGroup (label + field) for ApplicationForm (may be used for any form)
 * Require from teh caller component this.state.data[attribute] with optionsDTO type and this.state.labels[attribute] as a valid label
 * @example <FieldOption attribute='port' component={this} />
 */
class FieldOption extends Component{
    constructor(props){
        super(props)
        this.isValid=this.isValid.bind(this)
        this.isStrict=this.isStrict.bind(this)
        this.label=this.label.bind(this)
    }

    /**
     * Is this field  valid?
     */
    isValid(){
        let component = this.props.component
        let key = this.props.attribute
        return !component.state.data[key].error
    }

    /**
     * Is this check preliminary or final
     */
    isStrict(){
        let component = this.props.component
        let key = this.props.attribute
        return component.state.data[key].strict
    }
    /**
     * Label or not label
     */
    label(){
        let component = this.props.component
        let key = this.props.attribute
        if(component.state.labels[key] != undefined){
            return(
            <Label for={key}>
                {component.state.labels[key]}
            </Label>
            )
        }else{
            return []
        }
    }
    

    render(){
        let component = this.props.component
        let key = this.props.attribute
        if(!isUndefined(component.state.data[key])){
            component.state.data[key].justloaded=false
            return(
                <FormGroup>
                    {this.label()}
                    <Option id={key} bsSize="sm"
                            value={component.state.data[key].value}
                            onChange={(e)=>{
                                component.state.data[key].value=e
                                component.setState(component.state.data[key])
                            }}
                            valid={this.isValid()}
                            strict={this.isStrict()}
                            suggest={component.state.data[key].suggest}
                    />
                </FormGroup>
            )
        }else{
            return []
        }
    }


}
export default FieldOption
FieldOption.propTypes={
    attribute  :PropTypes.string.isRequired,   //name of a OptionDTO type attribute
    component   :PropTypes.object.isRequired  //caller component

}