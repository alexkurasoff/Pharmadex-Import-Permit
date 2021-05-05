import React , {Component} from 'react'
import {Card, CardHeader, CardBody, Row, Col} from 'reactstrap'
import PropTypes from 'prop-types'
import Locales from './utils/Locales'
import Fetchers from './utils/Fetchers'
import CollectorTable from './utils/CollectorTable'
import ApplicationCheckList from './ApplicationCheckList'
import { isUndefined } from 'util'

/**
 *Events for an application
 @property {number} applID
 @example <ApplicationEvents applId={this.props.applId}/>
 */
class ApplicationEvents extends Component{
    constructor(props){
        super(props)
        this.state={
            data:{
                id:0,
                events:{
                    headers:{},
                    rows:[]
                }
            },
            alertColor:"info",
            expandRow:-1,
            labels:{
                applicationevents:"",
            }
        }
        this.loadDate=this.loadData.bind(this)
    }
    componentDidMount(){
        Locales.resolveLabels(this)
        this.loadData()
    }
    loadData(){
        this.state.data.id=this.props.appId
        Fetchers.postJSON("/api/common/application/events",this.state.data,(query,result)=>{
            this.state.data=result
            this.setState(this.state)
        })
    }

    showCheckList(){
        if(this.state.expandRow != -1){
            return (
                <ApplicationCheckList appId={this.state.data.id + ""} prevStatusId={this.state.expandRow} colorize={false} readOnly={!this.state.data.editable}/>
            )
        }else{
            return []
        }
    }

    render(){
        if(this.props.appId==0){
            return []
        }else{
            return(
                <Card style={{fontSize:"0.8rem"}}>
                    <CardHeader>
                        <h6>{this.state.labels.applicationevents}</h6>
                    </CardHeader>
                    <CardBody>
                        <CollectorTable
                            tableData={this.state.data.events}
                            loader={this.loadData}
                            headBackground={'#0099cc'}
                            linkProcessor={
                                (rowNo,cell)=>{
                                    let id = this.state.data.events.rows[rowNo].dbID
                                    if(this.state.expandRow != id){
                                        this.state.expandRow = id
                                        this.setState(this.state)
                                    }else{
                                        this.state.expandRow=-1
                                        this.setState(this.state)
                                    }
                                }
                            }
                        />
                        {this.showCheckList()}
                    </CardBody>
                </Card>

            )
        }
    }


}
export default ApplicationEvents
ApplicationEvents.propTypes={
    appId:PropTypes.string.isRequired,          //PIP id
}