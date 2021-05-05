import React , {Component} from 'react'
import { Card,CardHeader,CardBody,Row, Col, Button} from 'reactstrap';
import Locales from './utils/Locales'
import Navigator from './Navigator'
import Fetchers from './utils/Fetchers';
import SearchControl from './utils/SearchControl'
import CollectorTable from './utils/CollectorTable'
import Downloader from './utils/Downloader'

/**
 * Display notifications table for applicants, reviewers, head
 */
class Notifications extends Component{
    constructor(props){
        super(props)
        this.state={
            data:{
                table:{
                    headers:{},
                    rows:[]
                }
            },
            labels:{
                search:"Search",
                exportExcel:"Export to Excel"
            }
        }
        this.loadData=this.loadData.bind(this)
        this.excelTable=this.excelTable.bind(this)
    }
    /**
     * Resolve labels, load data
     */
    componentDidMount(){
        Locales.resolveLabels(this)
        this.loadData()
    }
    /**
     * load data to the table
     */
    loadData(){
        Fetchers.postJSON("/api/notifications/list", this.state.data, (query, result)=>{
            this.state.data=result
            this.setState(this.state.data)
        })
    }
    /**
     * Upload an Excel table
     */
    excelTable(){
        let downloader = new Downloader();
        downloader.postDownload("/api/notifications/list/excel", this.state.data, "notifications.xlsx")
    }

    render(){
        return(
            <Card style={{fontSize:"0.8rem"}}>
                <CardHeader>
                <Row>
                    <Col xs="12" sm="12" lg="2" xl="2">
                        <SearchControl label={this.state.labels.search} table={this.state.data.table} loader={this.loadData} />
                    </Col>
                    <Col xs="12" sm="12" lg="8" xl="8" className="text-center">
                   
                    </Col>
                    <Col xs="12" sm="12" lg="2" xl="2" className="d-flex justify-content-right">
                        <Button className="btn-block" size="sm" outline color="primary" 
                                onClick={this.excelTable}>{this.state.labels.exportExcel}
                        </Button>
                    </Col>
                </Row>
                </CardHeader>
                <CardBody>
                        <CollectorTable tableData={this.state.data.table} 
                                pagerProcessor={this.pagerProcessor}
                                styleCorrector={this.widthMaster}
                                loader={this.loadData}
                                headBackground={'#0099cc'}
                                linkProcessor={
                                    (rowNo, cell)=>{
                                        let navigator={
                                            tabSet:Navigator.tabSetName(),
                                            row:this.state.data.table.rows[rowNo]
                                        }
                                        Fetchers.postJSONNoSpinner("/api/notifications/link", navigator, (query,result)=>{
                                            Navigator.navigate(result.tab, result.component, result.params)
                                        })
                                    }
                                }
                                styleCorrector={(key)=>{
                                    if(key=='subject'){
                                        return {width:'40%'}
                                    }
                                }}
                                />
                    </CardBody>
                </Card>
        )
    }

}

export default Notifications