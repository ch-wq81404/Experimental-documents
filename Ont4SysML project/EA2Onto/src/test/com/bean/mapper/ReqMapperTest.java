package test.com.bean.mapper;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.swrlapi.exceptions.SWRLBuiltInException;
import org.swrlapi.parser.SWRLParseException;

import com.bean.UML.XMI;
import com.bean.onto.OntoGenerator;
import com.mapper.ReqMapper;
import com.util.ParseUtil;

public class ReqMapperTest {

	@Test
	public void test() {
		String rootPath = System.getProperty("user.dir");
		OntoGenerator g=new OntoGenerator();
		try {
			File _file=new File(rootPath+"\\src\\resource\\TIM.xml");
			g.init(_file);
			XMI xmi = ParseUtil.parseXMI(new File("src\\resource\\autocar1.xml"));
			ReqMapper.map(xmi, g);
			
			g.saveAsFile(System.getProperty("user.dir") + "\\generate\\robot.owl");
		} catch (OWLOntologyCreationException | OWLOntologyStorageException | SWRLParseException
				| SWRLBuiltInException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
