import React , {Component} from 'react'
import {Row, Col, Button} from 'reactstrap'
import PropTypes from 'prop-types'
import FieldInput from './form/FieldInput'
import FieldOption from './form/FieldOption'
import ButtonUni from './form/ButtonUni'

/**
 * It is a dummy component to create other components quickly
 * @property {object} question object with QuestionDTO structure
 * @property {object} labels labels map
 * @property {function} save function to save the answer object
 * @property {function} close function to close this form without save 
 * @example <QuestionAsk answer={this.state.data.answer} locales={this.state.labels} save={this.saveQuestion} close={()=>{this.state.aks=false this.setState(this.state)}}
 */
class QuestionAsk extends Component{
    constructor(props){
        super(props)
        this.state={
            labels:{},
            data:{
                id:0,
            }
        }
    }
    componentDidMount(){
        this.state.labels=this.props.labels
        this.state.data=this.props.question.answer
        this.state.question=this.props.question.question
        this.setState(this.state)
    }

    render(){
        if(this.state.labels.ask==undefined){
            return []
        }else{
            this.state.data=this.props.question.answer
            return(
                <Row>
                    <Col>
                        <Row>
                            {this.props.question.question}
                        </Row>
                        <Row>
                            <Col xs="12" sm="12" lg="6" xl="6">
                                <FieldInput mode='textarea' attribute='eQuestion' component={this} />
                            </Col>
                            <Col xs="12" sm="12" lg="6" xl="6">
                                <Row>
                                    <Col>
                                        <FieldOption attribute='expert' component={this}/>
                                    </Col>
                                </Row>
                                <Row>
                                    <Col xs="12" sm="12" lg="6" xl="6">
                                        <ButtonUni onClick={()=>{this.props.save(this.state.data)}} label={this.state.labels.ask} outline={false}/>
                                    </Col>
                                    <Col xs="12" sm="12" lg="6" xl="6">
                                        <ButtonUni onClick={this.props.close} label={this.state.labels.global_close} outline={true}/>
                                    </Col>
                                </Row>
                            </Col>
                        </Row>
                    </Col>
                </Row>
            )
        }
    }
}
export default QuestionAsk
QuestionAsk.propTypes={
    question:PropTypes.object.isRequired,
    labels:PropTypes.object.isRequired,
    save:PropTypes.func.isRequired,
    close:PropTypes.func.isRequired,
}