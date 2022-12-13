package com.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.bean.UML.UMLAssociation;
import com.bean.UML.UMLModelElementStereotype;
import com.bean.UML.UMLModelElementTaggedValue;
import com.bean.UML.UMLStereotype;
import com.bean.UML.XMI;
import com.bean.onto.OntoGenerator;
import com.logger.MyLogger;

public class TraceUtil {
    
//	最后要检查每个块是否对应有allocate活动分区
	public static void traceBlock2ActPart(String xmiid, String name, OntoGenerator ont) {
		// TODO Auto-generated method stub
		if (xmiid == null){
			MyLogger.LOGGER.error("Trace error: The activity partition " + name + " is not allocated by any block!");
			return;
		}
			String prefix = ont.getShortPrefix();
			String rule = String.format("%s:Block(?b)^%s:ActPartition(%s:%s)^%s:hasXmiID(?b,\"%s\")->%s:allocate(?b,%s:%s)",
					prefix, prefix, prefix, name, prefix, xmiid, prefix, prefix, name);
			MyLogger.LOGGER.info("Infer by the rule: "+rule);
			String rlt = ont.inferBySWRL(rule);
			if (!rlt.equals("true") && rlt.indexOf("Invalid OWL individual name")!=-1) {
				MyLogger.LOGGER.warn("Block "+rlt.replace("Invalid OWL individual name", "")+" doesn't exist.");
			}
			ont.infer();
	}
	
	public static void traceBlock2ActPart(Map<String,String> allocates, OntoGenerator ont) {
		// TODO Auto-generated method stub
		String prefix = ont.getShortPrefix();
		for(Map.Entry<String, String> entry:allocates.entrySet()){
			if(entry.getValue()==null){
				MyLogger.LOGGER.error("Trace error: The activity partition " + entry.getKey() + " is not allocated by any block!");
				continue;
			}
			ont.createObjectPropertyAxiom("allocate", entry.getValue(), entry.getKey());
		}
	}

//	最后要检查每个块方法是否都有mapToAN活动
	public static void traceActionAndFunction(String name, OntoGenerator ont) {
		// TODO Auto-generated method stub
		String _name = name.toLowerCase();
		String prefix = ont.getShortPrefix();
		String rule = String.format("%s:BlockFunction(%s:%s)->%s:mapToBF(%s:%s,%s:%s)^%s:mapToAN(%s:%s,%s:%s)", 
				prefix, prefix, _name, prefix, prefix, name,  prefix, _name, prefix, prefix, _name, prefix,
				name);
		MyLogger.LOGGER.info("Infer by the rule: "+rule);
		String rlt = ont.inferBySWRL(rule);
		if (!rlt.equals("true") && rlt.indexOf("Invalid OWL individual name")!=-1) {
			MyLogger.LOGGER.warn("Block Function "+rlt.replace("Invalid OWL individual name", "")+" doesn't exist.");
		}
		ont.infer();
	}
	
	public static void traceActionAndFunction(List<String> names, OntoGenerator ont) {
		// TODO Auto-generated method stub
		String prefix = ont.getShortPrefix();
		for(String name:names){
			String _name=name.toLowerCase();
			String rule = String.format("%s:BlockFunction(%s:%s)->%s:mapToBF(%s:%s,%s:%s)^%s:mapToAN(%s:%s,%s:%s)", 
					prefix, prefix, _name, prefix, prefix, name,  prefix, _name, prefix, prefix, _name, prefix,
					name);
			MyLogger.LOGGER.info("Create rule: "+rule);
			String rlt=ont.inferBySWRL(rule);
			if (!rlt.equals("true") && rlt.indexOf("Invalid OWL individual name")!=-1) {
				MyLogger.LOGGER.warn("Block "+rlt.replace("Invalid OWL individual name", "")+" doesn't exist.");
			}
		}
		ont.infer();
	}
	
//	最后要检查每个Edge是否都只有一个起点和终点
	public static void addSourceOfTransition(String xmiId, String name, OntoGenerator ont) {
		String prefix = ont.getShortPrefix();
		String rule = String.format("%s:SpecialNode(?n)^%s:hasXmiID(?n,\"%s\")->%s:hasSource(%s:%s,?n)", 
				prefix, prefix, xmiId, prefix, prefix, name);
		String rlt = ont.inferBySWRL(rule);
		MyLogger.LOGGER.info("Create rule: "+rule);
		if (!rlt.equals("true")) {
			MyLogger.LOGGER.error("Failed inferring by the rule: "+rule+" failed. "+ rlt);
		}
	}
	
