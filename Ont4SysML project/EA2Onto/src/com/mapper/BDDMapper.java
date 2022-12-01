package com.mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.bean.UML.UMLAssociation;
import com.bean.UML.UMLAttribute;
import com.bean.UML.UMLClass;
import com.bean.UML.UMLDiagram;
import com.bean.UML.UMLGeneralization;
import com.bean.UML.UMLOperation;
import com.bean.UML.XMI;
import com.bean.onto.OntoGenerator;
import com.util.BDDUtil;
import com.util.XMIUtil;

public class BDDMapper {
	public static void map(XMI xmi, OntoGenerator ont) {
		// System.out.println("��ʼת���鶨��ͼ����ǰ������� ��"+ont.getAximCount());
		System.out.println(
				"Start transforming the block definition diagram. Number of current axioms:" + ont.getAximCount());
		UMLDiagram diagram = BDDUtil.getDiagram(xmi);
		if (diagram != null) {
			ont.createIndividualOfClass(diagram.getName(), "BlockDefineDiagram");
			ont.createDataPropertyAxiom("hasXmiID", diagram.getName(), diagram.getXmiId());
			valueTypeMap(xmi, ont);
			blockMap(xmi, ont, diagram);
			edgeMap(xmi, ont, diagram);
		}
		// System.out.println("ת����ɡ���ǰ������� ��"+ont.getAximCount());
		System.out.println("Transform over. Number of current axioms:" + ont.getAximCount());
	}

	private static void edgeMap(XMI xmi, OntoGenerator ont, UMLDiagram diagram) {
		// TODO Auto-generated method stub
		aggregationMap(xmi, ont, diagram);
		commonRelationMap(xmi, ont, diagram);
		generalizationMap(xmi, ont, diagram);
	}

	private static void commonRelationMap(XMI xmi, OntoGenerator ont, UMLDiagram diagram) {
		// TODO Auto-generated method stub
		int count = 0;
		List<UMLAssociation> as = BDDUtil.getAssociationsOfDiagram(diagram, BDDUtil.getAllConnectors(xmi));
		for (UMLAssociation a : as) {
			HashMap<String, String> map = BDDUtil.getInstanceFromAssociation(xmi, a.getModelElementTaggedValue());
			// �������ʵ����Ϊʵ����Ӳ���������
			blockInstanceMap(xmi,ont,map.get("targetI"), map.get("TargetT"),diagram.getName());
			blockInstanceMap(xmi,ont,map.get("sourceI"), map.get("SourceT"),diagram.getName());
			// ��û�����־ͳ�ʼ��һ��
			String cmname;
			if (a.getName() == null) {
				cmname = "commonRelation" + count;
			} else
				cmname = a.getName();

			ont.createIndividualOfClass(cmname, "CommonRelation");
			ont.createObjectPropertyAxiom("hasCommonRelation", diagram.getName(), cmname);
			if (!map.containsValue(null)) {
				ont.createObjectPropertyAxiom("hasSource", cmname, map.get("sourceI"));
				ont.createObjectPropertyAxiom("hasTarget", cmname, map.get("targetI"));
			} else {
				if (map.get("sourceI") == null && map.get("targetI") != null) {
					Set<String> is = ont.getIndividualsOfClass(map.get("SourceT"));
					if (is == null) {
						ont.createIndividualOfClass(map.get("SourceT").toLowerCase() + count, map.get("SourceT"));
						ont.createObjectPropertyAxiom("hasSource", cmname, map.get("SourceT").toLowerCase());
					} else {
						for (String i : is)
							ont.createObjectPropertyAxiom("hasSource", cmname, i);
					}
					ont.createObjectPropertyAxiom("hasTarget", cmname, map.get("targetI"));
				} else if (map.get("sourceI") != null && map.get("targetI") == null) {
					Set<String> is = ont.getIndividualsOfClass(map.get("TargetT"));
					if (is == null) {
						ont.createIndividualOfClass(map.get("TargetT").toLowerCase() + count, map.get("TargetT"));
						ont.createObjectPropertyAxiom("hasTarget", cmname, map.get("TargetT").toLowerCase());
					} else {
						for (String i : is)
							ont.createObjectPropertyAxiom("hasTarget", cmname, i);
					}
					ont.createObjectPropertyAxiom("hasSource", cmname, map.get("sourceI"));
				} else {
					Set<String> sis = ont.getIndividualsOfClass(map.get("SourceT"));
					Set<String> tis = ont.getIndividualsOfClass(map.get("TargetT"));
					for (String si : sis) {
						for (String ti : tis) {
							ont.createObjectPropertyAxiom("hasSource", cmname, si);
							ont.createObjectPropertyAxiom("hasTarget", cmname, ti);
						}
					}
				}
			}
			++count;
		}
	}

