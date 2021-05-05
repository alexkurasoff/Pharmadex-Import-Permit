import React , {Component} from 'react'
import {Card, CardHeader, CardBody,Row, Col, Button} from 'reactstrap'
import PropTypes from 'prop-types'
import Locales from './utils/Locales'
import Fetchers from './utils/Fetchers'
import Question from './Question'

/**
 * Load verification checklist for an application given
 * Read only
 * @property {string} appId - application id
 * @example <VerificationResults appId={this.props.appId} />
 */
class VerificationResults extends Component{
    constructor(props){
        super(props)
        this.state={
            data:{
                id:props.appId,
            },
            labels:{
                    checklist:"",
                    global_yes:"",
                    global_no:"",
                    global_na:"",
                    ask:"",
                    eQuestion:"",
                    expert:"",
                    global_close:"",
                    notes:"",
                    verificationresults:"",
            }
        }
        this.loadData=this.loadData.bind(this)
        this.checkList=this.checkList.bind(this)
    }
    componentDidMount(){
        Locales.resolveLabels(this)
        this.loadData()
    }

    loadData(){
        Fetchers.postJSON("/api/common/verification/result", this.state.data, (query,result)=>{
            this.state.data=result
            this.setState(this.state.data)
        })
    }

        /**
     * Create a checklist
     */
    checkList(){
        let ret=[]
        if(Fetchers.isGoodArray(this.state.data.questions)){
            this.state.data.questions.forEach(element => {
                ret.push(
                    <Question key={element.order} question={element} labels={this.state.labels}
                     check={false}
                     readOnly={true} />
                )
            });
        }
        return ret
    }

    render(){
        if(Fetchers.isGoodArray(this.state.data.questions)){
            return(
                <Card style={{fontSize:"0.8rem"}}>
                    <CardHeader>
                        <Row>
                        <Col>
                            <h6>{this.state.labels.verificationresults}</h6>
                        </Col>
                        </Row>
                    </CardHeader>
                    <CardBody>
                        {this.checkList()}
                    </CardBody>

                </Card>
            )
        }else{
            return []
        }
    }


}
export default VerificationResults
VerificationResults.propTypes={
    appId:PropTypes.string.isRequired
}