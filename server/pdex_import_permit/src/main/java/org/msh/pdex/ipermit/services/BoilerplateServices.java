package org.msh.pdex.ipermit.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.joda.time.LocalDate;
import org.msh.pdex.dto.form.FormFieldDTO;
import org.msh.pdex.dto.form.OptionDTO;
import org.msh.pdex.dto.tables.Headers;
import org.msh.pdex.dto.tables.TableCell;
import org.msh.pdex.dto.tables.TableHeader;
import org.msh.pdex.dto.tables.TableQtb;
import org.msh.pdex.dto.tables.TableRow;
import org.msh.pdex.exceptions.ObjectNotFoundException;
import org.msh.pdex.i18N.Messages;
import org.msh.pdex.ipermit.dto.ToggleRowDTO;
import org.msh.pdex.model.Applicant;
import org.msh.pdex.model.Product;
import org.msh.pdex.model.ResourceBundle;
import org.msh.pdex.model.ResourceMessage;
import org.msh.pdex.model.Role;
import org.msh.pdex.model.User;
import org.msh.pdex.model.pip.Import_permit;
import org.msh.pdex.model.pip.Import_permit_detail;
import org.msh.pdex.model.pip.ItemType;
import org.msh.pdex.model.pip.OrderType;
import org.msh.pdex.model.pip.PIPDoc;
import org.msh.pdex.model.pip.PipStatus;
import org.msh.pdex.model.pip.QuestionInstancePIP;
import org.msh.pdex.model.rsecond.ApplicantDoc;
import org.msh.pdex.model.rsecond.Context;
import org.msh.pdex.model.rsecond.DocType;
import org.msh.pdex.model.rsecond.Document;
import org.msh.pdex.model.rsecond.Query;
import org.msh.pdex.repository.ApplicantRepository;
import org.msh.pdex.repository.ProductRepo;
import org.msh.pdex.repository.QueryRepository;
import org.msh.pdex.repository.ResourceBundleRepo;
import org.msh.pdex.repository.ResourceMessageRepo;
import org.msh.pdex.repository.RoleRepo;
import org.msh.pdex.repository.UserRepository;
import org.msh.pdex.repository.pip.ConsigneeRepo;
import org.msh.pdex.repository.pip.CurrencyRepo;
import org.msh.pdex.repository.pip.Import_permitRepo;
import org.msh.pdex.repository.pip.Import_permit_detailRepo;
import org.msh.pdex.repository.pip.IncotermsRepo;
import org.msh.pdex.repository.pip.ItemTypeRepo;
import org.msh.pdex.repository.pip.OrderTypeRepo;
import org.msh.pdex.repository.pip.PaymentModeRepo;
import org.msh.pdex.repository.pip.PipDocRepo;
import org.msh.pdex.repository.pip.PipStatusRepo;
import org.msh.pdex.repository.pip.PortRepo;
import org.msh.pdex.repository.pip.QuestionInstancePIPRepo;
import org.msh.pdex.repository.pip.SupplierRepo;
import org.msh.pdex.repository.pip.TransportRepo;
import org.msh.pdex.repository.rsecond.ApplicantDocRepo;
import org.msh.pdex.repository.rsecond.DocTypeRepo;
import org.msh.pdex.services.ContextServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Low coupling methods to avoid unnecessary, redundant boilerplate in other services
 * @author alexk
 *
 */
@Service
public class BoilerplateServices {
	private static final Logger logger = LoggerFactory.getLogger(BoilerplateServices.class);
	
