import React , {Component} from 'react'
import {Input, FormGroup, Label, FormText, FormFeedback} from 'reactstrap'
import PropTypes from 'prop-types'

/**
 * Free hand input - text, textarea, numbers
 * Require from the caller component this.state.data[attribute] and this.state.labels[attribute] as a valid label
 * Require FormFieldDTO object for this.state.data[attribute]
 * @example
 * <FieldInput mode='text' attribute='firstName' component={this}} />
 */
class FieldInput extends Component{
    constructor(props){
        super(props)
        this.ensureText=this.ensureText.bind(this)
        this.isValid=this.isValid.bind(this)
        this.isStrict=this.isStrict.bind(this)
    }

    /**
     * Ensure valid string value
     * @param {string} value 
     */
    ensureText(value){
        if(typeof value == 'undefined'){
            return ""
        }
        if(value==null){
            return ""
        }
        if(typeof value == 'string' || typeof value == 'number'){
            return value
        }
        return "";
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

    render(){
        let component = this.props.component
        let key = this.props.attribute
        let disabled=false
        if(this.props.disabled){
            disabled=true
        }
        let rows = this.props.rows== undefined?"":this.props.rows
        if(typeof component.state.data != 'undefined' && typeof component.state.data[key] != 'undefined'){
            component.state.data[key].justloaded=false
            return(
                <FormGroup style={{height:'auto'}}>
                <Label for={key}>
                    {component.state.labels[key]}
                </Label>
                <Input disabled={disabled} bsSize='sm' type={this.props.mode} id={key} lang={component.state.labels.locale} step="0.01" rows={rows}
                    value={this.ensureText(component.state.data[key].value)}
                    onChange={(e)=>{
                        let s = component.state
                        s.data[key].value=e.target.value
                        component.setState(s)
                    }}
                    valid={this.isValid() && this.isStrict()}
                    invalid={!this.isValid() && this.isStrict()}/>
                <FormFeedback valid={false}>{component.state.data[key].suggest}</FormFeedback>
                <FormText hidden={this.isStrict() || this.isValid()}>{component.state.data[key].suggest}</FormText>
            </FormGroup>
            )
        }else{
            return []
        }
    }


}
export default FieldInput
FieldInput.propTypes={
    disabled:PropTypes.bool,
    mode: PropTypes.oneOf(['text','textarea','number','boolean']).isRequired, //type of data
    attribute  :PropTypes.string.isRequired,                        //should be component.state.labels[attribute] and component.state.data[attribute]
    component   :PropTypes.object.isRequired,                        //caller component
    rows        :PropTypes.string                                   //rows in textarea
}