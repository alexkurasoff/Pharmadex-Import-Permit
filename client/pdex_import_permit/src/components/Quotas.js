import React , {Component} from 'react'
import QuotasAll from './QuotasAll'
import QuotasExpand from './QuotasExpand'

/**
 *
 * Display Quaotas table for an applicant
 */
class Quaotas extends Component{
    constructor(props){
        super(props)
        this.state={
            expandRow:-1,           //dbID of expanded row
        }

    }

    render(){
        if(this.state.expandRow==-1){
        return(
            <QuotasAll quotas={this} />
        )
        }else{
            return(
                <QuotasExpand quotas={this} />
            )
        }
    }

}

export default Quaotas