	private static void generalizationMap(XMI xmi, OntoGenerator ont, UMLDiagram diagram) {
		// TODO Auto-generated method stub
		List<UMLGeneralization> gs = BDDUtil.getGeneralizationsOfDiagram(diagram, BDDUtil.getAllGeneralizations(xmi));
		int count = 0;
		for (UMLGeneralization g : gs) {
			// ��û�����־ͳ�ʼ��һ��
			String name = "generalization" + count;
			ont.createIndividualOfClass(name, "Generalization");
			ont.createObjectPropertyAxiom("hasGeneralization", diagram.getName(), name);
			Set<String> sis = ont.getIndividualsOfClass(XMIUtil.getTaggedValueByTag("ea_sourceName", g.getModelElementTaggedValue()));
			Set<String> tis = ont.getIndividualsOfClass(XMIUtil.getTaggedValueByTag("ea_targetName", g.getModelElementTaggedValue()));
			for (String si : sis) {
				for (String ti : tis) {
					ont.createObjectPropertyAxiom("hasSource", name, si);
					ont.createObjectPropertyAxiom("hasTarget", name, ti);
				}
			}
		}
	}

	private static void aggregationMap(XMI xmi, OntoGenerator ont, UMLDiagram diagram) {
		// TODO Auto-generated method stub
		List<UMLAssociation> as = BDDUtil.getAssociationsOfDiagram(diagram,BDDUtil.getAllAggregations(xmi));
		for (UMLAssociation a : as) {
			HashMap<String, String> map = BDDUtil.getInstanceFromAssociation(xmi, a.getModelElementTaggedValue());
			// �������ʵ����Ϊʵ����Ӳ���������
			blockInstanceMap(xmi,ont,map.get("targetI"), map.get("TargetT"),diagram.getName());
			blockInstanceMap(xmi,ont,map.get("sourceI"), map.get("SourceT"),diagram.getName());
			if (!map.containsValue(null)) {
				ont.createObjectPropertyAxiom("isPartOf", map.get("sourceI"), map.get("targetI"));
				ont.createObjectPropertyAxiom("havePart", map.get("targetI"), map.get("sourceI"));
			}
		}
	}
	private static void blockInstanceMap(XMI xmi,  OntoGenerator ont, String ind, String type,String name){
		if(name==null || !ont.createIndividualOfClass(ind,type))
			return;
		ont.createObjectPropertyAxiom("hasBlock", name, ind);
//		Ϊblock�������xmiid����
		String xmiid=ont.getCommentOfClass(type).split(" ")[1];
		ont.createDataPropertyAxiom("hasXmiID", ind, xmiid);
//		Ϊblock������Ӳ����ͱ���
		blockOperationMap(xmi, ont, ind, type);
		blockAttributeMap(xmi, ont, ind, type);
	}
	private static void blockOperationMap(XMI xmi, OntoGenerator ont, String ind, String type) {
		List<UMLOperation> operations = BDDUtil.getOperationsOfBlock(xmi, type);
		if (operations != null) {
			for (UMLOperation o : operations) {
				String returnType = XMIUtil.getTaggedValueByTag("type", o.getModelElementTaggedValue());
				ont.createIndividualOfClass(o.getName(), "BlockFunction");
				ont.createDataPropertyAxiom("hasValueType", o.getName(), returnType);
				ont.createObjectPropertyAxiom("haveFunction", ind, o.getName());
			}

		}
	}

	private static void blockAttributeMap(XMI xmi, OntoGenerator ont, String ind, String type) {
		List<UMLAttribute> as = BDDUtil.getAttributesOfBlock(xmi, type);
		if (as != null) {
			for (UMLAttribute a : as) {
				String returnType = XMIUtil.getTaggedValueByTag("type", a.getModelElementTaggedValue());
				ont.createIndividualOfClass(a.getName(), "BlockPara");
				ont.createDataPropertyAxiom("hasValueType", a.getName(), returnType);
				ont.createObjectPropertyAxiom("havePara", ind, a.getName());
			}
		}
		

	}

	private static void blockMap(XMI xmi, OntoGenerator ont, UMLDiagram diagram) {
		// TODO Auto-S method stub
		List<UMLClass> blocks=BDDUtil.getBlocksOfDiagram(xmi, diagram);
		for(UMLClass block:blocks){
			ont.createClassOfParent(block.getName(), "Block");
			ont.createCommentAxiom(block.getName(),block.getXmiId());
		}
	}

	static private void valueTypeMap(XMI xmi, OntoGenerator ont) {
		// TODO Auto-generated method stub
		List<String> rlt = BDDUtil.getValueTypes(xmi);
		if (rlt != null) {
			ont.addRangeOfDataProperty("hasValueType", rlt);
		}
	}

}
