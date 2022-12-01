package com.bean.onto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.exceptions.SWRLBuiltInException;
import org.swrlapi.factory.SWRLAPIFactory;
import org.swrlapi.parser.SWRLParseException;
import org.swrlapi.sqwrl.SQWRLQueryEngine;
import org.swrlapi.sqwrl.SQWRLResult;
import org.swrlapi.sqwrl.exceptions.SQWRLException;

public class OntoGenerator {
	OWLOntology ont;
	OWLOntologyManager manager;
	OWLDataFactory factory;
	String ontIRI;
	OWLReasoner reasoner;
	SQWRLQueryEngine queryEngine;
	SWRLRuleEngine inferEngine;
	String shortPrefix;
	
	public OntoGenerator() {
		manager = OWLManager.createOWLOntologyManager();

	}

	public void init(File file)
			throws OWLOntologyCreationException, SWRLParseException, SWRLBuiltInException, OWLOntologyStorageException {
		
		try {
			ont = manager.loadOntologyFromOntologyDocument(file);
			ontIRI = ont.getOntologyID().getOntologyIRI().get()+"#";
			shortPrefix=ontIRI.substring(ontIRI.lastIndexOf("/")+1,ontIRI.length()-1);
			factory = manager.getOWLDataFactory();
			OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
			reasoner=reasonerFactory.createReasoner(ont);
			inferEngine=SWRLAPIFactory.createSWRLRuleEngine(ont);
			queryEngine =SWRLAPIFactory.createSQWRLQueryEngine(ont);
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getShortPrefix(){
		return shortPrefix;
	}

	public int getAximCount() {
		return ont.getAxiomCount();
	}

	public void saveAsFile(String fileName) throws OWLOntologyStorageException, IOException {
		File directory = new File("generate");
		if (!directory.exists())
			directory.mkdirs();
		final OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		File file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		OutputStream moduleOutputStream = null;
		moduleOutputStream = new FileOutputStream(file);
		m.saveOntology(ont, new RDFXMLDocumentFormat(), moduleOutputStream);	
	}

	public void createClassesOfParent(List<String> names, String parent) {
		OWLClass _parent = factory.getOWLClass(IRI.create(ontIRI + parent));
		OWLSubClassOfAxiom axiom;
		for (String name : names) {
			OWLClass child = factory.getOWLClass(IRI.create(ontIRI + name));
			axiom = factory.getOWLSubClassOfAxiom(child, _parent);
			manager.addAxiom(ont, axiom);
		}
	}

	public void createClassOfParent(String name, String parent) {
		OWLClass _parent = factory.getOWLClass(IRI.create(ontIRI + parent));
		OWLSubClassOfAxiom axiom;
		OWLClass child = factory.getOWLClass(IRI.create(ontIRI + name));
		axiom = factory.getOWLSubClassOfAxiom(child, _parent);
		manager.addAxiom(ont, axiom);
	}

	public void createClasses(List<String> names) {
		for (String name : names)
			factory.getOWLClass(IRI.create(ontIRI + name));
	}

	public void createClass(String name) {
		factory.getOWLClass(IRI.create(ontIRI + name));
	}

	public void createIndividualsOfClass(List<String> names, String type) {
		OWLClass _type = factory.getOWLClass(IRI.create(ontIRI + type));
		OWLClassAssertionAxiom axiom;
		for (String name : names) {
			OWLNamedIndividual individual = factory.getOWLNamedIndividual(IRI.create(ontIRI + name));
			axiom = factory.getOWLClassAssertionAxiom(_type, individual);
			manager.addAxiom(ont, axiom);
		}
	}

	public boolean createIndividualOfClass(String name, String type) {
		if (name==null || type == null || ont.containsIndividualInSignature(IRI.create(ontIRI + name)))
			return false;
		OWLClass _type = factory.getOWLClass(IRI.create(ontIRI + type));
		OWLNamedIndividual individual = factory.getOWLNamedIndividual(IRI.create(ontIRI + name));
		OWLClassAssertionAxiom axiom = factory.getOWLClassAssertionAxiom(_type, individual);
		manager.addAxiom(ont, axiom);
		return true;
	}

	public void createObjectPropertyAxiom(String objp, String subject, String object) {
		OWLObjectProperty p = factory.getOWLObjectProperty(IRI.create(ontIRI + objp));
		OWLNamedIndividual sub = factory.getOWLNamedIndividual(IRI.create(ontIRI + subject));
		OWLNamedIndividual obj = factory.getOWLNamedIndividual(IRI.create(ontIRI + object));
		OWLAxiom ax = factory.getOWLObjectPropertyAssertionAxiom(p, sub, obj);
		manager.addAxiom(ont, ax);
	}

	public void createObjectPropertyAxioms(String objp, String subject, List<String> classes) {
		// TODO Auto-generated method stub
		OWLObjectProperty p = factory.getOWLObjectProperty(IRI.create(ontIRI + objp));
		OWLNamedIndividual sub = factory.getOWLNamedIndividual(IRI.create(ontIRI + subject));
		for (String object : classes) {
			OWLNamedIndividual obj = factory.getOWLNamedIndividual(IRI.create(ontIRI + object));
			OWLAxiom ax = factory.getOWLObjectPropertyAssertionAxiom(p, sub, obj);
			manager.addAxiom(ont, ax);
		}

	}

	public void createDataPropertyAxiom(String dtp, String subject, String object) {
		if(dtp==null || subject==null || object==null){
			return;
		}
		OWLDataProperty p = factory.getOWLDataProperty(IRI.create(ontIRI + dtp));
		OWLNamedIndividual sub = factory.getOWLNamedIndividual(IRI.create(ontIRI + subject));
		OWLDatatype stringDt = factory.getOWLDatatype(OWL2Datatype.XSD_STRING.getIRI());
		OWLLiteral literal = factory.getOWLLiteral(object, stringDt);
		OWLAxiom ax = factory.getOWLDataPropertyAssertionAxiom(p, sub, literal);
		manager.addAxiom(ont, ax);
	}
	
	public void createCommentAxiom(String name, String xmiId) {
		// TODO Auto-generated method stub
		if(name==null || xmiId==null){
			return;
		}
		OWLAnnotationProperty comment = factory.getRDFSComment();
		OWLLiteral label = factory.getOWLLiteral("hasXmiID "+xmiId);
		OWLClass block=factory.getOWLClass(IRI.create(ontIRI+name));
		OWLAnnotation p=factory.getOWLAnnotation(comment, label);
		manager.addAxiom(ont, factory.getOWLAnnotationAssertionAxiom(block.getIRI(), p));
	}
	
	public String getCommentOfClass(String name){
		OWLClass block=factory.getOWLClass(IRI.create(ontIRI+name));
		return ont.getAnnotationAssertionAxioms(block.getIRI()).stream()
		.filter(axiom->axiom.getProperty().getIRI().toString().equals(OWLRDFVocabulary.RDFS_COMMENT.getIRI().toString()))
		.map(axiom->axiom.getAnnotation())
		.filter(anno->anno.getValue() instanceof OWLLiteral)
		.map(anno->(OWLLiteral) anno.getValue())
		.map(literal->literal.getLiteral())
		.findFirst().orElse(null);
	}
	
	public Set<String> getDataOfDataProperty(String value, String pro){
		OWLNamedIndividual ind=factory.getOWLNamedIndividual(IRI.create(ontIRI+value));
		OWLDataProperty p=factory.getOWLDataProperty(IRI.create(ontIRI + pro));
		return reasoner.getDataPropertyValues(ind, p).stream().map(l->l.getLiteral()).collect(Collectors.toSet());		
	}
	
	public void addRangeOfDataProperty(String string, List<String> string2) {
		// TODO Auto-generated method stub
		OWLDataProperty p = factory.getOWLDataProperty(IRI.create(ontIRI + string));
		List<OWLLiteral> literals = string2.stream().map(s -> factory.getOWLLiteral(s)).collect(Collectors.toList());
		OWLDataOneOf range = factory.getOWLDataOneOf(literals.toArray(new OWLLiteral[literals.size()]));
		factory.getOWLFunctionalDataPropertyAxiom(p);
		OWLDataPropertyRangeAxiom axiom = factory.getOWLDataPropertyRangeAxiom(p, range);
		manager.addAxiom(ont, axiom);

	}

	public static void main(String[] arg) {
		try {
			String rootPath = System.getProperty("user.dir");
			File file=new File(rootPath+"\\src\\resource\\TIM.xml");
			OntoGenerator g = new OntoGenerator();
			g.init(file);
			g.saveAsFile(System.getProperty("user.dir") + "\\generate\\robot.owl");
		} catch (OWLOntologyCreationException | OWLOntologyStorageException | SWRLParseException | SWRLBuiltInException
				| IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Set<String> getIndividualsOfClass(String string) {
		// TODO Auto-generated method stub
		OWLClass c = factory.getOWLClass(IRI.create(ontIRI + string));
		return reasoner.getInstances(c, true).getFlattened().stream().map(i->{
			String name=i.getIRI().toString();
			return name.substring(name.lastIndexOf("#")+1);
		}).collect(Collectors.toSet());
	}
	
	public Set<String> getDirectTypes(String ind){
		OWLNamedIndividual individual=factory.getOWLNamedIndividual(IRI.create(ontIRI+ind));
		return reasoner.getTypes(individual, true).getFlattened().stream().map(i->{
			String name=i.getIRI().toString();
			return name.substring(name.lastIndexOf("#")+1);
		}).collect(Collectors.toSet());
	}
	
	public boolean haveIndividualOfClass(String string){
		OWLClass c = factory.getOWLClass(IRI.create(ontIRI + string));
		return reasoner.getInstances(c, true).isEmpty();
	}
	
	public List<String> queryBySQWRL(String rule, String flag){
		List<String> ans=new ArrayList<String>();
		int size=queryEngine.getSQWRLQueryNames().size();
		try {
			queryEngine.createSQWRLQuery("Q"+String.valueOf(size),rule);
			SQWRLResult rlt=queryEngine.runSQWRLQuery("Q"+String.valueOf(size));
			while(rlt.next()){
				ans.add(rlt.getValue(flag).toString());
			}
		} catch (SQWRLException | SWRLParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ans;
	}
	
	public String inferBySWRL(String rule){
		int size=inferEngine.getSWRLRules().size();
		try {
			String name="I"+String.valueOf(size);
			inferEngine.createSWRLRule(name, rule);
		} catch (SWRLParseException | SWRLBuiltInException e) {
			return e.getMessage();
		}
		return "true";
	}
	
	public List<String> inferBySWRL(List<String> rule){
		int size=inferEngine.getSWRLRules().size();
		List<String> rlt=new ArrayList<String>();
		for(String r:rule){
			try {
				String name="I"+String.valueOf(size++);
				inferEngine.createSWRLRule(name, r);
			} catch (SWRLParseException | SWRLBuiltInException e) {
				rlt.add(e.getMessage());
			}
		}
		return rlt;
	}
	
	public void infer(){
		try{
			inferEngine.infer();
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
	}

	public List<String[]> queryBySQWRL(String rule, String[] strings) {
		// TODO Auto-generated method stub
		List<String[]> ans=new ArrayList<>();
		int size=queryEngine.getSQWRLQueryNames().size();
		try {
			queryEngine.createSQWRLQuery("Q"+String.valueOf(size),rule);
			SQWRLResult rlt=queryEngine.runSQWRLQuery("Q"+String.valueOf(size));
			while(rlt.next()){
				String[] tmp=new String[strings.length];
				for(int i=0;i<strings.length;i++){
					tmp[i]=rlt.getValue(i).toString();
				}
				ans.add(tmp);
			}
		} catch (SQWRLException | SWRLParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ans;
	}
	
}