	@Autowired
	ApplicantRepository applicantRepo;
	@Autowired
	QueryRepository queryRepo;
	@Autowired
	Import_permitRepo appRepo;
	@Autowired
	UserRepository userRepo;
	@Autowired
	RoleRepo roleRepo;
	@Autowired
	ContextServices contextServices;
	@Autowired
	Messages messages;
	@Autowired
	ConsigneeRepo consRepo;
	@Autowired
	CurrencyRepo currRepo;
	@Autowired
	IncotermsRepo incoRepo;
	@Autowired
	PaymentModeRepo payModeRepo;
	@Autowired
	PipStatusRepo pipStatusRepo;
	@Autowired
	PortRepo portRepo;
	@Autowired
	SupplierRepo supplierRepo;
	@Autowired
	TransportRepo transportRepo;
	@Autowired
	DocTypeRepo docTypeRepo;
	@Autowired
	ApplicantDocRepo applicantDocRepo;
	@Autowired
	PipDocRepo pipDocRepo;
	@Autowired
	QuestionInstancePIPRepo questionChecklistRepo;
	@Autowired
	ResourceBundleRepo resourceBundleRepo;
	@Autowired
	ResourceMessageRepo resourceMessageRepo;
	@Autowired
	ProductRepo productRepo;
	@Autowired
	Import_permit_detailRepo ipDetilRepo;


	/**
	 * Load a query from the database
	 * @param key
	 * @return select operator
	 * @throws ObjectNotFoundException 
	 */
	public String loadSelect(String key) throws ObjectNotFoundException {
		Optional<Query> selecto = queryRepo.findByKey(key);
		if(selecto.isPresent()) {
			return selecto.get().getSql();
		}else {
			throw new ObjectNotFoundException("loadSelect(). Cannot find a query by key " + key,logger);
		}
	}
	/**
	 * Load or create headers
	 * @param context the current context from which headers may be loaded
	 * @param contextName 
	 * @param table the table from which headers may be taken
	 * @param createHeaders functional interface to which create headers method should be bound
	 * @return headers that should be sat in a table!!!!
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public Headers loadHeaders(Context context, String contextName, TableQtb table, CreateHeaders createHeaders) throws JsonParseException, JsonMappingException, IOException {
		Headers headers = table.getHeaders();
		if(headers.getHeaders().size()==0) {
			headers = contextServices.loadHeaders(context, contextName);
			if(headers.getHeaders().size()==0) {
				headers = createHeaders.execute();
			}
		}
		return translateHeaders(headers);
	}

	/**
	 * Create display value for all headers
	 * @param headers
	 * @return
	 */
	public Headers translateHeaders(Headers headers) {
		for(TableHeader h :headers.getHeaders()) {
			h.setDisplayValue(messages.get(h.getValueKey()));
		}
		return headers;
	}

	/**
	 * GEt an applicant by the user login
	 * @param userLogin
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	@Transactional
	public User userByUserLogin(String userLogin) throws ObjectNotFoundException {
		Optional<User> usero = userRepo.findByUsername(userLogin);
		if(!usero.isPresent()) {
			throw new ObjectNotFoundException("userByUserLogin(). User not found. Login name is " + userLogin,logger);
		}
		usero.get().getApplicant();
		usero.get().getRoles();
		return usero.get();
	}
	/**
	 * Mark all selected rows and set quantity of all filtered and selected rows
	 * @param rows list of rows, suppose all filtered
	 * @param context 
	 * @param headers to set quantities
	 * @param contextCriteria key in context to get all selected rows
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public List<TableRow> markSelected(List<TableRow> rows,Context context, String contextCriteria, long id) throws JsonParseException, JsonMappingException, IOException {
		List<TableRow> selected = contextServices.loadSelected(context, contextCriteria,id);
		for(TableRow row : rows) {
			row.setSelected(selected.contains(row));
		}
		return rows;
	}
	/**
	 * Count selected rows
	 * @param rows
	 * @return
	 */
	public int countSelected(List<TableRow> rows) {
		int ret=0;
		if(rows != null) {
			for(TableRow row : rows) {
				if(row.getSelected()) {
					ret++;
				}
			}
		}
		return ret;
	}



	/**
	 * Toggle any row in any table
	 * @param rowDTO
	 * @param context
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @throws ObjectNotFoundException
	 * @throws JsonProcessingException
	 */
	public ToggleRowDTO toggleRow(ToggleRowDTO rowDTO, Context context, String contextName, long id) throws JsonParseException,
	JsonMappingException, IOException, ObjectNotFoundException, JsonProcessingException {
		List<TableRow> stored = contextServices.loadSelected(context,contextName,id);
		List<TableRow> toSave = new ArrayList<TableRow>();
		if(rowDTO.getRow().getSelected()) {
			//unselect
			rowDTO.getRow().setSelected(false);
			for(TableRow r : stored) {
				if(!r.equals(rowDTO.getRow())){
					toSave.add(r);
				}
			}
		}else {
			//select
			rowDTO.getRow().setSelected(true);
			toSave.addAll(stored);
			toSave.add(rowDTO.getRow());
		}
		contextServices.saveSelected(toSave, context, contextName,id);
		rowDTO.setSelectedCount(toSave.size());
		return rowDTO;
	}




