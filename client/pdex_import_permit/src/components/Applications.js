import React , {Component} from 'react'
import {Card,CardHeader,CardBody,Row, Col, Button} from 'reactstrap'
import Locales from './utils/Locales'
import Fetchers from './utils/Fetchers'
import SearchControl from './utils/SearchControl'
import CollectorTable from './utils/CollectorTable'
import Navigator from './Navigator'
import Downloader from './utils/Downloader'
import ApplicationsButtons from './ApplicationsButtons'

/**
 * List of applications
 */
class Applications extends Component{
    constructor(props){
        super(props)
        this.state={
            data:{},
            labels:{
                search:"",
                exportExcel:"",
            }
        }
        this.loadData=this.loadData.bind(this)
        this.excelTable=this.excelTable.bind(this)
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
        Fetchers.postJSON("/api/common/applications", this.state.data, (query, result)=>{
            this.state.data=result
            this.setState(this.state.data)
        })
    }
    /**
     * Load the table to MS Excel
     */
    excelTable(){
        let downloader = new Downloader();
        downloader.postDownload("/api/common/applications/excel",
             this.state.data, "applications.xlsx");
    }

    render(){
        if(this.state.data.table != undefined){
            return(
                <Card style={{fontSize:"0.8rem"}}>
                    <CardHeader>
                    <Row>
                        <Col xs="12" sm="12" lg="2" xl="2">
                            <SearchControl label={this.state.labels.search} table={this.state.data.table} loader={this.loadData} />
                        </Col>
                        <Col xs="12" sm="12" lg="9" xl="9">
                            <ApplicationsButtons buttons={this.state.data.buttons} reload={this.loadData}/>
                        </Col>
                        <Col xs="12" sm="12" lg="1" xl="1" className="d-flex justify-content-right">
                            <Button className="btn-block" size="sm" outline color="primary" 
                                    onClick={this.excelTable}>{this.state.labels.exportExcel}
                            </Button>
                        </Col>
                    </Row>
                    </CardHeader>
                    <CardBody>
                            <CollectorTable
                                tableData={this.state.data.table}
                                loader={this.loadData}
                                headBackground={'#0099cc'}
                                linkProcessor={ (rowNo,col)=>{
                                            Navigator.openApplication(this.state.data.table.rows[rowNo].dbID+"");
                                        }
                                    }
                                styleCorrector={(headerKey)=>{
                                    if(['destination','custom'].indexOf(headerKey)>-1){
                                        return {width:'20%'}
                                    }
                                }}
                            />
                    </CardBody>
                </Card>
            )
        }else{
            return []
        }
    }


}
export default Applications