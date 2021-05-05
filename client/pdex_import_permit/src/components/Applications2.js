import React , {Component} from 'react'
import Applications from './Applications'
/**
 * We want to have more than one application component on a tabset. With different content, indeed
 */
class Appications2 extends Component{
    constructor(props){
        super(props)
    }
    render(){
        return <Applications />
    }
}
export default Appications2