	public static void addTargetOfTransition(String xmiId, String name, OntoGenerator ont) {
		String prefix = ont.getShortPrefix();
		String rule = String.format("%s:SpecialNode(?n)^%s:hasXmiID(?n,\"%s\")->%s:hasTarget(%s:%s,?n)", 
				prefix, prefix, xmiId, prefix, prefix, name);
		String rlt = ont.inferBySWRL(rule);
		MyLogger.LOGGER.info("Create rule: "+rule);
		if (!rlt.equals("true")) {
			MyLogger.LOGGER.error("Failed inferring by the rule: "+rule+" failed. "+ rlt);
		}
	}
	
	public static void traceRequirement2Block(XMI xmi, OntoGenerator ont){
		List<UMLAssociation> traces=getAllTrace(xmi);
		List<String> rules=new ArrayList<String>();
		String prefix=ont.getShortPrefix();
		String rule="%s:Requirement(?r)^%s:%s(?i)^%s:hasReqtName(?r,\"%s\")->%s:trace(?r,?i)";
		for(UMLAssociation trace:traces){
			UMLModelElementTaggedValue values=trace.getModelElementTaggedValue();
			if(XMIUtil.getTaggedValueByTag("ea_sourceType", values).equals("Requirement")
					&&XMIUtil.getTaggedValueByTag("ea_targetType", values).equals("Class")){
				
				rules.add(String.format(rule, prefix,prefix,XMIUtil.getTaggedValueByTag("ea_targetName", values),prefix,
						XMIUtil.getTaggedValueByTag("ea_sourceName", values),prefix));
				MyLogger.LOGGER.info(rules.get(rules.size()-1));
			}
			else{
				MyLogger.LOGGER.error("'trace' should link from Requirement to Block in req.");
			}
		}
		ont.inferBySWRL(rules);
		ont.infer();
	}
	
	private static List<UMLAssociation> getAllTrace(XMI xmi){
		return XMIUtil.getTopPackage(xmi).getUmlNamespaceOwnedElement().getUmlAssociations().stream().filter(a->
		{
			String type=Optional.ofNullable(a).map(UMLAssociation::getModelElementStereotype).map(UMLModelElementStereotype::getStereotype)
					.map(UMLStereotype::getName).orElse(null);
			if(type!=null && type.equals("trace")){
				return true;
			}
			return false;
		}).collect(Collectors.toList());
	}
	

	public static void specifyTrace(XMI xmi, OntoGenerator g) {
		// TODO Auto-generated method stub
		
		traceRequirement2Block(xmi,g);
		specifyItemTrace();
//		specifySequence(g);
	}
	
	//使用规则推导结点之间的顺序关系，只是粗糙的记录
	private static void specifySequence(OntoGenerator g) {
		// TODO Auto-generated method stub
		String prefix=g.getShortPrefix();
		String rule="%s:SysMLEdge(?e)^%s:hasTarget(?e,?a)^%s:hasSource(?e,?b)->%s:before(?b,?a)";
		rule=String.format(rule, prefix,prefix,prefix,prefix);
		g.inferBySWRL(rule);
		g.infer();
	}

//	扫描所有的Guard、Trigger、Effect、Do、Entry、Exit，检查是否包含块参数或者块操作，若包含，设置relateToPara、relateToFunc属性
	//Guard(?other)^hasGuardValue(?other,?value)^ControlFlow(?e)^hasGuard(?e,?other)^SysMLNode(?n)
//	^hasSource(?e,?n)^SysMLDiagram(?d)^hasNode(?d,?e)^Block(?b)^allocate(?b,?d)->sqwrl:select(?other,?value,?b)
	public static void specifyItemTrace(){
//		List<OWLIndividual> 
	}
}
