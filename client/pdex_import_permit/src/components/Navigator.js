import Fetchers from './utils/Fetchers'
import Alerts from './utils/Alerts'

/**
 * Common page navigator library
 * Provides for a tabset component a set of functions to navigate inside the tabset
 * For navigation between tabsets the WebApp controller on the server is responsible
 * @example
 * Navigator nav = new Navigator(this) - allow navigation for tabset component
 * the tabset component should be ReactJS component
 * Navigator.navigate(tab, component, param) - navigate to tab, component and param
 * Navigator.openApplication(appId) - open an application in a righ component 
 * 
 * 
 */
class Navigator{
    constructor(component) {
        this.state={
            tabset:component,
            toggler:false,  //to call render
            activeTab:"",
            activeComponent:"",
            activeParameter:"",
            prevHash:""
        }
        this.onHashChanged=this.onHashChanged.bind(this)
        this.calcParams=this.calcParams
        this.switchComponent=this.switchComponent.bind(this)
        window.onhashchange=this.onHashChanged
        let params = this.calcParams()
        this.switchComponent(params[0], params[1], params[2])
    }

   /**
     * Hash change event
     */
    onHashChanged(){
        let params = this.calcParams()
        this.switchComponent(params[0], params[1], params[2])
        this.state.tabset.setState(this.state.tabset.state)
    }

    /**
     * Calc tab, component, param from hash string or create default one
     */
    calcParams(){
        let hash = window.location.hash
        let params = ["","",""]
        if(hash.length>0){
            hash=hash.replace(/^#/, '')
            params = hash.split(",");
            if(Fetchers.isGoodArray(params) && params.length>=2 && params.length<=3 ){
                if(params.length==2){
                    params.push("void")
                }
            this.state.prevHash=hash
            
            }
        }else{
            //default component for this tabset
            this.state.activeComponent="";
        }
        return params
    }


    /**
     * Return a name of the current tab set. Empty string may means the default tab (not always the first)
     */
    static tabSetName(){
        let path=window.location.pathname
        if(path.length==0){
            return "";
        }else{
            let pathArr = path.split("/")
            if(pathArr.length==2 && pathArr[0].length==0){
                if(pathArr[1].toUpperCase != Fetchers.contextName){
                    return pathArr[1]
                }else{
                    if(pathArr.length>2){
                        return pathArr[2]
                    }else{
                        return ""
                    }
                }
            }else{
                return "";
            }
        }
    }
    /**
     * Return a name for the current tab in the current tabset. Empty string means default for it
     */
    static tabName(){
        let hash = window.location.hash
        hash=hash.replace(/^#/, '')
        let params = hash.split(",");
        if(Fetchers.isGoodArray(params) && params.length>=1 && params.length<=3 ){
            return params[0]
        }else{
            return ""
        }
    }

    /**
     * May be called from any component. Creates new window.loaction from current url and tab, component, parameter
     * @param {String} tab a tab to switch 
     * @param {String} component a component to display inside the tab
     * @param {String} parameter a prarmeter for the component, interpret by the component, not mandatory
     */
    static navigate(tab, component,parameter){
        if(typeof tab == "string" && typeof component=="string"){
            let hash=tab+","+component
            let param = "void"
            if(parameter != undefined){
                param=parameter+''
            }
            hash=hash+","+param
            window.location.hash = hash
        }else{
            Alerts.show("bad navigate tab=" + tab +" component="+component + " parameter="+parameter,3)
        }

    }
    /**
     * Ask server which component should display an application data
     * @param {number} appId - id of the application to open 
     */
    static openApplication(appId){
        let data = {id:appId,tab:Navigator.tabName(),component:""}
        Fetchers.postJSON("/api/common/application/open", data, (query,result)=>{
            data=result;
            Navigator.navigate(data.tab,data.component, data.params)
        })
    }

    /**
     * Ask server which component should display an checkList data
     * @param {number} pipStatusId - id of the PipStatus 
     */
    static openCheckList(pipStatusId){
        let data = {id:pipStatusId, tab:Navigator.tabName(), component:""}
        Fetchers.postJSON("/api/moderator/checklist/open", data, (query, result)=>{
            data=result;
            Navigator.navigate(data.tab,data.component, data.params)
        })
    }

    /**
     * Ask server which component should display an document data
     * @param {number} 
     */
    static openDocument(id){
        let data = {id:id, tab:Navigator.tabName(), component:""}
        Fetchers.postJSON("/api/moderator/document/open", data, (query, result)=>{
            data=result;
            Navigator.navigate(data.tab,data.component, data.params)
        })
    }

    /**
     * Make the tab active and load the component to the tab in reply to hash change
     * all parameters are mandatory
     * @param {String} tab a tab to switch 
     * @param {String} component a component to display inside the tab
     * @param {String} parameter a prarmeter for the component, interpret by the component
     */
    switchComponent(tab, component, parameter){
        let s = this.state
        s.activeTab=tab
        s.activeComponent=component
        s.activeParameter=parameter
        s.toggler=!s.toggler
    }
    
}

export default Navigator 
