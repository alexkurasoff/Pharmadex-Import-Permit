import React , {Component} from 'react'
import {Card, CardHeader, CardBody,Row, Col, Button} from 'reactstrap'
import PropTypes from 'prop-types'
import Locales from './utils/Locales'
import Fetchers from './utils/Fetchers'

/**
 * Allow the current user edit own profile
 */
class UserProfile extends Component{
    constructor(props){
        super(props)
        this.state={
            labels:{}
        }
        this.cancel=this.cancel.bind(this)
    }
    componentDidMount(){
        Locales.resolveLabels(this)
    }

    cancel(){
        window.history.back()
    }

    render(){
        return(
            <Card style={{fontSize:"0.8rem"}}>
                <CardHeader>
                    <Row>
                    <Col xs='12' sm='12' lg='1' xl='1' className="text-right">
                        <Button close onClick={this.cancel}/>
                    </Col>
                    </Row>
                </CardHeader>
                <CardBody>

                </CardBody>

            </Card>
        )
    }


}
export default UserProfile
UserProfile.propTypes={
    
}