import React , {Component} from 'react'
import {Card, CardHeader, CardBody,Row, Col} from 'reactstrap'
import PropTypes from 'prop-types'
import ButtonUni from './form/ButtonUni'
import Alerts from './utils/Alerts'
import Fetchers from './utils/Fetchers'


/**
 * An answer to a particular question
 * @property {QuestionDTO} question
 * @property {object} labels labels from the parent component to avoid redundand same labels loading
 * @property {functin} reload - parent component function to reload list of answers
 * @example <AnswerQuestion question={question} component={this} />
 * 
 */
class QuestionAnswer extends Component{
    constructor(props){
        super(props)
        this.state={
        }
    }

    componentDidMount(){
        this.state.labels=this.props.labels
        this.setState(this.state)
    }

    render(){
        if(this.state.labels != undefined){
            return(
                <Card className="shadow m-2" style={{fontSize:"0.8rem"}}>
                    <CardHeader>
                        <Row>
                        <Col xs="12" sm="12" lg="8" xl="8">
                            <h6>{this.props.question.answer.answerHeader}</h6>
                        </Col>
                        <Col  xs="12" sm="12" lg="4" xl="4">
                            <ButtonUni onClick={()=>{
                                Alerts.warning(this.state.labels.didyouattachanswer,
                                    ()=>{
                                        //set "answered" on "Yes"
                                        Fetchers.postJSONNoSpinner("/api/common/notification/question/answered", this.props.question, (query, result)=>{
                                            this.props.reload()
                                        })
                                    },
                                    ()=>{
                                        //nothing to do on "No"
                                    })
                            }}
                            label={this.state.labels.answerattached}
                            outline={true}
                            disable={false}
                            />
                        </Col>
                        </Row>
                    </CardHeader>
                    <CardBody>
                        <Row>
                            <Col>
                                <b>{this.props.question.question}</b>
                            </Col>
                        </Row>
                        <Row>
                            <Col>
                                {this.props.question.answer.eQuestion.value}
                            </Col>
                        </Row>
                    </CardBody>
                </Card>
            )
        }else{
            return []
        }
    }


}
export default QuestionAnswer
QuestionAnswer.propTypes={
    question:PropTypes.object.isRequired,
    labels:PropTypes.object.isRequired,
    reload:PropTypes.func.isRequired,
}