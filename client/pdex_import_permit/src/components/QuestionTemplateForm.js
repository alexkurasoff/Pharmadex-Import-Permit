import React , {Component} from 'react'
import {Row, Col, CustomInput} from 'reactstrap'
import PropTypes from 'prop-types'
import FieldInput from './form/FieldInput'
import ButtonUni from './form/ButtonUni'

/**
 * It is a dummy component to create other components quickly
 * Just copy it
 */
class QuestionTemplateForm extends Component{
    constructor(props){
        super(props)
        this.state={
            labels:{},
            data:{}
        }
        this.cancel=this.cancel.bind(this)
    }
    componentDidMount(){
        //Locales.resolveLabels(this)
    }

    cancel(){
        window.history.back()
    }

    render(){
        let parent=this.props.questionTemplate
        this.state.data = parent.state.data
        this.state.labels = parent.state.labels
        if(parent.state.showform){
            return(
                <Row>
                    <Col>
                        <Row>
                            <Col xs="12" sm="12" lg="2" xl="2">
                                <CustomInput type="checkbox" id={this.state.data.id} 
                                        label={this.state.labels.head} inline 
                                        value={this.state.data.head.value} 
                                        checked={this.state.data.head.value}
                                        onChange={(e)=>{
                                            let s = this.state
                                            s.data.head.value=!s.data.head.value
                                            this.setState(s)
                                        }}
                                />
                            </Col>  
                        </Row>
                        <Row>
                            <Col xs="12" sm="12" lg="5" xl="5">
                                <FieldInput mode='textarea' attribute="quest" component={this}/>
                            </Col>
                            <Col xs="12" sm="12" lg="5" xl="5">
                                <FieldInput mode='textarea' attribute="questportu" component={this}/>
                            </Col>
                            <Col xs="12" sm="12" lg="2" xl="2">
                                <Row>
                                    <Col>
                                        <ButtonUni 
                                            label={this.state.labels.save}
                                            outline={false}
                                            onClick={()=>{
                                                parent.state.showform=false
                                                this.state.data.edit=true;
                                                parent.onClickSave()
                                            }}
                                        />
                                    </Col>
                                </Row>
                                <Row>
                                    <Col>
                                    <ButtonUni 
                                            label={this.state.labels.cancel}
                                            outline={true}
                                            onClick={()=>{
                                                parent.state.data.edit=false;
                                                parent.state.data.add=false;
                                                parent.state.showform=false
                                                parent.setState(parent.state)
                                            }}
                                        />
                                    </Col>
                                </Row>
                            </Col>
                        </Row>
                    </Col>
                </Row>
                
            )
        }else{
            return []
        }
        
    }


}
export default QuestionTemplateForm
QuestionTemplateForm.propTypes={
    questionTemplate:PropTypes.object.isRequired
}