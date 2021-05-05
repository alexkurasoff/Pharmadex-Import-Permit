import React , {Component} from 'react'
import {Button} from 'reactstrap'
import PropTypes from 'prop-types'
import { isUndefined } from 'util'

/**
 * The uniform button
 * @example for not outline button <ButtonUni onClick={this.buttonAction} label={this.state.labels.action} outline={false} disabled={false}/>
 */
class ButtonUni extends Component{
    constructor(props){
        super(props)
    }

    render(){
        let outline=true
        if(isUndefined(this.props.outline)){
            outline=true
        }else{
            outline=this.props.outline
        }
        let disabled=false
        if(!isUndefined(this.props.disabled)){
            disabled=this.props.disabled
        }
        let edit=true
        if(!isUndefined(this.props.edit)){
            edit=this.props.edit
        }
        if(edit){
            return(
                <Button className="btn-block mt-1" size="sm" outline={outline} disabled={disabled}  color="primary" 
                onClick={this.props.onClick}
                >
                {this.props.label}
                </Button>
            )
         }else{
            return []
        }
    }


}
export default ButtonUni
ButtonUni.propTypes={
    onClick:PropTypes.func.isRequired,
    label:PropTypes.string.isRequired,
    outline:PropTypes.bool,
    disabled:PropTypes.bool
}