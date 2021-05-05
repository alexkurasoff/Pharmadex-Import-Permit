import React , {Component} from 'react'
import Locales from './utils/Locales';
import { Container, Row,Col} from 'reactstrap';
import LoginForm from './LoginForm'
import Navigator from './Navigator'
import Fetchers from './utils/Fetchers';
import Enter from './Enter'


/**
 * Enter to the program for non login user
 */
class Login extends Component {   

    constructor( props, context ) {
        super( props, context );
        this.state={
            labels:{
                termsAndConditions_text:"...terms..."
            }
        }
    }

    componentDidMount() {
       Locales.resolveLabels(this)
    }
    render() {
            return (  
                    <Container fluid>
                            <Row>
                                <Col xs={0} sm={0} lg={8} xl={8} >
                                    {Enter.formatText(this.state.labels["termsAndConditions_text"])}
                                </Col>
                                <Col xs={12} sm={12} lg={4} xl={4}>
                                    <LoginForm 
                                    success={(userData)=>{
                                        window.location.assign(Fetchers.contextPath()+"/")
                                    }}
                                    cancel={()=>{
                                        Navigator.navigate("term", "Enter","void")
                                    }}/>
                                </Col>
                            </Row>
                    </Container>
            ); 
             
        }
   }
   
   export default Login;     