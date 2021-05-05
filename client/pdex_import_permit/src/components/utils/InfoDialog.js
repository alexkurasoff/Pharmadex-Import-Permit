import React , {Component} from 'react'
import {Modal, ModalBody, ModalHeader} from 'reactstrap';
import CollectorTable from './CollectorTable';
import Fetchers from './Fetchers';
class InfoDialog extends Component { 
    constructor(props) {
        super(props);
        this.state = {
        modal: false,
        tableOrders:{
            headers:{},
            rows:[]
        },
        info:"",
        genericCode:"",
        qtbIDs:[],
        options:{
            qtOrders:true,
            qtOther:false,
            omsFirm:false,
            omsDraft:false,
            omsDelivered:false
        },
    };
        this.toggle = this.toggle.bind(this);
        this.loadDataOrders=this.loadDataOrders.bind(this)
        this.pagerProcessorOrders=this.pagerProcessorOrders.bind(this)
        this.headerActionOrders=this.headerActionOrders.bind(this)
    }
    componentDidMount(){
    }

    toggle() {
        //let s = this.state
        //s.modal=s.modal
            this.setState(this.state) 
      }

      loadDataOrders(){
        this.state.genericCode=this.props.genericCode
        this.state.qtbIDs=this.props.qtbIDs
        this.state.options=this.props.option 
        Fetchers.postJSON('/api/analysis/dataOrders',this.state,(query,result)=>{
            this.state.info=this.props.messages.mess("genericCode")+": "+this.props.genericCode
            let s = this.state
            s=result
            s.modal=false
            this.setState(s) 
        })
    }
    pagerProcessorOrders(pageNo){
        let s=this.state
        s.tableOrders.headers.page=pageNo
       this.loadDataOrders()
    }
    headerActionOrders(header){
        this.state.tableOrders.headers.headers.length
        let index=-1;
        for(let i=0; i<this.state.tableOrders.headers.headers.length;i++){
            if(this.state.tableOrders.headers.headers[i].key== header.key){
                index=i
                break;
            }
        }
        if(index>-1){
           this.state.tableOrders.headers.headers[index] = header
        }
        this.loadDataOrders()
    }

    render() {
        this.state.modal = this.props.genericCode != "" && this.state.modal==false
        return(
                <div id="modal-root">
        <Modal 
        size='lg'
        isOpen={this.state.modal} toggle={this.toggle}
        onOpened={this.loadDataOrders}
        onClosed={()=>{
            this.state.genericCode=""
            this.state.qtbIDs=[]
            this.props.onClosed()
        }}>
          <ModalHeader>{this.state.info}</ModalHeader>
          <ModalBody>
            {
                    <CollectorTable tableData={this.state.tableOrders} 
                             pagerProcessor={this.pagerProcessorOrders} 
                            headerAction={this.headerActionOrders}
                            headBackground={'#0099cc'}
                            />
            }
          </ModalBody>
        </Modal>
                </div>
                )
            }
        
}
export default InfoDialog; 