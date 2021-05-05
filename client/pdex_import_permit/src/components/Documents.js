import React , {Component} from 'react'
import {Card,CardHeader,CardBody, Row, Col, ButtonGroup, Button} from 'reactstrap'
import Locales from './utils/Locales'
import Fetchers from './utils/Fetchers'
import CollectorTable from './utils/CollectorTable'
import Navigator from './Navigator'
import ButtonUni from './form/ButtonUni'

/**
 * List of type Documents
 */
class Documents extends Component{
    constructor(props){
        super(props)
        this.state={
            data:{
                table:{
                    headers:{
                        selectedOnly:false
                    },
                    rows:[]
                },
                filterBtns:{
                    keys:[],
                    names:[]
                }
            },
            labels:{
                locale:"",
                editDocType:"",
                global_add:""
            }
        }
        this.loadData=this.loadData.bind(this)
        this.addnew=this.addnew.bind(this)
    }

    componentDidMount(){
        Locales.resolveLabels(this)
        this.loadData()
    }
    
    /**
     * Load the data
     */
    loadData(){
        Fetchers.postJSON("/api/moderator/documents", this.state.data, (query, result)=>{
            this.state.data=result
            this.setState(this.state.data)
        })
    }

    addnew(){
       return  Navigator.openDocument("0");
    }

    buildFilterPnl(){
        let group = []
        if(Fetchers.isGoodArray(this.state.data.filterBtns.keys)){
            let i = 0
            this.state.data.filterBtns.keys.forEach((btn) => {
                group.push(<Button size="sm" outline color="primary" key={btn} 
                                    active={this.state.data.filterBtns.selectKey == btn}
                                    onClick={()=>{
                                        this.state.data.filterBtns.selectKey = btn
                                        this.loadData()
                                    }}>
                                        {this.state.filterBtns.names[i]}
                            </Button>)
                i = i + 1
            });
        }

        return group
    }

    render(){
        return(
            <Card style={{fontSize:"0.8rem"}}>
                <CardHeader>
                        <Row>
                            <Col xs='12' sm='12' lg='5' xl='5' className="d-flex justify-content-start">
                                <ButtonGroup className="m-1">
                                    {this.buildFilterPnl()}
                                </ButtonGroup>
                            </Col>
                            <Col xs='12' sm='12' lg='5' xl='5'>
                                <h2>{this.state.labels.editDocType}</h2>
                            </Col>
                            <Col xs='12' sm='12' lg='2' xl='2' className="d-flex justify-content-end">
                                <ButtonUni onClick={this.addnew} label={this.state.labels.global_add}/>
                            </Col>
                        </Row>
                </CardHeader>
                <CardBody>
                        <CollectorTable
                            tableData={this.state.data.table}
                            loader={this.loadData}
                            headBackground={'#0099cc'}
                            linkProcessor={ (rowNo,col)=>{
                                        Navigator.openDocument(this.state.data.table.rows[rowNo].dbID + "");
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
export default Documents
Documents.propTypes={
}