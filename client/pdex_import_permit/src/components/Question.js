import React , {Component} from 'react'
import {Row, Col, ButtonGroup, Button} from 'reactstrap'
import PropTypes from 'prop-types'
import Fetchers from './utils/Fetchers'
import QuestionAsk from './QuestionAsk'
import QuestionAnswerNotes from './QuestionAnswerNotes'


/**
 * Checklists question or header
 * @example
 * <Question question={this.state.questions[0]} labels={this.state.labels} check={!this.state.data.valid} readOnly={this.isSubitted()}/>
 */
class Question extends Component{
    constructor(props){
        super(props)
        this.state={
            notes:false,
            selectExpert:false,
            labels:{},
            data:{
                id:0,
            }
        }
        this.saveAnswer=this.saveAnswer.bind(this)
        this.rowClass=this.rowClass.bind(this)
        this.buttonGroupComponent=this.buttonGroupComponent.bind(this)
        this.activeComponent=this.activeComponent.bind(this)
        this.waitOrAnswered=this.waitOrAnswered.bind(this)
    }

    componentDidMount(){
        this.state.labels=this.props.labels
        this.state.data=this.props.question
        this.state.data.answer.fields=Object.keys(this.props.labels)
        this.setState(this.state)
    }


    /**
     * Save an answer
     */
    saveAnswer(question){
        if(!this.props.readOnly){
            Fetchers.postJSONNoSpinner("/api/common/answer/save",question, (query,result)=>{
                this.state.data=result
                if(result.answer.valid){
                    this.state.selectExpert=false
                }
                this.setState(this.state)
            })
        }
    }
    /**
     * The row class
     */
    rowClass(){
        let ret = "border mb-1"
        if(this.props.check){
            let question = this.state.data
            if(question.answer.yes || question.answer.no || question.answer.na){
                ret = "border border-success mb-1"
            }else{
                ret= "border border-danger mb-1"
            }
        }
        return(ret)
    }
    /**
     * select an active component
     */
    activeComponent(){
        if(this.state.selectExpert){
            return(
                <QuestionAsk 
                    question={this.state.data}
                    labels={this.state.labels}
                    save={(answer)=>{
                        this.state.data.answer=answer
                        this.state.data.answer.ask=true;
                        this.saveAnswer(this.state.data)
                        }
                    }
                    close={()=>{
                        this.state.selectExpert=false
                        this.setState(this.state)
                        }
                    }
                />
            )
        }else{
            return this.buttonGroupComponent()
        }
    }
    /**
     * Did expert answer on this question or wait for
     */
    waitOrAnswered(){
        let question = this.state.data
        if(question.answer.ask){
            return(
                <span>
                    <i className="fa fa-calendar text-warning" aria-hidden="true"/>
                    &nbsp;
                    {question.answer.message}
                </span>
            )
        }
        if(question.answer.answered){
            return (
                <span>
                    <i className="fa fa-check text-success" aria-hidden="true"/>
                    &nbsp;
                    {question.answer.message}
                </span>
            )
        }
        return []
    }
    /**
     * question's buttons
     */
    buttonGroupComponent(){
        let question = this.state.data
        if(question==undefined){
            return []
        }else{
            if(question.head){
                return(
                <Row>
                    <Col>
                        <b>{question.question}</b>
                    </Col>
                </Row>
                )
            }else{
                return(
                <Row className={this.rowClass()}>
                    <Col>
                        <Row>
                            <Col xs='12' sm='12' lg='6' xl='7'>
                                <label>{question.question}</label>
                            </Col>
                            <Col xs='12' sm='12' lg='6' xl='5'>
                                <Row>
                                    <Col>
                                        {this.waitOrAnswered()}
                                    </Col>
                                </Row>
                                <Row>
                                    <Col className="d-flex justify-content-end">
                                        <ButtonGroup className="m-1">
                                            <Button size="sm" outline color="success" key="1"
                                                active={question.answer.yes}
                                                onClick={()=>{
                                                    this.state.notes=false;
                                                    question.answer.yes=true;
                                                    question.answer.no=false;
                                                    question.answer.na=false;
                                                    question.answer.ask=false;
                                                    question.answer.answered=false;
                                                    this.saveAnswer(question)
                                                }}>
                                                    {this.props.labels.global_yes}
                                            </Button>
                                            <Button size="sm" outline color="primary" key="2"
                                                active={question.answer.no}
                                                onClick={()=>{
                                                    this.state.notes=false;
                                                    question.answer.yes=false;
                                                    question.answer.no=true;
                                                    question.answer.na=false;
                                                    question.answer.ask=false;
                                                    question.answer.answered=false;
                                                    this.saveAnswer(question)
                                                }}>
                                                    {this.props.labels.global_no}
                                            </Button>
                                            <Button size="sm" outline color="secondary" key="3"
                                                active={question.answer.na}
                                                onClick={()=>{
                                                    this.state.notes=false;
                                                    question.answer.yes=false;
                                                    question.answer.no=false;
                                                    question.answer.na=true;
                                                    question.answer.ask=false;
                                                    question.answer.answered=false;
                                                    this.saveAnswer(question)
                                                }}>
                                                    {this.props.labels.global_na}
                                            </Button>
                                            <Button size="sm" outline color="secondary" key="4" hidden={this.props.readOnly}
                                                active={question.answer.ask}
                                                onClick={()=>{
                                                    this.state.notes=false;
                                                    question.answer.yes=false;
                                                    question.answer.no=false;
                                                    question.answer.na=false;
                                                // question.answer.ask=true;
                                                // question.answer.answered=false;
                                                    this.state.selectExpert=true
                                                    this.setState(this.state)
                                                }}>
                                                    {this.props.labels.ask}
                                            </Button>
                                            <Button size="sm" outline color="secondary" key="5" hidden={this.props.readOnly}
                                                active={false}
                                                onClick={()=>{
                                                this.state.notes=!this.state.notes;
                                                this.setState(this.state)
                                                }}>
                                                    {this.props.labels.notes}
                                            </Button>
                                        </ButtonGroup>
                                    </Col>
                                </Row>
                                </Col>
                            </Row>
                            <Row>
                                <Col>
                                    <QuestionAnswerNotes component={this}/>
                                </Col>
                            </Row>
                        </Col>
                    </Row>

                )
            }
        }
    }
 
    render(){
        if(this.state.data.id==0){
            return []
        }else{
            return this.activeComponent()
        }
    }


}
export default Question
Question.propTypes={
    question:PropTypes.object.isRequired,    //QuestionDTO structured object
    labels:PropTypes.object.isRequired,      // labels should be from the upper object
    check:PropTypes.bool.isRequired,       // paint red border around not answered and green border around answered 
    readOnly:PropTypes.bool.isRequired      //disable edit answer
}