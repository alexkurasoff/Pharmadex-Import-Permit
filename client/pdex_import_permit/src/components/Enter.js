import React , {Component} from 'react'
import Locales from './utils/Locales';
import { Container} from 'reactstrap';


/**
 * Enter to the program for non login user
 */
class Enter extends Component {   

    constructor( props, context ) {
        super( props, context );
        this.state={
            labels:{
                termsAndConditions_text:""
            }
        }
    }

    /**
     * Format a text as an array of HTML components
     * @param {string} text 
     */
    static formatText(text){
        let strings = text.split("\n")
        let ret =[]
        strings.forEach((element)=>{
            ret.push(<span key={ret.length}>{element}<br></br></span>);
        })
        return ret;
    }

    componentDidMount() {
       Locales.resolveLabels(this)
    }
    render() {
            if(this.state.labels.termsAndConditions_text.length>0){
            return(  
            <Container fluid>
                {Enter.formatText(this.state.labels.termsAndConditions_text)}
            </Container>
            )
            }else{
                return []
            }
             
        }
   }
   
   export default Enter;     