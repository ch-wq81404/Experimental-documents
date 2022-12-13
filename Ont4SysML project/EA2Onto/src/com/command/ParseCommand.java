package com.command;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.swrlapi.exceptions.SWRLBuiltInException;
import org.swrlapi.parser.SWRLParseException;

import com.bean.UML.XMI;
import com.bean.onto.OntoGenerator;
import com.checker.Checker;
import com.mapper.*;
import com.util.ParseUtil;
import com.util.TraceUtil;
import com.value.verifier.ValueVerifier;

/**
 * 命令行参数： -h : 解释可用的命令行参数，即以下两点。 -t 路径a
 * ：将路径a下的SysML模型XML文件转换为本体模型的XML文件，输出到jar同路径的generate文件夹中 -v 路径b :
 * 验证路径b下本体模型里的价值观
 * 
 * @author wq
 *
 */
public class ParseCommand {
	public static void main(String[] args) throws Exception {
		int a = args.length;
		if (a == 0) {
			System.out.println("You can use \"-h\" to view the correct command.");
			return;
		}
		if (a == 1 && args[0].equals("-h")) {
			System.out.println("-h: Explain the available command line parameters.");
			System.out.println(
					"-t A: Convert the SysML model under path A to the ontology model, and output the XML file to the \"generate\" folder");
			System.out.println("-v B: values verification of the ontology model under path B.");
		}

		else if (a == 2 && args[0].equals("-t")) {
			File file = new File(args[1]);
			
			if (!file.exists()) {
				System.out.println("File does not exist: " + args[1]);
				return;
			}
			String _name=file.getName();
			XMI xmi = ParseUtil.parseXMI(file);
			String rootPath = System.getProperty("user.dir");
			OntoGenerator g = new OntoGenerator();
			try {
				File _file=new File(rootPath+"\\src\\resource\\TIM.xml");
				g.init(_file);
				ReqMapper.map(xmi, g);
				BDDMapper.map(xmi, g);
				Checker.BDDFirstCheck(xmi, g);
				ActMapper.map(xmi, g);
				Checker.ActObjectCheck(xmi, g);
				StmMapper.map(xmi, g);
				System.out.println("Start check and specify");
				Checker.SysMLEdgeCheck(xmi, g);
				TraceUtil.specifyTrace(xmi, g);
				System.out.println("Check and specify over.");
				addValue(g);
				String name = _name.substring(0, _name.length() - 4);
				g.saveAsFile(System.getProperty("user.dir") + "\\generate\\" + name + ".owl");
			} catch (OWLOntologyCreationException | OWLOntologyStorageException | SWRLParseException
					| SWRLBuiltInException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Transform over!! Please check the log file for more details.");
		}

		else if (a == 2 && args[0].equals("-v")) {
			File file = new File(args[1]);
			if (!file.exists()) {
				System.out.println("File does not exist: " + args[1]);
				return;
			}
//			System.out.println(args[1]);
			OntoGenerator g = new OntoGenerator();
			try{
				g.init(file);
				ValueVerifier.valueVerify(g);
			}catch(OWLOntologyCreationException | OWLOntologyStorageException | SWRLParseException
					| SWRLBuiltInException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("verify over!!");
		}
		return;
	}

	private static void addValue(OntoGenerator g) {
		// TODO Auto-generated method stub
		List<String> values= new ArrayList<String>();
		g.createIndividualsOfClass(values, "HumanValue");
		g.createObjectPropertyAxiom("request", "value1", "B1.1.1");
		g.createDataPropertyAxiom("RuleType", "value1", "sequence");
		g.createDataPropertyAxiom("RuleValue", "value1", "Get_Destination before Start_Vehicle");
	}
}
