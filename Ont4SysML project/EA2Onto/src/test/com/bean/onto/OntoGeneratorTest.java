package test.com.bean.onto;

import java.io.File;

import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.swrlapi.exceptions.SWRLBuiltInException;
import org.swrlapi.parser.SWRLParseException;

import com.bean.UML.XMI;
import com.bean.onto.OntoGenerator;
import com.mapper.BDDMapper;
import com.mapper.ReqMapper;
import com.util.ParseUtil;

public class OntoGeneratorTest extends OntoGenerator {

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
			XMI xmi = ParseUtil.parseXMI(new File("src\\resource\\autocar.xml"));
			ReqMapper.map(xmi, g);
			BDDMapper.map(xmi, g);
			String rule="TIM:Block(?b)->sqwrl:select(?b)";
			System.out.println(g.queryBySQWRL(rule, "b"));
//^abox:sia(TIM:controlsubsystem,?b)
	}

}
