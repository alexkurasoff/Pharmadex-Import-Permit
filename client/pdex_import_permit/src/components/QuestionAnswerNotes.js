import React , {Component} from 'react'
import {Alert, Row, Col} from 'reactstrap'
import PropTypes from 'prop-types'
import FieldInput from './form/FieldInput'
import ButtonUni from './form/ButtonUni'


/**
 * Notes to an answer
 * @property {object} component - <Question />component
 * @example <QuestionAnswerNotes component={this}/>
 */
class QuestionAnswerNotes extends Component{
    constructor(props){
        super(props)
        this.state={
            labels:{},
            data:{}
        }

    }

    render(){
        let component=this.props.component
        this.state.data = component.state.data.answer
        this.state.labels = component.state.labels
        if(component.state.notes){
            return(
                <Row>
                    <Col xs="12" sm="12" lg="10" xl="10">
                        <FieldInput mode='textarea'  attribute="notes" component={this}/>
                    </Col>
                    <Col xs="12" sm="12" lg="2" xl="2">
                        <Row>
                            <Col>
                                <ButtonUni 
                                    label={component.state.labels.save}
                                    outline={false}
                                    onClick={()=>{
                                        component.state.notes=false
                                        component.saveAnswer(component.state.data);
                                    }}
                                 />
                            </Col>
                        </Row>
                        <Row>
                            <Col>
                            <ButtonUni 
                                    label={component.state.labels.cancel}
                                    outline={true}
                                    onClick={()=>{
                                        component.state.notes=false
                                        component.setState(component.state)
                                    }}
                                 />
                            </Col>
                        </Row>
                    </Col>
                </Row>
            )
        }else{
            return (
                <Alert color="info" hidden={this.state.data.notes.value.length==0}>
                    {this.state.data.notes.value}
                </Alert>
            )
        }
    }


}
export default QuestionAnswerNotes
QuestionAnswerNotes.propTypes={
    component:PropTypes.object.isRequired,
}