import React , {Component} from 'react'
import {Card,CardHeader,CardBody} from 'reactstrap'
import Locales from './utils/Locales'
import Fetchers from './utils/Fetchers'
import CollectorTable from './utils/CollectorTable'
import Navigator from './Navigator'

/**
 * List of Checklist
 */
class CheckLists extends Component{
    constructor(props){
        super(props)
        this.state={
            data:{
                table:{
                    headers:{
                        selectedOnly:false
                    },
                    rows:[]
                }
            },
            labels:{
                locale:"",
                editchecklist:"",
            }
        }
        this.loadData=this.loadData.bind(this)
    }

    componentDidMount(){
        Locales.resolveLabels(this)
        this.loadData()
    }
    
    /**
     * Load the data
     */
    loadData(){
        this.state.data.tabName=Navigator.tabName()
        Fetchers.postJSON("/api/moderator/checklists", this.state.data, (query, result)=>{
            this.state.data=result
            this.setState(this.state.data)
        })
    }

    render(){
        return(
            <Card style={{fontSize:"0.8rem"}}>
                <CardHeader>
                    <h2>{this.state.labels.editchecklist}</h2>
                </CardHeader>
                <CardBody>
                        <CollectorTable
                            tableData={this.state.data.table}
                            loader={this.loadData}
                            headBackground={'#0099cc'}
                            linkProcessor={ (rowNo,col)=>{
                                        Navigator.openCheckList(this.state.data.table.rows[rowNo].dbID + "");
                                    }
                                }
                            styleCorrector={(headerKey)=>{
                                if(headerKey=='Code'){
                                    return {width:'10%'}
                                }
                            }}
                        />
                </CardBody>
            </Card>
        )
    }


}
export default CheckLists
CheckLists.propTypes={
}