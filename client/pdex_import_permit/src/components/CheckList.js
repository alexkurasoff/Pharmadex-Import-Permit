import React , {Component} from 'react'
import {Card, CardHeader, CardBody,Row, Col, Button, Alert} from 'reactstrap'
import PropTypes from 'prop-types'
import Locales from './utils/Locales'
import Fetchers from './utils/Fetchers'
import QuestionTemplate from './QuestionTemplate'

/**
 * Edit Checklist
 * @property {number} pipStatusId - id of the PipStatus 
 */
class CheckList extends Component{
    constructor(props){
        super(props)
        this.state={
            alertColor:"info",
            data:{
                valid:true,
                id:0,
                state:""
            },
            labels:{
                locale:"",
                editchecklist:"",
                cancelapplication:"",
                global_delete: "",
                global_add: "",
                label_edit: "",
                label_up: "",
                label_down: "",
                quest:"",
                questportu:"",
                head:"",
                save:"",
                cancel:"",
            }
        }
        this.loadData=this.loadData.bind(this)
        this.cancel=this.cancel.bind(this)
        this.saveCheckList=this.saveCheckList.bind(this)
    }
    componentDidMount(){
        Locales.resolveLabels(this)
        this.loadData()
    }

    loadData(){
        this.state.data.id=this.props.pipStatusId
        Fetchers.postJSON("/api/moderator/checklist/open/edit", this.state.data, (query,result)=>{
            this.state.data=result
            this.setState(this.state)
        })
    }

    saveCheckList(){
        Fetchers.postJSON("/api/moderator/question/save", this.state.data, (query,result)=>{
            this.state.data=result
            this.setState(this.state)
        })
    }

    cancel(){
        window.history.back()
    }

    createQuestionItem(q){
        //let question = this.state.data.questions
        if(q==undefined){
            return []
        }else{
            if(q.head){
                return(
                <Row>
                    <Col>
                        <b>{q.question}</b>
                    </Col>
                </Row>
                )
            }else{
                return(
                    <Row className="border mb-1">
                        <Col xs='12' sm='12' lg='12' xl='12'>
                            <label>{q.question}</label>
                        </Col>
                    </Row>
    
                    )
            }
        }
    }

    /**
     * Create a list of question
     */
    questions(){
        let ret=[]
        if(Fetchers.isGoodArray(this.state.data.questions)){
            this.state.data.questions.forEach((question)=>{
                ret.push(
                    <QuestionTemplate id={question.id} checkList={this} key={question.questionKey+question.id}/>
                )
            })
        }
        return ret
    }

    render(){
        return(
            <Card style={{fontSize:"0.8rem"}}>
                <CardHeader>
                    <Row>
                    <Col xs='12' sm='12' lg='11' xl='11'>
                        <h2>{this.state.labels.editchecklist+" / " + this.state.data.stateDescr}</h2>
                    </Col>
                    <Col xs='12' sm='12' lg='1' xl='1' className="d-flex justify-content-end">
                        <Button close onClick={()=>{window.history.back()}}/>
                    </Col>
                    </Row>
                </CardHeader>
                <CardBody>
                    {this.questions()}
                </CardBody>

            </Card>
        )
    }


}
export default CheckList
CheckList.propTypes={
    pipStatusId:PropTypes.string.isRequired,
    colorize:PropTypes.bool
}