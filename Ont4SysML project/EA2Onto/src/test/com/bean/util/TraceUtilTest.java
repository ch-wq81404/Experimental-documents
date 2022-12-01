package test.com.bean.util;

import java.io.File;

import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.swrlapi.exceptions.SWRLBuiltInException;
import org.swrlapi.parser.SWRLParseException;

import com.bean.onto.OntoGenerator;
import com.util.TraceUtil;

public class TraceUtilTest {

	@Test
	public void test() {
		String rootPath = System.getProperty("user.dir");
		OntoGenerator g=new OntoGenerator();
			try {
				File _file=new File(rootPath+"\\src\\resource\\TIM.xml");
				g.init(_file);
			
			} catch (OWLOntologyCreationException | OWLOntologyStorageException | SWRLParseException
					| SWRLBuiltInException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		TraceUtil.traceBlock2ActPart("123", "Act", g);
	}

}
