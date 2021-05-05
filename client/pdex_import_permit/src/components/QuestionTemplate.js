import React , {Component} from 'react'
import {Row, Col, ButtonGroup, Button} from 'reactstrap'
import Fetchers from './utils/Fetchers'
import Alerts from './utils/Alerts'
import QuestionTemplateForm from './QuestionTemplateForm'

/**
 * Template question
 * 
 */
class QuestionTemplate extends Component{
    constructor(props){
        super(props)
        this.state={
            showform:false,
            labels:{
            },
            data:{
                id:0
            }
        }
        
        this.listQuestions=this.listQuestions.bind(this)
        this.findQuestion=this.findQuestion.bind(this)
        this.activeComponent=this.activeComponent.bind(this)
    }

    componentDidMount(){
        this.state.labels=this.props.checkList.state.labels
        this.state.data.id=this.props.id
        this.setState(this.state)
    }

    /**
     * Get product data from property
     */
    listQuestions(){
        return this.props.checkList.state.data.questions
    }

    findQuestion(){
        if(this.state.data.id > 0){
            if(Fetchers.isGoodArray(this.listQuestions())){
                var s = this.listQuestions().length
                for(var i = 0; i < s; i++){
                    let q = this.listQuestions()[i]
                    if(this.state.data.id == q.id){
                        this.state.data = q
                        break
                    }
                }
            }
        }
    }

    buildLabel(){
        if(this.state.data.head.value){
            return (
                <b>{this.state.data.question}</b>
            )
        }else{
            return(
                <label>{this.state.data.question}</label>
            )
        }
    }

    onClickSave(){
        if(Fetchers.isGoodArray(this.listQuestions())){
            var s = this.listQuestions().length
            for(var i = 0; i < s; i++){
                let q = this.listQuestions()[i]
                if(this.state.data.id == q.id){
                    this.listQuestions()[i] = this.state.data
                }
            }
        }
        this.props.checkList.saveCheckList()
    }

    activeComponent(){
        return(
            <Row className="border mb-1">
                <Col>
                    <Row>
                        <Col xs='12' sm='12' lg='6' xl='7'>
                            {this.buildLabel()}
                        </Col>
                        <Col xs='12' sm='12' lg='6' xl='5'>
                            <Row>
                                <Col className="d-flex justify-content-end">
                                    <ButtonGroup className="m-1">
                                        <Button size="sm" outline color="primary" key="1" 
                                        onClick={()=>{
                                            Alerts.warning(this.state.labels.global_delete + "?", ()=>{
                                                this.state.showform=false;
                                                this.state.data.delete=true;
                                                this.onClickSave()
                                            }, ()=>{ })
                                            }}>
                                            {this.state.labels.global_delete}
                                        </Button>
                                        <Button size="sm" outline color="primary" key="2"
                                        onClick={()=>{
                                            this.state.data.edit=true;
                                            this.state.showform=true;
                                            this.setState(this.state)
                                        }}>
                                            {this.state.labels.label_edit}
                                        </Button>
                                        <Button size="sm" outline color="primary" key="3"
                                        onClick={()=>{
                                            this.state.showform=false;
                                            this.state.data.up=true;
                                            this.onClickSave()
                                            }}>
                                            {this.state.labels.label_up}
                                        </Button>
                                        <Button size="sm" outline color="primary" key="4"
                                        onClick={()=>{
                                            this.state.showform=false;
                                            this.state.data.down=true;
                                            this.onClickSave()
                                            }}>
                                            {this.state.labels.label_down}
                                        </Button>
                                        <Button size="sm" outline color="primary" key="5"
                                        onClick={()=>{
                                            this.state.showform=false;
                                            this.state.data.add=true;
                                            this.onClickSave()
                                        }}>
                                            {this.state.labels.global_add}
                                        </Button>
                                    </ButtonGroup>
                                </Col>
                            </Row>
                        </Col>
                    </Row>
                    <Row>
                        <Col>
                             <QuestionTemplateForm questionTemplate={this}/>
                        </Col>
                    </Row>
                </Col>
            </Row>
        )
    }

    render(){
        if(this.state.data.id==0){
            return []
        }else{
            this.findQuestion()
            return this.activeComponent()
        }
    }
}

export default QuestionTemplate
QuestionTemplate.propTypes={

}