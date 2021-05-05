import React , {Component} from 'react'
import {Card, CardHeader, CardBody, Row, Col} from 'reactstrap'
import PropTypes from 'prop-types'
import Locales from './utils/Locales'
import Fetchers from './utils/Fetchers'
import Question from './Question'
import Downloader from './utils/Downloader'
import ButtonUni from './form/ButtonUni'
import { isUndefined } from 'util'

/**
 *Ask for a checklist, depends on the application state and user role
 @property {number} applID
 @example <ApplicationCheckList appId={this.props.applId} colorize={this.state.valid} readOnly={false}/>
 */
class ApplicationCheckList extends Component{
    constructor(props){
        super(props)
        this.state={
            data:{
                id:0,
                typeTmplCheckList:"",
                prevStatusId:0
            },
            alertColor:"info",
            labels:{
                checklist:"",
                global_yes:"",
                global_no:"",
                global_na:"",
                global_download:"",
                ask:"",
                eQuestion:"",
                expert:"",
                global_close:"",
                notes:"",
                save:"",
                cancel:"",
            }
        }
        this.checkList=this.checkList.bind(this)
        this.download=this.download.bind(this)
    }

    componentDidMount(){
        Locales.resolveLabels(this)
        this.loadData()
    }

    componentDidUpdate(){
        if(this.props.prevStatusId > 0){
            if(this.state.data.prevStatusId != this.props.prevStatusId){
                this.loadData()
            }
        }
    }

    loadData(){
        this.state.data.id=new Number(this.props.appId)
        this.state.data.prevStatusId=this.props.prevStatusId
        Fetchers.postJSON("/api/common/checklist/open",this.state.data,(query,result)=>{
            this.state.data=result
            this.setState(this.state.data.questions)
        })
    }


    /**
     * Create a checklist
     */
    checkList(){
        let ret=[]
        let list = this.state.data.questions
        if(Fetchers.isGoodArray(list)){
            list.forEach(q => {
                ret.push(
                    <Question key={q.order} question={q} labels={this.state.labels}
                     check={this.props.colorize}
                     readOnly={this.props.readOnly} />
                )
            });
        }
        return ret
    }

    download(){
        let downloader = new Downloader();
        downloader.postDownload("/api/common/upload/checklist",
             this.state.data, "checklist.docx");
    }

    render(){
        if(isUndefined(this.state.data.questions)){
            return []
        }else{
        return(
            <Card key={this.state.data.id+this.state.data.prevStatusId} className="border-5 border-primary shadow-lg m-5" style={{fontSize:"0.8rem"}}>
                <CardHeader>
                    <Row>
                        <Col xs='12' sm='12' lg='9' xl='9'>
                            <h6>{this.state.labels.checklist}</h6>
                        </Col>
                        <Col xs='12' sm='12' lg='3' xl='3' className="d-flex justify-content-end">
                            <ButtonUni onClick={this.download} label={this.state.labels.global_download} 
                                        edit={this.state.data.typeTmplCheckList != ""}/>
                        </Col>
                    </Row>
                </CardHeader>
                <CardBody>
                    <Row>
                        <Col xs='12' sm='12' lg='12' xl='12'>
                            {this.checkList()}
                        </Col>
                    </Row>
                </CardBody>
            </Card>

        )
        }
    }


}
export default ApplicationCheckList
ApplicationCheckList.propTypes={
    appId:PropTypes.string.isRequired,          //PIP id
    colorize:PropTypes.bool,                  //colorize empty/not empty
    readOnly:PropTypes.bool                    //disable change answers
}