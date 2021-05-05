/**
 * Call checkChanges method of this class when you will need to determine
 * did any form field changes occur since the most recent checkChange call
 * DO NOT SUIT FOR FieldInput !!!!
 * @example
 * constructor(props){
 *  super(props)
 * 
 *  ...
 * }
 * ...
 * loadData(){
 *      ...
 *      this.state.data = result
 *      this.comparator = new FieldsComparator(this) //component this should has this.state.data
 *      ...
 * }
 * ...
 * componentDidUpdate(){
 *  const fld = this.comparator.checkChanges()
 *  if(fld.lenght>0 && fld == "nameOfTextField"){
 *      this.save(); 
 *  }
 * }
 * 
 */
class FieldsComparator{

    constructor(component){
        this.data = component.state.data
        if(this.data != undefined){
            this.copy = this.cloneIt(this.data)
        }else{
            this.data={}
            this.copy={}
        }

        this.cloneIt=this.cloneIt.bind(this)
        this.checkChanges=this.checkChanges.bind(this)
        this.compareFldValues=this.compareFldValues.bind(this)
    }

    /**
     * Has any field been changed?
     * @returns field name or empty string if no changes
     */
    checkChanges(){
        for(let key in this.copy){
            if(this.copy[key] != undefined && this.copy[key].value != undefined){
                if(!this.compareFldValues(this.copy[key].value,this.data[key].value)){
                    this.copy=this.cloneIt(this.data);
                    return key
                }
            }
        }
        return ""
    }

    /**
     * Compare field's values. Typically copy and original ones
     * @param {obj} copyVal - copy of a field 
     * @param {obj} origVal - original of a field
     */
    compareFldValues(copyVal, origVal){
        if(copyVal.id == undefined){
            return copyVal==origVal
        }else{
            return copyVal.id == origVal.id
        }
    }

    /**
     * Simple and unefficient way to make a deep clone
     * @param {object} obj 
     */
    cloneIt(obj){
        const s = JSON.stringify(obj)
        return JSON.parse(s)
    }
}
export default FieldsComparator