	/**
	 * Replace selected rows to other set of ones
	 * @param context
	 * @param rows
	 * @param criteriaName
	 * @param id appication ID
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@Transactional
	public void replaceSelectedRows(Context context, List<TableRow> rows, String criteriaName, long id) throws JsonParseException, JsonMappingException, IOException {
		List<TableRow> toSave = new ArrayList<TableRow>();
		for(TableRow row : rows) {
			if(row.getSelected()) {
				toSave.add(row);
			}
		}
		contextServices.saveSelected(toSave, context, criteriaName,id);
	}

	/**
	 * Cut from the table columns from the zero up to the leftmost inclusive
	 * @param table
	 * @param leftmost
	 * @return the new table
	 * @throws IOException
	 */
	public TableQtb subTable(TableQtb table, int leftmost) throws IOException {
		Headers shortHeaders = cloneHeaders(table.getHeaders());
		int size = shortHeaders.getHeaders().size();
		List<TableRow> shortRows = new ArrayList<TableRow>();
		if(size>leftmost) {
			int toRemove = size-leftmost-1;
			for(int i=0;i<toRemove;i++) {
				shortHeaders.getHeaders().remove(leftmost+1);
			};
			for(TableHeader h : shortHeaders.getHeaders()) {
				h.setFilterAllowed(false);
				h.setSort(false);
			}
			shortHeaders = translateHeaders(shortHeaders);
			//shortHeaders.getHeaders().get(0).setColumnType(TableHeader.COLUMN_STRING);

			for(TableRow row :table.getRows()) {
				TableRow shortRow = cloneRow(row);
				for(int i=0;i<toRemove;i++) {
					shortRow.getRow().remove(leftmost+1);
				};
				shortRows.add(shortRow);
			}
		}
		TableQtb ret = new TableQtb();
		ret.setHeaders(shortHeaders);
		ret.setRows(shortRows);
		ret.setSelectable(false);
		return ret;
	}
	/**
	 * Clone headers use JSON 
	 * @param headers
	 * @return
	 * @throws IOException 
	 */
	public Headers cloneHeaders(Headers headers) throws IOException {
		ObjectMapper mapper = contextServices.getObjectMapper();
		String s = mapper.writeValueAsString(headers);
		Headers ret = (Headers) mapper.readValue(s, headers.getClass());
		return ret;
	}
	/**
	 * Clone a row using JSON to and from
	 * @param row
	 * @return
	 * @throws IOException 
	 */
	public TableRow cloneRow(TableRow row) throws IOException {
		ObjectMapper mapper = contextServices.getObjectMapper();
		String s = mapper.writeValueAsString(row);
		TableRow ret = (TableRow) mapper.readValue(s, row.getClass());
		return ret;
	}
	/**
	 * Is this user has applicant role
	 * @param user
	 * @return
	 */
	public boolean isUserApplicant(User user) {
		boolean ret = false;
		for(Role role : user.getRoles()){
			ret = ret || role.getRolename().equalsIgnoreCase("ROLE_COMPANY");
		}
		return ret;
	}
	/**
	 * Application may be need very often
	 * @param appId
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	@Transactional
	public Import_permit loadApplication(long appId) throws ObjectNotFoundException {
		Optional<Import_permit> modelo= appRepo.findById(appId);
		if(modelo.isPresent()) {
			return modelo.get();
		}else {
			throw new ObjectNotFoundException("applicationModel(). Application not found. ID="+appId,logger);
		}
	}
	/**
	 * Application status should be
	 * @param appId
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	public PipStatus pipStatus(long appId) throws ObjectNotFoundException {
		Import_permit model = loadApplication(appId);
		if(model.getPipStatus() != null) {
			return model.getPipStatus();
		}else {
			throw new ObjectNotFoundException("pipStatus(). Not fount status for Application/ Application ID is "+appId,logger);
		}
	}
	
	/**
	 * user's roles as strings in upper cases
	 * @param userLogin
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	@Transactional
	public List<String> userRoles(String userLogin) throws ObjectNotFoundException {
		List<String> ret = new ArrayList<String>();
		User user = userByUserLogin(userLogin);
		for(Role r : user.getRoles()) {
			ret.add(r.getRolename().toUpperCase());
		}
		return ret;
	}
	/**
	 * Role by name
	 * @param roleName
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	@Transactional
	public Role roleByName(String roleName) throws ObjectNotFoundException {
		Optional<Role> reto = roleRepo.findByrolename(roleName);
		if(reto.isPresent()) {
			return reto.get();
		}else {
			throw new ObjectNotFoundException("roleByName(). Role not found. Name is " + roleName,logger);
		}
	}
	/**
	 * Translate all i8 cells 
	 * @param rows
	 * @param rowKey
	 */
	public TableQtb translateRows(TableQtb table) {
		for(TableRow row : table.getRows()) {
			for(TableCell cell : row.getRow()) {
				String headKey= cell.getKey();
				TableHeader head = table.getHeaders().getHeaderByKey(headKey);
				if(head != null) {
					if(head.getColumnType()==TableHeader.COLUMN_I18) {
						cell.setValue(messages.get((String) cell.getValue()));
					}
				}
			}
		}
		return table;
	}
	/**
	 * Find IDs of all selected rows
	 * @param table
	 * @return
	 */
	public List<Long> findSelected(TableQtb table) {
		List<Long> ret = new ArrayList<Long>();
		for(TableRow row : table.getRows()) {
			if(row.getSelected()) {
				ret.add(new Long(row.getDbID()));
			}
		}
		return ret;
	}
	/**
	 * MArk selected rows, if possible
	 * @param selected
	 * @param table
	 * @return
	 */
	public TableQtb setSelected(List<Long> selected, TableQtb table) {
		for(TableRow row : table.getRows()) {
			row.setSelected(selected.contains(new Long(row.getDbID())));
		}
		return table;
	}
	/**
	 * Translate values to the current language.
	 * Keys are in description
	 * @param docType
	 * @return
	 */
	public FormFieldDTO<OptionDTO> translateOption(FormFieldDTO<OptionDTO> optField) {
		optField.getValue().setCode(messages.get(optField.getValue().getDescription()));
		optField.getValue().setDescription("");
		for(OptionDTO opt : optField.getValue().getOptions()) {
			opt.setCode(messages.get(opt.getDescription()));
			opt.setDescription("");
		}
		return optField;
	}
	/**
	 * Load an applciant by id. Caller should be @Transactional
	 * @param applicantId
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	public Applicant applicant(long applicantId) throws ObjectNotFoundException {
		Optional<Applicant> reto = applicantRepo.findById(applicantId);
		if(reto.isPresent()) {
			return reto.get();
		}else {
			throw new ObjectNotFoundException("applicant(). Applicant not found. Id is "+applicantId, logger);
		}
	}
	/**
	 * Returns descendants of Document class
	 * @param docId
	 * @return
	 * @throws ObjectNotFoundException
	 */
	public Document document(long docId) throws ObjectNotFoundException {
		Optional<ApplicantDoc> doco = applicantDocRepo.findById(docId);
		if(doco.isPresent()) {
			return doco.get();
		}else {
			Optional<PIPDoc> pdoco = pipDocRepo.findById(docId);
			if(pdoco.isPresent()) {
				return pdoco.get();
			}else {
				throw new ObjectNotFoundException("document(). Document not found. Id is "+docId,logger);
			}
		}
	}

	
	public boolean isUniqueDocTypeCode(String code) {
		boolean isUn = true;
		if(code != null && code.length() > 0) {
			List<DocType> list = docTypeRepo.findByCode(code);
			if(list != null && list.size() > 0) {
				isUn = false;
			}
		}
		return isUn;
	}

