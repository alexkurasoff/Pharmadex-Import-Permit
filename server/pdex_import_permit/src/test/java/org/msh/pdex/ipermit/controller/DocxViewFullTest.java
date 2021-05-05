package org.msh.pdex.ipermit.controller;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.msh.pdex.dto.tables.TableCell;
import org.msh.pdex.dto.tables.TableHeader;
import org.msh.pdex.dto.tables.TableQtb;
import org.msh.pdex.dto.tables.TableRow;
import org.msh.pdex.exceptions.ObjectNotFoundException;
import org.msh.pdex.ipermit.PdexImportPermitApplicationTest;
import org.msh.pdex.ipermit.dto.ApplicationDTO;
import org.msh.pdex.ipermit.services.ApplicantService;
import org.msh.pdex.ipermit.services.DocumentService;
import org.msh.pdex.ipermit.services.EntityToDtoService;
import org.msh.pdex.model.enums.TemplateType;
import org.msh.pdex.model.pip.Import_permit;
import org.msh.pdex.model.rsecond.Context;
import org.msh.pdex.repository.pip.Import_permitRepo;
import org.msh.pdex.services.ContextServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

/**
 * Test it with Spring Boot
 * @author alexk
 *
 */
@SuppressWarnings("unused")
@RunWith(SpringRunner.class)
@SpringBootTest(classes=PdexImportPermitApplicationTest.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class DocxViewFullTest {

	@Autowired
	Import_permitRepo applicationRepo;
	
	@Autowired
	DocumentService docService;
	@Autowired
	ApplicantService applicantService;
	@Autowired
	ContextServices contextServ;
	@Autowired
	EntityToDtoService entityToDTOServ;

	
	@Test
	@Transactional
	public void shouldFindTemplateThenCreateModelAndView() throws ObjectNotFoundException, IOException {
		Runtime.getRuntime().gc();

		//load the template
		Optional<InputStream> fileStreamo = docService.findTemplate(TemplateType.BIEF);
		assertTrue(fileStreamo.isPresent());
		//find any application for which an applicant is defined
		Import_permit application = null;
		for(Import_permit a : applicationRepo.findAll()) {
			if(a.getApplicant() != null) {
				application = a;
				break;
			}
		}
		ApplicationDTO appDTO = new ApplicationDTO();
		appDTO.setId(application.getId());
		Optional<String> contextId = Optional.empty();
		//appDTO = applicantService.applicationOpen("applicant", appDTO, contextServ.loadContext(contextId));
		appDTO=entityToDTOServ.ApplicationToDto(contextServ.loadContext(contextId),application, "applicant", "applicant", appDTO);
		//create a model and a view
		DocxView view = new DocxView(fileStreamo.get());
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("name", "The best applicant");
		model.put("address", "Applicando Avenida, Applican Town");
		model.put("phones", "555-666-777");
		model.put("table", appDTO.getDetails());
		model.put("executor", "Isidora Covarubio de los Lianos");
		Path resourceDirectory = Paths.get("src","test","resources");
		String absolutePath = resourceDirectory.toFile().getAbsolutePath();
		FileOutputStream out = new FileOutputStream(absolutePath+"/resultFull.docx");
	
		view.resolveDocument(model);
		
		view.getDoc().write(out);
		view.getDoc().close();
		view.getDoc().close();
	}
	
}
