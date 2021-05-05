import React , {Component} from 'react'
import {Col, Row} from 'reactstrap'
import PropTypes from 'prop-types'
import Locales from './utils/Locales'
import Fetchers from './utils/Fetchers'
import QuestionAnswer from './QuestionAnswer'
import { isUndefined } from 'util'

/**
 * Answer appication related questions. Attach Expert Opinion documents
 * @property {number} applId
 * @example <QuestionAnswers applId={this.props.applId} />
 */
class QuestionAnswers extends Component{
    constructor(props){
        super(props)
        this.state={
            alertColor:"info",
            data:{
                id:0,
            },
            labels:{
                locale:"",
                answerquestions:"",
                answerattached:"",
                didyouattachanswer:"",
            }
        }
        this.loadData=this.loadData.bind(this)
    }
    componentDidMount(){
        Locales.resolveLabels(this)
        this.loadData()
    }

    loadData(){
        this.state.data.id=this.props.appId
        Fetchers.postJSON("/api/common/questions/answers",this.state.data,(query,result)=>{
            this.state.data=result
            this.setState(this.state)
        })
    }
    /**
     * Create a list of questions
     */
    questions(){
        let ret=[]
        if(Fetchers.isGoodArray(this.state.data.questions)){
            this.state.data.questions.forEach((question)=>{
                ret.push(
                    <Row key={question.id}>
                        <Col>
                            <QuestionAnswer question={question} labels={this.state.labels}
                                reload={this.loadData} />
                        </Col>
                    </Row>
                )
            })
        }
        return ret
    }
    render(){
        if(isUndefined(this.state.data.pipNumber)){
            return []
        }else{
        return(
            this.questions()
        )
        }
    }
}
export default QuestionAnswers
QuestionAnswers.propTypes={
    appId:PropTypes.string.isRequired          //PIP id
}