	/**
	 * The user has a role 
	 * @param user
	 * @param role substr of role name, case is ignored
	 * @return
	 */
	public boolean hasRole(User user, String role) {
		boolean ret=false;
		if(user.getRoles() != null) {
			for(Role r :user.getRoles()) {
				if(r.getRolename().toUpperCase().contains(role.toUpperCase())) {
					return true;
				}
			}
		}
		return ret;
	}

	/**
	 * user by id
	 * @param id
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	public User userById(long id) throws ObjectNotFoundException {
		Optional<User> usero = userRepo.findById(id);
		if(usero.isPresent()) {
			return usero.get();
		}else {
			throw new ObjectNotFoundException("userById(). User not found. Id is "+id, logger);
		}
	}
	/**
	 * load a question by id
	 * @param id
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	@Transactional
	public QuestionInstancePIP question(long id) throws ObjectNotFoundException {
		Optional<QuestionInstancePIP> reto = questionChecklistRepo.findById(id);
		if(reto.isPresent()) {
			return reto.get();
		}else {
			throw new ObjectNotFoundException("question(). Checklist question not found Id is "+id, logger);
		}
	}
	/**
	 * load a product by the id
	 * @param prodId
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	@Transactional
	public Product loadProduct(long prodId) throws ObjectNotFoundException {
		Optional<Product> producto = productRepo.findById(prodId);
		if(producto.isPresent()) {
			return producto.get();
		}else {
			throw new ObjectNotFoundException("loadProduct(). Product not found Id is "+prodId, logger);
		}
	}
	/**
	 * load product detail by id
	 * @param id
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	public Import_permit_detail loadProductDetail(long id) throws ObjectNotFoundException {
		Optional<Import_permit_detail> ipdo = ipDetilRepo.findById(id);
		if(ipdo.isPresent()) {
			return ipdo.get();
		}
		throw new ObjectNotFoundException("loadProductDetail(). Import_Permit_detail not found id is "+id);
	}
	public ResourceBundle findResourceBundle_en() {
		ResourceBundle rb = null;
		Optional<ResourceBundle> bundle = resourceBundleRepo.findById(3L);
		if(bundle.isPresent()) {
			rb = bundle.get();
		}
		return rb;
	}
	
	public ResourceBundle findResourceBundle_pt() {
		ResourceBundle rb = null;
		Optional<ResourceBundle> bundle = resourceBundleRepo.findById(1L);
		if(bundle.isPresent()) {
			rb = bundle.get();
		}
		return rb;
	}

	
	/**
	 * Create/update a message in a bundle with a key given
	 * @param key
	 * @param value_en
	 * @param value_pt
	 * @return
	 */
	@Transactional
	public void updateResourceMessage(String key, String value_en, String value_pt) {
		ResourceMessage rm_en = findMessage(key, true);
		if(rm_en == null) {
			rm_en = new ResourceMessage();
			rm_en.setKey(key);
			rm_en.setBundle( findResourceBundle_en());
		}
		rm_en.setValue(value_en);
		rm_en = resourceMessageRepo.save(rm_en);
		
		ResourceMessage rm_pt = findMessage(key, false);
		if(rm_pt == null) {
			rm_pt = new ResourceMessage();
			rm_pt.setKey(key);
			rm_pt.setBundle( findResourceBundle_pt());
		}
		rm_pt.setValue(value_pt);
		rm_pt = resourceMessageRepo.save(rm_pt);
	}
	
	public ResourceMessage findMessage(String key, boolean isEn) {
		ResourceMessage rm = null;
		ResourceBundle rb = findResourceBundle_pt();
		if(isEn)
			rb = findResourceBundle_en();
		List<ResourceMessage> list = resourceMessageRepo.findAllByKeyAndBundle(key, rb.getId());
		if(list != null && list.size() > 0) {
			rm = list.get(0);
		}
		return rm;
	}
	
	/**
	 * Local date to java date
	 * @param value
	 * @return
	 */
	public Date toDate(LocalDate value) {
		if(value!=null) {
			return value.toDate();
		}else {
			return null;
		}
	}
}
